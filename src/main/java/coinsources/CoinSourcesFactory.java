package coinsources;

import nicehash.NHApi;
import nicehash.NHApiImpl;

public class CoinSourcesFactory {
    private static CoinSources coinSources = null;

    // Singleton
    public static CoinSources getInstance() {
        if (coinSources == null) {
            coinSources = new CoinSourcesImpl();
        }

        return coinSources;
    }
}
