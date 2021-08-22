package dataclasses;

import java.util.Objects;

public class AlgoMarketPair {
    String algoName;
    String marketName;

    public AlgoMarketPair(String algoName, String marketName) {
        this.algoName = algoName;
        this.marketName = marketName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlgoMarketPair)) return false;
        AlgoMarketPair that = (AlgoMarketPair) o;
        return Objects.equals(algoName, that.algoName) &&
                Objects.equals(marketName, that.marketName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algoName, marketName);
    }
}
