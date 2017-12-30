package io.compactd.player.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.adapter.ArtistsAdapter;
import io.compactd.player.adapter.ModelAdapter;
import io.compactd.player.adapter.TracksAdapter;

/**
 * Created by vinz243 on 16/12/2017.
 */

public class TracksFragment extends ModelFragment<CompactdTrack> {
    public static TracksFragment newInstance (@LayoutMode int layout, String startKye) {
        return ModelFragment.newInstance(TracksFragment.class,  layout, startKye);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        Manager manager = CompactdManager.getInstance(getContext());

        List<CompactdTrack> items = new ArrayList<>();

        try {
            items.addAll(CompactdTrack.findAll(manager, mStartkey, CompactdModel.FindMode.OnlyIds));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        bindModel(TracksAdapter.class, items);
        adapter.setTintBackground(false);


        return root;
    }

    public void setShowHidden (boolean flag) {
        TracksAdapter adapter = (TracksAdapter) this.adapter;
        adapter.setShowHidden(flag);
    }
}
