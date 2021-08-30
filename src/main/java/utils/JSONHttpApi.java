package utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JSONHttpApi {
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        HttpGet get = new HttpGet(url);
        get.setConfig(Config.getRequestConfig());

        //Execute and get the response.
        HttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = httpclient.execute(get);
        HttpEntity entity = response.getEntity();

        try (InputStream stream = entity.getContent()) {
            String json = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return new JSONObject(json);
        }
    }
}
