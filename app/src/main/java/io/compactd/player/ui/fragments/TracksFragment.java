package io.compactd.player.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.adapter.ModelAdapter;
import io.compactd.player.adapter.TracksAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TracksFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ID_START = "param1";

    // TODO: Rename and change types of parameters
    private String mIdStart;

    @BindView(R.id.tracks_recyclerview)
    RecyclerView recyclerView;


    public TracksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TracksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TracksFragment newInstance(String param1) {
        TracksFragment fragment = new TracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID_START, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIdStart = getArguments().getString(ARG_ID_START);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        TracksAdapter adapter = new TracksAdapter(getContext(), ModelAdapter.LayoutType.ListItem);
        recyclerView.setAdapter(adapter);

        try {
            Manager manager = CompactdManager.getInstance(getContext());
            List<CompactdTrack> tracks = CompactdTrack.findAll(manager,
                    CompactdModel.FindMode.OnlyIds);
            adapter.setTintBackground(false);
            adapter.swapItems(tracks);
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
