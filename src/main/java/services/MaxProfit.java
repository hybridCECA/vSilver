package services;

import database.Connection;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import nicehash.NHApi;
import org.json.JSONException;
import services.DataCollector;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import utils.Logging;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MaxProfit extends vService {
    private static final Map<TriplePair, Integer> maxProfitCache = new ConcurrentHashMap<>();
    private static final int INVALID_VALUE = -1;
    public final static Logger LOGGER = Logging.getLogger(MaxProfit.class);

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.MAX_PROFIT_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Max profit start");
            updateMaxProfits();
            LOGGER.info("Max profit done");
        } catch (Exception e) {
            LOGGER.severe(Conversions.exceptionToString(e));
        }
    }

    public static void updateMaxProfits() throws JSONException {
        for (TriplePair pair : maxProfitCache.keySet()) {
            // Get prices
            int analyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);
            List<PriceRecord> priceRecords = Connection.getPrices(pair.getAlgo(), pair.getMarket(), analyzeMinutes);

            // Maximize profit, simple greedy algorithm
            int hitCount = 0;
            long maxProfit = Long.MIN_VALUE;
            NicehashAlgorithmBuyInfo buyInfo = NHApi.getAlgoBuyInfo(pair.getAlgo());
            int maxProfitPrice = buyInfo.getMinPrice();
            long revenue = Connection.getCoinRevenue(pair.getCoin());
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

            LOGGER.info(pair.getCoin() + " max profit price: " + maxProfitPrice);
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
