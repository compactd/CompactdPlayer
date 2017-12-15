package io.compactd.player.glide;

import android.support.annotation.NonNull;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdArtwork;

/**
 * Created by vinz243 on 13/12/2017.
 */

public class MediaCover {
    private CompactdArtist artist;
    private CompactdAlbum album;

    public MediaCover(@NonNull CompactdAlbum album) {
        this.album = album;
    }


    public MediaCover(@NonNull CompactdArtist artist) {
        this.artist = artist;
    }

    public CompactdArtwork getArtwork () {
        if (artist != null) {
            return new CompactdArtwork(artist.getManager(), artist.getId());
        }

        if (album != null) {
            return new CompactdArtwork(album.getManager(), album.getId());
        }
        return null;
    }
}
