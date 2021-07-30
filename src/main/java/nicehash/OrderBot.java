package nicehash;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Conversions;
import whattomine.Coins;
import whattomine.WhatToMineCoin;

import java.io.IOException;

public class OrderBot {
    private double minProfitMargin;
    private double fulfillSpeed;
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

            Api.updateOrder(orderId, targetPrice, Conversions.getDisplayMarketFactor(hashUnit), Conversions.getMarketFactor(hashUnit));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getPriceCeiling(String coinName, char hashPrefix) throws IOException, JSONException {
        WhatToMineCoin coin = Coins.getCoin(coinName);
        double unitProfit = coin.getProfitability();
        double btcProfit = Conversions.unitProfitToDailyBTC(unitProfit, hashPrefix);
        double priceCeiling = btcProfit / minProfitMargin;

        return Conversions.stringPriceToIntPrice(Double.toString(priceCeiling));
    }

    private int getNHPriceFloor(String id, String algoName, String market) throws JSONException {
        NicehashOrder order = Api.getOrder(id, algoName, market);
        int downStep = Api.getDownStep(algoName);

        return order.getPrice() + downStep;
    }
}
