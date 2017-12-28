package io.compactd.client.models;

import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Revision;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.compactd.client.CompactdClient;

/**
 * Created by vinz243 on 10/11/2017.
 */

public class CompactdArtwork extends CompactdModel {
    public static final String DATABASE_NAME = "artworks";
    public static final String TAG = "CompactArtwork";
    private String mOwner;

    public CompactdArtwork(Manager manager, String id) {
        super(manager, id.startsWith("artworks/") ? id : "artworks/" + id);
    }

    public String databaseName() {
        return CompactdClient.getInstance().getPrefix() + DATABASE_NAME;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        super.fromMap(map);
        mOwner = (String) map.get("owner");
    }

    @Override
    public void fetch() throws CouchbaseLiteException {
        if (getState() == ModelState.Fetched) return;
        Database db = this.mManager.getDatabase(databaseName());
        Document doc = db.getDocument(mId);
        fromMap(doc.getProperties());
        mState = ModelState.Fetched;
    }

    @Override
    public Map<String, String> getURIProps() {
        Map<String, String> props = new HashMap<>();
        props.put("owner", getId().substring(9));
        return props;
    }

    public String getOwner() {
        return mOwner;
    }

    public InputStream getImage (ArtworkSize size) {
        try {
            Database db = this.mManager.getDatabase(databaseName());
            Document doc = db.getDocument(getId());
            Revision rev = doc.getCurrentRevision();
            if (rev != null) {
                Attachment att = rev.getAttachment(size.getSize());
                if (att != null) {
                    return att.getContent();
                }
            }
        } catch (CouchbaseLiteException ignored) {
        }
        try {
            return ((AndroidContext) mManager.getContext()).getWrappedContext().getResources().getAssets().open("album_fallback.jpg");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
