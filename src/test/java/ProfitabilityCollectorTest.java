import nicehash.Api;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ProfitabilityCollectorTest {

    @Test
    void testProfitabilityCollector() throws IOException, JSONException {
        Api.loadConfig();

        try {
            ProfitabilityCollector.start();
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail();
        }
    }
}