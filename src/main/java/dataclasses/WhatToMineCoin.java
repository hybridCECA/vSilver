package dataclasses;

import nicehash.Api;
import org.json.JSONException;
import test.generated.tables.records.CoinDataRecord;
import utils.CoinAlgoMatcher;
import utils.Conversions;

import java.util.List;

public class WhatToMineCoin {
    private String name;
    private double exchangeRate;
    private double unitProfitability;
    private String algorithm;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public double getUnitProfitability() {
        return unitProfitability;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setUnitProfitability(double unitProfitability) {
        this.unitProfitability = unitProfitability;
    }

    public int getIntProfitability() throws JSONException {
        return Conversions.unitProfitToIntPrice(unitProfitability, getHashPrefix());
    }

    private char getHashPrefix() throws JSONException {
        List<NicehashAlgorithmBuyInfo> buyInfos = Api.getBuyInfo();
        for (NicehashAlgorithmBuyInfo buyInfo : buyInfos) {
            String algoName = buyInfo.getName();
            if (CoinAlgoMatcher.match(algoName, algorithm)) {
                NicehashAlgorithmBuyInfo algoBuyInfo = Api.getAlgoBuyInfo(algoName);
                return Conversions.speedTextToHashPrefix(algoBuyInfo.getSpeedText());
            }
        }

        throw new RuntimeException("Couldn't find associated algo");
    }

    public CoinDataRecord toRecord() throws JSONException {
        CoinDataRecord coinRecord = new CoinDataRecord();

        coinRecord.setCoinRevenue(getIntProfitability());
        coinRecord.setCoinName(name);
        coinRecord.setExchangeRate(exchangeRate);

        return coinRecord;
    }
}
