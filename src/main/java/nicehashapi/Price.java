package nicehashapi;

import org.json.JSONException;
import utils.Conversion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Price {
    public static void main(String[] args) throws IOException, JSONException {
        System.out.println(getSweepPrice(0.01, "cryptonightr", "EU", "228f50b7-9b91-4c2b-b10e-67991347c536"));
        System.out.println(Conversion.intPriceToStringPrice(56));
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


    public static double getFulfillPrice(double fulfillPrice, String algoName, String market) throws IOException, JSONException {
        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/hashpower/orderBook?algorithm=" + algoName.toUpperCase());
        JSONArray orders = json.getJSONObject("stats").getJSONObject(market).getJSONArray("orders");
        System.out.println(json);

        List<NicehashOrder> list = Api.getOrderbook(algoName, market);

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

        double downStep = AlgoInfo.getDownStep(algoName);
        return price - downStep;
    }

     */

    public static int getSweepPrice(double fulfillSpeed, String algoName, String market, String id) throws IOException, JSONException {
        int step = -1 * Api.getDownStep(algoName);

        List<NicehashOrder> orderbook = Api.getOrderbook(algoName, market);
        int maxPrice = orderbook.get(0).getPrice();

        for (int price = 0; price <= maxPrice; price += step) {
            List<NicehashOrder> orderbookCopy = new ArrayList<>();
            for (NicehashOrder order : orderbook) {
                orderbookCopy.add(new NicehashOrder(order));
            }


            double speed = getSpeedAtPrice(orderbookCopy, price, id);

            if (speed >= fulfillSpeed) {
                return price;
            }
        }

        return maxPrice;
    }

    public static double getSpeedAtPrice(List<NicehashOrder> orderbook, int payingPrice, String id) throws IOException, JSONException {
        NicehashOrder currentOrder = null;
        for (NicehashOrder order : orderbook) {
            if (order.getId().equals(id)) {
                currentOrder = order;
            }
        }

        double speed = 0;
        if (payingPrice > currentOrder.getPrice()) {
            // If price increase
            // Sum all speed less than or equal to current order
            for (NicehashOrder order : orderbook) {
                if (order.getPrice() <= payingPrice) {
                    speed += order.getSpeed();
                }
            }
        } else if (payingPrice == currentOrder.getPrice()) {
            // If same price
            // Sum all speed less than or equal to the current order
            Collections.reverse(orderbook);
            for (NicehashOrder order : orderbook) {
                speed += order.getSpeed();

                if (order == currentOrder) {
                    break;
                }
            }
        } else {
            // If price decrease
            double allocateSpeed = currentOrder.getSpeed();
            orderbook.remove(currentOrder);

            // Reallocate current order
            for (NicehashOrder order : orderbook) {
                double space = order.getLimit() - order.getSpeed();

                if (space > 0) {
                    if (allocateSpeed >= space) {
                        // Allocate as much space as possible
                        order.setSpeed(order.getLimit());

                        allocateSpeed -= space;
                    } else {
                        order.setSpeed(order.getSpeed() + allocateSpeed);

                        allocateSpeed = 0;
                    }
                }
            }

            // Sum speed less than price
            for (NicehashOrder order : orderbook) {
                if (order.getPrice() < payingPrice) {
                    speed += order.getSpeed();
                }
            }
        }

        return speed;
    }
}
