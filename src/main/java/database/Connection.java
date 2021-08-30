package database;

import com.google.common.collect.Multimap;
import dataclasses.AlgoAssociatedData;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import nicehash.OrderBot;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import test.generated.tables.records.*;
import utils.Config;
import utils.Consts;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

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

    public static void putMap(Map<AlgoDataRecord, AlgoAssociatedData> map) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            for (Map.Entry<AlgoDataRecord, AlgoAssociatedData> entry : map.entrySet()) {
                AlgoAssociatedData associatedData = entry.getValue();

                // Insert algo data and get id
                create.insertInto(ALGO_DATA).set(entry.getKey()).execute();
                long id = create.lastID().longValue();

                // Insert coin data records after setting algo id
                List<CoinDataRecord> coinDataRecords = associatedData.getCoinDataRecords();
                for (CoinDataRecord record : coinDataRecords) {
                    record.setAlgoId(id);
                }
                create.batchInsert(coinDataRecords).execute();

                // Insert market data after setting algo id and market id
                Multimap<MarketDataRecord, MarketPriceDataRecord> marketData = associatedData.getMarketData();
                for (MarketDataRecord marketDataRecord : marketData.keySet()) {
                    // Execute this first because if the id is set first, then the multimap won't work
                    Collection<MarketPriceDataRecord> priceRecords = marketData.get(marketDataRecord);

                    marketDataRecord.setAlgoId(id);
                    create.insertInto(MARKET_DATA).set(marketDataRecord).execute();

                    long marketId = create.lastID().longValue();

                    for (MarketPriceDataRecord priceRecord : priceRecords) {
                        priceRecord.setMarketId(marketId);
                    }

                    create.batchInsert(priceRecords).execute();
                }
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

    public static List<PriceRecord> getPrices(OrderBot bot, int analyzeMinutes) {
        String algoName = bot.getAlgoName();
        double fulfillSpeed = bot.getLimit();
        String marketName = bot.getMarketName();

        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            // Get nearest fulfillSpeed
            final String DIFFERENCE = "difference";
            Record2<Double, Double> speedResult = create.select(
                    MARKET_PRICE_DATA.FULFILL_SPEED,
                    DSL.abs(MARKET_PRICE_DATA.FULFILL_SPEED.minus(fulfillSpeed)).as(DSL.inline(DIFFERENCE))
            )
                    .from(
                            ALGO_DATA
                                    .join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                                    .join(MARKET_PRICE_DATA).on(MARKET_DATA.ID.eq(MARKET_PRICE_DATA.MARKET_ID))
                    )
                    .where(ALGO_DATA.ALGO_NAME.eq(algoName))
                    .and(MARKET_DATA.MARKET_NAME.eq(marketName))
                    .orderBy(DSL.inline(DIFFERENCE).asc())
                    .limit(1)
                    .fetchOne();

            double nearestFulfillSpeed = speedResult.get(MARKET_PRICE_DATA.FULFILL_PRICE);

            Field<LocalDateTime> analyzeStart = DSL.currentLocalDateTime().minus((new DayToSecond(0, 0, analyzeMinutes)));
            Result<Record2<Integer, Integer>> result = create.select(MARKET_PRICE_DATA.FULFILL_PRICE, DSL.count(MARKET_PRICE_DATA.FULFILL_PRICE))
                    .from(
                            ALGO_DATA
                                    .join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                                    .join(MARKET_PRICE_DATA).on(MARKET_DATA.ID.eq(MARKET_PRICE_DATA.MARKET_ID))
                    )
                    .where(ALGO_DATA.TIMESTAMP.ge(analyzeStart))
                    .and(ALGO_DATA.ALGO_NAME.eq(algoName))
                    .and(MARKET_DATA.MARKET_NAME.eq(marketName))
                    .and(MARKET_PRICE_DATA.FULFILL_SPEED.eq(nearestFulfillSpeed))
                    .groupBy(MARKET_PRICE_DATA.FULFILL_PRICE)
                    .orderBy(MARKET_PRICE_DATA.FULFILL_PRICE.asc())
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
/*
    public static List<List<AllDataRecord>> getAllData(List<TriplePair> triplePairs, int analyzeMinutes) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            List<List<AllDataRecord>> list = new ArrayList<>();
            for (TriplePair pair : triplePairs) {
                Result<Record> result = create.selectFrom(
                        ALGO_DATA.join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                                .join(COIN_DATA).on((ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
                ).where(ALGO_DATA.TIMESTAMP.ge(DSL.currentLocalDateTime().minus((new DayToSecond(0, 0, analyzeMinutes)))))
                        .and(COIN_DATA.COIN_NAME.eq(pair.getCoin()))
                        .and(MARKET_DATA.MARKET_NAME.eq(pair.getMarket()))
                        .and(ALGO_DATA.ALGO_NAME.eq(pair.getAlgo()))
                        .orderBy(ALGO_DATA.TIMESTAMP.asc())
                        .fetch();

                List<AllDataRecord> allDataRecords = result.stream()
                        .map(Connection::recordToAllDataRecord)
                        .collect(Collectors.toList());

                list.add(allDataRecords);
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

 */

    public static List<TriplePair> getCoinMarketPairs() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            Result<Record3<String, String, String>> result = create.selectDistinct(COIN_DATA.COIN_NAME, MARKET_DATA.MARKET_NAME, ALGO_DATA.ALGO_NAME).from(
                    ALGO_DATA.join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                            .join(COIN_DATA).on((ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
            ).fetch();

            List<TriplePair> list = new ArrayList<>();
            for (Record record : result) {
                String coinName = record.get(COIN_DATA.COIN_NAME);
                String marketName = record.get(MARKET_DATA.MARKET_NAME);
                String algoName = record.get(ALGO_DATA.ALGO_NAME);

                TriplePair pair = new TriplePair(algoName, marketName, coinName);
                list.add(pair);
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static int getNumToClean() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            int cleanDatabaseDays = Config.getConfigInt(Consts.CLEAN_DATABASE_DAYS);
            Field<LocalDateTime> deleteEnd = DSL.currentLocalDateTime().minus((new DayToSecond(cleanDatabaseDays)));
            Record1<Integer> count = create.select(DSL.count()).from(ALGO_DATA).where(ALGO_DATA.TIMESTAMP.le(deleteEnd)).fetchOne();

            return count.value1();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static void cleanDatabase() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            int cleanDatabaseDays = Config.getConfigInt(Consts.CLEAN_DATABASE_DAYS);
            Field<LocalDateTime> deleteEnd = DSL.currentLocalDateTime().minus((new DayToSecond(cleanDatabaseDays)));
            create.deleteFrom(ALGO_DATA).where(ALGO_DATA.TIMESTAMP.le(deleteEnd)).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

