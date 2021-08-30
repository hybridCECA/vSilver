package services;

import coinsources.CoinSources;
import database.Connection;
import nicehash.NHApi;
import nicehash.OrderBot;
import org.json.JSONException;
import utils.*;

import java.util.*;

public class BotSynchronizer implements vService {
    private final static VLogger LOGGER = Logging.getLogger(BotSynchronizer.class);
    private static final MaxProfit maxProfit = SingletonFactory.getInstance(MaxProfit.class);

    private static void synchronize() throws JSONException {
        // Refresh
        LOGGER.info("Refreshing order bots");
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);
        Set<OrderBot> newActiveOrders = nhApi.getActiveOrders();

        // Filter out invalid coins
        CoinSources coinSources = SingletonFactory.getInstance(CoinSources.class);
        Iterator<OrderBot> iterator = newActiveOrders.iterator();
        while (iterator.hasNext()) {
            String coin = iterator.next().getCoinName();
            try {
                coinSources.getCoin(coin);
            } catch (Exception e) {
                iterator.remove();
            }
        }

        // order_id -> limit
        Map<String, Double> orderLimits = Connection.getOrderLimits();

        Set<OrderBot> orderBots = AdjustBot.getOrderBots();

        // Add
        for (OrderBot newOrder : newActiveOrders) {
            if (!orderBots.contains(newOrder)) {
                // If we've already recorded this id, use that
                // Otherwise record the limit
                String orderId = newOrder.getOrderId();
                if (orderLimits.containsKey(orderId)) {
                    double limit = orderLimits.get(orderId);
                    newOrder.setLimit(limit);
                } else {
                    Connection.putOrderLimit(orderId, newOrder.getLimit());
                }

                maxProfit.register(newOrder);
                orderBots.add(newOrder);
            }
        }

        // Delete
        List<OrderBot> toDelete = new ArrayList<>();
        for (OrderBot bot : orderBots) {
            if (!newActiveOrders.contains(bot)) {
                Connection.deleteOrderLimit(bot.getOrderId());
                toDelete.add(bot);
                maxProfit.unregister(bot);
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

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.BOT_SYNCHRONIZER_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            synchronize();
        } catch (Exception e) {
            LOGGER.error(Conversions.exceptionToString(e));
        }
    }
}
