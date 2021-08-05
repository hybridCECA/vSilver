package nicehash;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.Conversions;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Api {
    private static HttpApi api;

    public static void main(String[] args) throws IOException, JSONException {
        loadConfig();
        System.out.println(getMinLimit("cryptonightr"));
        System.out.println(getMinLimit("lyra2z"));
    }

    public static void loadConfig() throws IOException, JSONException {
        JSONObject config = Config.getConfigObject();

        String orgId =  config.getString("orgId");
        String apiKey = config.getString("apiKey");
        String apiSecret = config.getString("apiSecret");

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

    public static List<NicehashOrder> getOrderbook(String algoName, String market) throws JSONException {
        String response = api.get("main/api/v2/hashpower/orderBook?algorithm=" + algoName.toUpperCase());
        JSONObject json = new JSONObject(response);
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

    public static int getDownStep(String algoName) throws JSONException {
        algoName = algoName.toLowerCase();

        String response = api.get("main/api/v2/public/buy/info");
        JSONObject json = new JSONObject(response);
        JSONArray algos = json.getJSONArray("miningAlgorithms");

        for (int i = 0; i < algos.length(); i++) {
            JSONObject algo = algos.getJSONObject(i);
            String name = algo.getString("name");

            if (name.toLowerCase().equals(algoName)) {
                String downStepString = algo.getString("down_step");
                return Conversions.stringPriceToIntPrice(downStepString);
            }
        }

        throw new RuntimeException("Algo " + algoName + " not found!");
    }

    public static double getMinLimit(String algoName) throws JSONException {
        algoName = algoName.toLowerCase();

        String response = api.get("main/api/v2/public/buy/info");
        JSONObject json = new JSONObject(response);
        JSONArray algos = json.getJSONArray("miningAlgorithms");

        for (int i = 0; i < algos.length(); i++) {
            JSONObject algo = algos.getJSONObject(i);
            String name = algo.getString("name");

            if (name.toLowerCase().equals(algoName)) {
                return algo.getDouble("min_limit");
            }
        }

        throw new RuntimeException("Algo " + algoName + " not found!");
    }
}
