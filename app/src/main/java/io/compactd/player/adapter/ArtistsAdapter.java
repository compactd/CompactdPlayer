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

import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.utils.NavigationUtil;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistsAdapter extends ModelAdapter<CompactdArtist> {

    public ArtistsAdapter(Context context, LayoutType layoutType) {
        super(context, layoutType);
    }

    @Override
    protected MediaCover getMediaCover(CompactdArtist item) {
        return new MediaCover(item);
    }

    @Override
    protected String getText(CompactdArtist item) {
        return item.getAlbumCount() + " albums";
    }

    @Override
    protected String getTitle(CompactdArtist item) {
        return item.getName();
    }

    @Override
    protected PopupMenu inflateMenu(View view, CompactdArtist model) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.inflate(R.menu.menu_artist);
        return menu;
    }

    @Override
    protected boolean onMenuOptionSelected(MenuItem item, CompactdArtist model, ItemViewHolder holder) {
        switch (item.getItemId()) {
            case R.id.action_play_shuffled:
                try {
                    List<CompactdTrack> queue = model.getTracks(CompactdModel.FindMode.OnlyIds);
                    Collections.shuffle(queue);
                    MusicPlayerRemote.getInstance(context).openQueue(queue, 0, true);
                    MusicPlayerRemote.getInstance(context).setShuffling(true);
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            case R.id.action_play_next:
                try {
                    List<CompactdTrack> queue = model.getTracks(CompactdModel.FindMode.OnlyIds);
                    MusicPlayerRemote.getInstance(context).insert(queue);
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }


        }
        return false;
    }

    @Override
    protected void onItemSelected(CompactdArtist artist, int position, ItemViewHolder holder) {
        NavigationUtil.goToArtist((Activity) context, artist,
                Pair.create(holder.image, context.getString(R.string.transition_artist_cover)));
    }
}
