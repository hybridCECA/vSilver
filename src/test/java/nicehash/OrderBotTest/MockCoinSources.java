package nicehash.OrderBotTest;

import coinsources.CoinSources;
import dataclasses.Coin;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MockCoinSources implements CoinSources {
    private Coin coin = null;

    @Override
    public List<Coin> getCoinList() {
        return null;
    }

    @Override
    public Coin getCoin(String coinName, String algoName) {
        return coin;
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
    }
}
