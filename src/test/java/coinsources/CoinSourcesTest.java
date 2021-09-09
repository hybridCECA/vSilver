package coinsources;

import dataclasses.Coin;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.Config;
import utils.SingletonFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


public class CoinSourcesTest {
    @BeforeClass
    static public void loadConfig() {
        Config.setDatabaseConfig(System.getenv("database_username"), System.getenv("database_password"), System.getenv("database_url"));
    }

    @Test
    public void testGetCoinList() throws IOException, JSONException {
        CoinSources coinSources = SingletonFactory.getInstance(CoinSources.class);
        List<Coin> list = coinSources.getCoinList();

        assertTrue(list.size() > 0);

        for (Coin coin : list) {
            assertNotNull(coin.getName());
            assertNotNull(coin.getAlgorithm());
        }

        Coin centerCoin = list.get(list.size() / 2);
        Coin coinCopy = coinSources.getCoin(centerCoin.getName(), centerCoin.getAlgorithm());

        assertEquals(coinCopy.getName(), centerCoin.getName());
        assertEquals(coinCopy.getAlgorithm(), centerCoin.getAlgorithm());
    }
}