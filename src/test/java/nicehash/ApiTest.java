package nicehash;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashOrder;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.Config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiTest {
    private static final String ALGO_NAME = "SHA256";
    private static final String MARKET = "EU";

    @BeforeAll
    static void loadConfig() {
        Config.setDatabaseConfig(System.getenv("database_username"), System.getenv("database_password"), System.getenv("database_url"));
    }

    @Test
    void testOrder() throws JSONException {
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
    void testAlgos() throws JSONException {
        Api.loadConfig();

        List<NicehashAlgorithm> algoList = Api.getAlgoList();
        assertTrue(algoList.size() > 1);

        int downStep = Api.getAlgoBuyInfo(ALGO_NAME).getDownStep();
        assertEquals(downStep, EXPECTED_DOWNSTEP);

        double minLimit = Api.getAlgoBuyInfo(ALGO_NAME).getMinLimit();
        assertEquals(minLimit, EXPECTED_MIN_LIMIT);
    }
}