package nicehash;

import dataclasses.NicehashOrder;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Price {
    public static void main(String[] args) throws IOException, JSONException {
        Api.loadConfig();
        System.out.println(getSweepPrice(0.01, "cryptonightr", "EU", "228f50b7-9b91-4c2b-b10e-67991347c536"));
        System.out.println(getSpeedAtPrice(Api.getOrderbook("cryptonightr", "EU"), 57, "228f50b7-9b91-4c2b-b10e-67991347c536"));
    }

    public static int getSweepPrice(double fulfillSpeed, String algoName, String market, String id) throws JSONException {
        int step = 1;

        List<NicehashOrder> orderbook = Api.getOrderbook(algoName, market);
        int maxPrice = orderbook.get(0).getPrice() + step;

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

    public static double getSpeedAtPrice(List<NicehashOrder> orderbook, int payingPrice, String id) {
        NicehashOrder currentOrder = null;
        for (NicehashOrder order : orderbook) {
            if (order.getId().equals(id)) {
                currentOrder = order;
            }
        }

        if (currentOrder == null) {
            throw new RuntimeException("Order " + id + " not found!");
        }

        double speed = 0;
        if (payingPrice > currentOrder.getPrice()) {
            // If price increase
            // Sum all speed less than current order
            for (NicehashOrder order : orderbook) {
                if (order.getPrice() < payingPrice) {
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

            // Add order at bottom to absorb any speed
            orderbook.add(new NicehashOrder(0, 0, "fake_order_id", Double.MAX_VALUE));

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
