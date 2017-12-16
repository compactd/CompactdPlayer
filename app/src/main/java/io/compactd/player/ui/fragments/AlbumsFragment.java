package io.compactd.player.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.adapter.AlbumsAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link AlbumsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumsFragment extends ModelFragment<CompactdAlbum> {


    public static AlbumsFragment newInstance(@LayoutMode int layout, String startKey) {
        return ModelFragment.newInstance(AlbumsFragment.class, layout, startKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        Manager manager = CompactdManager.getInstance(getContext());

        List<CompactdAlbum> items = new ArrayList<>();

        try {
            items.addAll(CompactdAlbum.findAll(manager, mStartkey, CompactdModel.FindMode.OnlyIds));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        bindModel(AlbumsAdapter.class, items);

        adapter.setTintBackground(true);

        return root;
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
