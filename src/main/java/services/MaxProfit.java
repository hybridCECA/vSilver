package services;

import database.Connection;
import dataclasses.PriceRecord;
import dataclasses.TriplePair;
import utils.Config;
import utils.Consts;
import utils.Conversions;

import java.util.List;

public interface MaxProfit extends vService {
    void updateMaxProfits();

    int getMaxProfitPrice(List<PriceRecord> list, int revenue);

    void register(TriplePair pair);

    void unregister(TriplePair pair);

    int getMaxProfit(TriplePair pair);

    boolean hasMaxProfit(TriplePair pair);
}
