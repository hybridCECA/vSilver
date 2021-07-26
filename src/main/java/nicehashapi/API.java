package nicehashapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.JSON;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class API {
    public void updateOrder(String id, double price) throws IOException, JSONException {
        URL url = new URL("https://api2.nicehash.com/main/api/v2/hashpower/order/" + id + "/updatePriceAndLimit");
        URLConnection con = url.openConnection();

        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("price", price);

        byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();

        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
    }

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
