package nicehash.OrderBotTest;

import dataclasses.Coin;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import nicehash.NHApi;
import nicehash.OrderBot;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Test;
import utils.SingletonFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderBotTest {
    private static final String ALGO = "test_algo";
    private static final String COIN = "test_coin";
    private static final String MARKET = "test_market";
    private static final String ORDER_ID = "test_id";
    private static final double ORDER_LIMIT = 2;
    private static final double MIN_LIMIT = 0;

    @Test
    public void testOrderBot() throws JSONException {
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

    public void runTest(List<NicehashOrder> orderbook, int expectedPrice, int profitability, int maxProfitBound, double expectedLimit) throws JSONException {
        NicehashAlgorithmBuyInfo buyInfo = new NicehashAlgorithmBuyInfo(ALGO, -1, MIN_LIMIT, 0.1, new JSONArray(), "KH", 1);
        List<NicehashAlgorithmBuyInfo> buyInfoList = List.of(buyInfo);

        MockNHApi mockNHApi = new MockNHApi();
        mockNHApi.setOrderbook(orderbook);
        mockNHApi.setBuyInfoList(buyInfoList);

        MockCoinSources mockCoinSources = new MockCoinSources();
        Coin coin = new Coin();
        coin.setName(COIN);
        coin.setAlgorithm(ALGO);
        double unitProfitabilityFactor = 1.0 / 10000.0 * 100E6 / 1E3;
        coin.setUnitProfitability(profitability * unitProfitabilityFactor);
        mockCoinSources.setCoin(coin);

        MockMaxProfit mockMaxProfit = new MockMaxProfit();
        mockMaxProfit.setMaxProfitBound(maxProfitBound);

        SingletonFactory.setInstance(NHApi.class, mockNHApi);

        OrderBot orderBot = new OrderBot(ORDER_ID, ORDER_LIMIT, COIN, ALGO, MARKET);
        orderBot.setCoinSources(mockCoinSources);
        orderBot.setMaxProfit(mockMaxProfit);
        orderBot.run();

        int lastPrice = mockNHApi.getLastPrice();

        assertEquals(expectedPrice, lastPrice);
    }

    @AfterClass
    public static void teardown() {
        SingletonFactory.clearInstances();
    }
}