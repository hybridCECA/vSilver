package coinsources;

import dataclasses.Coin;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Conversions;
import utils.JSONHttpApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WhatToMine {
    protected static List<Coin> getCoinList() throws IOException, JSONException {
        List<Coin> coinList = new ArrayList<>();

        addCoins(coinList, "https://whattomine.com/coins.json");
        addCoins(coinList, "https://whattomine.com/asic.json");

        return coinList;
    }

    private static void addCoins(List<Coin> list, String url) throws IOException, JSONException {
        JSONObject json = JSONHttpApi.readJsonFromUrl(url);
        JSONObject coins = json.getJSONObject("coins");
        Iterator<String> keys = coins.keys();

        while (keys.hasNext()) {
            String coinName = keys.next();

            JSONObject coinObj = coins.getJSONObject(coinName);
            if (coinObj.getString("tag").toLowerCase().equals("nicehash")) {
                continue;
            }

            Coin wtmCoin = new Coin();
            wtmCoin.setName("whattomine-" + coinName);
            wtmCoin.setAlgorithm(coinObj.getString("algorithm"));
            wtmCoin.setUnitProfitability(getCoinUnitProfitability(coinObj));
            wtmCoin.setExchangeRate(coinObj.getDouble("exchange_rate"));
            wtmCoin.setNethash(coinObj.getDouble("nethash"));

            list.add(wtmCoin);
        }
    }

    private static double getCoinUnitProfitability(JSONObject coinObj) throws JSONException {
        if (coinObj.getString("tag").toLowerCase().equals("btc")) {
            coinObj.put("exchange_rate", 1D);
        }

        return Conversions.BTC_TO_SATOSHIS / coinObj.getDouble("nethash") * coinObj.getDouble("block_reward") / coinObj.getDouble("block_time") * coinObj.getDouble("exchange_rate") * Conversions.DAYS_TO_SECONDS;
    }

    private static double getProfitabilityFromDifficulty(JSONObject coinObj) throws JSONException {
        if (coinObj.getString("tag").toLowerCase().equals("btc")) {
            coinObj.put("exchange_rate", 1D);
        }

        double constant = coinObj.getDouble("block_time") * coinObj.getDouble("nethash") / coinObj.getDouble("difficulty");

        double averageBlocktime = coinObj.getDouble("difficulty") * constant;

        return Conversions.BTC_TO_SATOSHIS * coinObj.getDouble("block_reward") / averageBlocktime * coinObj.getDouble("exchange_rate") * 60 * 60 * 24;
    }
}
