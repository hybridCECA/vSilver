package nicehash;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.AdjustBot;
import utils.*;

import java.time.Instant;
import java.util.*;

public class NHApiImpl implements NHApi {
    private final Map<String, JSONObject> orderbookCache = new HashMap<>();
    private List<NicehashAlgorithmBuyInfo> buyInfoCache;
    private final NHHttpApi api;

    public NHApiImpl() {
        String orgId = Config.getConfigValue(Consts.ORG_ID);
        String apiKey = Config.getConfigValue(Consts.API_KEY);
        String apiSecret = Config.getConfigValue(Consts.API_SECRET);

        api = new NHHttpApi("https://api2.nicehash.com/", orgId, apiKey, apiSecret);
    }

    private JSONObject getOrderbookJson(String algoName) throws JSONException {
        if (orderbookCache.containsKey(algoName)) {
            return orderbookCache.get(algoName);
        } else {
            String response = api.get("main/api/v2/hashpower/orderBook?algorithm=" + algoName.toUpperCase() + "&size=100000");
            JSONObject json = new JSONObject(response);

            orderbookCache.put(algoName, json);
            return json;
        }
    }

    private String getTime() {
        return Long.toString(Instant.now().toEpochMilli());
    }

    public void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) throws JSONException {
        VLogger logger = Logging.getLogger(AdjustBot.class);

        String priceString = Conversions.intPriceToStringPrice(price);
        logger.info("Submit price: " + priceString);

        String limitString = Conversions.doublePriceToStringPrice(limit);
        logger.info("Submit limit: " + limit);

        JSONObject body = new JSONObject();
        body.put("price", priceString);
        body.put("limit", limitString);
        body.put("displayMarketFactor", displayMarketFactor);
        body.put("marketFactor", marketFactor);

        String response = api.post("main/api/v2/hashpower/order/" + id + "/updatePriceAndLimit", body.toString(), getTime(), true);
        logger.info("Response: " + response);
    }

    public NicehashOrder getOrder(String id, String algoName, String market) throws JSONException {
        List<NicehashOrder> orderbook = getOrderbook(algoName, market);
        for (NicehashOrder order : orderbook) {
            if (order.getId().equals(id)) {
                return order;
            }
        }

        throw new RuntimeException("Order not found");
    }

    public List<NicehashOrder> getOrderbook(String algoName, String market) throws JSONException {
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

    public void invalidateOrderbookCache(String algoName) {
        orderbookCache.remove(algoName);
    }

    public void invalidateOrderbookCache() {
        orderbookCache.clear();
    }

    public Set<OrderBot> getActiveOrders() throws JSONException {
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

    public Map<String, Double> getOrderCompletionRatios() throws JSONException {
        String response = api.get("main/api/v2/hashpower/myOrders?active=true&ts=0&op=GT&limit=1000", true, getTime());
        JSONObject json = new JSONObject(response);
        JSONArray orders = json.getJSONArray("list");

        Map<String, Double> map = new HashMap<>();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);

            String orderId = order.getString("id");
            double availableAmount = order.getDouble("availableAmount");
            double payedAmount = order.getDouble("payedAmount");
            double completionRatio = payedAmount / availableAmount;

            map.put(orderId, completionRatio);
        }

        return map;
    }

    public double getAvailableBTC() throws JSONException {
        String response = api.get("main/api/v2/accounting/account2/BTC", true, getTime());
        JSONObject json = new JSONObject(response);
        return json.getDouble("available");
    }

    public List<NicehashAlgorithm> getAlgoList() throws JSONException {
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
            nicehashAlgo.setUnitProfitability(rate);

            algoList.add(nicehashAlgo);
        }

        return algoList;
    }

    public List<NicehashAlgorithmBuyInfo> getBuyInfo() throws JSONException {
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

    public NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) throws JSONException {
        List<NicehashAlgorithmBuyInfo> buyInfo = getBuyInfo();
        algoName = algoName.toLowerCase();

        for (NicehashAlgorithmBuyInfo info : buyInfo) {
            if (info.getName().toLowerCase().equals(algoName)) {
                return info;
            }
        }

        throw new RuntimeException("Algo not found");
    }

    public void invalidateBuyInfoCache() {
        buyInfoCache = null;
    }

    public String getLightningAddress(double amount) throws JSONException {
        String response = api.get("main/api/v2/accounting/depositAddresses?currency=BTC&walletType=LIGHTNING&amount=" + amount, true, getTime());
        JSONObject json = new JSONObject(response);

        JSONArray array = json.getJSONArray("list");

        return array.getJSONObject(0).getString("address");
    }
}
