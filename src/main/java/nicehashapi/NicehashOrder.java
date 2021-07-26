package nicehashapi;

public class NicehashOrder implements Comparable<NicehashOrder> {
    private double price;
    private double speed;

    public NicehashOrder(double price, double speed) {
        this.price = price;
        this.speed = speed;
    }

    public double getPrice() {
        return price;
    }

    public double getSpeed() {
        return speed;
    }


    @Override
    public int compareTo(NicehashOrder o) {
        return Double.compare(this.price, o.price);
    }
}
