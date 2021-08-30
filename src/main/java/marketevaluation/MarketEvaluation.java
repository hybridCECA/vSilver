package marketevaluation;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import database.Connection;
import dataclasses.AllDataRecord;
import dataclasses.PriceRecord;
import dataclasses.ProfitReport;
import dataclasses.TriplePair;
import nicehash.OrderBot;
import services.MaxProfitImpl;
import utils.Config;
import utils.Consts;
import utils.Logging;
import utils.VLogger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MarketEvaluation {
    public static final String ORDER_ID = "order_id";

    public static void start() {
        int maxProfitAnalyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);
        int analyzeWindow = 1440 + maxProfitAnalyzeMinutes;
        double minimumMarketRatio = 10;

        VLogger logger = Logging.getLogger(MarketEvaluation.class);

        logger.info("Getting pairs");
        List<TriplePair> triplePairs = Connection.getCoinMarketPairs();
        /*
        logger.info("Getting ratios");
        Map<TriplePair, Double> ratios = Connection.getMostRecentTotalSpeedRatios(triplePairs);

        logger.info("Removing ratios");
        // Remove coin market pairs below min ratio
        for (Map.Entry<TriplePair, Double> entry : ratios.entrySet()) {
            if (entry.getValue() <= minimumMarketRatio) {
                triplePairs.remove(entry.getKey());
            }
        }

         */

        /*
        List<TriplePair> triplePairs = List.of(new TriplePair("ZHASH", "EU", "BitcoinGold"));

         */

        logger.info("Getting all data records");

        //List<List<AllDataRecord>> allDataRecords = Connection.getAllData(triplePairs, analyzeWindow);
        List<List<AllDataRecord>> allDataRecords = null;

        ListMultimap<ProfitReport, AllDataRecord> profits = MultimapBuilder.treeKeys().arrayListValues().build();

        allDataRecords.parallelStream().forEach(list -> {
            ProfitReport profit = getAverageTemporalProfit(list, maxProfitAnalyzeMinutes);

            AllDataRecord record = list.get(list.size() - 1);
            profits.put(profit, record);

            logger.info(record.getCoinName() + " done");
        });

        for (ProfitReport profit : profits.keySet()) {
            System.out.println(profit);

            List<AllDataRecord> pairList = profits.get(profit);
            for (AllDataRecord record : pairList) {
                double ratio = record.getTotalSpeed() / record.getFulfillSpeed();
                TriplePair pair = new TriplePair(record);
                System.out.println(pair + " " + ratio);
            }

            System.out.println();
        }
    }

    private static ProfitReport getAverageTemporalProfit(List<AllDataRecord> allDataRecords, int maxProfitAnalyzeMinutes) {
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
        MockCoinSources coinSources = new MockCoinSources(algoName);
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
        double profit = 0;
        int count = 0;

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

            if (currentPrice > record.getFulfillPrice()) {
                profit += (double) (record.getCoinRevenue() - currentPrice) / currentPrice;
                count++;
            }
        }

        AllDataRecord lastRecord = allDataRecords.get(allDataRecords.size() - 1);

        int numRuns = allDataRecords.size() - startingIndex;
        double temporalProfit = profit / numRuns;
        double totalProfit = profit / count;
        double percentageActive = (double) count / numRuns;
        double totalSpeedRatio = lastRecord.getTotalSpeed() / lastRecord.getFulfillSpeed();

        return new ProfitReport(temporalProfit, totalProfit, percentageActive, totalSpeedRatio);
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
