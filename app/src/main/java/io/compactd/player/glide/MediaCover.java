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

    @Override
    public String toString() {
        return "MediaCover{" +
                "artist=" + artist +
                ", album=" + album +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaCover that = (MediaCover) o;

        if (artist != null && !artist.equals(that.artist)) {
            return false;
        }

        if (album != null && !album.equals(that.album)) {
            return false;
        }

        return that.album == null && that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = artist != null ? artist.getId().hashCode() : 0;
        result = 31 * result + (album != null ? album.getId().hashCode() : 0);
        return result;
    }
}
