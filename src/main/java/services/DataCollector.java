package services;

import coinsources.CoinSources;
import coinsources.CoinSourcesFactory;
import database.Connection;
import dataclasses.*;
import nicehash.*;
import org.json.JSONException;
import test.generated.tables.records.AlgoDataRecord;
import test.generated.tables.records.CoinDataRecord;
import test.generated.tables.records.MarketDataRecord;
import utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DataCollector implements vService {
    public final static Logger LOGGER = Logging.getLogger(DataCollector.class);

    @Override
    public int getRunPeriodSeconds() {
        return Config.getConfigInt(Consts.DATA_COLLECTOR_PERIOD_SECONDS);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("DataCollector start");
            collect();
            LOGGER.info("DataCollector done");
        } catch (Exception e) {
            LOGGER.severe(Conversions.exceptionToString(e));
        }
    }

    private static void collect() throws IOException, JSONException {
        NHApi nhApi = NHApiFactory.getInstance();
        nhApi.invalidateOrderbookCache();

        List<NicehashAlgorithm> algoList = nhApi.getAlgoList();

        Map<AlgoDataRecord, AlgoAssociatedData> map = new HashMap<>();

        CoinSources coinSources = CoinSourcesFactory.getInstance();
        List<Coin> coinList = coinSources.getCoinList();

        for (NicehashAlgorithm algo : algoList) {
            String algoName = algo.getAlgorithm();

            AlgoDataRecord algoRecord = new AlgoDataRecord();
            algoRecord.setAlgoName(algoName);

            // Get associated coins
            List<CoinDataRecord> coinRecordList = new ArrayList<>();
            for (Coin coin : coinList) {
                if (CoinAlgoMatcher.match(algoName, coin.getAlgorithm())) {
                    CoinDataRecord coinRecord = coin.toRecord();
                    coinRecordList.add(coinRecord);
                }
            }

            // Calculate fulfill speed
            NicehashAlgorithmBuyInfo algoBuyInfo = nhApi.getAlgoBuyInfo(algoName);
            double estimatedPrice = algo.getDoubleProfitability();
            double minAmount = algoBuyInfo.getMinAmount();
            double targetDays = Config.getConfigDouble(Consts.ORDER_TARGET_DAYS);
            double fulfillSpeed = minAmount / estimatedPrice / targetDays;
            fulfillSpeed = Math.max(fulfillSpeed, algoBuyInfo.getMinLimit());

            List<MarketDataRecord> marketRecordList = new ArrayList<>();
            for (String market : algoBuyInfo.getMarkets()) {
                List<NicehashOrder> orderbook = nhApi.getOrderbook(algoName, market);

                double totalSpeed = Price.getTotalSpeed(orderbook);
                int algoPrice = Price.getSweepPrice(orderbook, fulfillSpeed);

                // Market record
                MarketDataRecord marketRecord = new MarketDataRecord();
                marketRecord.setFulfillPrice(algoPrice);
                marketRecord.setFulfillSpeed(fulfillSpeed);
                marketRecord.setMarketName(market);
                marketRecord.setTotalSpeed(totalSpeed);
                marketRecordList.add(marketRecord);
            }

            AlgoAssociatedData associatedData = new AlgoAssociatedData(marketRecordList, coinRecordList);
            map.put(algoRecord, associatedData);
        }

        Connection.insertMap(map);
    }
}
