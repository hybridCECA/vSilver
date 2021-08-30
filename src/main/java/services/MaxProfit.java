package services;

import nicehash.OrderBot;

public interface MaxProfit extends vService {
    void updateMaxProfits();

    void register(OrderBot bot);

    void unregister(OrderBot bot);

    int getMaxProfit(OrderBot bot);

    boolean hasMaxProfit(OrderBot bot);
}
