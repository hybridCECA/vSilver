package utils;

import dataclasses.NicehashAlgorithm;
import dataclasses.WhatToMineCoin;

import java.util.Map;

public class CoinAlgoMatcher {
    public static final Map<String, String> matches = Map.of(
            "daggerhashimoto", "ethash",
            "sha256asicboost", "sha-256",
            "sha256", "sha-256",
            "randomxmonero", "randomx",
            "beamv3", "beamhashiii",
            "decred", "blake (14r)"
    );

    public static boolean match(NicehashAlgorithm algo, WhatToMineCoin coin) {
        String algoAlgo = algo.getAlgorithm().toLowerCase();
        String coinAlgo = coin.getAlgorithm().toLowerCase();

        algoAlgo = algoAlgo.replaceFirst("^grin", "");

        String value = matches.get(algoAlgo);
        if (coinAlgo.equals(value)) {
            return true;
        }

        return algoAlgo.equals(coinAlgo);
    }
}
