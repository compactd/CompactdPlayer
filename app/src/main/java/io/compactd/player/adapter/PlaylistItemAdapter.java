package io.compactd.player.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.helper.MusicPlayerRemote;
import io.compactd.player.ui.views.PlaylistItemViewHolder;

/**
 * Created by vinz243 on 15/12/2017.
 */

public class PlaylistItemAdapter extends RecyclerView.Adapter<PlaylistItemViewHolder>  {
    private final List<CompactdTrack> playlist = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    public PlaylistItemAdapter(Context context) {
        context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.playlist_item, parent, false);
        return new PlaylistItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistItemViewHolder holder, final int position) {
        final CompactdTrack track = playlist.get(position);
        try {
            track.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        holder.text.setText(track.getArtist().getName());
        holder.title.setText(track.getName());
        holder.playlistPosition.setText(String.valueOf(position));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerRemote.getInstance(context).skipTracks(position + 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    public void swapDataSet (List<CompactdTrack> queue) {
        playlist.clear();
        playlist.addAll(queue);
        notifyDataSetChanged();
    }
}
