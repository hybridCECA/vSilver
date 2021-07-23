import nicehashapi.Algorithms;
import nicehashapi.NicehashAlgorithm;
import org.json.JSONException;
import utils.CoinAlgoMatcher;
import utils.CoinAlgoPair;
import whattomineapi.Coins;
import whattomineapi.WhatToMineCoin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int EXPECTED_PAIRS = 65;

    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
            try {
                printlogs();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        };

        service.scheduleAtFixedRate(print, 0, 1, TimeUnit.MINUTES);
    }

    private static void printlogs() throws IOException, JSONException {
        List<NicehashAlgorithm> algoList = Algorithms.getAlgoList();
        List<WhatToMineCoin> coinList = Coins.getCoinList();

        List<CoinAlgoPair> list = new ArrayList<>();

        boolean[] usedAlgos = new boolean[algoList.size()];
        boolean[] usedCoins = new boolean[coinList.size()];

        for (int i = 0; i < algoList.size(); i++) {
            NicehashAlgorithm algo = algoList.get(i);
            for (int j = 0; j < coinList.size(); j++) {
                WhatToMineCoin coin = coinList.get(j);
                if (CoinAlgoMatcher.match(algo, coin)) {
                    usedAlgos[i] = true;
                    usedCoins[j] = true;

                    double gains = coin.getProfitability() / algo.getProfitability() * 100;
                    list.add(new CoinAlgoPair(coin, algo, gains));
                }
            }
        }

        if (list.size() != EXPECTED_PAIRS) {
            System.err.println(list.size());
            throw new RuntimeException("Wrong number of pairs! Check code");
        }

        Collections.sort(list, Collections.reverseOrder());

        for (CoinAlgoPair pair : list) {
            System.out.println(pair);
        }

        // Unmatched
        /*
        for (int i = 0; i < algoList.size(); i++) {
            if (!usedAlgos[i]) {
                System.out.println(algoList.get(i));
            }
        }
        for (int i = 0; i < coinList.size(); i++) {
            if (!usedCoins[i]) {
                System.out.println(coinList.get(i));
            }
        }
         */

    }
}
