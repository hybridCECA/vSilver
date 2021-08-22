package dataclasses;

import java.util.Objects;

public class CoinMarketPair {
    String coinName;
    String marketName;

    public CoinMarketPair(String coinName, String marketName) {
        this.coinName = coinName;
        this.marketName = marketName;
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
}
