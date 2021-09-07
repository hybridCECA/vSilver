package coinsources;

import dataclasses.Coin;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.Config;
import utils.SingletonFactory;

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
        CoinSources coinSources = SingletonFactory.getInstance(CoinSources.class);
        List<Coin> list = coinSources.getCoinList();

        assertTrue(list.size() > 0);

        for (Coin coin : list) {
            assertNotNull(coin.getName());
            assertNotNull(coin.getAlgorithm());

            assertTrue(coin.getExchangeRate() >= 0);
            assertTrue(coin.getUnitProfitability() >= 0);
        }

        Coin centerCoin = list.get(list.size() / 2);
        Coin coinCopy = coinSources.getCoin(centerCoin.getName(), centerCoin.getAlgorithm());

        System.out.println(coinCopy);
        System.out.println(centerCoin);
        assertEquals(coinCopy.getName(), centerCoin.getName());
        assertEquals(coinCopy.getAlgorithm(), centerCoin.getAlgorithm());
    }
}