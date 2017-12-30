package io.compactd.client.models;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.Map;

import io.compactd.client.CompactdClient;

/**
 * Created by vinz243 on 30/10/2017.
 */

public abstract class CompactdModel implements Cloneable{

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompactdModel that = (CompactdModel) o;

        if (!mId.equals(that.mId)) return false;
        return mState == that.mState;
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mState.hashCode();
        return result;
    }

    public enum ModelState {
        Barebone, Prefetched, Fetched, Deleted
    }

    public enum FindMode {
        OnlyIds, Prefetch, Fetch
    }

    static final char LAST_CHARACTER = '\uffff';
    static final String START_KEY = "library/";

    final String mId;
    final Manager mManager;
    ModelState mState;

    CompactdModel(Manager manager, String id) {
        mManager = manager;
        mId = id;
        mState = ModelState.Barebone;
    }

    /**
     * Copy constructor
     * @param model
     */
    CompactdModel(CompactdModel model) {
        mId = model.mId;
        mManager = model.mManager;
    }

    protected String getDatabaseName (String originalName) {
        return CompactdClient.getInstance().getPrefix() + originalName;
    }

    public Manager getManager () {
        return mManager;
    }

    public String getId() {
        return mId;
    }

    public void fromMap (Map<String, Object> map) {
        mState = ModelState.Prefetched;
    }
    public abstract void fetch () throws CouchbaseLiteException;

    public abstract Map<String, String> getURIProps();

    public ModelState getState() {
        return mState;
    }
}
