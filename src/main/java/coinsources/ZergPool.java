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

public class ZergPool {
    protected static List<Coin> getCoinList() throws IOException, JSONException {
        List<Coin> list = new ArrayList<>();

        JSONObject json = JSONHttpApi.readJsonFromUrl("http://api.zergpool.com:8080/api/currencies");

        @SuppressWarnings("unchecked") Iterator<String> iterator = json.keys();

        while (iterator.hasNext()) {
            String name = iterator.next();
            JSONObject coinInfo = json.getJSONObject(name);

            addCoin(list, name, coinInfo);
        }

        return list;
    }

    private static void addCoin(List<Coin> list, String name, JSONObject coinInfo) throws JSONException {
        Coin coin = new Coin();

        coin.setAlgorithm(coinInfo.getString("algo"));
        coin.setName("zergpool-" + name);
        coin.setUnitProfitability(getUnitProfitability(coinInfo));
        coin.setExchangeRate(getExchangeRate(coinInfo));
        coin.setNethash(coinInfo.getDouble("network_hashrate"));

        list.add(coin);
    }

    private static double getUnitProfitability(JSONObject coinInfo) throws JSONException {
        double factor = coinInfo.getDouble("mbtc_mh_factor") * 1E6;

        return coinInfo.getDouble("estimate_current") / factor * Conversions.BTC_TO_SATOSHIS;
    }

    private static double getExchangeRate(JSONObject coinInfo) throws JSONException {
        double unitProfit = getUnitProfitability(coinInfo);

        return unitProfit / Conversions.BTC_TO_SATOSHIS
                / Conversions.DAYS_TO_SECONDS
                * coinInfo.getDouble("network_hashrate")
                * coinInfo.getDouble("blocktime")
                / coinInfo.getDouble("reward");
    }
}
