package io.compactd.player.ui.library;


import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.player.R;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.utils.ImageUtils;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = "AlbumViewHolder";

    private final Unbinder unbinder;

    @BindView(R.id.title)
    TextView albumNameText;

    @BindView(R.id.card_view)
    CardView cardView;

    @BindView(R.id.text)
    TextView albumSub;

    @BindView(R.id.image)
    ImageView albumImageView;

    private CompactdAlbum album;

    AlbumViewHolder(View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);

        try {
            albumImageView.setImageDrawable(ImageUtils.getFallback(itemView.getContext()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        itemView.setOnClickListener(this);
        // setIsRecyclable(false);
    }

    void bindAlbum(CompactdAlbum compactdAlbum) {
        album = compactdAlbum;
    }

    @Override
    public void onClick(View view) {

//        Intent intent = new Intent(view.getContext(), AlbumActivity.class);
//        intent.putExtra(AlbumActivity.BUNDLE_ARTIST_KEY, new CompactdParcel<>(album));
//        view.getContext().startActivity(intent);
    }


}
