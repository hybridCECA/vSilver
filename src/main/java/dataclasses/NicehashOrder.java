package dataclasses;

public class NicehashOrder implements Comparable<NicehashOrder> {
    private final int price;
    private final String id;
    private final double limit;
    private double speed;

    public NicehashOrder(NicehashOrder otherOrder) {
        this.price = otherOrder.price;
        this.speed = otherOrder.speed;
        this.id = otherOrder.id;
        this.limit = otherOrder.limit;
    }

    public NicehashOrder(int price, double speed, String id, double limit) {
        this.price = price;
        this.speed = speed;
        this.id = id;
        this.limit = limit;
    }

    public int getPrice() {
        return price;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getId() {
        return id;
    }

    public double getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "NicehashOrder{" +
                "price=" + price +
                ", speed=" + speed +
                ", id='" + id + '\'' +
                ", limit=" + limit +
                '}';
    }

    @Override
    public int compareTo(NicehashOrder o) {
        return Double.compare(this.price, o.price);
    }
}
