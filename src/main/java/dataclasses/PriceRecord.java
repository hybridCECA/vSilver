package dataclasses;

public class PriceRecord {
    private int fulfillPrice;
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
}
