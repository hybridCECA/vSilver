package marketevaluation;

import dataclasses.NicehashOrder;
import nicehash.PriceTools;
import org.json.JSONException;

import java.util.List;

public class MockPriceTools implements PriceTools {
    private int fulfillPrice;

    public void setFulfillPrice(int fulfillPrice) {
        this.fulfillPrice = fulfillPrice;
    }

    @Override
    public int getSweepPrice(List<NicehashOrder> orderbook, double fulfillSpeed, String id) {
        return fulfillPrice;
    }

    @Override
    public double getSpeedAtPrice(List<NicehashOrder> orderbook, int payingPrice, String id) {
        return 0;
    }

    @Override
    public double getTotalSpeed(List<NicehashOrder> orderbook) {
        return 0;
    }

    @Override
    public int getSweepPrice(List<NicehashOrder> orderbook, double fulfillSpeed) {
        return 0;
    }
}
