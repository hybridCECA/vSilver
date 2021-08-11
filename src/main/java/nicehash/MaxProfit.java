package nicehash;

import database.Connection;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import org.json.JSONException;
import utils.Conversions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MaxProfit {
    private static final Map<TriplePair, Integer> maxProfitCache = new ConcurrentHashMap<>();
    private static final int INVALID_VALUE = -1;

    public static void updateMaxProfits() throws JSONException {
        for (TriplePair pair : maxProfitCache.keySet()) {
            // Get prices
            List<PriceRecord> priceRecords = Connection.getPrices(pair.getAlgo(), pair.getMarket());

            // Get coin revenue
            NicehashAlgorithmBuyInfo buyInfo = Api.getAlgoBuyInfo(pair.getAlgo());
            String speedText = buyInfo.getSpeedText();
            char hashPrefix = Conversions.speedTextToHashPrefix(speedText);
            double revenueUnit = Connection.getCoinRevenue(pair.getCoin());
            int revenue = Conversions.unitProfitToIntPrice(revenueUnit, hashPrefix);

            // Maximize profit, simple greedy algorithm
            int hitCount = 0;
            long maxProfit = Long.MIN_VALUE;
            int maxProfitPrice = buyInfo.getMinPrice();
            for (PriceRecord record : priceRecords) {
                int price = record.getFulfillPrice();

                hitCount += record.getCount();

                long profit = (revenue - price) * hitCount;

                if (profit > maxProfit) {
                    maxProfit = profit;
                    maxProfitPrice = price;
                }
            }

            maxProfitCache.put(pair, maxProfitPrice);

            System.out.println("Max profit price: " + maxProfitPrice);
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
