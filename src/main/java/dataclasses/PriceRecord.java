package dataclasses;

public class PriceRecord implements Comparable<PriceRecord> {
    private final int fulfillPrice;
    private final int count;

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

    @Override
    public int compareTo(PriceRecord o) {
        return Integer.compare(this.fulfillPrice, o.fulfillPrice);
    }
}
