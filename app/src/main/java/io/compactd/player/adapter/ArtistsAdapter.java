package io.compactd.player.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.adapter.ModelAdapter;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.GlideRequest;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.utils.ImageUtils;

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
}
