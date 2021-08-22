package coinsources;

import dataclasses.Coin;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.Config;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoinSourcesTest {
    @BeforeAll
    static void loadConfig() {
        Config.setDatabaseConfig(System.getenv("database_username"), System.getenv("database_password"), System.getenv("database_url"));
    }

    @Test
    void testGetCoinList() throws IOException, JSONException {
        CoinSources coinSources = CoinSourcesFactory.getInstance();
        List<Coin> list = coinSources.getCoinList();

        assertTrue(list.size() > 0);

        for (Coin coin : list) {
            assertNotNull(coin.getName());
            assertNotNull(coin.getAlgorithm());

            if (coin.getName().startsWith("zergpool-")) {
                assertEquals(0, coin.getExchangeRate());
            } else {
                assertTrue(coin.getExchangeRate() > 0);
            }
        }

        Coin centerCoin = list.get(list.size() / 2);

        Coin coinCopy = coinSources.getCoin(centerCoin.getName());
        assertEquals(coinCopy.getName(), centerCoin.getName());
        assertEquals(coinCopy.getAlgorithm(), centerCoin.getAlgorithm());
    }
}