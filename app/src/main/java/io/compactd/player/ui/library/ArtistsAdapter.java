package io.compactd.player.ui.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistViewHolder> {
    private static final String TAG = ArtistsAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;

    ArtistsAdapter(Context context) {
        this.mContext = context;
        this.artists = new ArrayList<>();
        this.mInflater = LayoutInflater.from(context);
    }

    private final Context mContext;

    public List<CompactdArtist> getArtists() {
        return artists;
    }

    void swapArtists (List<CompactdArtist> artists) {
        this.artists.clear();
        this.artists.addAll(artists);
        notifyDataSetChanged();
    }

    private final List<CompactdArtist> artists;

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_artist_item, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        holder.bindArtist(artists.get(position));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return this.artists.size();
    }
}
