package io.compactd.client.models;

import android.util.SparseArray;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by vinz243 on 30/10/2017.
 */

public class CompactdArtist extends CompactdModel {
    public static final String DATABASE_NAME = "artists";
    private static final String TAG = "CompactdArtist";

    private static Database sDatabase;

    private static SparseArray<CompactdArtist> cache = new SparseArray<>();

    private String mName;

    public CompactdArtist(Manager manager, String id) {
        super(manager, id);
    }

    CompactdArtist(CompactdArtist artist) {
        super(artist);
        mName = artist.getName();
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        if (map == null) return;
        mName = (String) map.get("name");
        mState = ModelState.Prefetched;
    }

    @Override
    public void fetch() throws CouchbaseLiteException {
        if (getState() == ModelState.Fetched) return;
        Database db = this.mManager.getDatabase(DATABASE_NAME);
        Document doc = db.getDocument(mId);
        fromMap(doc.getProperties());
        mState = ModelState.Fetched;
    }

    public String getName() {
        return mName;
    }


    public List<CompactdAlbum> getAlbums () throws CouchbaseLiteException {
        return getAlbums(FindMode.Prefetch);
    }

    public List<CompactdAlbum> getAlbums (FindMode mode) throws CouchbaseLiteException {
        return CompactdAlbum.findAll(mManager, getId(), mode);
    }

    public int getAlbumCount () {
        try {
            return getAlbums(FindMode.OnlyIds).size();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int getTrackCount () {
        try {
            return CompactdTrack.findAll(mManager, getId(), FindMode.OnlyIds).size();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<CompactdTrack> getTracks (FindMode mode) throws CouchbaseLiteException {
        return CompactdTrack.findAll(mManager, getId(), mode);
    }
    @Override
    public Map<String, String> getURIProps () {
        Map<String, String> props = new HashMap<>();
        String[] splat = getId().split("/");
        props.put("mName", splat[1]);
        return props;
    }

    public InputStream getImage (ArtworkSize size) {
        CompactdArtwork art = new CompactdArtwork(mManager, getId());
        return art.getImage(size);
    }

    public String getArtworkURI (URL base, int size) {
        String url = base.toString();

        url = url + "/api/aquarelle/" + getURIProps().get("mName") + "?s=" + size;

        return url;
    }

    public static List<CompactdArtist> findAll (Manager manager, FindMode mode) throws CouchbaseLiteException {
        return findAll(manager, "library/", mode);
    }

    public static List<CompactdArtist> findAll (Manager manager, String key, FindMode mode) throws CouchbaseLiteException {
        Database db = manager.getDatabase(DATABASE_NAME);
        Query query = db.createAllDocumentsQuery();
        query.setStartKey(key);
        query.setEndKey(key + LAST_CHARACTER);
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        query.setPrefetch(mode == FindMode.Prefetch);

        List<CompactdArtist> artists = new ArrayList<>();

        QueryEnumerator result = query.run();

        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdArtist artist = new CompactdArtist(manager, row.getDocumentId());

            if (mode == FindMode.Fetch) {
                artist.fetch();
            } else if (mode == FindMode.Prefetch) {
                artist.fromMap(row.getDocumentProperties());
            }

            artists.add(artist);
        }
        return artists;
    }

    @Nullable
    public static CompactdArtist findById (Manager manager, int id, boolean fetch)  {
        CompactdArtist cached = cache.get(id);
        if (cached != null) {
            if (fetch && cached.getState() != ModelState.Fetched) {
                try {
                    cached.fetch();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
            return new CompactdArtist(cached);
        }
        
        List<CompactdArtist> artists = null;
        
        try {
            artists = findAll(manager, FindMode.OnlyIds);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
        for (CompactdArtist artist : artists) {
            if (artist.getId().hashCode() == id) {
                if (fetch) {
                    try {
                        artist.fetch();
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                cache.append(id, new CompactdArtist(artist));
                return artist;
            }
        }
        return null;
    }

    @Nullable
    public static CompactdArtist findById (Manager manager, String id, boolean fetch) {
        if (fetch) {
            CompactdArtist artist = new CompactdArtist(manager, id);
            try {
                artist.fetch();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return null;
            }
            return artist;
        } else {
            return findById(manager, id.hashCode(), false);
        }
    }

}
