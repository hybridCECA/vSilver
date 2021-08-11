package services;

import database.Connection;
import nicehash.Api;
import nicehash.MaxProfit;
import nicehash.OrderBot;
import org.json.JSONException;
import utils.Config;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdjustBot {
    private static int counter = 0;
    private static final Set<OrderBot> orderBots = new HashSet<>();

    public static void start() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        String adjustToRefreshRatioString = Config.getConfigValue("adjust_bot_adjust_to_refresh_ratio");
        int adjustToRefreshRatio = Integer.parseInt(adjustToRefreshRatioString);

        Runnable run = () -> {
            try {
                if (counter == 0) {
                    synchronize();
                }

                adjust();

                counter = (counter + 1) % adjustToRefreshRatio;
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        String periodString = Config.getConfigValue("adjust_bot_period_seconds");
        int period = Integer.parseInt(periodString);

        service.scheduleAtFixedRate(run, 0, period, TimeUnit.SECONDS);
    }

    private static void synchronize() throws JSONException {
        // Refresh
        System.out.println("Refreshing order bots");
        Set<OrderBot> newActiveOrders = Api.getActiveOrders();

        // order_id -> limit
        Map<String, Double> orderLimits = Connection.getOrderLimits();

        // Add
        for (OrderBot newOrder : newActiveOrders) {
            if (!orderBots.contains(newOrder)) {
                // If we've already recorded this id, use that
                // Otherwise record the id
                String orderId = newOrder.getOrderId();
                if (orderLimits.containsKey(orderId)) {
                    double limit = orderLimits.get(orderId);
                    newOrder.setLimit(limit);
                } else {
                    Connection.putOrderLimit(orderId, newOrder.getLimit());
                }

                orderBots.add(newOrder);
                MaxProfit.register(newOrder.getTriplePair());
            }
        }

        // Delete
        List<OrderBot> toDelete = new ArrayList<>();
        for (OrderBot bot: orderBots) {
            if (!newActiveOrders.contains(bot)) {
                Connection.deleteOrderLimit(bot.getOrderId());
                toDelete.add(bot);
                MaxProfit.unregister(bot.getTriplePair());
            }
        }
        orderBots.removeAll(toDelete);

        // Clean database stale entries
        for (String id : orderLimits.keySet()) {
            // Matches on id only
            OrderBot searchBot = new OrderBot(id, 0, "", "", "");
            if (!orderBots.contains(searchBot)) {
                Connection.deleteOrderLimit(id);
            }
        }

        System.out.println("Done refreshing order bots");
    }

    private static void adjust() {
        // Adjust
        for (OrderBot bot : orderBots) {
            System.out.println("Order id " + bot.getOrderId() + ":");
            bot.run();
            System.out.println();
        }
    }
}
