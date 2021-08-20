package dataclasses;

import java.util.Objects;

public class TriplePair implements Comparable<TriplePair> {
    private final String algo;
    private final String market;
    private final String coin;

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

    @Override
    public int compareTo(TriplePair o) {
        return this.coin.compareTo(o.coin);
    }
}
