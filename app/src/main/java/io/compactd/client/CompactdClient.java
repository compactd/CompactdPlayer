package io.compactd.client;

import android.util.Base64;

import com.github.zafarkhaja.semver.Version;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vinz243 on 26/11/2017.
 */

public class CompactdClient {
    public static final Version LATEST_VERSION = Version.valueOf("1.2.1");
    public static final String COMPATIBLE_VERSION_RANGE = "^1.2.0";
    private static final CompactdClient sInstance = new CompactdClient();

    private String token;
    private Map<String, String> prefixCache = new HashMap<>();

    public static CompactdClient getInstance() {
        return sInstance;
    }

    private URL url;
    private String username;

    private CompactdClient() {
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Version getServerVersion () throws JSONException, CompactdException, IOException {
        CompactdRequest req = new CompactdRequest(url, "/api/status");
        JSONObject res = req.send();
        JSONObject versions = res.getJSONObject("versions");
        return Version.valueOf(versions.getString("server"));
    }

    public boolean isServerValid () {
        try {
            return getServerVersion().satisfies(COMPATIBLE_VERSION_RANGE);
        } catch (IOException| JSONException | CompactdException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Login  to  a compactd server
     * @param username
     * @param password
     * @return true if the token alread exists
     * @throws IOException
     * @throws JSONException
     * @throws CompactdException
     */
    public boolean login (String username, String password) throws IOException, JSONException, CompactdException {
        if (isTokenValid(token)) {
            return true;
        }

        CompactdRequest req = new CompactdRequest(url, "/api/sessions");
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        token = req.post(body).getString("token");
        if (!decode(token).getString("user").equals(username)) {
            token = null;
            return false;
        }
        this.username = username;
        return true;

    }
    private static JSONObject decode (String token) throws UnsupportedEncodingException, JSONException {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String[] split = token.split("\\.");
        return new JSONObject(getJson(split[1]));
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    public boolean isTokenValid (String token) {
        try {
            JSONObject decoded = decode(token);
            return decoded != null && (System.currentTimeMillis() + 1000 * 60 * 60)
                    / 1000L <= decoded.getInt("exp");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getUserFromToken (String token) throws UnsupportedEncodingException, JSONException {

        JSONObject decoded = decode(token);
        return decoded.getString("user");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrefix() {
        String url = this.url.toString();
        if (prefixCache.containsKey(url)) {
            return prefixCache.get(url);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte d : hash) {
                String h = Integer.toHexString(0xFF & d);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            String prefix = "db_" + hexString.toString().substring(0, 5) + "_";
            prefixCache.put(url, prefix);
            return prefix;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "local_";
        }
    }

    public String getUsername() {
        return username;
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getToken());
        return headers;
    }
}
