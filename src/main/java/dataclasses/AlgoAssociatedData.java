package dataclasses;

import com.google.common.collect.Multimap;
import test.generated.tables.records.CoinDataRecord;
import test.generated.tables.records.MarketDataRecord;
import test.generated.tables.records.MarketPriceDataRecord;

import java.util.List;

public class AlgoAssociatedData {
    private final Multimap<MarketDataRecord, MarketPriceDataRecord> marketData;
    private final List<CoinDataRecord> coinDataRecords;

    public AlgoAssociatedData(Multimap<MarketDataRecord, MarketPriceDataRecord> marketData, List<CoinDataRecord> coinDataRecords) {
        this.marketData = marketData;
        this.coinDataRecords = coinDataRecords;
    }

    public Multimap<MarketDataRecord, MarketPriceDataRecord> getMarketData() {
        return marketData;
    }

    public List<CoinDataRecord> getCoinDataRecords() {
        return coinDataRecords;
    }
}
