package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.Collections;
import java.util.List;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.utils.NavigationUtils;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class AlbumsAdapter extends ModelAdapter<CompactdAlbum> {
    public AlbumsAdapter(Context context, LayoutType layoutType) {
        super(context, layoutType);
    }

    @Override
    protected MediaCover getMediaCover(CompactdAlbum item) {
        return new MediaCover(item);
    }

    @Override
    protected String getText(CompactdAlbum item) {
        return item.getArtist().getName();
    }

    @Override
    protected String getTitle(CompactdAlbum item) {
        return item.getName();
    }

    @Override
    protected PopupMenu inflateMenu(View view, CompactdAlbum album) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.menu_album);
        popupMenu.getMenu().findItem(R.id.menu_sync_offline).setChecked(!album.isExcludedFromSync());
        return popupMenu;
    }

    @Override
    protected boolean onMenuOptionSelected(MenuItem item, CompactdAlbum album, ItemViewHolder holder) {
        switch (item.getItemId()) {
            case R.id.menu_goto_artist:
                NavigationUtils.goToArtist((Activity) context, album.getArtist());
                return true;
            case R.id.menu_play_shuffle:
                try {
                    List<CompactdTrack> queue = album.getTracks(CompactdModel.FindMode.OnlyIds);
                    Collections.shuffle(queue);
                    MusicPlayerRemote.getInstance(context).openQueue(queue, 0, true);
                    MusicPlayerRemote.getInstance(context).setShuffling(true);
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            case R.id.menu_play_next:
                try {
                    List<CompactdTrack> queue = album.getTracks(CompactdModel.FindMode.OnlyIds);
                    MusicPlayerRemote.getInstance(context).insert(queue);
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            case R.id.menu_sync_offline:
                item.setChecked(!item.isChecked());
                album.setExcludedFromSync(!item.isChecked());
                album.update();
                updateStatus(holder, album);
                return true;
        }
        return false;
    }

    @Override
    protected void onItemSelected(CompactdAlbum current, int position, ItemViewHolder holder) {
        super.onItemSelected(current, position, holder);

        NavigationUtils.goToAlbum((Activity) context, current, Pair.create(holder.image, context.getString(R.string.transition_album_cover)));
    }

    @Override
    protected int getStatusResource(CompactdAlbum item) {
        return item.isExcludedFromSync() ? R.drawable.ic_sync_disabled_white_24dp : 0;
    }
}
