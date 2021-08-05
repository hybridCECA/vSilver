package nicehash;
import dataclasses.NicehashOrder;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Conversions;
import whattomine.Coins;
import dataclasses.WhatToMineCoin;

import java.io.IOException;

public class OrderBot {
    private double minProfitMargin;
    private double fulfillSpeed;
    private double limit;
    private String coinName;
    private String algoName;
    private char hashUnit;
    private String orderId;
    private String market;

    public OrderBot(JSONObject config) throws JSONException {
        minProfitMargin = config.getDouble("min_profit_margin");
        coinName = config.getString("coin_name");
        algoName = config.getString("algo_name");
        hashUnit = config.getString("hash_unit").charAt(0);
        orderId = config.getString("order_id");
        market = config.getString("market");
        fulfillSpeed = config.getDouble("fulfill_speed");
        limit = config.getDouble("limit");
    }

    public void run() {
        try {
            int targetPrice = Price.getSweepPrice(fulfillSpeed, algoName, market, orderId);
            System.out.println("Target price: " + targetPrice);

            int priceCeiling = getPriceCeiling(coinName, hashUnit);
            System.out.println("Price ceiling: " + priceCeiling);
            targetPrice = Math.min(targetPrice, priceCeiling);

            int nhPriceFloor = getNHPriceFloor(orderId, algoName, market);
            System.out.println("Price floor: " + nhPriceFloor);
            targetPrice = Math.max(targetPrice, nhPriceFloor);

            double submitLimit = limit;
            if (targetPrice >= priceCeiling) {
                submitLimit = Api.getMinLimit(algoName);
            }

            Api.updateOrder(orderId, targetPrice, Conversions.getDisplayMarketFactor(hashUnit), Conversions.getMarketFactor(hashUnit), submitLimit);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getPriceCeiling(String coinName, char hashPrefix) throws IOException, JSONException {
        WhatToMineCoin coin = Coins.getCoin(coinName);
        // Choose smaller profitability from current and 24 hour exchange average
        if (coin.getProfitability() < coin.getProfitability24()) {
            System.out.println("Using current exchange rate");
        } else {
            System.out.println("Using 24 hour average exchange rate");
        }
        double unitProfit = Math.min(coin.getProfitability(), coin.getProfitability24());
        double btcProfit = Conversions.unitProfitToDailyBTC(unitProfit, hashPrefix);
        double priceCeiling = btcProfit / minProfitMargin;

        return Conversions.stringPriceToIntPrice(Double.toString(priceCeiling));
    }

    public int getNHPriceFloor(String id, String algoName, String market) throws JSONException {
        NicehashOrder order = Api.getOrder(id, algoName, market);
        int downStep = Api.getDownStep(algoName);

        return order.getPrice() + downStep;
    }
}
