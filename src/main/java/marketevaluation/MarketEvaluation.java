package marketevaluation;

import database.AllData;
import dataclasses.*;
import nicehash.OrderBot;
import org.json.JSONException;
import services.MaxProfitImpl;
import utils.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class MarketEvaluation {
    public static final String ORDER_ID = "order_id";
    private static final VLogger LOGGER = Logging.getLogger(MarketEvaluation.class);

    public static Map<CryptoInvestment, ProfitReport> getProfitReports() throws SQLException {
        int maxProfitAnalyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);
        int analyzeMinutes = Config.getConfigInt(Consts.MARKET_EVALUATION_ANALYZE_MINUTES);
        int dataWindow = maxProfitAnalyzeMinutes + analyzeMinutes;

        LOGGER.info("Getting all data records");

        Map<CryptoInvestment, ProfitReport> profitReports = new HashMap<>();
        do {
            List<List<AllDataRecord>> allDataRecords = AllData.getNextData(dataWindow);
            allDataRecords.parallelStream().forEach(list -> {
                // Skip if no entries
                if (list.size() == 0) {
                    return;
                }

                AllDataRecord record = list.get(0);
                CryptoInvestment investment = new CryptoInvestment(record);
                try {
                    ProfitReport profitReport = getProfitReport(list, maxProfitAnalyzeMinutes);
                    //profits.put(profit, investment);
                    profitReports.put(investment, profitReport);

                    LOGGER.info(record.getCoinName() + " done");
                } catch (JSONException | IndexOutOfBoundsException e) {
                    // Skip this one, usually when a high limit doesn't have enough entries
                }

            });
        } while (AllData.hasData());

        return profitReports;
    }

    private static ProfitReport getProfitReport(List<AllDataRecord> allDataRecords, int maxProfitAnalyzeMinutes) throws JSONException {
        // Traverse to start
        AllDataRecord firstRecord = allDataRecords.get(0);
        LocalDateTime startingTime = firstRecord.getTimestamp();
        int startingIndex = -1;
        for (int i = 0; i < allDataRecords.size(); i++) {
            AllDataRecord record = allDataRecords.get(i);
            LocalDateTime time = record.getTimestamp();
            if (time.isAfter(startingTime.plusMinutes(maxProfitAnalyzeMinutes))) {
                startingIndex = i;
                break;
            }
        }

        // Setup order bot
        String algoName = firstRecord.getAlgoName();
        double limit = allDataRecords.get(0).getFulfillSpeed();

        MockNHApi nhApi = new MockNHApi(limit);
        MockCoinSources coinSources = new MockCoinSources();
        MockMaxProfit maxProfit = new MockMaxProfit();
        MockPriceTools priceTools = new MockPriceTools();

        OrderBot orderBot = new OrderBot(ORDER_ID, limit, firstRecord.getCoinName(), algoName, firstRecord.getMarketName());
        orderBot.setNhApi(nhApi);
        orderBot.setCoinSources(coinSources);
        orderBot.setMaxProfit(maxProfit);
        orderBot.setPriceTools(priceTools);
        orderBot.disableLogging();

        LocalDateTime lastDownAdjustTime = startingTime.minusMinutes(15);
        int currentPrice = 0;
        double profitRatioTotal = 0;
        int numPaid = 0;
        double adjustedProfit = 0;
        double minProfitMargin = Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);

        // Main simulation loop
        for (int i = startingIndex; i < allDataRecords.size(); i++) {
            AllDataRecord record = allDataRecords.get(i);

            priceTools.setFulfillPrice(record.getFulfillPrice());
            coinSources.setCoinRevenue(record.getCoinRevenue());

            LocalDateTime currentTime = record.getTimestamp();
            LocalDateTime analyzeStartTime = currentTime.minusMinutes(maxProfitAnalyzeMinutes);
            List<PriceRecord> priceRecords = getPriceRecordsList(allDataRecords, analyzeStartTime, currentTime);
            int maxProfitPrice = new MaxProfitImpl().getMaxProfitPrice(priceRecords, record.getCoinRevenue());
            maxProfit.setMaxProfit(maxProfitPrice);

            nhApi.setCurrentPrice(currentPrice);

            orderBot.run();

            int submitPrice = nhApi.getSubmitPrice();

            /*
            double hour = currentTime.getHour();
            hour += currentTime.getMinute() / 60.0;
            System.out.println("(" + hour + ", " + submitPrice + ")");

             */

            if (submitPrice < currentPrice) {
                if (currentTime.isBefore(lastDownAdjustTime.plusMinutes(10))) {
                    submitPrice = currentPrice;
                } else {
                    lastDownAdjustTime = currentTime;
                }
            }

            currentPrice = submitPrice;

            NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(record.getAlgoName());
            String speedText = buyInfo.getSpeedText();
            char hashPrefix = Conversions.speedTextToHashPrefix(speedText);
            double factor = Conversions.getMarketFactor(hashPrefix);

            if (currentPrice > record.getFulfillPrice()) {
                profitRatioTotal += (double) (record.getCoinRevenue() - currentPrice) / currentPrice;
                adjustedProfit += record.getCoinRevenue() * record.getNethash() / (record.getNethash() + limit * factor) - currentPrice * minProfitMargin;
                numPaid++;
            }
        }

        AllDataRecord lastRecord = allDataRecords.get(allDataRecords.size() - 1);

        int numRuns = allDataRecords.size() - startingIndex;
        double temporalProfitRatio = profitRatioTotal / numRuns;
        double totalProfitRatio = profitRatioTotal / numPaid;
        double activeRatio = (double) numPaid / numRuns;
        double marketSpeedRatio = lastRecord.getTotalSpeed() / lastRecord.getFulfillSpeed();
        double totalProfit = adjustedProfit * limit;

        NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(lastRecord.getAlgoName());
        String speedText = buyInfo.getSpeedText();
        char hashPrefix = Conversions.speedTextToHashPrefix(speedText);
        double factor = Conversions.getMarketFactor(hashPrefix);
        double coinSpeedRatio = lastRecord.getNethash() / lastRecord.getFulfillSpeed() / factor;

        return new ProfitReport(temporalProfitRatio, totalProfitRatio, activeRatio, marketSpeedRatio, coinSpeedRatio, totalProfit);
    }

    private static List<PriceRecord> getPriceRecordsList(List<AllDataRecord> allDataRecords, LocalDateTime startTime, LocalDateTime endTime) {
        // Fullfill price -> count
        Map<Integer, Integer> counter = new TreeMap<>();
        for (AllDataRecord record : allDataRecords) {
            LocalDateTime time = record.getTimestamp();
            if (time.isAfter(endTime)) {
                break;
            }

            if (time.isAfter(startTime)) {
                int fulfillPrice = record.getFulfillPrice();

                if (counter.containsKey(fulfillPrice)) {
                    int currentCount = counter.get(fulfillPrice);
                    currentCount++;
                    counter.put(fulfillPrice, currentCount);
                } else {
                    counter.put(fulfillPrice, 1);
                }
            }
        }

        List<PriceRecord> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
            int fulfillPrice = entry.getKey();
            int count = entry.getValue();

            PriceRecord priceRecord = new PriceRecord(fulfillPrice, count);
            list.add(priceRecord);
        }

        return list;
    }
}
