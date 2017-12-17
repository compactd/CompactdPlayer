package io.compactd.player.adapter;

import android.content.Context;
import android.content.Intent;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.ui.activities.ArtistActivity;

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
    protected void onItemSelected(CompactdArtist artist, int position) {
        Intent intent = new Intent(context, ArtistActivity.class);
        intent.putExtra(ArtistActivity.BUNDLE_ARTIST_KEY, new CompactdParcel<>(artist));
        context.startActivity(intent);
    }
}
