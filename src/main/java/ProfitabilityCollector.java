import nicehash.Api;
import dataclasses.NicehashAlgorithm;
import org.json.JSONException;
import utils.CoinAlgoMatcher;
import dataclasses.CoinAlgoPair;
import whattomine.Coins;
import dataclasses.WhatToMineCoin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProfitabilityCollector {
    private static final int EXPECTED_PAIRS = 65;

    public static void start() throws IOException, JSONException {
        Api.loadConfig();

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
            try {
                printlogs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        service.scheduleAtFixedRate(print, 0, 1, TimeUnit.MINUTES);
    }

    public static void printlogs() throws IOException, JSONException {
        List<NicehashAlgorithm> algoList = Api.getAlgoList();
        List<WhatToMineCoin> coinList = Coins.getCoinList();

        List<CoinAlgoPair> list = new ArrayList<>();

        //boolean[] usedAlgos = new boolean[algoList.size()];
        //boolean[] usedCoins = new boolean[coinList.size()];

        for (int i = 0; i < algoList.size(); i++) {
            NicehashAlgorithm algo = algoList.get(i);
            for (int j = 0; j < coinList.size(); j++) {
                WhatToMineCoin coin = coinList.get(j);
                if (CoinAlgoMatcher.match(algo, coin)) {
                    //usedAlgos[i] = true;
                    //usedCoins[j] = true;

                    double gains = coin.getProfitability() / algo.getProfitability() * 100;
                    list.add(new CoinAlgoPair(coin, algo, gains));
                }
            }
        }

        if (list.size() != EXPECTED_PAIRS) {
            System.err.println(list.size());
            throw new RuntimeException("Wrong number of pairs!");
        }

        list.sort(Collections.reverseOrder());

        for (CoinAlgoPair pair : list) {
            System.out.println(pair);
        }

        /*
        // Unmatched
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
