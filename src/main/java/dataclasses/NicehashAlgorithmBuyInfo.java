package dataclasses;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NicehashAlgorithmBuyInfo {
    private String name;
    private int downStep;
    private double minLimit;
    private double minAmount;
    private List<String> markets;
    private String speedText;
    private int minPrice;

    public NicehashAlgorithmBuyInfo(String name, int downStep, double minLimit, double minAmount, JSONArray markets, String speedText, int minPrice) throws JSONException {
        this.name = name;
        this.downStep = downStep;
        this.minLimit = minLimit;
        this.minAmount = minAmount;
        this.speedText = speedText;
        this.minPrice = minPrice;

        this.markets = new ArrayList<>();
        for (int i = 0; i < markets.length(); i++) {
            String market = markets.getString(i);
            this.markets.add(market);
        }
    }

    public String getName() {
        return name;
    }

    public int getDownStep() {
        return downStep;
    }

    public double getMinLimit() {
        return minLimit;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public List<String> getMarkets() {
        return markets;
    }

    public String getSpeedText() {
        return speedText;
    }

    public int getMinPrice() {
        return minPrice;
    }
}
