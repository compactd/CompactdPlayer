package io.compactd.client;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdRequest {
    private static final String TAG = "CompactdRequest";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private URL mBaseURL;
    private String mEndpoint;
    private String mSessionToken;

    public CompactdRequest(URL baseURL, String endpoint) {
        this.mBaseURL = baseURL;
        this.mEndpoint = endpoint;
    }
    public CompactdRequest(URL baseURL, String endpoint, String token) {
        this.mBaseURL = baseURL;
        this.mEndpoint = endpoint;
        this.mSessionToken = token;
    }
    public String getSessionToken() {
        return mSessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.mSessionToken = sessionToken;
    }

    /**
     * Send a GET request to the server
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws CompactdException
     */
    public JSONObject send ()  throws IOException, JSONException, CompactdException {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                .build();
//        builder.sslSocketFactory(new NoSSLv3Factory())
//        client.(new NoSSLv3Factory());

        String remote = mBaseURL.toString();

        if (remote.endsWith("/") && this.mEndpoint.startsWith("/")) {
            remote = remote.substring(0, remote.length() - 1);
        }

        remote = remote + this.mEndpoint;

        Request.Builder requestBuilder = new Request.Builder()
                .url(new URL(remote)).get();


        if (getSessionToken() != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + getSessionToken());
        }

        Request request = requestBuilder.build();

        Response response = client.newCall(request).execute();

        String raw = response.body().string();

        Log.d(TAG, "Response: " + raw);

        JSONObject res = new JSONObject(raw);

        if (res.has("error")) {
            String error = res.getString("error");

            if (error != null && !error.isEmpty()) {
                if (error.equals("Invalid credentials")) {
                    throw new CompactdException(CompactdErrorCode.INVALID_CREDENTIALS);
                }
                throw new CompactdException(CompactdErrorCode.SERVER_ERROR);
            }
        }
        return res;

    }
    public JSONObject post (JSONObject data) throws IOException, JSONException, CompactdException {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                .build();
//        builder.sslSocketFactory(new NoSSLv3Factory())
//        client.(new NoSSLv3Factory());

        String remote = mBaseURL.toString();

        if (remote.endsWith("/") && this.mEndpoint.startsWith("/")) {
            remote = remote.substring(0, remote.length() - 1);
        }

        remote = remote + this.mEndpoint;

        Log.d(TAG, "POST " + remote);
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            if (key.equals("password")) {
                int length = data.get(key).toString().length();

                Log.d(TAG, "POST   " + key + " = " +
                        new String(new char[length]).replace('\0', '*'));
            } else {

                Log.d(TAG, "POST   " + key + " = " + data.get(key));
            }
        }

        RequestBody body = RequestBody.create(JSON, data.toString());
        Request.Builder requestBuilder = new Request.Builder()
                .url(new URL(remote))
                .post(body);


        if (getSessionToken() != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + getSessionToken());
        }
        Request request = requestBuilder.build();

        Response response = client.newCall(request).execute();

        String raw = response.body().string();

        Log.d(TAG, "Response: " + raw);

        JSONObject res = new JSONObject(raw);

        if (res.has("error")) {
            String error = res.getString("error");

            if (error != null && !error.isEmpty()) {
                if (error.equals("Invalid credentials")) {
                    throw new CompactdException(CompactdErrorCode.INVALID_CREDENTIALS);
                }
                throw new CompactdException(CompactdErrorCode.SERVER_ERROR);
            }
        }
        return res;

    }
}
