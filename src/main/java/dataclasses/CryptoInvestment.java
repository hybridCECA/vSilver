package dataclasses;

import nicehash.OrderBot;

import java.util.Objects;

public class CryptoInvestment {
    private final String algo;
    private final String market;
    private final String coin;
    private final double fulfillSpeed;

    public CryptoInvestment(AllDataRecord record) {
        this.algo = record.getAlgoName();
        this.market = record.getMarketName();
        this.coin = record.getCoinName();
        this.fulfillSpeed = record.getFulfillSpeed();
    }

    public CryptoInvestment(OrderBot bot) {
        this.algo = bot.getAlgoName();
        this.market = bot.getMarketName();
        this.coin = bot.getCoinName();
        this.fulfillSpeed = bot.getLimit();
    }

    public CryptoInvestment(String algo, String market, String coin, double fulfillSpeed) {
        this.algo = algo;
        this.market = market;
        this.coin = coin;
        this.fulfillSpeed = fulfillSpeed;
    }

    public String getAlgo() {
        return algo;
    }

    public String getMarket() {
        return market;
    }

    public String getCoin() {
        return coin;
    }

    public double getFulfillSpeed() {
        return fulfillSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CryptoInvestment)) return false;
        CryptoInvestment that = (CryptoInvestment) o;
        return Double.compare(that.fulfillSpeed, fulfillSpeed) == 0 && Objects.equals(algo, that.algo) && Objects.equals(market, that.market) && Objects.equals(coin, that.coin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algo, market, coin, fulfillSpeed);
    }

    @Override
    public String toString() {
        return "CryptoInvestment{" +
                "algo='" + algo + '\'' +
                ", market='" + market + '\'' +
                ", coin='" + coin + '\'' +
                ", fulfillSpeed=" + fulfillSpeed +
                '}';
    }
}
