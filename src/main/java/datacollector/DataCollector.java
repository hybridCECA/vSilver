package datacollector;

import database.Connection;
import dataclasses.CoinAlgoPair;
import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.WhatToMineCoin;
import nicehash.Api;
import nicehash.Price;
import org.json.JSONException;
import utils.CoinAlgoMatcher;
import utils.Conversions;
import whattomine.Coins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataCollector {
    public static void start() {
        Api.loadConfig();

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
            try {
                collect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        service.scheduleAtFixedRate(print, 0, 1, TimeUnit.MINUTES);
    }

    private static void collect() throws IOException, JSONException {
        List<CoinAlgoPair> pairs = getPairsList();

        List<NicehashAlgorithmBuyInfo> buyInfo = Api.getBuyInfo();
        for (CoinAlgoPair pair : pairs) {
            NicehashAlgorithm algo = pair.getNhAlgo();
            WhatToMineCoin coin = pair.getWtmCoin();

            String algoName = algo.getAlgorithm();
            NicehashAlgorithmBuyInfo algoBuyInfo = Api.getAlgoBuyInfo(buyInfo, algoName);
            char hashPrefix = Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());

            // Calculate fulfill speed
            double estimatedPrice = Conversions.unitProfitToDailyBTC(algo.getProfitability(), hashPrefix);
            double minAmount = algoBuyInfo.getMinAmount();
            String targetDaysString = Connection.getConfigValue("order_target_days");
            double targetDays = Double.parseDouble(targetDaysString);
            double fulfillSpeed = minAmount / estimatedPrice / targetDays;
            fulfillSpeed = Math.max(fulfillSpeed, algoBuyInfo.getMinLimit());

            // Get price for each market and record
            for (String market : algoBuyInfo.getMarkets()) {
                int algoPrice = Price.getSweepPrice(fulfillSpeed, algoName, market);
                Connection.putPair(algoPrice, algo.getAlgorithm(), coin.getName(), Conversions.unitProfitToStringPrice(coin.getProfitability(), hashPrefix), coin.getExchangeRate(), market);
            }
        }
    }

    public static final int EXPECTED_PAIRS = 65;
    public static List<CoinAlgoPair> getPairsList() throws JSONException, IOException {
        List<NicehashAlgorithm> algoList = Api.getAlgoList();
        List<WhatToMineCoin> coinList = Coins.getCoinList();

        List<CoinAlgoPair> list = new ArrayList<>();

        for (NicehashAlgorithm algo : algoList) {
            for (WhatToMineCoin coin : coinList) {
                if (CoinAlgoMatcher.match(algo, coin)) {
                    list.add(new CoinAlgoPair(coin, algo));
                }
            }
        }

        if (list.size() != EXPECTED_PAIRS) {
            System.err.println(list.size());
            throw new RuntimeException("Wrong number of pairs!");
        }

        return list;
    }
}
