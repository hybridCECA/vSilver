package marketevaluation;

import nicehash.OrderBot;
import services.MaxProfit;

public class MockMaxProfit implements MaxProfit {
    int maxProfit;

    public void setMaxProfit(int maxProfit) {
        this.maxProfit = maxProfit;
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
        return maxProfit;
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
