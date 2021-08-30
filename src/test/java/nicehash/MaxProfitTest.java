package nicehash;

import database.Connection;
import dataclasses.PriceRecord;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import services.MaxProfit;
import utils.Config;
import utils.Consts;
import utils.SingletonFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;

class MaxProfitTest {
    private static final String ALGO = "test_algo";
    private static final String COIN = "test_coin";
    private static final String MARKET = "test_market";
    private static final double LIMIT = 1;
    private static final OrderBot BOT = new OrderBot("id", LIMIT, COIN, ALGO, MARKET);
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

    private void runTest(List<PriceRecord> priceRecords, int revenue, int expectedPrice) {
        try (MockedStatic<Config> mockedConfig = mockStatic(Config.class)) {
            mockedConfig.when(() -> Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES)).thenReturn(ANALYZE_MINUTES);

            try (MockedStatic<Connection> mockedConnection = mockStatic(Connection.class)) {
                mockedConnection.when(() -> Connection.getPrices(BOT, ANALYZE_MINUTES)).thenReturn(priceRecords);
                mockedConnection.when(() -> Connection.getCoinRevenue(COIN)).thenReturn(revenue);

                MaxProfit maxProfit = SingletonFactory.getInstance(MaxProfit.class);
                maxProfit.register(BOT);
                assertFalse(maxProfit.hasMaxProfit(BOT));
                maxProfit.updateMaxProfits();
                assertEquals(expectedPrice, maxProfit.getMaxProfit(BOT));
            }
        }
    }
}