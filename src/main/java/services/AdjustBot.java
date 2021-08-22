package services;

import database.Connection;
import nicehash.NHApi;
import nicehash.NHApiFactory;
import nicehash.OrderBot;
import org.json.JSONException;
import utils.Config;
import utils.Consts;
import utils.Conversions;
import utils.Logging;

import java.util.*;
import java.util.logging.Logger;

public class AdjustBot implements vService {
    private static int counter = 0;
    private static final Set<OrderBot> orderBots = new HashSet<>();
    public final static Logger LOGGER = Logging.getLogger(AdjustBot.class);
    private static final MaxProfit maxProfit = MaxProfitFactory.getInstance();

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.ADJUST_BOT_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        int adjustToRefreshRatio = Config.getConfigInt(Consts.ADJUST_BOT_ADJUST_TO_REFRESH_RATIO);

        try {
            if (counter == 0) {
                synchronize();
            }

            adjust();

            counter = (counter + 1) % adjustToRefreshRatio;
        } catch (Exception e) {
            LOGGER.severe(Conversions.exceptionToString(e));
        }
    }

    private static void synchronize() throws JSONException {
        // Refresh
        LOGGER.info("Refreshing order bots");
        NHApi nhApi = NHApiFactory.getInstance();
        Set<OrderBot> newActiveOrders = nhApi.getActiveOrders();

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

                maxProfit.register(newOrder.getTriplePair());
            }
        }

        // Delete
        List<OrderBot> toDelete = new ArrayList<>();
        for (OrderBot bot: orderBots) {
            if (!newActiveOrders.contains(bot)) {
                Connection.deleteOrderLimit(bot.getOrderId());
                toDelete.add(bot);
                maxProfit.unregister(bot.getTriplePair());
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

        LOGGER.info("Done refreshing order bots");
    }

    private static void adjust() {
        // Adjust
        for (OrderBot bot : orderBots) {
            LOGGER.info("Order id " + bot.getOrderId() + ":");
            bot.run();
        }
    }
}
