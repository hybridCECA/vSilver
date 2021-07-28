package nicehashapi;

public class NicehashOrder implements Comparable<NicehashOrder> {
    private int price;
    private double speed;
    private String id;
    private double limit;

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
    public int compareTo(NicehashOrder o) {
        return Double.compare(this.price, o.price);
    }
}
