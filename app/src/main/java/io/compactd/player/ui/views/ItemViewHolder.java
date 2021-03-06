package io.compactd.player.ui.views;


import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.compactd.player.R;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title)
    public TextView title;

    @BindView(R.id.frame)
    public FrameLayout layout;

    @BindView(R.id.text)
    public TextView text;

    @BindView(R.id.image)
    public ImageView image;

    @BindView(R.id.overflow_icon)
    public ImageView overflowImage;

    @Nullable
    @BindView(R.id.status_image)
    public ImageView statusImage;

    public ItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        // setIsRecyclable(false);
    }
}
