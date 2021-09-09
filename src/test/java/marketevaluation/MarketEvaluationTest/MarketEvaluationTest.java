package marketevaluation.MarketEvaluationTest;

import database.AllData;
import database.Connection;
import dataclasses.AllDataRecord;
import dataclasses.CryptoInvestment;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.ProfitReport;
import marketevaluation.MarketEvaluation;
import nicehash.NHApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import utils.SingletonFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

public class MarketEvaluationTest {
    private static final String ALGO_NAME = "ALGO";
    private static final String MARKET_NAME = "MARKET";
    private static final double FULFILL_SPEED = 0.1;
    private static final double TOTAL_SPEED = 100;
    private static final double EXCHANGE_RATE = 10;
    private static final double NETHASH = 1000;

    // Coin name -> answer
    private static final String COIN1 = "COIN1";
    private static final String COIN2 = "COIN2";
    private static final Map<String, String> solutionsMap = Map.of(
            COIN1, "ProfitReport{temporalProfitRatio=0.23266288123461443, totalProfitRatio=0.482585620127583, activeRatio=0.4821173104434907, marketSpeedRatio=1000.0, coinSpeedRatio=10.0, totalProfit=1140.2999999999981, evaluationScore=1058.2752900247938}",
            COIN2, "ProfitReport{temporalProfitRatio=0.06456007674827342, totalProfitRatio=0.0932386232376924, activeRatio=0.6924177396280401, marketSpeedRatio=1000.0, coinSpeedRatio=10.0, totalProfit=-1022.6281818181817, evaluationScore=-1013.4751938047226}"
    );

    private List<List<AllDataRecord>> getTestAllData() {
        List<List<AllDataRecord>> output = new ArrayList<>();

        LocalDateTime time = Connection.getRandomLocalDateTime();
        for (String coinName : solutionsMap.keySet()) {
            List<AllDataRecord> allDataRecords = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                int coinRevenue = -1;
                int fulfillPrice = -1;
                if (coinName.equals(COIN1)) {
                    coinRevenue = (int)(100 * Math.sin(i / 10.0) + 200);
                    fulfillPrice = (int)(100 * Math.sin(i / 20.0) + 150);
                } else if (coinName.equals(COIN2)) {
                    coinRevenue = (int)(200 * Math.sin(i / 5.0) * Math.sin(i / 10.0) + 200);
                    fulfillPrice = (int)(100 * Math.sin(i / 20.0) + 150);
                }

                AllDataRecord record = new AllDataRecord(time, ALGO_NAME, MARKET_NAME, FULFILL_SPEED, TOTAL_SPEED, fulfillPrice, coinName, coinRevenue, EXCHANGE_RATE, NETHASH);
                allDataRecords.add(record);

                time = time.plusMinutes(1);
            }
            output.add(allDataRecords);
        }

        return output;
    }

    @Test
    public void getProfitReports() throws JSONException {
        MockNHApi mockNHApi = new MockNHApi();
        NicehashAlgorithmBuyInfo buyInfo = new NicehashAlgorithmBuyInfo(ALGO_NAME, -1, 0, 0, new JSONArray(), "kh", 0);
        List<NicehashAlgorithmBuyInfo> buyInfoList = List.of(buyInfo);
        mockNHApi.setBuyInfoList(buyInfoList);
        SingletonFactory.setInstance(NHApi.class, mockNHApi);

        try (MockedStatic<AllData> mockedAllData = mockStatic(AllData.class)) {
            mockedAllData.when(() -> AllData.getNextData(anyInt())).thenReturn(getTestAllData());
            mockedAllData.when(AllData::hasData).thenReturn(false);

            Map<CryptoInvestment, ProfitReport> profitReportMap = MarketEvaluation.getProfitReports();

            assertEquals(solutionsMap.size(), profitReportMap.size());
            for (Map.Entry<CryptoInvestment, ProfitReport> entry : profitReportMap.entrySet()) {
                ProfitReport report = entry.getValue();
                String coinName = entry.getKey().getCoin();
                String expectedReport = solutionsMap.get(coinName);

                assertEquals(expectedReport, report.toString());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @AfterClass
    public static void teardown() {
        SingletonFactory.clearInstances();
    }
}