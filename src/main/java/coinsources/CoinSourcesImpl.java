package coinsources;

import dataclasses.Coin;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoinSourcesImpl implements CoinSources {
    public List<Coin> getCoinList() throws IOException, JSONException {
        List<Coin> list = new ArrayList<>();

        list.addAll(WhatToMine.getCoinList());
        list.addAll(ZergPool.getCoinList());

        return list;
    }

    public Coin getCoin(String coinName) throws IOException, JSONException {
        List<Coin> coinList = getCoinList();
        for (Coin coin : coinList) {
            if (coin.getName().equals(coinName)) {
                return coin;
            }
        }

        throw new RuntimeException("Coin not found");
    }
}
