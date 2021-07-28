import nicehashapi.Api;
import nicehashapi.NicehashOrder;
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
    public static double FULFILL_SPEED;
    public static String COIN_NAME;
    public static String ALGO_NAME;
    public static char HASH_UNIT;
    public static String ORDER_ID;
    public static String MARKET;

    private static void loadConfig() throws IOException, JSONException {
        JSONObject config = Config.getConfigObject();

        MIN_PROFIT_MARGIN = config.getDouble("min_profit_margin");
        COIN_NAME = config.getString("coin_name");
        ALGO_NAME = config.getString("algo_name");
        HASH_UNIT = config.getString("hash_unit").charAt(0);
        ORDER_ID = config.getString("order_id");
        MARKET = config.getString("market");
        FULFILL_SPEED = config.getDouble("fulfill_speed");
    }

    public static void main(String[] args) throws IOException, JSONException {
        loadConfig();
        Api.loadConfig();

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
            try {
                int targetPrice = Price.getSweepPrice(FULFILL_SPEED, ALGO_NAME, MARKET, ORDER_ID);
                System.out.println("Target price: " + targetPrice);
                int priceCeiling = getPriceCeiling(COIN_NAME, HASH_UNIT);
                System.out.println("Price ceiling: " + priceCeiling);
                targetPrice = Math.min(targetPrice, priceCeiling);

                int nhPriceFloor = getNHPriceFloor(ORDER_ID, ALGO_NAME, MARKET);
                System.out.println("Price floor: " + nhPriceFloor);
                targetPrice = Math.max(targetPrice, nhPriceFloor);

                Api.updateOrder(ORDER_ID, targetPrice, Conversion.getDisplayMarketFactor(HASH_UNIT), Conversion.getMarketFactor(HASH_UNIT));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        service.scheduleAtFixedRate(print, 0, 30, TimeUnit.SECONDS);
    }

    private static int getPriceCeiling(String coinName, char hashPrefix) throws IOException, JSONException {
        WhatToMineCoin coin = Coins.getCoin(coinName);
        double unitProfit = coin.getProfitability();
        double btcProfit = Conversion.unitProfitToDailyBTC(unitProfit, hashPrefix);
        double priceCeiling = btcProfit / MIN_PROFIT_MARGIN;

        return Conversion.stringPriceToIntPrice(Double.toString(priceCeiling));
    }

    private static int getNHPriceFloor(String id, String algoName, String market) throws JSONException, IOException {
        NicehashOrder order = Api.getOrder(id, algoName, market);
        int downStep = Api.getDownStep(algoName);

        return order.getPrice() + downStep;
    }
}
