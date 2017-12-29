package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.ui.activities.AlbumActivity;
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
    protected String getCacheId(CompactdAlbum item) {
        return item.getId();
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
    protected void onItemSelected(CompactdAlbum current, int position, ItemViewHolder holder) {
        super.onItemSelected(current, position, holder);

        NavigationUtils.goToAlbum((Activity) context, current, Pair.create(holder.image, context.getString(R.string.transition_album_cover)));
    }
}
