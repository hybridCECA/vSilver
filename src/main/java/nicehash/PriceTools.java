package nicehash;

import dataclasses.NicehashOrder;

import java.util.List;

public interface PriceTools {
    int getSweepPrice(List<NicehashOrder> orderbook, double fulfillSpeed, String id);

    double getSpeedAtPrice(List<NicehashOrder> orderbook, int payingPrice, String id);

    double getTotalSpeed(List<NicehashOrder> orderbook);

    int getSweepPrice(List<NicehashOrder> orderbook, double fulfillSpeed);
}
