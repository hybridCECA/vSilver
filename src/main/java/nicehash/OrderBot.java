package nicehash;

import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import dataclasses.TriplePair;
import dataclasses.WhatToMineCoin;
import org.json.JSONException;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import whattomine.Coins;

import java.io.IOException;
import java.util.Objects;

public class OrderBot implements Comparable<OrderBot> {
    private String orderId;
    private double limit;
    private String coinName;
    private String algoName;
    private String marketName;

    public OrderBot(String orderId, double limit, String coinName, String algoName, String marketName) {
        this.orderId = orderId;
        this.limit = limit;
        this.coinName = coinName;
        this.algoName = algoName;
        this.marketName = marketName;
    }

    public void run() {
        try {
            Api.invalidateOrderbookCache(algoName);

            int price = Price.getSweepPrice(limit, algoName, marketName, orderId);
            System.out.println("Target price: " + price);

            NicehashAlgorithmBuyInfo algoBuyInfo = Api.getAlgoBuyInfo(algoName);
            char hashPrefix = Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());

            int profitabilityBound = getProfitabilityBound(coinName);
            System.out.println("Profitability bound: " + profitabilityBound);
            price = Math.min(price, profitabilityBound);

            TriplePair pair = getTriplePair();
            if (!MaxProfit.hasMaxProfit(pair)) {
                System.out.println("No max profit yet, waiting...");
                return;
            }
            int maxProfitabilityBound = MaxProfit.getMaxProfit(pair);
            System.out.println("Max profitability bound: " + maxProfitabilityBound);
            price = Math.min(price, maxProfitabilityBound);

            int decraseBound = getPriceDecreaseBound(orderId, algoName, marketName);
            System.out.println("Decrease bound: " + decraseBound);
            price = Math.max(price, decraseBound);

            double submitLimit = limit;
            if (price > profitabilityBound) {
                submitLimit = algoBuyInfo.getMinLimit();
            }

            Api.updateOrder(orderId, price, Conversions.getDisplayMarketFactor(hashPrefix), Conversions.getMarketFactor(hashPrefix), submitLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TriplePair getTriplePair() {
        return new TriplePair(algoName, marketName, coinName);
    }

    public int getProfitabilityBound(String coinName) throws IOException, JSONException {
        WhatToMineCoin coin = Coins.getCoin(coinName);
        double minProfitMargin = Config.getConfigDouble(Consts.ORDER_BOT_MIN_PROFIT_MARGIN);

        double profitabilityBound = coin.getIntProfitability() / minProfitMargin;

        return Math.toIntExact(Math.round(profitabilityBound));
    }

    public int getPriceDecreaseBound(String id, String algoName, String market) throws JSONException {
        NicehashOrder order = Api.getOrder(id, algoName, market);
        int downStep = Api.getAlgoBuyInfo(algoName).getDownStep();

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
