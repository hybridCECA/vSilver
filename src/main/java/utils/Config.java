package utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    public static final String configFileName = "config.json";

    public static JSONObject getConfigObject() throws JSONException, IOException {
        String configContent = Files.readString(Paths.get(configFileName));
        return new JSONObject(configContent);
    }
}
