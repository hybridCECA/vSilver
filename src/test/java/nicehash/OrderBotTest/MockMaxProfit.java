package nicehash.OrderBotTest;

import nicehash.OrderBot;
import services.MaxProfit;

public class MockMaxProfit implements MaxProfit {
    private int maxProfitBound = 0;

    public void setMaxProfitBound(int maxProfitBound) {
        this.maxProfitBound = maxProfitBound;
    }

    @Override
    public void updateMaxProfits() {

    }

    @Override
    public void register(OrderBot bot) {

    }

    @Override
    public void unregister(OrderBot bot) {

    }

    @Override
    public int getMaxProfit(OrderBot bot) {
        return maxProfitBound;
    }

    @Override
    public boolean hasMaxProfit(OrderBot bot) {
        return true;
    }

    @Override
    public int getRunPeriodSeconds() {
        return 0;
    }

    @Override
    public void run() {

    }
}
