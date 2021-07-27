package nicehashapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.AlgoInfo;
import utils.JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Price {
    public static void main(String[] args) throws IOException, JSONException {
        System.out.println(getFulfillPrice(0.1, "cryptonightr", "USA"));
    }

    /*
    DEPRECATED
    private static double getPercentilePrice(double percentile) throws IOException, JSONException {
        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/hashpower/orderBook?algorithm=CRYPTONIGHTR");
        JSONArray orders = json.getJSONObject("stats").getJSONObject("EU").getJSONArray("orders");

        List<NicehashOrder> list = new ArrayList<>();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            double price = order.getDouble("price");
            double speed = order.getDouble("acceptedSpeed");
            NicehashOrder nhOrder = new NicehashOrder(price, speed);
            list.add(nhOrder);
        }

        Collections.sort(list);

        double totalSpeed = list.stream().mapToDouble(NicehashOrder::getSpeed).sum();

        double percentileTraversed = 0;
        double price = list.get(0).getPrice();
        for (NicehashOrder order : list) {
            percentileTraversed += order.getSpeed() / totalSpeed * 100;
            price = order.getPrice();

            if (percentileTraversed > percentile) {
                break;
            }
        }

        return price;
    }

     */

    public static double getFulfillPrice(double fulfillPrice, String algoName, String market) throws IOException, JSONException {
        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/hashpower/orderBook?algorithm=" + algoName.toUpperCase());
        JSONArray orders = json.getJSONObject("stats").getJSONObject(market).getJSONArray("orders");
        System.out.println(json);

        List<NicehashOrder> list = new ArrayList<>();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            double price = order.getDouble("price");
            double speed = order.getDouble("acceptedSpeed");
            NicehashOrder nhOrder = new NicehashOrder(price, speed);
            list.add(nhOrder);
        }

        Collections.sort(list);

        double speedTraversed = 0;
        double price = list.get(0).getPrice();
        for (NicehashOrder order : list) {
            speedTraversed += order.getSpeed();
            price = order.getPrice();

            if (speedTraversed >= fulfillPrice) {
                break;
            }
        }

        double downStep = AlgoInfo.getDownStep("CUCKOOCYCLE");
        return price - downStep;
    }
}
