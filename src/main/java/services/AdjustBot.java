package services;

import nicehash.OrderBot;
import utils.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AdjustBot implements vService {
    private static final Set<OrderBot> orderBots = ConcurrentHashMap.newKeySet();
    private final static VLogger LOGGER = Logging.getLogger(AdjustBot.class);

    private static void adjust() {
        // Adjust
        for (OrderBot bot : orderBots) {
            LOGGER.info("Order id " + bot.getOrderId() + ":");
            bot.run();
        }
    }

    public static Set<OrderBot> getOrderBots() {
        return orderBots;
    }

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.ADJUST_BOT_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            adjust();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}
