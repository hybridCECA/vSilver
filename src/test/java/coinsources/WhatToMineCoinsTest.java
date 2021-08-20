package coinsources;

import dataclasses.Coin;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhatToMineCoinsTest {

    @Test
    void testGetCoinList() throws IOException, JSONException {
        List<Coin> list = WhatToMineCoins.getCoinList();

        assertTrue(list.size() > 0);

        for (Coin coin : list) {
            assertNotNull(coin.getName());
            assertNotNull(coin.getAlgorithm());

            assertTrue(coin.getExchangeRate() > 0);
            assertTrue(coin.getUnitProfitability() > 0);
        }

        Coin centerCoin = list.get(list.size() / 2);

        Coin coinCopy = WhatToMineCoins.getCoin(centerCoin.getName());
        assertEquals(coinCopy.getName(), centerCoin.getName());
        assertEquals(coinCopy.getAlgorithm(), centerCoin.getAlgorithm());
    }
}