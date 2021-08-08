package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final String configFileName = "config.json";
    private static String databaseUserName;
    private static String databasePassword;
    private static String databaseUrl;

    public static void setDatabaseConfig(String databaseUserName, String databasePassword, String databaseUrl) {
        Config.databaseUserName = databaseUserName;
        Config.databasePassword = databasePassword;
        Config.databaseUrl = databaseUrl;
    }

    public static String getDatabaseUserName() {
        return databaseUserName;
    }

    public static String getDatabasePassword() {
        return databasePassword;
    }

    public static String getDatabaseUrl() {
        return databaseUrl;
    }

    public static JSONObject getConfigObject() throws JSONException, IOException {
        String configContent = Files.readString(Paths.get(configFileName));
        return new JSONObject(configContent);
    }

    public static List<JSONObject> getOrderConfigList() throws JSONException, IOException {
        JSONObject config = getConfigObject();
        JSONArray orderArray = config.getJSONArray("orderBots");

        List<JSONObject> output = new ArrayList<>();
        for (int i = 0; i < orderArray.length(); i++) {
            output.add(orderArray.getJSONObject(i));
        }

        return output;
    }
}
