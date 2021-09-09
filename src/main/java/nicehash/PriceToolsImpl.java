package nicehash;

import dataclasses.NicehashOrder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PriceToolsImpl implements PriceTools {
    public int getSweepPrice(List<NicehashOrder> orderbook, double fulfillSpeed, String id) {
        int step = 1;

        int maxPrice = orderbook.get(0).getPrice() + step;

        for (int price = 0; price <= maxPrice; price += step) {
            List<NicehashOrder> orderbookCopy = copyOrderbook(orderbook);

            double speed = getSpeedAtPrice(orderbookCopy, price, id);

            if (speed >= fulfillSpeed) {
                return price;
            }
        }

        return maxPrice;
    }

    public double getSpeedAtPrice(List<NicehashOrder> orderbook, int payingPrice, String id) {
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

    public double getTotalSpeed(List<NicehashOrder> orderbook) {
        return orderbook.stream().mapToDouble(NicehashOrder::getSpeed).sum();
    }

    // Overloads without current order id
    public int getSweepPrice(List<NicehashOrder> orderbook, double fulfillSpeed) {
        orderbook = copyOrderbook(orderbook);

        if (orderbook.size() == 0) {
            return 1;
        }

        int step = 1;
        int maxPrice = orderbook.get(0).getPrice() + step;

        Collections.reverse(orderbook);
        double speed = 0;
        for (NicehashOrder order : orderbook) {
            speed += order.getSpeed();
            if (speed >= fulfillSpeed) {
                return order.getPrice() + step;
            }
        }

        return maxPrice;
    }

    private List<NicehashOrder> copyOrderbook(List<NicehashOrder> orderbook) {
        return orderbook.stream().map(NicehashOrder::new).collect(Collectors.toList());
    }
}
