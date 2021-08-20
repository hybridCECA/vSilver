package nicehash;

import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import dataclasses.TriplePair;
import dataclasses.Coin;
import org.json.JSONException;
import services.AdjustBot;
import services.MaxProfit;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import coinsources.WhatToMineCoins;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class OrderBot implements Comparable<OrderBot> {
    private static final Logger LOGGER = AdjustBot.LOGGER;
    private final String orderId;
    private double limit;
    private final String coinName;
    private final String algoName;
    private final String marketName;

    public OrderBot(String orderId, double limit, String coinName, String algoName, String marketName) {
        this.orderId = orderId;
        this.limit = limit;
        this.coinName = coinName;
        this.algoName = algoName;
        this.marketName = marketName;
    }

    public void run() {
        try {
            NHApi.invalidateOrderbookCache(algoName);

            int price = Price.getSweepPrice(limit, algoName, marketName, orderId);
            LOGGER.info("Target price: " + price);

            NicehashAlgorithmBuyInfo algoBuyInfo = NHApi.getAlgoBuyInfo(algoName);
            char hashPrefix = Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());

            int profitabilityBound = getProfitabilityBound(coinName);
            LOGGER.info("Profitability bound: " + profitabilityBound);
            price = Math.min(price, profitabilityBound);

            TriplePair pair = getTriplePair();
            if (!MaxProfit.hasMaxProfit(pair)) {
                LOGGER.info("No max profit yet, waiting...");
                return;
            }
            int maxProfitabilityBound = MaxProfit.getMaxProfit(pair);
            LOGGER.info("Max profitability bound: " + maxProfitabilityBound);
            price = Math.min(price, maxProfitabilityBound);

            int decraseBound = getPriceDecreaseBound(orderId, algoName, marketName);
            LOGGER.info("Decrease bound: " + decraseBound);
            price = Math.max(price, decraseBound);

            double submitLimit = limit;
            if (price > profitabilityBound) {
                submitLimit = algoBuyInfo.getMinLimit();
            }

            NHApi.updateOrder(orderId, price, Conversions.getDisplayMarketFactor(hashPrefix), Conversions.getMarketFactor(hashPrefix), submitLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TriplePair getTriplePair() {
        return new TriplePair(algoName, marketName, coinName);
    }

    public int getProfitabilityBound(String coinName) throws IOException, JSONException {
        Coin coin = WhatToMineCoins.getCoin(coinName);
        double minProfitMargin = Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);

        double profitabilityBound = coin.getIntProfitability() / minProfitMargin;

        return Math.toIntExact(Math.round(profitabilityBound));
    }

    public int getPriceDecreaseBound(String id, String algoName, String market) throws JSONException {
        NicehashOrder order = NHApi.getOrder(id, algoName, market);
        int downStep = NHApi.getAlgoBuyInfo(algoName).getDownStep();

        return order.getPrice() + downStep;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderBot)) return false;
        OrderBot orderBot = (OrderBot) o;
        return orderId.equals(orderBot.orderId);
    }

    @Override
    public int compareTo(OrderBot o) {
        return this.orderId.compareTo(o.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
