package io.compactd.player.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

/**
 * Created by vinz243 on 13/12/2017.
 */
@GlideModule
public class CompactdAppGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.prepend(MediaCover.class, InputStream.class, new ModelLoaderFactory<MediaCover, InputStream>() {
            @Override
            public ModelLoader<MediaCover, InputStream> build(MultiModelLoaderFactory multiFactory) {
                return new MediaCoverLoader();
            }

            @Override
            public void teardown() {

            }
        });
    }
}
