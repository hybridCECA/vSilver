package utils;

import nicehashapi.NicehashAlgorithm;
import org.json.JSONException;
import org.json.JSONObject;
import whattomineapi.WhatToMineCoin;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CoinAlgoPair implements Comparable<CoinAlgoPair> {
    private WhatToMineCoin wtmCoin;
    private NicehashAlgorithm nhAlgo;
    private double gains;

    public CoinAlgoPair(WhatToMineCoin wtmCoin, NicehashAlgorithm nhAlgo, double gains) {
        this.wtmCoin = wtmCoin;
        this.nhAlgo = nhAlgo;
        this.gains = gains;
    }

    @Override
    public String toString() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        JSONObject obj = new JSONObject();
        try {
            obj.put("timestamp", timeStamp);
            obj.put("coin", wtmCoin.getName());
            obj.put("algo", nhAlgo.getAlgorithm());
            obj.put("gains", gains);
            return obj.toString();
        } catch (JSONException e) {
            return e.toString();
        }
    }

    @Override
    public int compareTo(CoinAlgoPair o) {
        return Double.compare(this.gains, o.gains);
    }
}
