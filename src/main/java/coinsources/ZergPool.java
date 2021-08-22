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

        JSONObject json = JSONHttpApi.readJsonFromUrl("http://api.zergpool.com:8080/api/status");

        Iterator<String> iterator = json.keys();

        while (iterator.hasNext()) {
            String algoName = iterator.next();
            JSONObject algoInfo = json.getJSONObject(algoName);

            addCoin(list, algoName, algoInfo);
        }

        return list;
    }

    private static void addCoin(List<Coin> list, String algoName, JSONObject algoInfo) throws JSONException {
        Coin source = new Coin();

        source.setAlgorithm(algoName);
        source.setName("zergpool-" + algoName);
        source.setUnitProfitability(getUnitProfitability(algoInfo));
        source.setExchangeRate(0);

        list.add(source);
    }

    private static double getUnitProfitability(JSONObject algoInfo) throws JSONException {
        double factor = algoInfo.getDouble("mbtc_mh_factor") * 1E6;

        return algoInfo.getDouble("estimate_current") / factor * Conversions.BTC_TO_SATOSHIS;
    }
}
