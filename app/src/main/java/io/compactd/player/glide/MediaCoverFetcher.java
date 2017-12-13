package io.compactd.player.glide;

import android.hardware.camera2.CaptureResult;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.InputStream;

import io.compactd.client.models.ArtworkSize;
import io.compactd.client.models.CompactdArtwork;

/**
 * Created by vinz243 on 13/12/2017.
 */

public class MediaCoverFetcher implements DataFetcher<InputStream> {
    private final MediaCover mediaCover;

    public MediaCoverFetcher(MediaCover mediaCover) {
        this.mediaCover = mediaCover;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        CompactdArtwork artwork = mediaCover.getArtwork();
        try {
            callback.onDataReady(artwork.getImage(ArtworkSize.LARGE));
        } catch (NullPointerException e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

}