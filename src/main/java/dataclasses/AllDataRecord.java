package dataclasses;

import java.time.LocalDateTime;

import org.jooq.Record;
import static test.generated.Tables.*;

public class AllDataRecord {
    LocalDateTime timestamp;
    String algoName;
    String marketName;
    double fulfillSpeed;
    double totalSpeed;
    int fulfillPrice;
    String coinName;
    int coinRevenue;
    double exchangeRate;
    double nethash;

    public AllDataRecord(Record record) {
        timestamp = record.get(ALGO_DATA.TIMESTAMP);
        algoName = record.get(ALGO_DATA.ALGO_NAME);
        marketName = record.get(MARKET_DATA.MARKET_NAME);
        fulfillSpeed = record.get(MARKET_PRICE_DATA.FULFILL_SPEED);
        totalSpeed = record.get(MARKET_DATA.TOTAL_SPEED);
        fulfillPrice = record.get(MARKET_PRICE_DATA.FULFILL_PRICE);
        coinName = record.get(COIN_DATA.COIN_NAME);
        coinRevenue = record.get(COIN_DATA.COIN_REVENUE);
        nethash = record.get(COIN_DATA.NETHASH);
    }

    public AllDataRecord(LocalDateTime timestamp, String algoName, String marketName, double fulfillSpeed, double totalSpeed, int fulfillPrice, String coinName, int coinRevenue, double exchangeRate, double nethash) {
        this.timestamp = timestamp;
        this.algoName = algoName;
        this.marketName = marketName;
        this.fulfillSpeed = fulfillSpeed;
        this.totalSpeed = totalSpeed;
        this.fulfillPrice = fulfillPrice;
        this.coinName = coinName;
        this.coinRevenue = coinRevenue;
        this.exchangeRate = exchangeRate;
        this.nethash = nethash;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

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

    public double getNethash() {
        return nethash;
    }

    public void setNethash(double nethash) {
        this.nethash = nethash;
    }

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
