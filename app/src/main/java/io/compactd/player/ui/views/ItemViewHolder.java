package io.compactd.player.ui.views;


import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class ItemViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "AlbumViewHolder";

    private final Unbinder unbinder;

    @BindView(R.id.title)
    public TextView title;

    @BindView(R.id.frame)
    public FrameLayout layout;

    @BindView(R.id.text)
    public TextView text;

    @BindView(R.id.image)
    public ImageView image;

    private CompactdAlbum album;

    public ItemViewHolder(View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);

        // setIsRecyclable(false);
    }
}
