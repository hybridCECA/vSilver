package southxchange;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Formatter;
import java.util.List;

public class SXHttpApi {
    private String key;
    private String secret;
    private static final String HMAC_SHA512 = "HmacSHA512";

    public SXHttpApi(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public String post(String relativeUri, JSONObject request) throws Exception {
        request.put("key", key);
        request.put("nonce", System.currentTimeMillis());

        String jsonData = request.toString();

        HttpPost httppost = new HttpPost("https://www.southxchange.com/api/" + relativeUri);
        httppost.setConfig(Config.getRequestConfig());

        httppost.addHeader("Hash", getHash(jsonData));

        // Request parameters and other properties.
        StringEntity requestEntity = new StringEntity(jsonData, ContentType.APPLICATION_JSON);
        httppost.setEntity(requestEntity);

        //Execute and get the response.
        HttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        try (InputStream stream = entity.getContent()) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
    }

    private String getHash(String jsonData) throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] byteKey = secret.getBytes(StandardCharsets.UTF_8);
        Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
        sha512Hmac.init(keySpec);
        byte[] macData = sha512Hmac.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));

        return Hex.encodeHexString(macData);
    }
}