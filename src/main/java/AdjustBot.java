import nicehashapi.Api;
import nicehashapi.HttpApi;
import nicehashapi.Price;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.Conversion;
import whattomineapi.Coins;
import whattomineapi.WhatToMineCoin;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdjustBot {
    public static double MIN_PROFIT_MARGIN;
    public static String COIN_NAME;
    public static char HASH_UNIT;
    public static String ORDER_ID;
    public static String MARKET;

    private static void loadConfig() throws IOException, JSONException {
        JSONObject config = Config.getConfigObject();

        MIN_PROFIT_MARGIN = config.getDouble("min_profit_margin");
        COIN_NAME = config.getString("coin_name");
        HASH_UNIT = config.getString("hash_unit").charAt(0);
        ORDER_ID = config.getString("order_id");
        MARKET = config.getString("market");
    }

    public static void main(String[] args) throws IOException, JSONException {
        loadConfig();
        Api.loadConfig();

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
            try {
                double targetPrice = Price.getFulfillPrice(0.001, "cryptonightr", MARKET);
                System.out.println("Target price before ceiling: " + targetPrice);
                targetPrice = Math.min(targetPrice, getPriceCeiling(COIN_NAME, HASH_UNIT));
                System.out.println("Target price after ceiling: " + targetPrice);

                Api.updateOrder(ORDER_ID, targetPrice, Conversion.getDisplayMarketFactor(HASH_UNIT), Conversion.getMarketFactor(HASH_UNIT));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        service.scheduleAtFixedRate(print, 0, 30, TimeUnit.SECONDS);
    }

    private static double getPriceCeiling(String coinName, char hashPrefix) throws IOException, JSONException {
        WhatToMineCoin coin = Coins.getCoin(coinName);
        double unitProfit = coin.getProfitability();
        double btcProfit = Conversion.unitProfitToDailyBTC(unitProfit, hashPrefix);
        double priceCeiling = btcProfit / MIN_PROFIT_MARGIN;

        System.out.println("btcProfit: " + btcProfit);
        System.out.println("Price Ceiling: " + priceCeiling);

        return priceCeiling;
    }
}
