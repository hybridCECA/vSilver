package nicehash;

import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import dataclasses.TriplePair;
import dataclasses.Coin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import services.MaxProfit;
import utils.Config;
import utils.Consts;
import coinsources.WhatToMineCoins;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

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
        runTest(orderbook, 17, 17, 19, ORDER_LIMIT);
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
        // Mock scope
        try (MockedStatic<NHApi> mockedApi = mockStatic(NHApi.class)) {
            AtomicBoolean correctPrice = new AtomicBoolean(false);
            Answer<?> setCorrect = invocation -> {
                correctPrice.set(true);
                return null;
            };

            NicehashAlgorithmBuyInfo buyInfo = new NicehashAlgorithmBuyInfo(ALGO, -1, MIN_LIMIT, 0.1, new JSONArray(), "KH", 1);
            List<NicehashAlgorithmBuyInfo> buyInfoList = List.of(buyInfo);

            // Mocking
            mockedApi.when(() -> NHApi.getOrderbook(ALGO, MARKET)).thenReturn(orderbook);
            mockedApi.when(() -> NHApi.updateOrder(ORDER_ID, expectedPrice, "KH", 1000, expectedLimit)).then(setCorrect);
            mockedApi.when(() -> NHApi.getAlgoBuyInfo(ALGO)).thenReturn(buyInfo);
            mockedApi.when(NHApi::getBuyInfo).thenReturn(buyInfoList);
            mockedApi.when(() -> NHApi.getOrder(ORDER_ID, ALGO, MARKET)).thenCallRealMethod();

            try (MockedStatic<WhatToMineCoins> mockedCoin = mockStatic(WhatToMineCoins.class)) {
                Coin coin = new Coin();
                coin.setName(COIN);
                coin.setAlgorithm(ALGO);
                double unitProfitabilityFactor = 1.0 / 10000.0 * 100E6 / 1E3;
                coin.setUnitProfitability(profitability * unitProfitabilityFactor);

                mockedCoin.when(() -> WhatToMineCoins.getCoin(COIN)).thenReturn(coin);

                try (MockedStatic<MaxProfit> mockedMaxProfit = mockStatic(MaxProfit.class)) {
                    TriplePair pair = new TriplePair(ALGO, MARKET, COIN);
                    mockedMaxProfit.when(() -> MaxProfit.getMaxProfit(pair)).thenReturn(maxProfitBound);
                    mockedMaxProfit.when(() -> MaxProfit.hasMaxProfit(pair)).thenReturn(true);

                    try (MockedStatic<Config> mockedConfig = mockStatic(Config.class)) {
                        mockedConfig.when(() -> Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN)).thenReturn(1.0);

                        JSONObject config = new JSONObject();
                        config.put("min_profit_margin", 1);
                        config.put("coin_name", COIN);
                        config.put("algo_name", ALGO);
                        config.put("hash_unit", "k");
                        config.put("order_id", ORDER_ID);
                        config.put("market", MARKET);
                        config.put("fulfill_speed", ORDER_LIMIT);
                        config.put("limit", ORDER_LIMIT);

                        new OrderBot(ORDER_ID, ORDER_LIMIT, COIN, ALGO, MARKET).run();

                        assertTrue(correctPrice.get());
                    }
                }
            }
        }
    }
}