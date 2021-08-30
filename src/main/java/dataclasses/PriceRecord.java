package dataclasses;

import java.util.Objects;

public class PriceRecord implements Comparable<PriceRecord> {
    private final int fulfillPrice;
    private int count;

    public PriceRecord(int fulfillPrice, int count) {
        this.fulfillPrice = fulfillPrice;
        this.count = count;
    }

    public int getFulfillPrice() {
        return fulfillPrice;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceRecord)) return false;
        PriceRecord that = (PriceRecord) o;
        return fulfillPrice == that.fulfillPrice;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fulfillPrice);
    }

    @Override
    public int compareTo(PriceRecord o) {
        return Integer.compare(this.fulfillPrice, o.fulfillPrice);
    }
}
