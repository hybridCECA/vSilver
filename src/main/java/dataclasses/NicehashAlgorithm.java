package dataclasses;

public class NicehashAlgorithm {
    private String algorithm;
    private double profitability;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public double getProfitability() {
        return profitability;
    }

    public void setProfitability(double profitability) {
        this.profitability = profitability;
    }

    @Override
    public String toString() {
        return "NicehashAlgorithm{" +
                "algorithm='" + algorithm + '\'' +
                ", profitability=" + profitability +
                '}';
    }
}
