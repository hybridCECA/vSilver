package datacollector;

import database.Connection;
import dataclasses.*;
import nicehash.Api;
import nicehash.Price;
import org.json.JSONException;
import org.json.JSONObject;
import test.generated.tables.AlgoData;
import test.generated.tables.records.AlgoDataRecord;
import test.generated.tables.records.CoinDataRecord;
import test.generated.tables.records.MarketDataRecord;
import utils.CoinAlgoMatcher;
import utils.Config;
import utils.Conversions;
import whattomine.Coins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

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
        System.out.println("Start");

        Api.invalidateOrderbookCache();

        Map<AlgoDataRecord, AlgoAssociatedData> map = new HashMap<>();

        List<NicehashAlgorithm> algoList = Api.getAlgoList();
        List<WhatToMineCoin> coinList = Coins.getCoinList();
        for (NicehashAlgorithm algo : algoList) {
            String algoName = algo.getAlgorithm();

            AlgoDataRecord algoRecord = new AlgoDataRecord();
            algoRecord.setAlgoName(algoName);

            // Get associated coins
            List<CoinDataRecord> coinRecordList = new ArrayList<>();
            for (WhatToMineCoin coin : coinList) {
                if (CoinAlgoMatcher.match(algo, coin)) {
                    CoinDataRecord coinRecord = new CoinDataRecord();
                    coinRecord.setCoinName(coin.getName());
                    coinRecord.setCoinRevenue(coin.getProfitability());
                    coinRecord.setExchangeRate(coin.getExchangeRate());
                    coinRecordList.add(coinRecord);
                }
            }

            // Get associated markets
            NicehashAlgorithmBuyInfo algoBuyInfo = Api.getAlgoBuyInfo(algoName);
            char hashPrefix = Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());

            // Calculate fulfill speed
            double estimatedPrice = Conversions.unitProfitToDailyBTC(algo.getProfitability(), hashPrefix);
            double minAmount = algoBuyInfo.getMinAmount();
            String targetDaysString = Config.getConfigValue("order_target_days");
            double targetDays = Double.parseDouble(targetDaysString);
            double fulfillSpeed = minAmount / estimatedPrice / targetDays;
            fulfillSpeed = Math.max(fulfillSpeed, algoBuyInfo.getMinLimit());

            List<MarketDataRecord> marketRecordList = new ArrayList<>();
            for (String market : algoBuyInfo.getMarkets()) {
                double totalSpeed = Price.getTotalSpeed(algoName, market);
                int algoPrice = Price.getSweepPrice(fulfillSpeed, algoName, market);

                // Market record
                MarketDataRecord marketRecord = new MarketDataRecord();
                marketRecord.setFulfillPrice(algoPrice);
                marketRecord.setFulfillSpeed(fulfillSpeed);
                marketRecord.setMarketName(market);
                marketRecord.setTotalSpeed(totalSpeed);
                marketRecordList.add(marketRecord);
            }

            AlgoAssociatedData associatedData = new AlgoAssociatedData();
            associatedData.coinDataRecords = coinRecordList;
            associatedData.marketDataRecords = marketRecordList;

            map.put(algoRecord, associatedData);
        }

        Connection.insertMap(map);
        System.out.println("Done");
    }
}
