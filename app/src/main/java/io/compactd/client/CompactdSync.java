package io.compactd.client;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationState;
import com.couchbase.lite.support.HttpClientFactory;
import com.couchbase.lite.support.PersistentCookieJar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdArtwork;
import io.compactd.client.models.CompactdHttpClientFactory;
import io.compactd.client.models.CompactdTrack;
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
        CompactdAlbum.DATABASE_NAME,
        CompactdArtwork.DATABASE_NAME,
        CompactdArtist.DATABASE_NAME
    };
    private final Manager mManager;
    private HttpClientFactory factory;
    private String mToken;
    private String mURL;
    private List<SyncEventListener> mListeners = new ArrayList<SyncEventListener>();

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public void setURL(String mURL) {
        this.mURL = mURL;
    }

    public interface SyncEventListener {
        void finished () ;
        void databaseChanged (String database);
        void databaseSyncStarted (String database);
        void databaseSyncFinished (String database);
        void onCouchException (CouchbaseLiteException exc);
        void onURLException (MalformedURLException exc);
        void onProgress (float progress);
    }

    public interface DatabaseChangedListener {
        void onDatabaseChanged ();
    }

    private CompactdSync(Context context) {
        this.mManager = CompactdManager.getInstance(context);
      //  interceptor = new ChuckInterceptor(context);
        try {
            factory = new CompactdHttpClientFactory(
                    new PersistentCookieJar(mManager.getDatabase("cookies")));

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
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
        final Database db = mManager.getDatabase(prefix + database);

        Replication rep = db.createPullReplication(new URL(mURL + "/database/" + database));

        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.mToken);
        rep.setHeaders(headers);

        rep.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
            if (event.getChangeCount() > 0) {
                for (SyncEventListener listener : mListeners) {
                    listener.databaseChanged(database);
                }
            }
            for (SyncEventListener listener : mListeners) {
                listener.onProgress((
                        ((float) event.getCompletedChangeCount() / ((float) event.getChangeCount() + 1)
                                + (float) index) / (float) DATABASES.length));
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


