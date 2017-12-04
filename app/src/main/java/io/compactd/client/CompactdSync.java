package io.compactd.client;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationState;
import com.couchbase.lite.support.HttpClientFactory;
import com.readystatesoftware.chuck.ChuckInterceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdArtwork;
import io.compactd.client.models.CompactdTrack;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
///**
// * Created by Vincent on 30/10/2017.
// */
//class CompactdAuthorizer extends BaseAuthorizer
//        implements CustomHeadersAuthorizer, CredentialAuthorizer {
//
//    public static final String TAG = Log.TAG_SYNC;
//    private String mToken;
//
//    public CompactdAuthorizer(String token) {
//        this.mToken = token;
//    }
//
//    @Override
//    public boolean authorizeURLRequest(Request.Builder builder) {
//        if (authUserInfo() == null)
//            return false;
//        builder.addHeader("Authorization", "Bearer " + mToken);
//        return true;
//    }
//
//    @Override
//    public String authUserInfo() {
//        if (this.mToken != null && !this.mToken.isEmpty()) {
//            try {
//                return new Authenticator().getUserFromToken(mToken);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                return null;
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return null;
//    }
    ////////////////////////////////////////////////////////////
    // Implementation of Authorizer
    ////////////////////////////////////////////////////////////

//    @Override
//    public boolean removeStoredCredentials() {
//        this.mToken = null;
//        return true;
//    }
//
//    public String getToken() {
//        // @optional
//        return mToken;
//    }
//}



public class CompactdSync {

    private static final String TAG = "CompactdSync";
    private static CompactdSync sInstance;
    private final String[] DATABASES = {
        CompactdTrack.DATABASE_NAME,
    };
    private final Manager mManager;
    private final HttpClientFactory factory;
    private String mToken;
    private String mURL;
    private List<SyncEventListener> mListeners = new ArrayList<SyncEventListener>();
    private ChuckInterceptor interceptor;

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public void setURL(String mURL) {
        this.mURL = mURL;
    }

    public interface SyncEventListener {
        public void finished () ;
        public void databaseChanged (String database);
        public void databaseSyncStarted (String database);
        public void databaseSyncFinished (String database);
        public void onCouchException (CouchbaseLiteException exc);
        public void onURLException (MalformedURLException exc);
        public void onProgress (float progress);
    }

    public interface DatabaseChangedListener {
        public void onDatabaseChanged ();
    }

    private CompactdSync(Context context) {
        this.mManager = CompactdManager.getInstance(context);
      //  interceptor = new ChuckInterceptor(context);
        factory = new HttpClientFactory() {
            @Override
            public OkHttpClient getOkHttpClient() {
                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                        .build();
                return new OkHttpClient.Builder()
                      //  .addInterceptor(interceptor)
                        .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                        .build();
            }

            @Override
            public void addCookies(List<Cookie> cookies) {
                android.util.Log.e(TAG, "addCookies: " + cookies);
            }

            @Override
            public void deleteCookie(String name) {
                android.util.Log.e(TAG, "deleteCookie: " + name);
            }

            @Override
            public void deleteCookie(URL url) {
                android.util.Log.e(TAG, "deleteCookie: " + url );
            }

            @Override
            public void resetCookieStore() {
                android.util.Log.e(TAG, "resetCookieStore:");
            }

            @Override
            public CookieJar getCookieStore() {
                android.util.Log.e(TAG, "getCookieStore: ");
                return null;
            }

            @Override
            public void evictAllConnectionsInPool() {

            }
        };
    }

    public void start (String prefix) {
        try {
            sync(prefix, 0);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            for (SyncEventListener listener : mListeners) {
                listener.onCouchException(e);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            for (SyncEventListener listener : mListeners) {
                listener.onURLException(e);
            }

        }
    }
    public void subscribe (final DatabaseChangedListener listener) {
        this.addEventListener(new SyncEventListener() {
            boolean changed;

            @Override
            public void finished() {
                if (changed) {
                    listener.onDatabaseChanged();
                }
            }

            @Override
            public void databaseChanged(String database) {

            }

            @Override
            public void databaseSyncStarted(String database) {

            }

            @Override
            public void databaseSyncFinished(String database) {

            }

            @Override
            public void onCouchException(CouchbaseLiteException exc) {

            }

            @Override
            public void onURLException(MalformedURLException exc) {

            }

            @Override
            public void onProgress(float progress) {

            }
        });
    }
    public void addEventListener (SyncEventListener l) {
        mListeners.add(l);
    }

    private void sync (final String prefix, final int index) throws CouchbaseLiteException, MalformedURLException {
        final String database = DATABASES[index];
        
        mManager.setDefaultHttpClientFactory(factory);

        DatabaseOptions options = new DatabaseOptions();
        options.setCreate(true);
        Log.d(TAG, "sync: " + prefix + database);
        final Database db = mManager.getDatabase(prefix + database);

        db.delete();

        DatabaseOptions opts = new DatabaseOptions();

        opts.setCreate(true);

        db.open(opts);


        Log.d(TAG, "sync: "+db);
        Replication rep = db.createPullReplication(new URL(mURL + "/database/" + database));

        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.mToken);
        rep.setHeaders(headers);

        rep.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
            Log.d(TAG, event.toString());
            if (event.getChangeCount() > 0) {
                for (SyncEventListener listener : mListeners) {
                    listener.databaseChanged(database);
                }
            }
            for (SyncEventListener listener : mListeners) {
                listener.onProgress((
                        (event.getCompletedChangeCount() / (event.getChangeCount() + 1)
                                + index) / DATABASES.length));
            }
            if (event.getTransition() != null && event.getTransition().getDestination().equals(ReplicationState.STOPPED)) {

               if (index < DATABASES.length - 1) {
                   try {
                       for (SyncEventListener listener : mListeners) {
                           listener.databaseSyncFinished(database);
                           listener.databaseSyncStarted(database);
                       }
                       db.close();
                       sync(prefix, index + 1);
                   } catch (CouchbaseLiteException e) {
                       e.printStackTrace();
                       for (SyncEventListener listener : mListeners) {
                           listener.onCouchException(e);
                       }

                   } catch (MalformedURLException e) {
                       e.printStackTrace();
                       for (SyncEventListener listener : mListeners) {
                           listener.onURLException(e);
                       }

                   }
               } else {
                   for (SyncEventListener listener : mListeners) {
                       listener.finished();
                   }
               }
            }
            }
        });
        rep.setContinuous(false);

        rep.start();

    }
    public static CompactdSync getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CompactdSync(context.getApplicationContext());
        }
        return sInstance;
    }
}


