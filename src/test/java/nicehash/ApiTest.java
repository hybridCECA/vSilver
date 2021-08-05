package nicehash;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashOrder;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiTest {
    private static final String ALGO_NAME = "SHA256";
    private static final String MARKET = "EU";

    @Test
    void testOrder() throws IOException, JSONException {
        Api.loadConfig();
        List<NicehashOrder> orderbook = Api.getOrderbook(ALGO_NAME, MARKET);
        assertTrue(orderbook.size() > 1);

        NicehashOrder middleOrder = orderbook.get(orderbook.size() / 2);
        NicehashOrder order = Api.getOrder(middleOrder.getId(), ALGO_NAME, MARKET);
        assertEquals(middleOrder.getPrice(), order.getPrice());
        assertEquals(middleOrder.getSpeed(), order.getSpeed());
        assertEquals(middleOrder.getId(), order.getId());
        assertEquals(middleOrder.getLimit(), order.getLimit());
    }

    private static final int EXPECTED_DOWNSTEP = -1;
    private static final double EXPECTED_MIN_LIMIT = 0.05;

    @Test
    void testAlgos() throws JSONException, IOException {
        Api.loadConfig();

        List<NicehashAlgorithm> algoList = Api.getAlgoList();
        assertTrue(algoList.size() > 1);

        int downStep = Api.getDownStep(ALGO_NAME);
        assertEquals(downStep, EXPECTED_DOWNSTEP);

        double minLimit = Api.getMinLimit(ALGO_NAME);
        assertEquals(minLimit, EXPECTED_MIN_LIMIT);
    }
}