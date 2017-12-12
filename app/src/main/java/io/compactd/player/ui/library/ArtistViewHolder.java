package io.compactd.player.ui.library;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistViewHolder extends RecyclerView.ViewHolder {
    private final TextView mArtistNameText;
    private CompactdArtist artist;

    ArtistViewHolder(View itemView) {
        super(itemView);
        mArtistNameText = itemView.findViewById(R.id.artist_name_text);
    }

    void bindArtist(CompactdArtist compactdArtist) {
        this.artist = compactdArtist;
        mArtistNameText.setText(artist.getName());
    }
}
