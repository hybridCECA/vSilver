package southxchange;

import dataclasses.SXBalance;
import org.json.JSONArray;
import org.json.JSONObject;
import services.TransferBot;
import utils.Config;
import utils.Consts;
import utils.Logging;
import utils.VLogger;

import java.util.ArrayList;
import java.util.List;

public class SXApi {
    private static final VLogger vLogger = Logging.getLogger(TransferBot.class);
    private static SXHttpApi httpApi;

    public static void loadConfig() {
        String apiKey = Config.getConfigValue(Consts.SX_API_KEY);
        String apiSecret = Config.getConfigValue(Consts.SX_API_SECRET);

        httpApi = new SXHttpApi(apiKey, apiSecret);
    }

    public static List<SXBalance> listBalances() throws Exception {
        String response = httpApi.post("listBalances", new JSONObject());
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

    public static void withdrawLightning(String currency, String address, double amount) throws Exception {
        JSONObject request = new JSONObject();
        request.put("currency", currency);
        request.put("address", address);
        request.put("destination", address);
        request.put("destinationType", 1);
        request.put("amount", amount);

        String response = httpApi.post("withdraw", request);
        vLogger.info(response);
    }
}
