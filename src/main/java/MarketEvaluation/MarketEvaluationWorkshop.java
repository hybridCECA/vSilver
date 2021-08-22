package MarketEvaluation;

import database.Connection;
import dataclasses.AllDataRecord;
import dataclasses.CoinMarketPair;
import dataclasses.PriceRecord;
import nicehash.NHApi;
import nicehash.OrderBot;
import nicehash.Price;
import services.MaxProfit;
import services.MaxProfitFactory;
import services.MaxProfitImpl;
import utils.Config;
import utils.Consts;
import utils.Logging;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MarketEvaluationWorkshop {
    public static final String ORDER_ID = "order_id";
    public static final String ALGO = "algo";

    public static void start() {
        LogManager.getLogManager().reset();
        int maxProfitAnalyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);
        int analyzeWindow = 7200 + maxProfitAnalyzeMinutes;
        double minimumMarketRatio = 10;

        List<CoinMarketPair> coinMarketPairs = Connection.getCoinMarketPairs();
        Logger logger = Logging.getLogger(MarketEvaluationWorkshop.class);

        logger.info("Pairs gotten");

        Map<CoinMarketPair, Double> ratios = Connection.getMostRecentTotalSpeedRatios(coinMarketPairs);

        logger.info("Ratios gotten");
        // Remove coin market pairs below min ratio
        for (Map.Entry<CoinMarketPair, Double> entry : ratios.entrySet()) {
            if (entry.getValue() <= minimumMarketRatio) {
                coinMarketPairs.remove(entry.getKey());
            }
        }


        // Just use one for now
        //coinMarketPairs = List.of(coinMarketPairs.get(0));

        logger.info("Removing done");

        Map<CoinMarketPair, List<AllDataRecord>> map = Connection.getAllData(coinMarketPairs, analyzeWindow);

        Map<Double, String> profits = new TreeMap<>();

        for (Map.Entry<CoinMarketPair, List<AllDataRecord>> entry : map.entrySet()) {
            CoinMarketPair pair = entry.getKey();
            List<AllDataRecord> allDataRecords = entry.getValue();

            // Traverse to start
            LocalDateTime startingTime = allDataRecords.get(0).getTimestamp();
            int startingIndex = -1;
            for (int i = 0; i < allDataRecords.size(); i++) {
                AllDataRecord record = allDataRecords.get(i);
                LocalDateTime time = record.getTimestamp();
                if (time.isAfter(startingTime.plusMinutes(maxProfitAnalyzeMinutes))) {
                    startingIndex = i;
                    break;
                }
            }

            String algoName = allDataRecords.get(0).getAlgoName();
            double fulfillSpeed = allDataRecords.get(0).getFulfillSpeed();

            MockNHApi nhApi = new MockNHApi();
            MockCoinSources coinSources = new MockCoinSources(algoName);
            MockMaxProfit maxProfit = new MockMaxProfit();

            OrderBot orderBot = new OrderBot(ORDER_ID, fulfillSpeed, pair.getCoinName(), algoName, pair.getMarketName());
            orderBot.setNhApi(nhApi);
            orderBot.setCoinSources(coinSources);
            orderBot.setMaxProfit(maxProfit);

            LocalDateTime lastDownAdjustTime = startingTime.minusMinutes(15);

            int currentPrice = 0;
            double profit = 0;
            int count = 0;

            for (int i = startingIndex; i < allDataRecords.size(); i++) {
                AllDataRecord record = allDataRecords.get(i);

                nhApi.setFulfillPrice(record.getFulfillPrice());
                coinSources.setProfitability(record.getCoinRevenue());

                LocalDateTime currentTime = record.getTimestamp();
                List<PriceRecord> priceRecords = getPriceRecordsList(allDataRecords, currentTime.minusMinutes(maxProfitAnalyzeMinutes), currentTime);
                int maxProfitPrice = MaxProfitFactory.getInstance().getMaxProfitPrice(priceRecords, record.getCoinRevenue());
                maxProfit.setMaxProfit(maxProfitPrice);

                nhApi.setCurrentPrice(currentPrice);

                orderBot.run();

                int submitPrice = nhApi.getSubmitPrice();

                if (submitPrice < currentPrice && currentTime.isBefore(lastDownAdjustTime.plusMinutes(10))) {
                    submitPrice = currentPrice;
                }
                currentPrice = submitPrice;

                if (currentPrice > record.getFulfillPrice()) {
                    profit += (double)(record.getCoinRevenue() - currentPrice) / currentPrice;
                }
                count++;
            }

            double averageProfit = profit / count;
            profits.put(averageProfit, pair.getCoinName());
            System.out.println(pair.getCoinName() + " done");
        }

        for (Map.Entry<Double, String> entry : profits.entrySet()) {
            System.out.println(entry.getValue() + ": " + entry.getKey());
        }

        logger.info("All data got");

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

    private static Map<String, Double> getCoinAverages(List<AllDataRecord> list) {
        Map<String, Long> sums = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (AllDataRecord record : list) {
            String coinName = record.getCoinName();

            if (sums.containsKey(coinName)) {
                long currentSum = sums.get(coinName);
                currentSum += record.getCoinRevenue();
                sums.put(coinName, currentSum);
            } else {
                sums.put(coinName, (long) record.getCoinRevenue());
            }

            if (counts.containsKey(coinName)) {
                int currentCount = counts.get(coinName);
                currentCount++;
                counts.put(coinName, currentCount);
            } else {
                counts.put(coinName, 1);
            }
        }

        Map<String, Double> averages = new HashMap<>();
        for (String coinName : sums.keySet()) {
            double average = ((double) sums.get(coinName)) / counts.get(coinName);
            averages.put(coinName, average);
        }

        return averages;
    }
}
