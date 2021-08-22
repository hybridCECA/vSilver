package dataclasses;

import java.time.LocalDateTime;

public class AllDataRecord {
    LocalDateTime timestamp;
    String algoName;
    String marketName;
    double fulfillSpeed;
    double totalSpeed;
    int fulfillPrice;

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    String coinName;
    int coinRevenue;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAlgoName() {
        return algoName;
    }

    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public double getFulfillSpeed() {
        return fulfillSpeed;
    }

    public void setFulfillSpeed(double fulfillSpeed) {
        this.fulfillSpeed = fulfillSpeed;
    }

    public double getTotalSpeed() {
        return totalSpeed;
    }

    public void setTotalSpeed(double totalSpeed) {
        this.totalSpeed = totalSpeed;
    }

    public int getFulfillPrice() {
        return fulfillPrice;
    }

    public void setFulfillPrice(int fulfillPrice) {
        this.fulfillPrice = fulfillPrice;
    }

    public int getCoinRevenue() {
        return coinRevenue;
    }

    public void setCoinRevenue(int coinRevenue) {
        this.coinRevenue = coinRevenue;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    double exchangeRate;

    @Override
    public String toString() {
        return "AllDataRecord{" +
                "timestamp=" + timestamp +
                ", algoName='" + algoName + '\'' +
                ", marketName='" + marketName + '\'' +
                ", fulfillSpeed=" + fulfillSpeed +
                ", totalSpeed=" + totalSpeed +
                ", fulfillPrice=" + fulfillPrice +
                ", coinName='" + coinName + '\'' +
                ", coinRevenue=" + coinRevenue +
                ", exchangeRate=" + exchangeRate +
                '}';
    }
}
