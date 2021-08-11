import nicehash.Api;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import services.ProfitabilityCollector;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

class ProfitabilityCollectorTest {

    @Test
    void testProfitabilityCollector() {
        Api.loadConfig();

        try {
            ProfitabilityCollector.start();
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail();
        }
    }
}