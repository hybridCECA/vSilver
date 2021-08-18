package southxchange;

import dataclasses.SXBalance;
import nicehash.HttpApi;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Config;
import utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class SXApi {
    private static SXHttpApi httpApi;

    public static void loadConfig() {
        String apiKey = Config.getConfigValue(Consts.SX_API_KEY);
        String apiSecret = Config.getConfigValue(Consts.SX_API_SECRET);

        httpApi = new SXHttpApi(apiKey, apiSecret);
    }

    public static List<SXBalance> listBalances() throws Exception {
        String response =  httpApi.post("listBalances", new JSONObject());
        JSONArray balances = new JSONArray(response);

        List<SXBalance> list = new ArrayList<>();
        for (int i = 0; i < balances.length(); i++) {
            JSONObject jsonObject = balances.getJSONObject(i);

            String currency = jsonObject.getString("Currency");
            double available = jsonObject.getDouble("Available");

            if (available <= 0) {
                continue;
            }

            SXBalance balance = new SXBalance(currency, available);
            list.add(balance);
        }

        return list;
    }

    public static void sell(String listingCurrency, String referenceCurrency, double amount) throws Exception {
        JSONObject request = new JSONObject();
        request.put("listingCurrency", listingCurrency);
        request.put("referenceCurrency", referenceCurrency);
        request.put("type", "sell");
        request.put("amount", amount);

        httpApi.post("placeOrder", request);
    }
}
