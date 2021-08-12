package nicehash;

import database.Connection;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import org.jooq.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import utils.Config;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class MaxProfitTest {
    private static final String ALGO = "test_algo";
    private static final String COIN = "test_coin";
    private static final String MARKET = "test_market";
    private static final TriplePair PAIR = new TriplePair(ALGO, MARKET, COIN);
    private static final String ANALYZE_MINUTES_STRING = "300";
    private static final int ANALYZE_MINUTES = 300;

    @Test
    void testMaxProfit() throws JSONException {
        List<PriceRecord> records = List.of(
                new PriceRecord(1, 1),
                new PriceRecord(2, 1),
                new PriceRecord(3, 1)
        );
        runTest(records, 4, 2);

        records = List.of(
                new PriceRecord(1, 3),
                new PriceRecord(2, 2),
                new PriceRecord(3, 1)
        );
        runTest(records, 4, 2);

        records = List.of(
                new PriceRecord(1, 4),
                new PriceRecord(2, 1),
                new PriceRecord(3, 1)
        );
        runTest(records, 4, 1);
    }

    private void runTest(List<PriceRecord> priceRecords, int revenue, int expectedPrice) throws JSONException {
        try (MockedStatic<Config> mockedConfig = mockStatic(Config.class)) {
            mockedConfig.when(() -> Config.getConfigValue("max_profit_analyze_minutes")).thenReturn(ANALYZE_MINUTES_STRING);

            try (MockedStatic<Connection> mockedConnection = mockStatic(Connection.class)) {
                double unitProfitabilityFactor = 1.0 / 10000 * 100E6 / 1E3;
                System.out.println(revenue * unitProfitabilityFactor);

                mockedConnection.when(() -> Connection.getPrices(ALGO, MARKET, ANALYZE_MINUTES)).thenReturn(priceRecords);
                mockedConnection.when(() -> Connection.getCoinRevenue(COIN)).thenReturn(revenue * unitProfitabilityFactor);

                try (MockedStatic<Api> mockedApi = mockStatic(Api.class)) {
                    mockedApi.when(() -> Api.getAlgoBuyInfo(ALGO)).thenReturn(new NicehashAlgorithmBuyInfo(ALGO, 0, 0, 0, new JSONArray(), "k", 0));

                    MaxProfit.register(PAIR);
                    assertFalse(MaxProfit.hasMaxProfit(PAIR));
                    MaxProfit.updateMaxProfits();
                    assertEquals(expectedPrice, MaxProfit.getMaxProfit(PAIR));
                }
            }
        }
    }
}