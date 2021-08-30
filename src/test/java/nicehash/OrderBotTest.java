package nicehash;

import coinsources.CoinSources;
import dataclasses.Coin;
import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import services.MaxProfit;
import utils.SingletonFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBotTest {
    private static final String ALGO = "test_algo";
    private static final String COIN = "test_coin";
    private static final String MARKET = "test_market";
    private static final String ORDER_ID = "test_id";
    private static final double ORDER_LIMIT = 2;
    private static final double MIN_LIMIT = 0;

    @Test
    void testOrderBot() throws JSONException {
        List<NicehashOrder> orderbook = List.of(
                new NicehashOrder(20, 2, "other_order_1", 2),
                new NicehashOrder(15, 1, "other_order_2", 2),
                new NicehashOrder(10, 0, ORDER_ID, ORDER_LIMIT),
                new NicehashOrder(5, 0, "other_order_3", 2)
        );

        runTest(orderbook, 21, 25, 25, ORDER_LIMIT);
        runTest(orderbook, 17, 25, 17, ORDER_LIMIT);
        runTest(orderbook, 13, 17, 13, ORDER_LIMIT);
        runTest(orderbook, 9, 7, 7, MIN_LIMIT);

        orderbook = List.of(
                new NicehashOrder(20, 2, "other_order_1", 2),
                new NicehashOrder(15, 2, "other_order_2", 2),
                new NicehashOrder(10, 0, ORDER_ID, ORDER_LIMIT),
                new NicehashOrder(5, 0, "other_order_3", 2)
        );
        runTest(orderbook, 16, 25, 25, ORDER_LIMIT);

        orderbook = List.of(
                new NicehashOrder(20, 2, "other_order_1", 2),
                new NicehashOrder(15, 2, "other_order_2", 2),
                new NicehashOrder(10, 2, ORDER_ID, ORDER_LIMIT),
                new NicehashOrder(5, 2, "other_order_3", 2)
        );
        runTest(orderbook, 9, 25, 25, ORDER_LIMIT);
    }

    void runTest(List<NicehashOrder> orderbook, int expectedPrice, int profitability, int maxProfitBound, double expectedLimit) throws JSONException {
        NicehashAlgorithmBuyInfo buyInfo = new NicehashAlgorithmBuyInfo(ALGO, -1, MIN_LIMIT, 0.1, new JSONArray(), "KH", 1);
        List<NicehashAlgorithmBuyInfo> buyInfoList = List.of(buyInfo);

        NHApi testNhApi = new NHApi() {
            @Override
            public void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) {
                assertEquals(price, expectedPrice);
            }

            @Override
            public NicehashOrder getOrder(String id, String algoName, String market) {
                for (NicehashOrder order : orderbook) {
                    if (order.getId().equals(id)) {
                        return order;
                    }
                }

                return null;
            }

            @Override
            public List<NicehashOrder> getOrderbook(String algoName, String market) {
                return orderbook;
            }

            @Override
            public void invalidateOrderbookCache(String algoName) {

            }

            @Override
            public void invalidateOrderbookCache() {

            }

            @Override
            public Set<OrderBot> getActiveOrders() {
                return null;
            }

            @Override
            public List<NicehashAlgorithm> getAlgoList() {
                return null;
            }

            @Override
            public List<NicehashAlgorithmBuyInfo> getBuyInfo() {
                return buyInfoList;
            }

            @Override
            public NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) {
                return buyInfo;
            }

            @Override
            public void invalidateBuyInfoCache() {

            }

            @Override
            public String getLightningAddress(double amount) {
                return null;
            }
        };

        CoinSources testCoinSources = new CoinSources() {
            @Override
            public List<Coin> getCoinList() {
                return null;
            }

            @Override
            public Coin getCoin(String coinName) {
                Coin coin = new Coin();
                coin.setName(COIN);
                coin.setAlgorithm(ALGO);
                double unitProfitabilityFactor = 1.0 / 10000.0 * 100E6 / 1E3;
                coin.setUnitProfitability(profitability * unitProfitabilityFactor);

                return coin;
            }
        };

        MaxProfit testMaxProfit = new MaxProfit() {
            @Override
            public void updateMaxProfits() {

            }

            @Override
            public void register(OrderBot bot) {

            }

            @Override
            public void unregister(OrderBot bot) {

            }

            @Override
            public int getMaxProfit(OrderBot bot) {
                return maxProfitBound;
            }

            @Override
            public boolean hasMaxProfit(OrderBot bot) {
                return true;
            }

            @Override
            public int getRunPeriodSeconds() {
                return 0;
            }

            @Override
            public void run() {

            }
        };

        SingletonFactory.setInstance(NHApi.class, testNhApi);

        OrderBot orderBot = new OrderBot(ORDER_ID, ORDER_LIMIT, COIN, ALGO, MARKET);
        orderBot.setCoinSources(testCoinSources);
        orderBot.setMaxProfit(testMaxProfit);
        orderBot.run();
    }
}