package services.BotSynchronizerTest;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import nicehash.NHApi;
import nicehash.OrderBot;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockNHApi implements NHApi {
    private Set<OrderBot> orderBots;

    @Override
    public void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) {

    }

    @Override
    public NicehashOrder getOrder(String id, String algoName, String market) {
        return null;
    }

    @Override
    public List<NicehashOrder> getOrderbook(String algoName, String market) {
        return null;
    }

    @Override
    public void invalidateOrderbookCache(String algoName) {

    }

    @Override
    public void invalidateOrderbookCache() {

    }

    @Override
    public Set<OrderBot> getActiveOrders() {
        return orderBots;
    }

    public void setActiveOrders(Set<OrderBot> orderBots) {
        this.orderBots = orderBots;
    }

    @Override
    public List<NicehashAlgorithm> getAlgoList() {
        return null;
    }

    @Override
    public List<NicehashAlgorithmBuyInfo> getBuyInfo() {
        return null;
    }

    @Override
    public NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) {
        return null;
    }

    @Override
    public void invalidateBuyInfoCache() {

    }

    @Override
    public String getLightningAddress(double amount) {
        return null;
    }

    @Override
    public Map<String, Double> getOrderRemainingAmounts() {
        return null;
    }

    @Override
    public double getAvailableBTC() {
        return 0;
    }
}
