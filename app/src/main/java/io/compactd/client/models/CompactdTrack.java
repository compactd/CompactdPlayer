package io.compactd.client.models;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import io.compactd.client.CompactdClient;
import io.compactd.client.CompactdPreset;
import io.compactd.client.CompactdRequest;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdTrack extends CompactdModel {
    public static final String DATABASE_NAME = "tracks";
    private static final String TAG = "CompactdTrack";
    private String mName;
    private boolean mHidden;
    private CompactdArtist mArtist;
    private CompactdAlbum mAlbum;
    private double mDuration;
    private int mNumber;
    private String mStorageLocation;

    private static SparseArray<CompactdTrack> cache = new SparseArray<>();
    private CompactdPreset mStoragePreset;

    public CompactdTrack(Manager manager, String id) {
        super(manager, id);
    }

    public CompactdTrack(CompactdTrack other) {
        super(other);
        mName = other.getName();
        mHidden = other.isHidden();
        mArtist = new CompactdArtist(other.getArtist());
        mAlbum = new CompactdAlbum(other.getAlbum());
        mDuration = other.getDuration();
        mNumber = other.getNumber();
        mStorageLocation = other.getStorageLocation();
        mStoragePreset = other.getStoragePreset();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("name", getName());
        map.put("hidden", isHidden());
        map.put("artist", getArtist().getId());
        map.put("album", getAlbum().getId());
        map.put("duration", getDuration());
        map.put("number", getNumber());

        if (getStorageLocation() != null && !getStorageLocation().isEmpty()) {
            map.put("storage_loc", getStorageLocation());
            map.put("storage_preset", getStoragePreset());
        }

        return map;
    }
    @Override
    public void fromMap(Map<String, Object> map) {
        super.fromMap(map);

        mName     = (String) map.get("name");
        mHidden   = map.containsKey("hidden") && (boolean) map.get("hidden");

        Object artist = map.get("artist");

        if (artist instanceof CompactdArtist) {
            mArtist = (CompactdArtist) artist;
        } else {
            mArtist = new CompactdArtist(mManager, (String) artist);
        }

        Object album = map.get("album");

        if (album instanceof CompactdAlbum) {
            mAlbum = (CompactdAlbum) album;
        } else {
            mAlbum    = new CompactdAlbum(mManager, (String) album);
        }

        mDuration = getMillisDurationFromSeconds(map.get("duration"));
        mNumber   = (Integer) map.get("number");

        if (map.containsKey("storage_preset") && map.get("storage_preset") != null) {
            mStoragePreset = CompactdPreset.from(map.get("storage_preset").toString());
        }

        if (map.containsKey("storage_loc") && map.get("storage_loc") != null) {
            mStorageLocation = (String) map.get("storage_loc");
        }
    }

    private int getMillisDurationFromSeconds (Object ms) {
        if (ms instanceof  Double) {
            return (int) Math.floor((Double) ms);
        }
        if (ms instanceof  Integer) {
            return (int) Math.floor((Integer) ms);
        }
        return 0;
    }

    @Override
    public void fetch() throws CouchbaseLiteException {
        if (getState() == ModelState.Fetched) return;

        Database db = this.mManager.getDatabase(databaseName());
        Document doc = db.getDocument(mId);

        Map<String, Object> props = new HashMap<>();
        props.putAll(doc.getProperties());

        CompactdArtist artist = new CompactdArtist(mManager, (String) props.get("artist"));
        artist.fetch();

        props.put("artist", artist);

        CompactdAlbum album = new CompactdAlbum(mManager, (String) props.get("album"));
        album.fetch();

        props.put("album", album);

        fromMap(props);

        mState = ModelState.Fetched;
    }

    @Override
    public Map<String, String> getURIProps() {

        Map<String, String> props = new HashMap<>();
        String[] splat = getId().split("/");
        props.put("artist", splat[1]);
        props.put("album", splat[2]);
        props.put("number", splat[3]);
        props.put("name", splat[4]);
        return props;
    }

    public String getName() {
        return mName;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public void setHidden(boolean mHidden) {
        this.mHidden = mHidden;
    }

    public CompactdArtist getArtist() {
        return mArtist;
    }

    public CompactdAlbum getAlbum() {
        return mAlbum;
    }

    public double getDuration() {
        return mDuration;
    }

    public int getNumber() {
        return mNumber;
    }

    public static List<CompactdTrack> findAll (Manager manager, FindMode mode) throws CouchbaseLiteException {
        return findAll(manager, "library/", mode);
    }

    public static List<CompactdTrack> findAll (Manager manager, String key, FindMode mode) throws CouchbaseLiteException {
        Log.d(TAG, "findAll: key=" + key + ", mode="+ mode.name());
        Database db = manager.getDatabase(databaseName());
        Query query = db.createAllDocumentsQuery();
        query.setStartKey(key);
        query.setEndKey(key + "\uffff");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        query.setPrefetch(mode == FindMode.Prefetch);

        List<CompactdTrack> tracks = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdTrack album = new CompactdTrack(manager, row.getDocumentId());
            if (mode == FindMode.Fetch) {
                album.fetch();
            } else if (mode == FindMode.Prefetch){
                album.fromMap(row.getDocumentProperties());
            }
            tracks.add(album);
        }
        return tracks;
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, int id, boolean fetch) {

        CompactdTrack cached = cache.get(id);
        if (cached != null) {
            if (fetch) {
                try {
                    cached.fetch();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
            return new CompactdTrack(cached);
        }

        List<CompactdTrack> tracks = null;

        try {
            tracks = findAll(manager, FindMode.OnlyIds);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }

        for (CompactdTrack track : tracks) {
            if (track.getId().hashCode() == id) {
                Log.d(TAG, "findById: " + track.getId());
                if (true) {
                    try {
                        track.fetch();
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                Log.d(TAG, "findById: " + track);
                cache.put(id, new CompactdTrack(track));
                return track;
            }
        }
        return null;
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, String id, boolean fetch) {
        if (fetch) {
            CompactdTrack track = new CompactdTrack(manager, id);
            try {
                track.fetch();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return null;
            }
            return track;
        } else {
            return findById(manager, id.hashCode(), false);
        }
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, String id) {
        return findById(manager, id, true);
    }

    public String getStreamingURL(String preset) {
        try {
            this.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
        return "/api/boombox/" + getId().substring(8) + "/" + preset;
    }

    @Override
    public String toString() {
        return "CompactdTrack{" +
                "mId='" + mId + '\'' +
                ", mState=" + mState +
                ", mName='" + mName + '\'' +
                ", mHidden=" + mHidden +
                ", mArtist=" + mArtist +
                ", mAlbum=" + mAlbum +
                ", mDuration=" + mDuration +
                ", mNumber=" + mNumber +
                '}';
    }

    public static String databaseName() {
        return CompactdClient.getInstance().getPrefix() + DATABASE_NAME;
    }

    public String getStorageLocation() {
        return mStorageLocation;
    }

    private void setStorageLocation(String mStorageLocation) {
        this.mStorageLocation = mStorageLocation;
    }

    public void update () throws CouchbaseLiteException {
        Database database = mManager.getDatabase(databaseName());
        Document doc = database.getDocument(getId());
        Map<String, Object> properties = new HashMap<>();
        properties.putAll(doc.getProperties());
        properties.putAll(toMap());

        try {
            doc.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public CompactdPreset getStoragePreset() {
        return mStoragePreset;
    }

    private void setStoragePreset(CompactdPreset preset) {
        mStoragePreset = preset;
    }

    public void setStorageOptions (SyncOptions opts) {
        String dir = opts.getDestination();
        try {
            fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }
        String artist = getArtist().getName();
        String album  = getAlbum().getName();
        String track  = getName();
        String number = String.format(Locale.getDefault(),"%02d", getNumber());
        String base   = dir + (dir.endsWith("/") ? "" : "/");

        String dest = base + artist + " - " + album + "/" + number + " - " + track + ".mp3";

        setStorageLocation(dest);
        setStoragePreset(opts.getPreset());
    }

    public boolean isAvailableOffline () {
        return getStorageLocation() != null && new File(getStorageLocation()).exists();
    }

    public void sync () {
        Log.d(TAG, "sync: '" + getId() + "' to '" + getStorageLocation() + "'");
        if (isAvailableOffline()) {
            return;
        }

        String uri = getId().replaceAll("^library\\/", "");

        CompactdClient client = CompactdClient.getInstance();

        URL baseURL = client.getUrl();
        String endpoint = "/api/boombox/" + uri + "/" + getStoragePreset().getPreset();

        String token = client.getToken();

        CompactdRequest req = new CompactdRequest(baseURL, endpoint, token);

        String dest = getStorageLocation();

        try {
            req.saveToDisk(dest + ".tmp");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        File temp = new File(dest + ".tmp");

        if (temp.renameTo(new File(dest))) {
            try {
                update();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }
}
