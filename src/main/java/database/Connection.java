package database;

import com.google.common.collect.Multimap;
import dataclasses.*;
import nicehash.OrderBot;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.DayToSecond;
import test.generated.tables.OrderInitialData;
import test.generated.tables.records.*;
import utils.Config;
import utils.Consts;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static test.generated.Tables.*;

public class Connection {
    static java.sql.Connection getConnection() throws SQLException {
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

    private static double getNearestFulfillPrice(DSLContext create, TriplePair pair, double limit) {
        // Get nearest fulfillSpeed
        final int DIFFERENCE = 1;
        Record2<Double, Double> speedResult = create.select(
                        MARKET_PRICE_DATA.FULFILL_SPEED,
                        DSL.abs(MARKET_PRICE_DATA.FULFILL_SPEED.minus(limit)).as(DSL.inline(DIFFERENCE))
                )
                .from(
                        ALGO_DATA
                                .join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                                .join(MARKET_PRICE_DATA).on(MARKET_DATA.ID.eq(MARKET_PRICE_DATA.MARKET_ID))
                )
                .where(ALGO_DATA.ALGO_NAME.eq(pair.getAlgo()))
                .and(MARKET_DATA.MARKET_NAME.eq(pair.getMarket()))
                .orderBy(DSL.inline(DIFFERENCE).asc())
                .limit(1)
                .fetchOne();

        return speedResult.get(MARKET_PRICE_DATA.FULFILL_SPEED);
    }

    public static List<PriceRecord> getPrices(OrderBot bot, int analyzeMinutes) {
        String algoName = bot.getAlgoName();
        double fulfillSpeed = bot.getLimit();
        String marketName = bot.getMarketName();

        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            double nearestFulfillSpeed = getNearestFulfillPrice(create, bot.getTriplePair(), fulfillSpeed);

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

    public static void putOrderInitialData(String orderId, double orderLimit, double evaluationScore) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            OrderInitialDataRecord record = create.newRecord(ORDER_INITIAL_DATA);
            record.setOrderId(orderId);
            record.setOrderLimit(orderLimit);
            record.setEvaluationScore(evaluationScore);
            record.setMarketEvaluationVersion(Consts.MARKET_EVALUATION_VERSION);
            record.store();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrderInitialData(String orderId) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            create.deleteFrom(ORDER_INITIAL_DATA)
                    .where(ORDER_INITIAL_DATA.ORDER_ID.eq(orderId))
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> getOrderInitialLimits() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            Result<OrderInitialDataRecord> result = create.selectFrom(ORDER_INITIAL_DATA)
                    .where(ORDER_INITIAL_DATA.MARKET_EVALUATION_VERSION.eq(Consts.MARKET_EVALUATION_VERSION))
                    .fetch();

            Map<String, Double> map = new HashMap<>();
            for (OrderInitialDataRecord record : result) {
                map.put(record.getOrderId(), record.getOrderLimit());
            }

            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static Map<String, Double> getOrderInitialEvaluationScores() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            Result<OrderInitialDataRecord> result = create.selectFrom(ORDER_INITIAL_DATA)
                    .where(ORDER_INITIAL_DATA.MARKET_EVALUATION_VERSION.eq(Consts.MARKET_EVALUATION_VERSION))
                    .fetch();

            Map<String, Double> map = new HashMap<>();
            for (OrderInitialDataRecord record : result) {
                map.put(record.getOrderId(), record.getEvaluationScore());
            }

            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static List<List<AllDataRecord>> getAllData(List<CryptoInvestment> investments, int analyzeMinutes) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            List<List<AllDataRecord>> list = new ArrayList<>();

            for (CryptoInvestment investment : investments) {
                Result<Record> result = create.selectFrom(
                                ALGO_DATA.join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                                        .join(COIN_DATA).on((ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
                                        .join(MARKET_PRICE_DATA).on(MARKET_DATA.ID.eq(MARKET_PRICE_DATA.MARKET_ID))
                        ).where(ALGO_DATA.TIMESTAMP.ge(DSL.currentLocalDateTime().minus((new DayToSecond(0, 0, analyzeMinutes)))))
                        .and(COIN_DATA.COIN_NAME.eq(investment.getCoin()))
                        .and(MARKET_DATA.MARKET_NAME.eq(investment.getMarket()))
                        .and(ALGO_DATA.ALGO_NAME.eq(investment.getAlgo()))
                        .and(MARKET_PRICE_DATA.FULFILL_SPEED.eq(investment.getFulfillSpeed()))
                        .orderBy(ALGO_DATA.TIMESTAMP.asc())
                        .fetch();

                List<AllDataRecord> allDataRecords = result.stream()
                        .map(AllDataRecord::new)
                        .collect(Collectors.toList());

                list.add(allDataRecords);

            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("SQL Error");
    }

    public static List<CryptoInvestment> getInvestmentsList() {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
            Result<Record4<String, String, String, Double>> result = create.selectDistinct(COIN_DATA.COIN_NAME, MARKET_DATA.MARKET_NAME, ALGO_DATA.ALGO_NAME, MARKET_PRICE_DATA.FULFILL_SPEED).from(
                    ALGO_DATA.join(MARKET_DATA).on(ALGO_DATA.ID.eq(MARKET_DATA.ALGO_ID))
                            .join(COIN_DATA).on((ALGO_DATA.ID.eq(COIN_DATA.ALGO_ID)))
                            .join(MARKET_PRICE_DATA).on(MARKET_DATA.ID.eq(MARKET_PRICE_DATA.MARKET_ID))
            ).fetch();

            List<CryptoInvestment> investments = new ArrayList<>();
            for (Record record : result) {
                String coinName = record.get(COIN_DATA.COIN_NAME);
                String marketName = record.get(MARKET_DATA.MARKET_NAME);
                String algoName = record.get(ALGO_DATA.ALGO_NAME);
                double fulfillSpeed = record.get(MARKET_PRICE_DATA.FULFILL_SPEED);

                CryptoInvestment investment = new CryptoInvestment(algoName, marketName, coinName, fulfillSpeed);
                investments.add(investment);
            }

            return investments;
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

