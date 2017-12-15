package io.compactd.player.ui.library;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.ui.ArtistActivity;

/**
 * Created by vinz243 on 12/12/2017.
 */

    private static final String TAG = "ArtistViewHolder";
    final TextView artistNameText;

    public ImageView getArtistImage() {
        return artistImageView;
    }

    final ImageView artistImageView;
    final View artistBackground;
    final TextView artistSub;
    private CompactdArtist artist;

    ArtistViewHolder(View itemView) {
        super(itemView);
        artistNameText = itemView.findViewById(R.id.artist_name_text);
        artistImageView = itemView.findViewById(R.id.artistImage);
        artistBackground = itemView.findViewById(R.id.palette_color_container);
        artistSub = itemView.findViewById(R.id.artist_sub);
        // setIsRecyclable(false);
    }

    void bindArtist(CompactdArtist compactdArtist) {



    @Override

    }
}
