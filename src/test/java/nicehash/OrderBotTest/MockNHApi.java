package nicehash.OrderBotTest;

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
    private List<NicehashOrder> orderbook = null;
    private int lastPrice = -1;
    private List<NicehashAlgorithmBuyInfo> buyInfoList = null;

    public void setOrderbook(List<NicehashOrder> orderbook) {
        this.orderbook = orderbook;
    }

    public void setBuyInfoList(List<NicehashAlgorithmBuyInfo> buyInfoList) {
        this.buyInfoList = buyInfoList;
    }

    public int getLastPrice() {
        return lastPrice;
    }

    @Override
    public void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) {
        lastPrice = price;
    }

    @Override
    public NicehashOrder getOrder(String id, String algoName, String market) {
        for (NicehashOrder order : orderbook) {
            if (order.getId().equals(id)) {
                return order;
            }
        }

        return null;
    }

    @Override
    public List<NicehashOrder> getOrderbook(String algoName, String market) {
        return orderbook;
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
    public List<NicehashAlgorithmBuyInfo> getBuyInfo() {
        return buyInfoList;
    }

    @Override
    public NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) {
        return buyInfoList.get(0);
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
