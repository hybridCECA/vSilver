package nicehashapi;

import utils.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Algorithms {
    public static List<NicehashAlgorithm> getAlgoList() throws IOException, JSONException {
        List<NicehashAlgorithm> algoList = new ArrayList<>();

        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/public/simplemultialgo/info");
        JSONArray algos = json.getJSONArray("miningAlgorithms");
        for (int i = 0; i < algos.length(); i++) {
            NicehashAlgorithm nicehashAlgo = new NicehashAlgorithm();

            JSONObject algo = algos.getJSONObject(i);
            String algorithm = algo.getString("algorithm");
            double rate = algo.getDouble("paying");

            nicehashAlgo.setAlgorithm(algorithm);
            nicehashAlgo.setProfitability(rate);

            algoList.add(nicehashAlgo);
        }

        return algoList;
    }
}
