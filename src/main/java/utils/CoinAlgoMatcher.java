package utils;

import dataclasses.Coin;
import dataclasses.NicehashAlgorithm;
import nicehash.NHApi;
import org.json.JSONException;
import coinsources.WhatToMineCoins;
import coinsources.ZergPool;

import java.io.IOException;
import java.util.*;

public class CoinAlgoMatcher {
    public static void workshop() throws IOException, JSONException {
        Map<String, List<Coin>> matches = new HashMap<>();

        List<NicehashAlgorithm> algoList = NHApi.getAlgoList();

        List<Coin> coinList = new ArrayList<>();
        coinList.addAll(WhatToMineCoins.getCoinList());
        coinList.addAll(ZergPool.getRevenueSources());

        for (NicehashAlgorithm algo : algoList) {
            Iterator<Coin> iterator = coinList.iterator();
            while (iterator.hasNext()) {
                Coin coin = iterator.next();

                if (match(algo.getAlgorithm(), coin.getAlgorithm())) {
                    if (matches.containsKey(algo.getAlgorithm())) {
                        List<Coin> list = matches.get(algo.getAlgorithm());
                        list.add(coin);
                    } else {
                        List<Coin> list = new ArrayList<>();
                        list.add(coin);
                        matches.put(algo.getAlgorithm(), list);
                    }
                    iterator.remove();
                }
            }
        }

        int count = 0;
        System.out.println("Matched");
        for (Map.Entry<String, List<Coin>> entry : matches.entrySet()) {
            for (Coin coin : entry.getValue()) {
                System.out.println(entry.getKey() + " " + coin);
                count++;
            }
        }

        System.out.println("Unmatched coins");
        for (Coin coin : coinList) {
            System.out.println(coin);
        }

        System.out.println("Match count: " + count);

    }

    public static final Map<String, String> matches = Map.of(
            "daggerhashimoto", "ethash",
            "sha256asicboost", "sha-256",
            "sha256", "sha-256",
            "randomxmonero", "randomx",
            "beamv3", "beamhashiii",
            "decred", "blake (14r)",
            "grincuckatoo32", "cuckatoo32",
            "grincuckatoo31", "cuckatoo31",
            "lyra2rev2", "lyra2v2",
            "zhash", "equihash144"
    );

    public static boolean match(String algo, String coinAlgo) {
        algo = algo.toLowerCase();
        coinAlgo = coinAlgo.toLowerCase();

        String value = matches.get(algo);
        if (coinAlgo.equals(value)) {
            return true;
        }

        return algo.equals(coinAlgo);
    }
}
