package whattomine;

import dataclasses.WhatToMineCoin;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoinsTest {

    @Test
    void testGetCoinList() throws IOException, JSONException {
        List<WhatToMineCoin> list = Coins.getCoinList();

        assertTrue(list.size() > 0);

        for (WhatToMineCoin coin : list) {
            assertNotNull(coin.getName());
            assertNotNull(coin.getAlgorithm());

            assertTrue(coin.getExchangeRate() > 0);
            assertTrue(coin.getUnitProfitability() > 0);
        }

        WhatToMineCoin centerCoin = list.get(list.size() / 2);

        WhatToMineCoin coinCopy = Coins.getCoin(centerCoin.getName());
        assertEquals(coinCopy.getName(), centerCoin.getName());
        assertEquals(coinCopy.getAlgorithm(), centerCoin.getAlgorithm());
    }
}