package io.compactd.player.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.adapter.AlbumsAdapter;
import io.compactd.player.adapter.ArtistsAdapter;
import io.compactd.player.adapter.ModelAdapter;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.utils.ArtistsLoader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ArtistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistsFragment extends ModelFragment<CompactdArtist> {

    public static ArtistsFragment newInstance(@LayoutMode int layout, String startKey) {
        return ModelFragment.newInstance(ArtistsFragment.class, layout, startKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        Manager manager = CompactdManager.getInstance(getContext());

        List<CompactdArtist> items = new ArrayList<>();

        try {
            items.addAll(CompactdArtist.findAll(manager, mStartkey, CompactdModel.FindMode.OnlyIds));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        bindModel(ArtistsAdapter.class, items);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
