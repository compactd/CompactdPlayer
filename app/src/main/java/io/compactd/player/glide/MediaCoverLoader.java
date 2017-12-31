package io.compactd.player.glide;

import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * Created by vinz243 on 13/12/2017.
 */

public class MediaCoverLoader implements ModelLoader<MediaCover, InputStream> {
    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(MediaCover mediaCover, int width, int height, Options options) {
        return new LoadData<InputStream>(new ObjectKey(mediaCover.getArtwork().getId()), new MediaCoverFetcher(mediaCover));
    }

    @Override
    public boolean handles(MediaCover mediaCover) {
        return true;
    }
}
