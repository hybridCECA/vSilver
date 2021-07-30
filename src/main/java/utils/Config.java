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
    public static final String configFileName = "config.json";

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
