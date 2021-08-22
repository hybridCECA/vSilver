package services;

import database.Connection;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import utils.Logging;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MaxProfitImpl implements MaxProfit {
    private static final int INVALID_VALUE = -1;
    private final static Logger LOGGER = Logging.getLogger(MaxProfit.class);

    private final Map<TriplePair, Integer> maxProfitCache;

    public MaxProfitImpl() {
        maxProfitCache = new ConcurrentHashMap<>();
    }

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

    public void updateMaxProfits() {
        for (TriplePair pair : maxProfitCache.keySet()) {
            // Get prices
            int analyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);
            List<PriceRecord> priceRecords = Connection.getPrices(pair.getAlgo(), pair.getMarket(), analyzeMinutes);
            int revenue = Connection.getCoinRevenue(pair.getCoin());

            int maxProfitPrice = getMaxProfitPrice(priceRecords, revenue);
            maxProfitCache.put(pair, maxProfitPrice);

            LOGGER.info(pair.getCoin() + " max profit price: " + maxProfitPrice);
        }
    }

    public int getMaxProfitPrice(List<PriceRecord> list, int revenue) {
        // Maximize profit, simple greedy algorithm
        int hitCount = 0;
        long maxProfit = 0;
        int maxProfitPrice = 0;
        for (PriceRecord record : list) {
            int price = record.getFulfillPrice();

            hitCount += record.getCount();

            long profit = (long)(revenue - price) * hitCount;
            if (profit > maxProfit) {
                maxProfit = profit;
                maxProfitPrice = price;
            }
        }

        return maxProfitPrice;
    }

    public void register(TriplePair pair) {
        maxProfitCache.put(pair, INVALID_VALUE);
    }

    public void unregister(TriplePair pair) {
        maxProfitCache.remove(pair);
    }

    public int getMaxProfit(TriplePair pair) {
        if (hasMaxProfit(pair)) {
            return maxProfitCache.get(pair);
        } else {
            throw new RuntimeException("Doesn't have max profit!");
        }
    }

    public boolean hasMaxProfit(TriplePair pair) {
        return maxProfitCache.get(pair) != INVALID_VALUE;
    }
}
