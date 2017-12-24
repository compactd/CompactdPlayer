package io.compactd.player.ui.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.player.R;

/**
 * Created by Vincent on 24/12/2017.
 */

public class PlaylistItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title)
    public TextView title;

    @BindView(R.id.frame)
    public FrameLayout layout;

    @BindView(R.id.text)
    public TextView text;

    @BindView(R.id.playlist_position)
    public TextView playlistPosition;

    public PlaylistItemViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }
}
