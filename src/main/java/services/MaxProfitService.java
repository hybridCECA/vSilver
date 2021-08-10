package services;

import database.Connection;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import nicehash.Api;
import org.json.JSONException;
import utils.Conversions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MaxProfitService {
    private static final Map<TriplePair, Integer> maxProfitCache = new ConcurrentHashMap<>();
    private static final int INVALID_VALUE = -1;

    public static void start() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable run = () -> {
            try {
                updateMaxProfits();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        service.scheduleAtFixedRate(run, 0, 1, TimeUnit.MINUTES);
    }

    private static void updateMaxProfits() throws JSONException {
        for (TriplePair pair : maxProfitCache.keySet()) {
            // Get prices
            List<PriceRecord> priceRecords = Connection.getPrices(pair.getAlgo(), pair.getMarket());

            // Get coin revenue
            String speedText = Api.getAlgoBuyInfo(pair.getAlgo()).getSpeedText();
            char hashPrefix = Conversions.speedTextToHashPrefix(speedText);
            double revenueUnit = Connection.getCoinRevenue(pair.getCoin());
            int revenue = Conversions.unitProfitToIntPrice(revenueUnit, hashPrefix);


            // Build and fill hit increase array
            // At price 0, there are arr[0] hits
            // arr[a] = b indicates that at price a, there are b more hits than at a - 1
            // Used for O(n) performance
            PriceRecord maxPriceRecord = priceRecords.get(priceRecords.size() - 1);
            int maxPrice = maxPriceRecord.getFulfillPrice() + 1;
            int[] hitIncreaseArray = new int[maxPrice];
            for (PriceRecord record : priceRecords) {
                int price = record.getFulfillPrice();

                hitIncreaseArray[price] = record.getCount();
            }

            // Maximize profit, simple greedy algorithm
            int hitCount = 0;
            long maxProfit = Long.MIN_VALUE;
            int maxProfitPrice = -1;
            for (int price = 0; price < hitIncreaseArray.length; price++) {
                hitCount += hitIncreaseArray[price];

                long profit = (revenue - price) * hitCount;

                if (profit > maxProfit) {
                    maxProfit = profit;
                    maxProfitPrice = price;
                }
            }

            maxProfitCache.put(pair, maxProfitPrice);

            System.out.println(maxProfitPrice);
        }
    }

    public static void register(TriplePair pair) {
        maxProfitCache.put(pair, INVALID_VALUE);
    }

    public static void unregister(TriplePair pair) {
        maxProfitCache.remove(pair);
    }

    public static int getMaxProfit(TriplePair pair) {
        if (hasMaxProfit(pair)) {
            return maxProfitCache.get(pair);
        } else {
            throw new RuntimeException("Doesn't have max profit!");
        }
    }

    public static boolean hasMaxProfit(TriplePair pair) {
        return maxProfitCache.get(pair) != INVALID_VALUE;
    }
}
