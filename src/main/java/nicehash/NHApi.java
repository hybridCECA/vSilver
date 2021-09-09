package nicehash;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NHApi {
    void updateOrder(String id, int price, String displayMarketFactor, double marketFactor, double limit) throws JSONException;

    NicehashOrder getOrder(String id, String algoName, String market) throws JSONException;

    List<NicehashOrder> getOrderbook(String algoName, String market) throws JSONException;

    void invalidateOrderbookCache(String algoName);

    void invalidateOrderbookCache();

    Set<OrderBot> getActiveOrders() throws JSONException;

    List<NicehashAlgorithm> getAlgoList() throws JSONException;

    List<NicehashAlgorithmBuyInfo> getBuyInfo() throws JSONException;

    NicehashAlgorithmBuyInfo getAlgoBuyInfo(String algoName) throws JSONException;

    void invalidateBuyInfoCache();

    String getLightningAddress(double amount) throws JSONException;

    Map<String, Double> getOrderRemainingAmounts() throws JSONException;

    double getAvailableBTC() throws JSONException;
}
