package whattomine;

import dataclasses.WhatToMineCoin;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Conversions;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Coins {
    public static List<WhatToMineCoin> getCoinList() throws IOException, JSONException {
        List<WhatToMineCoin> coinList = new ArrayList<>();

        addCoins(coinList, "https://whattomine.com/coins.json");
        addCoins(coinList, "https://whattomine.com/asic.json");

        return coinList;
    }

    public static WhatToMineCoin getCoin(String coinName) throws IOException, JSONException {
        List<WhatToMineCoin> coinList = Coins.getCoinList();
        for (WhatToMineCoin coin : coinList) {
            if (coin.getName().equals(coinName)) {
                return coin;
            }
        }

        throw new RuntimeException("Coin not found");
    }

    private static void addCoins(List<WhatToMineCoin> list, String url) throws IOException, JSONException {
        JSONObject json = readJsonFromUrl(url);
        JSONObject coins = json.getJSONObject("coins");
        Iterator<String> keys = coins.keys();

        while (keys.hasNext()) {
            String coinName = keys.next();

            JSONObject coinObj = coins.getJSONObject(coinName);
            if (coinObj.getString("tag").toLowerCase().equals("nicehash")) {
                continue;
            }

            WhatToMineCoin wtmCoin = new WhatToMineCoin();
            wtmCoin.setName(coinName);
            wtmCoin.setAlgorithm(coinObj.getString("algorithm"));
            wtmCoin.setUnitProfitability(getCoinProfitability(coinObj));
            wtmCoin.setExchangeRate(coinObj.getDouble("exchange_rate"));

            list.add(wtmCoin);
        }
    }

    static double getCoinProfitability(JSONObject coinObj) throws JSONException {
        if (coinObj.getString("tag").toLowerCase().equals("btc")) {
            coinObj.put("exchange_rate", 1D);
        }

        return Conversions.BTC_TO_SATOSHIS / coinObj.getDouble("nethash") * coinObj.getDouble("block_reward") / coinObj.getDouble("block_time") * coinObj.getDouble("exchange_rate") * 60 * 60 * 24;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    static double getProfitabilityFromDifficulty(JSONObject coinObj) throws JSONException {
        if (coinObj.getString("tag").toLowerCase().equals("btc")) {
            coinObj.put("exchange_rate", 1D);
        }

        double constant = coinObj.getDouble("block_time") * coinObj.getDouble("nethash") / coinObj.getDouble("difficulty");

        double averageBlocktime = coinObj.getDouble("difficulty") * constant;

        return Conversions.BTC_TO_SATOSHIS * coinObj.getDouble("block_reward") / averageBlocktime * coinObj.getDouble("exchange_rate") * 60 * 60 * 24;
    }
}
