package dataclasses;

import test.generated.tables.records.CoinDataRecord;
import test.generated.tables.records.MarketDataRecord;

import java.util.List;

public class AlgoAssociatedData {
    public List<MarketDataRecord> marketDataRecords;
    public List<CoinDataRecord> coinDataRecords;

    public AlgoAssociatedData(List<MarketDataRecord> marketDataRecords, List<CoinDataRecord> coinDataRecords) {
        this.marketDataRecords = marketDataRecords;
        this.coinDataRecords = coinDataRecords;
    }
}
