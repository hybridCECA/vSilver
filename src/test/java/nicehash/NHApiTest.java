package nicehash;

import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashOrder;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.Config;
import utils.SingletonFactory;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NHApiTest {
    private static final String ALGO_NAME = "SHA256";
    private static final String MARKET = "EU";
    private static final int EXPECTED_DOWNSTEP = -1;
    private static final double EXPECTED_MIN_LIMIT = 0.05;
    private static NHApi nhApi;

    @BeforeClass
    public static void loadConfig() {
        Config.setDatabaseConfig(System.getenv("database_username"), System.getenv("database_password"), System.getenv("database_url"));
        nhApi = SingletonFactory.getInstance(NHApi.class);
    }

    @Test
    public void testOrder() throws JSONException {
        List<NicehashOrder> orderbook = nhApi.getOrderbook(ALGO_NAME, MARKET);
        assertTrue(orderbook.size() > 1);

        NicehashOrder middleOrder = orderbook.get(orderbook.size() / 2);
        NicehashOrder order = nhApi.getOrder(middleOrder.getId(), ALGO_NAME, MARKET);
        assertEquals(middleOrder.getPrice(), order.getPrice());
        assertEquals(middleOrder.getSpeed(), order.getSpeed(), 0);
        assertEquals(middleOrder.getId(), order.getId());
        assertEquals(middleOrder.getLimit(), order.getLimit(), 0);
    }

    @Test
    public void testAlgos() throws JSONException {
        List<NicehashAlgorithm> algoList = nhApi.getAlgoList();
        assertTrue(algoList.size() > 1);

        int downStep = nhApi.getAlgoBuyInfo(ALGO_NAME).getDownStep();
        assertEquals(downStep, EXPECTED_DOWNSTEP);

        double minLimit = nhApi.getAlgoBuyInfo(ALGO_NAME).getMinLimit();
        assertEquals(minLimit, EXPECTED_MIN_LIMIT, 0);
    }

    @Test
    public void testAvailableBTC() throws JSONException {
        double availableBTC = nhApi.getAvailableBTC();
        assertTrue(availableBTC > 0);
    }

    @Test
    public void testCompletionRatio() throws JSONException {
        Map<String, Double> remainingAmounts = nhApi.getOrderRemainingAmounts();
        for (Map.Entry<String, Double> entry : remainingAmounts.entrySet()) {
            double remainingAmount = entry.getValue();
            assertTrue(remainingAmount > 0);
        }
    }
}