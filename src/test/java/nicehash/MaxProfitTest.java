package nicehash;

import database.Connection;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import utils.Config;
import utils.Consts;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;

class MaxProfitTest {
    private static final String ALGO = "test_algo";
    private static final String COIN = "test_coin";
    private static final String MARKET = "test_market";
    private static final TriplePair PAIR = new TriplePair(ALGO, MARKET, COIN);
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
            mockedConfig.when(() -> Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES)).thenReturn(ANALYZE_MINUTES);

            try (MockedStatic<Connection> mockedConnection = mockStatic(Connection.class)) {
                mockedConnection.when(() -> Connection.getPrices(ALGO, MARKET, ANALYZE_MINUTES)).thenReturn(priceRecords);
                mockedConnection.when(() -> Connection.getCoinRevenue(COIN)).thenReturn(revenue);

                try (MockedStatic<NHApi> mockedApi = mockStatic(NHApi.class)) {
                    mockedApi.when(() -> NHApi.getAlgoBuyInfo(ALGO)).thenReturn(new NicehashAlgorithmBuyInfo(ALGO, 0, 0, 0, new JSONArray(), "k", 0));

                    MaxProfit.register(PAIR);
                    assertFalse(MaxProfit.hasMaxProfit(PAIR));
                    MaxProfit.updateMaxProfits();
                    assertEquals(expectedPrice, MaxProfit.getMaxProfit(PAIR));
                }
            }
        }
    }
}