package io.compactd.player.ui.library;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.adapter.ArtistsAdapter;
import io.compactd.player.adapter.ModelAdapter;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.utils.ArtistsLoader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArtistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<CompactdArtist>> {

    public static final int LOADER_ID = 0x1A4;
    private static final String TAG = ArtistsFragment.class.getSimpleName();
    private OnFragmentInteractionListener mListener;
    private RecyclerView mArtistRecyclerView;
    private ArtistsAdapter mArtistsAdapter;

    public ArtistsFragment() {

        // Required empty public constructor
    }

    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        
        super.onResume();


        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        mArtistRecyclerView = rootView.findViewById(R.id.artists_recyclerview);

        LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mArtistRecyclerView.setLayoutManager(layoutManager);

        mArtistsAdapter = new ArtistsAdapter(getActivity(), ModelAdapter.LayoutType.GridItem);
        mArtistRecyclerView.setAdapter(mArtistsAdapter);

        FixedPreloadSizeProvider<CompactdArtist> coverSizeProvider = new FixedPreloadSizeProvider<>(
                64, 64);
        RecyclerViewPreloader<CompactdArtist> preloader = new RecyclerViewPreloader<>(
                GlideApp.with(this), mArtistsAdapter, coverSizeProvider, 2);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<CompactdArtist>> onCreateLoader(int id, Bundle args) {
        return new ArtistsLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<CompactdArtist>> loader, List<CompactdArtist> data) {
        mArtistsAdapter.swapItems(data);
    }

    @Override
    public void onLoaderReset(Loader<List<CompactdArtist>> loader) {
        mArtistsAdapter.swapItems(new ArrayList<CompactdArtist>());
    }
    
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
