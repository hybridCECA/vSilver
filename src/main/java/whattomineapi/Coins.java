package whattomineapi;

import utils.Conversion;
import utils.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class Coins {
    private static List<WhatToMineCoin> coinList;

    public static List<WhatToMineCoin> getCoinList() throws IOException, JSONException {
        coinList = new ArrayList<>();

        addCoins("https://whattomine.com/coins.json");
        addCoins("https://whattomine.com/asic.json");

        //removeUnprofitable();

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

    private static void removeUnprofitable() {
        for (int i = coinList.size() - 1; i >= 0; i--) {
            WhatToMineCoin wtmCoin1 = coinList.get(i);
            for (int j = i - 1; j >= 0; j--) {
                WhatToMineCoin wtmCoin2 = coinList.get(j);
                if (wtmCoin1.getAlgorithm().equals(wtmCoin2.getAlgorithm())) {
                    if (wtmCoin1.getProfitability() > wtmCoin2.getProfitability()) {
                        coinList.remove(j);
                        i--;
                    } else {
                        coinList.remove(i);
                        break;
                    }
                }

            }
        }
    }

    private static void addCoins(String url) throws IOException, JSONException {
        JSONObject json = JSON.readJsonFromUrl(url);
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
            wtmCoin.setProfitability(getCoinProfitability(coinObj));

            coinList.add(wtmCoin);
        }
    }

    private static final Set<String> PROPS = Set.of("exchange_rate", "block_reward", "block_time", "nethash");
    static double getCoinProfitability(JSONObject coinObj) throws JSONException {
        Iterator<String> coinKeys = coinObj.keys();
        Map<String, Double> coinProps = new HashMap<>();
        while (coinKeys.hasNext()) {
            String coinKey = coinKeys.next();
            if (!PROPS.contains(coinKey)) {
                continue;
            }

            String prop = coinObj.getString(coinKey);
            double value = Double.parseDouble(prop);
            coinProps.put(coinKey, value);
        }

        if (coinObj.getString("tag").toLowerCase().equals("btc")) {
            coinProps.put("exchange_rate", 1D);
        }

        return Conversion.BTC_TO_SATOSHIS / coinProps.get("nethash") * coinProps.get("block_reward") / coinProps.get("block_time") * coinProps.get("exchange_rate") * 60 * 60 * 24;
    }
}
