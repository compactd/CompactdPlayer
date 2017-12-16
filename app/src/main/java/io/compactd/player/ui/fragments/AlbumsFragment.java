package io.compactd.player.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.adapter.AlbumsAdapter;
import io.compactd.player.adapter.ModelAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link AlbumsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumsFragment extends Fragment {

    @BindView(R.id.albums_list)
    RecyclerView albumRecyclerView;
    private AlbumsAdapter adapter;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AlbumsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumsFragment newInstance() {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        ButterKnife.bind(this, view);

        GridLayoutManager layout = new GridLayoutManager(getContext(), 3);
        albumRecyclerView.setLayoutManager(layout);

        adapter = new AlbumsAdapter(getActivity(), ModelAdapter.LayoutType.GridItem);
        albumRecyclerView.setAdapter(adapter);
        try {
            List<CompactdAlbum> albums = CompactdAlbum.findAll(
                    CompactdManager.getInstance(getContext()), CompactdModel.FindMode.OnlyIds);
            adapter.swapItems(albums);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return view;
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
