package nicehash;

import com.sun.source.util.Trees;
import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.Conversions;

import java.time.Instant;
import java.util.*;

public class Api {
    private static HttpApi api;
    private static final Map<String, JSONObject> orderbookCache = new HashMap<>();
    private static List<NicehashAlgorithmBuyInfo> buyInfoCache;

    public static void loadConfig() {
        String orgId =  Config.getConfigValue("org_id");
        String apiKey = Config.getConfigValue("api_key");
        String apiSecret = Config.getConfigValue("api_secret");

        api = new HttpApi("https://api2.nicehash.com/", orgId, apiKey, apiSecret);
    }

    public static void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) throws JSONException {
        String priceString = Conversions.intPriceToStringPrice(price);
        System.out.println("Submit price: " + priceString);

        String limitString = Conversions.doublePriceToStringPrice(limit);
        System.out.println("Submit limit: " + limit);

        JSONObject body = new JSONObject();
        body.put("price", priceString);
        body.put("limit", limitString);
        body.put("displayMarketFactor", displayMarketFactor);
        body.put("marketFactor", marketFactor);

        String response = api.post("main/api/v2/hashpower/order/" + id + "/updatePriceAndLimit", body.toString(),  getTime(), true);
        System.out.println("Response: " + response);
    }

    public static NicehashOrder getOrder(String id, String algoName, String market) throws JSONException {
        List<NicehashOrder> orderbook = getOrderbook(algoName, market);
        for (NicehashOrder order : orderbook) {
            if (order.getId().equals(id)) {
                return order;
            }
        }

        throw new RuntimeException("Order not found");
    }

    private static JSONObject getOrderbookJson(String algoName) throws JSONException {
        if (orderbookCache.containsKey(algoName)) {
            return orderbookCache.get(algoName);
        } else {
            String response = api.get("main/api/v2/hashpower/orderBook?algorithm=" + algoName.toUpperCase());
            JSONObject json = new JSONObject(response);

            orderbookCache.put(algoName, json);
            return json;
        }
    }

    public static List<NicehashOrder> getOrderbook(String algoName, String market) throws JSONException {
        JSONObject json = getOrderbookJson(algoName);
        JSONArray orders = json.getJSONObject("stats").getJSONObject(market).getJSONArray("orders");

        List<NicehashOrder> list = new ArrayList<>();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            String priceString = order.getString("price");
            int price = Conversions.stringPriceToIntPrice(priceString);
            double speed = order.getDouble("payingSpeed");
            String id = order.getString("id");

            double limit = order.getDouble("limit");
            if (limit == 0D) {
                limit = Double.MAX_VALUE;
            }

            NicehashOrder nhOrder = new NicehashOrder(price, speed, id, limit);
            list.add(nhOrder);
        }

        return list;
    }

    public static void invalidateOrderbookCache(String algoName) {
        orderbookCache.remove(algoName);
    }

    public static void invalidateOrderbookCache() {
        orderbookCache.clear();
    }

    public static Set<OrderBot> getActiveOrders() throws JSONException {
        String response = api.get("main/api/v2/hashpower/myOrders?active=true&ts=0&op=GT&limit=1000", true, getTime());
        JSONObject json = new JSONObject(response);
        JSONArray orders = json.getJSONArray("list");

        Set<OrderBot> set = new HashSet<>();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);

            String orderId = order.getString("id");
            double limit = order.getDouble("limit");
            String[] poolWords = order.getJSONObject("pool").getString("name").split(" ");
            String coinName = poolWords[0];
            String algoName = order.getJSONObject("algorithm").getString("algorithm");
            String marketName = order.getString("market");

            OrderBot bot = new OrderBot(orderId, limit, coinName, algoName, marketName);
            set.add(bot);
        }

        return set;
    }

    public static List<NicehashAlgorithm> getAlgoList() throws JSONException {
        List<NicehashAlgorithm> algoList = new ArrayList<>();

        String response = api.get("main/api/v2/public/simplemultialgo/info");
        JSONObject json = new JSONObject(response);
        JSONArray algos = json.getJSONArray("miningAlgorithms");
        for (int i = 0; i < algos.length(); i++) {
            NicehashAlgorithm nicehashAlgo = new NicehashAlgorithm();

            JSONObject algo = algos.getJSONObject(i);
            String algorithm = algo.getString("algorithm");
            double rate = algo.getDouble("paying");

            nicehashAlgo.setAlgorithm(algorithm);
            nicehashAlgo.setProfitability(rate);

            algoList.add(nicehashAlgo);
        }

        return algoList;
    }

    private static String getTime() {
        return Long.toString(Instant.now().toEpochMilli());
    }

    private static List<NicehashAlgorithmBuyInfo> getBuyInfo() throws JSONException {
        if (buyInfoCache != null) {
            return buyInfoCache;
        }

        String response = api.get("main/api/v2/public/buy/info");
        JSONObject json = new JSONObject(response);
        JSONArray algos = json.getJSONArray("miningAlgorithms");

        List<NicehashAlgorithmBuyInfo> list = new ArrayList<>();
        for (int i = 0; i < algos.length(); i++) {
            JSONObject algo = algos.getJSONObject(i);
            String name = algo.getString("name");
            int downStep = Conversions.stringPriceToIntPrice(algo.getString("down_step"));
            double minLimit = algo.getDouble("min_limit");
            double minAmount = algo.getDouble("min_amount");
            JSONArray markets = algo.getJSONArray("enabledHashpowerMarkets");
            String speedText = algo.getString("speed_text");
            int minPrice = Conversions.stringPriceToIntPrice(algo.getString("min_price"));

            NicehashAlgorithmBuyInfo algoBuyInfo = new NicehashAlgorithmBuyInfo(name, downStep, minLimit, minAmount, markets, speedText, minPrice);
            list.add(algoBuyInfo);
        }

        buyInfoCache = list;
        return list;
    }

    public static NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) throws JSONException {
        List<NicehashAlgorithmBuyInfo> buyInfo = getBuyInfo();
        algoName = algoName.toLowerCase();

        for (NicehashAlgorithmBuyInfo info : buyInfo) {
            if (info.getName().toLowerCase().equals(algoName)) {
                return info;
            }
        }

        throw new RuntimeException("Algo not found");
    }

    public static void invalidateBuyInfoCache() {
        buyInfoCache = null;
    }
}
