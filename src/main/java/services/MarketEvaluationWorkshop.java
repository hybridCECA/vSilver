package services;

import database.Connection;
import dataclasses.AllDataRecord;
import dataclasses.CoinMarketPair;

import java.util.*;

public class MarketEvaluationWorkshop {
    public static void start() {
        int analyzeWindow = 300;
        double minimumMarketRatio = 10;

        List<AllDataRecord> list = Connection.getAllData(analyzeWindow * 2);
        System.out.println(list.size());

        // Get min ratio to delete
        Set<CoinMarketPair> toDelete = new HashSet<>();
        for (AllDataRecord record : list) {
            double marketRatio = record.getTotalSpeed() / record.getFulfillSpeed();

            if (marketRatio < minimumMarketRatio) {
                CoinMarketPair pair = getCoinPairFromAllData(record);
                toDelete.add(pair);
            }
        }

        // Delete min ratio
        Iterator<AllDataRecord> iterator = list.iterator();
        while (iterator.hasNext()) {
            AllDataRecord record = iterator.next();
            CoinMarketPair pair = getCoinPairFromAllData(record);

            if (toDelete.contains(pair)) {
                iterator.remove();
            }
        }
        System.out.println(list.size());

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

    private static CoinMarketPair getCoinPairFromAllData(AllDataRecord record) {
        String coinName = record.getCoinName();
        String marketName = record.getMarketName();
        return new CoinMarketPair(coinName, marketName);
    }
}
