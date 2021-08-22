package MarketEvaluation;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import nicehash.NHApi;
import nicehash.NHApiFactory;
import nicehash.OrderBot;
import org.json.JSONException;

import java.util.List;
import java.util.Set;

public class MockNHApi implements NHApi {
    private int fulfillPrice;
    private int currentPrice;
    private int submitPrice;
    private NHApi nhApi;

    public void setFulfillPrice(int fulfillPrice) {
        this.fulfillPrice = fulfillPrice;
        nhApi = NHApiFactory.getInstance();
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public int getSubmitPrice() {
        return submitPrice;
    }

    @Override
    public void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) throws JSONException {
        submitPrice = price;
    }

    @Override
    public NicehashOrder getOrder(String id, String algoName, String market) throws JSONException {
        return new NicehashOrder(currentPrice, 0, MarketEvaluationWorkshop.ORDER_ID, 0);
    }

    @Override
    public List<NicehashOrder> getOrderbook(String algoName, String market) throws JSONException {
        return List.of(
            new NicehashOrder(fulfillPrice - 1, Double.MAX_VALUE, "other_order_id", Double.MAX_VALUE),
            new NicehashOrder(1, 0, MarketEvaluationWorkshop.ORDER_ID, 0)
        );
    }

    @Override
    public void invalidateOrderbookCache(String algoName) {

    }

    @Override
    public void invalidateOrderbookCache() {

    }

    @Override
    public Set<OrderBot> getActiveOrders() throws JSONException {
        return null;
    }

    @Override
    public List<NicehashAlgorithm> getAlgoList() throws JSONException {
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
    public String getLightningAddress(double amount) throws JSONException {
        return null;
    }
}
