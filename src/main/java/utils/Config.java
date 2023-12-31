package utils;

import database.Connection;
import org.apache.http.client.config.RequestConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Config {
    private static final Map<String, String> configCache = new ConcurrentHashMap<>();
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

    public static void invalidateCache() {
        configCache.clear();
    }

    public static String getConfigValue(String key) {
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        } else {
            String value = Connection.getConfigValue(key);
            configCache.put(key, value);

            return value;
        }
    }

    public static int getConfigInt(String key) {
        String value = getConfigValue(key);
        return Integer.parseInt(value);
    }

    public static double getConfigDouble(String key) {
        String value = getConfigValue(key);
        return Double.parseDouble(value);
    }

    public static RequestConfig getRequestConfig() {
        int connectionTimeoutMs = getConfigInt(Consts.CONNECTION_TIMEOUT_MS);

        return RequestConfig.custom()
                .setConnectionRequestTimeout(connectionTimeoutMs)
                .setConnectTimeout(connectionTimeoutMs)
                .setSocketTimeout(connectionTimeoutMs)
                .build();
    }
}
