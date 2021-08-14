package services;

import database.Connection;
import dataclasses.AlgoAssociatedData;
import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.WhatToMineCoin;
import nicehash.Api;
import nicehash.MaxProfit;
import nicehash.Price;
import org.json.JSONException;
import test.generated.tables.records.AlgoDataRecord;
import test.generated.tables.records.CoinDataRecord;
import test.generated.tables.records.MarketDataRecord;
import utils.CoinAlgoMatcher;
import utils.Config;
import utils.Consts;
import whattomine.Coins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataCollector {
    public static void start() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
            try {
                System.out.println("DataCollector start");
                collect();
                System.out.println("MaxProfit start");
                MaxProfit.updateMaxProfits();
                System.out.println("MaxProfit done");
                System.out.println("DataCollector done");
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        int period = Config.getConfigInt(Consts.DATA_COLLECTOR_PERIOD_SECONDS);
        service.scheduleAtFixedRate(print, 0, period, TimeUnit.SECONDS);
    }

    private static void collect() throws IOException, JSONException {
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
                if (CoinAlgoMatcher.match(algoName, coin.getAlgorithm())) {
                    CoinDataRecord coinRecord = coin.toRecord();
                    coinRecordList.add(coinRecord);
                }
            }

            // Calculate fulfill speed
            NicehashAlgorithmBuyInfo algoBuyInfo = Api.getAlgoBuyInfo(algoName);
            double estimatedPrice = algo.getDoubleProfitability();
            double minAmount = algoBuyInfo.getMinAmount();
            double targetDays = Config.getConfigDouble(Consts.ORDER_TARGET_DAYS);
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
    }
}
