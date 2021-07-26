package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AlgoInfo {
    public static double getDownStep(String algoName) throws JSONException, IOException {
        algoName = algoName.toLowerCase();

        JSONObject json = JSON.readJsonFromUrl("https://api2.nicehash.com/main/api/v2/public/buy/info");
        JSONArray algos = json.getJSONArray("miningAlgorithms");

        for (int i = 0; i < algos.length(); i++) {
            JSONObject algo = algos.getJSONObject(i);
            String name = algo.getString("name");

            if (name.toLowerCase().equals(algoName)) {
                return algo.getDouble("down_step");
            }
        }

        throw new RuntimeException("Algo " + algoName + " not found!");
    }
}
