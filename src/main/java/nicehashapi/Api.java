package nicehashapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.Conversion;
import utils.JSON;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Api {
    private static HttpApi api;

    public static void loadConfig() throws IOException, JSONException {
        JSONObject config = Config.getConfigObject();

        String orgId =  config.getString("orgId");
        String apiKey = config.getString("apiKey");
        String apiSecret = config.getString("apiSecret");

        api = new HttpApi("https://api2.nicehash.com/", orgId, apiKey, apiSecret);
    }

    public static void updateOrder(String id, int price, String displayMarketFactor, double marketFactor) throws IOException, JSONException {
        String priceString = Conversion.intPriceToStringPrice(price);
        System.out.println("Submit price: " + priceString);

        JSONObject body = new JSONObject();
        body.put("price", priceString);
        body.put("displayMarketFactor", displayMarketFactor);
        body.put("marketFactor", marketFactor);

        String response = api.post("main/api/v2/hashpower/order/" + id + "/updatePriceAndLimit", body.toString(),  getTime(), true);
        System.out.println("Response: " + response);
    }

    public static NicehashOrder getOrder(String id, String algoName, String market) throws JSONException, IOException {
        List<NicehashOrder> orderbook = getOrderbook(algoName, market);
        for (NicehashOrder order : orderbook) {
            if (order.getId().equals(id)) {
                return order;
            }
        }

        throw new RuntimeException("Order not found");
    }

    public static List<NicehashOrder> getOrderbook(String algoName, String market) throws JSONException, IOException {
        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/hashpower/orderBook?algorithm=" + algoName.toUpperCase());
        JSONArray orders = json.getJSONObject("stats").getJSONObject(market).getJSONArray("orders");

        List<NicehashOrder> list = new ArrayList<>();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            String priceString = order.getString("price");
            int price = Conversion.stringPriceToIntPrice(priceString);
            double speed = order.getDouble("payingSpeed");
            String id = order.getString("id");
            double limit = order.getDouble("limit");

            NicehashOrder nhOrder = new NicehashOrder(price, speed, id, limit);
            list.add(nhOrder);
        }

        return list;
    }

    public static List<NicehashAlgorithm> getAlgoList() throws IOException, JSONException {
        List<NicehashAlgorithm> algoList = new ArrayList<>();

        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/public/simplemultialgo/info");
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

    public static int getDownStep(String algoName) throws JSONException, IOException {
        algoName = algoName.toLowerCase();

        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/public/buy/info");
        JSONArray algos = json.getJSONArray("miningAlgorithms");

        for (int i = 0; i < algos.length(); i++) {
            JSONObject algo = algos.getJSONObject(i);
            String name = algo.getString("name");

            if (name.toLowerCase().equals(algoName)) {
                String downStepString = algo.getString("down_step");
                return Conversion.stringPriceToIntPrice(downStepString);
            }
        }

        throw new RuntimeException("Algo " + algoName + " not found!");
    }
}
