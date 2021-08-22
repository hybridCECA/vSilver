package MarketEvaluation;

import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import services.MaxProfit;

import java.util.List;

public class MockMaxProfit implements MaxProfit {
    int maxProfit;

    public void setMaxProfit(int maxProfit) {
        this.maxProfit = maxProfit;
    }

    @Override
    public void updateMaxProfits() {

    }

    @Override
    public int getMaxProfitPrice(List<PriceRecord> list, int revenue) {
        return 0;
    }

    @Override
    public void register(TriplePair pair) {

    }

    @Override
    public void unregister(TriplePair pair) {

    }

    @Override
    public int getMaxProfit(TriplePair pair) {
        return maxProfit;
    }

    @Override
    public boolean hasMaxProfit(TriplePair pair) {
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
