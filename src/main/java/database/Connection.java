package database;

import dataclasses.AlgoAssociatedData;
import dataclasses.AllDataRecord;
import dataclasses.PriceRecord;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import test.generated.tables.records.*;
import utils.Config;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static test.generated.Tables.*;

public class Connection {
    private static java.sql.Connection getConnection() throws SQLException {
        String userName = Config.getDatabaseUserName();
        String password = Config.getDatabasePassword();
        String url = "jdbc:postgresql://" + Config.getDatabaseUrl();

        return DriverManager.getConnection(url, userName, password);
    }

    public static String getConfigValue(String key) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            ConfigRecord record = create.selectFrom(CONFIG).where(CONFIG.KEY.equal(key)).fetchOne();

            return record.getValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Config value not found!");
    }

    public static void insertMap(Map<AlgoDataRecord, AlgoAssociatedData> map) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            for (Map.Entry<AlgoDataRecord, AlgoAssociatedData> entry : map.entrySet()) {
                create.insertInto(ALGO_DATA).set(entry.getKey()).execute();
                long id = create.lastID().longValue();

                AlgoAssociatedData associatedData = entry.getValue();
                for (CoinDataRecord record : associatedData.coinDataRecords) {
                    record.setAlgoId(id);
                }
                for (MarketDataRecord record : associatedData.marketDataRecords) {
                    record.setAlgoId(id);
                }

                create.batchInsert(associatedData.coinDataRecords).execute();
                create.batchInsert(associatedData.marketDataRecords).execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getCoinRevenue(String coinName) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            Record1<Integer> result = create.select(COIN_DATA.COIN_REVENUE)
                    .from(ALGO_DATA.join(COIN_DATA).on(ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
                    .where(COIN_DATA.COIN_NAME.eq(coinName))
                    .orderBy(ALGO_DATA.TIMESTAMP.desc())
                    .limit(1)
                    .fetchOne();

            return result.value1();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Coin not found");
    }

    public static List<PriceRecord> getPrices(String algoName, String marketName, int analyzeMinutes) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            Result<Record2<Integer, Integer>> result = create.select(MARKET_DATA.FULFILL_PRICE, DSL.count(MARKET_DATA.FULFILL_PRICE))
                .from(
                    ALGO_DATA
                    .join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                )
                .where(ALGO_DATA.TIMESTAMP.ge(DSL.currentLocalDateTime().minus((new DayToSecond(0, 0, analyzeMinutes)))))
                .and(ALGO_DATA.ALGO_NAME.eq(algoName))
                .and(MARKET_DATA.MARKET_NAME.eq(marketName))
                .groupBy(MARKET_DATA.FULFILL_PRICE)
                .orderBy(MARKET_DATA.FULFILL_PRICE.asc())
                .fetch();

            List<PriceRecord> list = new ArrayList<>();
            for (Record2<Integer, Integer> record2 : result) {
                PriceRecord priceRecord = new PriceRecord(record2.value1(), record2.value2());
                list.add(priceRecord);
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static void putOrderLimit(String orderId, double orderLimit) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            OrderLimitsRecord record = create.newRecord(ORDER_LIMITS);
            record.setOrderId(orderId);
            record.setOrderLimit(orderLimit);
            record.store();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrderLimit(String orderId) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            OrderLimitsRecord record = create.fetchOne(ORDER_LIMITS, ORDER_LIMITS.ORDER_ID.eq(orderId));
            if (record != null) {
                record.delete();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> getOrderLimits() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            Result<OrderLimitsRecord> result = create.fetch(ORDER_LIMITS);

            Map<String, Double> map = new HashMap<>();
            for (OrderLimitsRecord record : result) {
                map.put(record.getOrderId(), record.getOrderLimit());
            }

            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static List<AllDataRecord> getAllData(int analyzeMinutes) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            Result<Record> result = create.selectFrom(
                        ALGO_DATA.join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                            .join(COIN_DATA).on((ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
                    )
                    .where(ALGO_DATA.TIMESTAMP.ge(DSL.currentLocalDateTime().minus((new DayToSecond(0, 0, analyzeMinutes)))))
                    .orderBy(ALGO_DATA.TIMESTAMP.asc())
                    .fetch();

            List<AllDataRecord> list = new ArrayList<>();
            for (Record record : result) {
                list.add(recordToAllDataRecord(record));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    private static AllDataRecord recordToAllDataRecord(Record record) {
        LocalDateTime timestamp = record.get(ALGO_DATA.TIMESTAMP);
        String algoName = record.get(ALGO_DATA.ALGO_NAME);
        String marketName = record.get(MARKET_DATA.MARKET_NAME);
        double fulfillSpeed = record.get(MARKET_DATA.FULFILL_SPEED);
        double totalSpeed = record.get(MARKET_DATA.TOTAL_SPEED);
        int fulfillPrice = record.get(MARKET_DATA.FULFILL_PRICE);
        String coinName = record.get(COIN_DATA.COIN_NAME);
        int coinRevenue = record.get(COIN_DATA.COIN_REVENUE);

        AllDataRecord allDataRecord = new AllDataRecord();
        allDataRecord.setTimestamp(timestamp);
        allDataRecord.setAlgoName(algoName);
        allDataRecord.setMarketName(marketName);
        allDataRecord.setFulfillSpeed(fulfillSpeed);
        allDataRecord.setTotalSpeed(totalSpeed);
        allDataRecord.setFulfillPrice(fulfillPrice);
        allDataRecord.setCoinName(coinName);
        allDataRecord.setCoinRevenue(coinRevenue);

        return allDataRecord;
    }
}

