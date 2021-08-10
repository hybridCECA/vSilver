package dataclasses;

import java.util.Objects;

public class TriplePair {
    private String algo;
    private String market;
    private String coin;

    public TriplePair(String algo, String market, String coin) {
        this.algo = algo;
        this.market = market;
        this.coin = coin;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TriplePair)) return false;
        TriplePair that = (TriplePair) o;
        return algo.equals(that.algo) &&
                market.equals(that.market) &&
                coin.equals(that.coin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algo, market, coin);
    }
}
