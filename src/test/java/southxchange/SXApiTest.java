package southxchange;

import dataclasses.SXBalance;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SXApiTest {
    @Test
    public void testSXApi() throws Exception {
        SXApi.loadConfig();
        List<SXBalance> balances = SXApi.listBalances();
        for (SXBalance balance : balances) {
            String currency = balance.getCurrency();
            assertEquals(currency, currency.toUpperCase());

            double amount = balance.getAvailable();
            assertTrue(amount > 0);
        }
    }
}