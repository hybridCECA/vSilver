package services;

import database.Connection;
import dataclasses.PriceRecord;
import nicehash.OrderBot;
import utils.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MaxProfitImpl implements MaxProfit {
    private static final int INVALID_VALUE = -1;
    private final static VLogger LOGGER = Logging.getLogger(MaxProfit.class);

    private final Map<OrderBot, Integer> maxProfitCache;

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
            LOGGER.error(e);
        }
    }

    public void updateMaxProfits() {
        for (OrderBot orderBot : maxProfitCache.keySet()) {
            // Get prices
            int analyzeMinutes = Config.getConfigInt(Consts.MAX_PROFIT_ANALYZE_MINUTES);

            List<PriceRecord> priceRecords = Connection.getPrices(orderBot, analyzeMinutes);
            int revenue = Connection.getCoinRevenue(orderBot.getCoinName());

            int maxProfitPrice = getMaxProfitPrice(priceRecords, revenue);
            maxProfitCache.put(orderBot, maxProfitPrice);

            LOGGER.info(orderBot.getCoinName() + " max profit price: " + maxProfitPrice);
        }
    }

    public int getMaxProfitPrice(Collection<PriceRecord> records, int revenue) {
        // Maximize profit, simple greedy algorithm
        // List must be sorted increasing
        int hitCount = 0;
        long maxProfit = 0;
        int maxProfitPrice = 0;
        for (PriceRecord record : records) {
            int price = record.getFulfillPrice();

            hitCount += record.getCount();

            long profit = (long) (revenue - price) * hitCount;
            if (profit > maxProfit) {
                maxProfit = profit;
                maxProfitPrice = price;
            }
        }

        return maxProfitPrice;
    }

    public void register(OrderBot bot) {
        maxProfitCache.put(bot, INVALID_VALUE);
    }

    public void unregister(OrderBot bot) {
        maxProfitCache.remove(bot);
    }

    public int getMaxProfit(OrderBot bot) {
        if (hasMaxProfit(bot)) {
            return maxProfitCache.get(bot);
        } else {
            throw new RuntimeException("Doesn't have max profit!");
        }
    }

    public boolean hasMaxProfit(OrderBot bot) {
        return maxProfitCache.get(bot) != INVALID_VALUE;
    }
}
