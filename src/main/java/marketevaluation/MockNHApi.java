package marketevaluation;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import nicehash.NHApi;
import nicehash.OrderBot;
import org.json.JSONException;
import utils.SingletonFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockNHApi implements NHApi {
    private int currentPrice;
    private int submitPrice;
    private double limit;
    private final NHApi nhApi;

    public MockNHApi(double limit) {
        this.limit = limit;
        nhApi = SingletonFactory.getInstance(NHApi.class);
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getSubmitPrice() {
        return submitPrice;
    }

    @Override
    public void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) {
        submitPrice = price;
    }

    @Override
    public NicehashOrder getOrder(String id, String algoName, String market) {
        return new NicehashOrder(currentPrice, 0, MarketEvaluation.ORDER_ID, limit);
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
        return null;
    }

    @Override
    public List<NicehashAlgorithm> getAlgoList() {
        return null;
    }

    @Override
    public List<NicehashAlgorithmBuyInfo> getBuyInfo() throws JSONException {
        return nhApi.getBuyInfo();
    }

    @Override
    public NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) throws JSONException {
        return nhApi.getAlgoBuyInfo(algoName);
    }

    @Override
    public void invalidateBuyInfoCache() {

    }

    @Override
    public String getLightningAddress(double amount) {
        return null;
    }

    @Override
    public Map<String, Double> getOrderCompletionRatios() throws JSONException {
        return null;
    }

    @Override
    public double getAvailableBTC() throws JSONException {
        return 0;
    }
}
