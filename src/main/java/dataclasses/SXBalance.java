package dataclasses;

public class SXBalance {
    final String currency;
    final double available;

    public SXBalance(String currency, double available) {
        this.currency = currency;
        this.available = available;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "SXBalance{" +
                "currency='" + currency + '\'' +
                ", available=" + available +
                '}';
    }
}
