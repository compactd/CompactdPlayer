package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;

import io.compactd.client.CompactdClient;
import io.compactd.client.models.CompactdTrack;
import io.compactd.client.models.SyncOptions;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.utils.NavigationUtils;
import io.compactd.player.utils.PreferenceUtil;

/**
 * Created by vinz243 on 15/12/2017.
 */

public class TracksAdapter extends ModelAdapter<CompactdTrack> {
    public TracksAdapter(Context context, LayoutType layoutType) {
        super(context, layoutType);
    }

    @Override
    protected MediaCover getMediaCover(CompactdTrack item) {
        return new MediaCover(item.getAlbum());
    }

    @Override
    protected String getText(CompactdTrack item) {
        return item.getArtist().getName();
    }

    @Override
    protected String getTitle(CompactdTrack item) {
        return item.getName();
    }

    @Override
    protected PopupMenu inflateMenu(View view, CompactdTrack model) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.menu_track);
        return popupMenu;
    }

    @Override
    protected boolean onMenuOptionSelected(MenuItem item, CompactdTrack model) {
        switch (item.getItemId()) {
            case R.id.action_goto_album:
                NavigationUtils.goToAlbum((Activity) context, model.getAlbum());
                return true;
            case R.id.action_goto_artist:
                NavigationUtils.goToArtist((Activity) context, model.getArtist());
                return true;
            case R.id.action_play_after:
                MusicPlayerRemote.getInstance(context).insert(Collections.singletonList(model));
                return true;
        }
        return false;
    }

    @Override
    protected void onItemSelected(CompactdTrack current, int position, ItemViewHolder holder) {
        super.onItemSelected(current, position, holder);
        if (isTrackAvailable(position)) {
            MusicPlayerRemote.getInstance(context)
                    .openQueue(items.subList(position, items.size() - 1), 0, true);
        }
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (!isTrackAvailable(position)) {
            holder.layout.setAlpha(0.5f);
        }
    }

    private boolean isTrackAvailable(int position) {
        if (!CompactdClient.getInstance().isOffline()) {
            return true;
        }
        CompactdTrack track = items.get(position);

        SyncOptions opts = new SyncOptions();
        opts.setDestination(PreferenceUtil.getInstance(context).getSyncDestination());

        track.setStorageOptions(opts);

        return track.isAvailableOffline();
    }
}
