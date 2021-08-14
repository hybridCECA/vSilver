package utils;

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

    public static boolean match(String algo, String coinAlgo) {
        algo = algo.toLowerCase();
        coinAlgo = coinAlgo.toLowerCase();

        algo = algo.replaceFirst("^grin", "");

        String value = matches.get(algo);
        if (coinAlgo.equals(value)) {
            return true;
        }

        return algo.equals(coinAlgo);
    }
}
