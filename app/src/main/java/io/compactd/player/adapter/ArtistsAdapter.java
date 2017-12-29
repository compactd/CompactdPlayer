package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.utils.NavigationUtils;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistsAdapter extends ModelAdapter<CompactdArtist> {

    public ArtistsAdapter(Context context, LayoutType layoutType) {
        super(context, layoutType);
    }

    @Override
    protected String getCacheId(CompactdArtist item) {
        return item.getId();
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
    protected void onItemSelected(CompactdArtist artist, int position, ItemViewHolder holder) {
        NavigationUtils.goToArtist((Activity) context, artist,
                Pair.create(holder.image, context.getString(R.string.transition_artist_cover)));
    }
}
