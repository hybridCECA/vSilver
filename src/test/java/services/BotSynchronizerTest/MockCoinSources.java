package services.BotSynchronizerTest;

import coinsources.CoinSources;
import dataclasses.Coin;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MockCoinSources implements CoinSources {
    boolean validCoin = true;

    @Override
    public List<Coin> getCoinList() {
        return null;
    }

    @Override
    public Coin getCoin(String coinName, String algoName) {
        if (!validCoin) {
            throw new RuntimeException("Coin not found");
        }

        return null;
    }

    public void setValidCoin(boolean validCoin) {
        this.validCoin = validCoin;
    }
}
