package nicehashapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
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

    public static void main(String[] args) throws JSONException {
        NicehashOrder order = getOrder("a0e4ccec-24c0-4945-9eef-bb1888db1d35");
        System.out.println(order.getPrice());
        System.out.println(order.getSpeed());
    }

    public static void updateOrder(String id, double price, String displayMarketFactor, double marketFactor) throws IOException, JSONException {
        DecimalFormat df = new DecimalFormat("##.####");
        df.setRoundingMode(RoundingMode.HALF_EVEN);

        String priceString = df.format(price);
        System.out.println("Submit price: " + priceString);

        JSONObject body = new JSONObject();
        body.put("price", priceString);
        body.put("displayMarketFactor", displayMarketFactor);
        body.put("marketFactor", marketFactor);

        String response = api.post("main/api/v2/hashpower/order/" + id + "/updatePriceAndLimit", body.toString(),  getTime(), true);
        System.out.println(response);
    }

    public static NicehashOrder getOrder(String id) throws JSONException {
        String response = api.get("main/api/v2/hashpower/order/" + id, true, getTime());
        JSONObject json = new JSONObject(response);
        double price = json.getDouble("price");
        double speed = json.getDouble("acceptedCurrentSpeed");

        return new NicehashOrder(price, speed);
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
}
