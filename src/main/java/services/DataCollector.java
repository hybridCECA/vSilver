package services;

import coinsources.CoinSources;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import database.Connection;
import dataclasses.*;
import nicehash.NHApi;
import nicehash.PriceTools;
import org.json.JSONException;
import test.generated.tables.records.AlgoDataRecord;
import test.generated.tables.records.CoinDataRecord;
import test.generated.tables.records.MarketDataRecord;
import test.generated.tables.records.MarketPriceDataRecord;
import utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCollector implements vService {
    private final static VLogger LOGGER = Logging.getLogger(DataCollector.class);

    private static void collect() throws IOException, JSONException {
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);
        nhApi.invalidateOrderbookCache();

        List<NicehashAlgorithm> algoList = nhApi.getAlgoList();

        Map<AlgoDataRecord, AlgoAssociatedData> map = new HashMap<>();

        CoinSources coinSources = SingletonFactory.getInstance(CoinSources.class);
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
            Multimap<MarketDataRecord, MarketPriceDataRecord> marketData = MultimapBuilder.hashKeys().arrayListValues().build();

            NicehashAlgorithmBuyInfo algoBuyInfo = nhApi.getAlgoBuyInfo(algoName);
            for (String market : algoBuyInfo.getMarkets()) {
                List<NicehashOrder> orderbook = nhApi.getOrderbook(algoName, market);

                PriceTools priceTools = SingletonFactory.getInstance(PriceTools.class);
                double totalSpeed = priceTools.getTotalSpeed(orderbook);

                // Market record
                MarketDataRecord marketRecord = new MarketDataRecord();
                marketRecord.setMarketName(market);
                marketRecord.setTotalSpeed(totalSpeed);

                // Market price records
                double minLimit = algoBuyInfo.getMinLimit();
                for (double speed = minLimit; speed < totalSpeed; speed *= 2) {
                    int fulfillPrice = priceTools.getSweepPrice(orderbook, speed);

                    MarketPriceDataRecord priceRecord = new MarketPriceDataRecord();
                    priceRecord.setFulfillSpeed(speed);
                    priceRecord.setFulfillPrice(fulfillPrice);

                    marketData.put(marketRecord, priceRecord);
                }
            }

            AlgoAssociatedData associatedData = new AlgoAssociatedData(marketData, coinRecordList);
            map.put(algoRecord, associatedData);
        }

        Connection.putMap(map);
    }

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
            LOGGER.error(e);
        }
    }
}
