package coinsources;

import dataclasses.Coin;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public interface CoinSources {
    List<Coin> getCoinList() throws IOException, JSONException;

    Coin getCoin(String coinName) throws IOException, JSONException;
}
