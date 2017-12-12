package io.compactd.player.utils;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistsLoader extends AsyncTaskLoader<List<CompactdArtist>> {
    private static final String TAG = ArtistsLoader.class.getSimpleName();
    public ArtistsLoader(Context context) {
        super(context);
        Log.d(TAG, "ArtistsLoader: ");
    }

    @Override
    public List<CompactdArtist> loadInBackground() {
        Log.d(TAG, "loadInBackground: ");
        try {
            return CompactdArtist.findAll(CompactdManager.getInstance(getContext()),
                    CompactdModel.FindMode.Fetch);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
