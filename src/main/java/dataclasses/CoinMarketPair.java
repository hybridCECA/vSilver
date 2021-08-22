package dataclasses;

import java.util.Objects;

public class CoinMarketPair {
    String coinName;
    String marketName;

    public CoinMarketPair(String coinName, String marketName) {
        this.coinName = coinName;
        this.marketName = marketName;
    }

    public String getCoinName() {
        return coinName;
    }

    public String getMarketName() {
        return marketName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoinMarketPair)) return false;
        CoinMarketPair that = (CoinMarketPair) o;
        return Objects.equals(coinName, that.coinName) &&
                Objects.equals(marketName, that.marketName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coinName, marketName);
    }

    @Override
    public String toString() {
        return "CoinMarketPair{" +
                "coinName='" + coinName + '\'' +
                ", marketName='" + marketName + '\'' +
                '}';
    }
}
