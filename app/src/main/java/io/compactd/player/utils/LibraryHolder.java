package io.compactd.player.utils;

import java.util.List;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdTrack;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class LibraryHolder {
    private final List<CompactdArtist> artists;
    private final List<CompactdAlbum> albums;
    private final List<CompactdTrack> tracks;

    public LibraryHolder(List<CompactdArtist> artists, List<CompactdAlbum> albums, List<CompactdTrack> tracks) {
        this.artists = artists;
        this.albums = albums;
        this.tracks = tracks;
    }

    public List<CompactdArtist> getArtists() {
        return artists;
    }

    public List<CompactdAlbum> getAlbums() {
        return albums;
    }

    public List<CompactdTrack> getTracks() {
        return tracks;
    }
}
