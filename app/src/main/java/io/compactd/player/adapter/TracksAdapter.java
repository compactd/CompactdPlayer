package io.compactd.player.adapter;

import android.content.Context;
import android.content.Intent;

import io.compactd.client.models.CompactdTrack;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.service.MediaPlayerService;

/**
 * Created by vinz243 on 15/12/2017.
 */

public class TracksAdapter extends ModelAdapter<CompactdTrack> {
    public TracksAdapter(Context context, LayoutType layoutType) {
        super(context, layoutType);
    }

    @Override
    protected String getCacheId(CompactdTrack item) {
        return item.getAlbum().getId();
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
    protected void onItemSelected(CompactdTrack current) {
        super.onItemSelected(current);
        Intent playerIntent = new Intent(context, MediaPlayerService.class);
        playerIntent.putExtra(MediaPlayerService.KEY_TRACK, current.getId());
        context.startService(playerIntent);

    }
}
