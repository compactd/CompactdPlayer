package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.Collections;
import java.util.Objects;

import io.compactd.client.CompactdClient;
import io.compactd.client.models.CompactdTrack;
import io.compactd.client.models.SyncOptions;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helper.MusicPlayerRemote;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.util.NavigationUtil;
import io.compactd.player.util.PreferenceUtil;

/**
 * Created by vinz243 on 15/12/2017.
 */

public class TracksAdapter extends ModelAdapter<CompactdTrack> implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean mShowHidden = false;
    private boolean mLocalPlayback = false;

    public TracksAdapter(Context context, LayoutType layoutType) {
        super(context, layoutType);
        PreferenceUtil.getInstance(context).registerOnSharedPreferenceChangeListener(this);
        mLocalPlayback = PreferenceUtil.getInstance(context).isLocalPlayback();
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
        popupMenu.getMenu().findItem(R.id.action_set_hidden).setChecked(model.isHidden());
        return popupMenu;
    }

    @Override
    protected boolean onMenuOptionSelected(MenuItem item, CompactdTrack model, ItemViewHolder holder) {
        switch (item.getItemId()) {
            case R.id.action_goto_album:
                NavigationUtil.goToAlbum((Activity) context, model.getAlbum());
                return true;
            case R.id.action_goto_artist:
                NavigationUtil.goToArtist((Activity) context, model.getArtist());
                return true;
            case R.id.action_play_after:
                MusicPlayerRemote.getInstance(context).insert(Collections.singletonList(model));
                return true;
            case R.id.action_set_hidden:
                item.setChecked(!item.isChecked());
                model.setHidden(item.isChecked());
                try {
                    model.update();
                    notifyDataSetChanged();
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    return false;
                }
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
        if (items.get(position).isHidden()) {
            if (getShowHidden()) {
                holder.layout.setVisibility(View.VISIBLE);
                holder.layout.setAlpha(0.5f);
                if (layoutType == LayoutType.ListItem) {
                    holder.layout.getLayoutParams().height = getLayoutHeight();
                }
            } else {
                holder.layout.setVisibility(View.GONE);
                if (layoutType == LayoutType.ListItem) {
                    holder.layout.getLayoutParams().height = 0;
                }
            }
        } else {
            holder.layout.setVisibility(View.VISIBLE);
        }
    }

    private int getLayoutHeight() {
        if (layoutType == LayoutType.ListItem) {
            return context.getResources().getDimensionPixelSize(R.dimen.list_item_height);
        } else {
            return 0;
        }
    }

    private boolean isTrackAvailable(int position) {
        if (!CompactdClient.getInstance().isOffline() && !mLocalPlayback) {
            return true;
        }
        CompactdTrack track = new CompactdTrack(items.get(position));

        SyncOptions opts = new SyncOptions();
        opts.setDestination(PreferenceUtil.getInstance(context).getSyncDestination());

        track.setStorageOptions(opts);

        return track.isAvailableOffline();
    }

    public boolean getShowHidden() {
        return mShowHidden;
    }

    public void setShowHidden(boolean showHidden) {
        mShowHidden = showHidden;
        notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtil.LOCAL_PLAYBACK.equals(key)) {
            mLocalPlayback = PreferenceUtil.getInstance(context).isLocalPlayback();
            notifyDataSetChanged();
        }
    }
}
