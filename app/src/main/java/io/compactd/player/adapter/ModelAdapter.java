package io.compactd.player.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.GlideRequest;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.ui.library.ItemViewHolder;
import io.compactd.player.utils.ImageUtils;

/**
 * Created by vinz243 on 15/12/2017.
 */

public abstract class ModelAdapter<M extends CompactdModel> extends RecyclerView.Adapter<ItemViewHolder> implements ListPreloader.PreloadModelProvider<M> {
    private final LayoutType layoutType;
    private final LayoutInflater inflater;
    private final GlideRequest<Bitmap> fullRequest;

    @NonNull
    @Override
    public List<M> getPreloadItems(int position) {
        return this.items.subList(position, position + 1);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(M item) {
        MediaCover cover = getMediaCover(item);
        return fullRequest.clone().thumbnail(0.2f).load(cover);
    }

    public enum LayoutType {
        GridItem, ListItem
    }

    private final Context context;
    private final List<M> items;

    ModelAdapter(Context context, LayoutType layoutType) {
        super();
        this.context = context;
        this.items   = new ArrayList<>();

        this.inflater    = LayoutInflater.from(context);
        this.layoutType  = layoutType;
        this.fullRequest = GlideApp.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).priority(Priority.LOW);
    }

    public void swapItems (List<M> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(getLayoutId(), parent, false);
        return new ItemViewHolder(view);
    }

    private int getLayoutId() {
        switch (layoutType) {
            case GridItem:
                return R.layout.grid_item;
            case ListItem:
                return R.layout.list_item;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        holder.image.setImageDrawable(null);
        holder.setIsRecyclable(false);
        final M current = items.get(position);


        try {
            current.fetch();

            holder.title.setText(getTitle(current));
            holder.text.setText(getText(current));

            loadImage(current, holder);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void loadImage(final M current, final ItemViewHolder holder) throws IOException {
        fullRequest.load(getMediaCover(current))
            .fallback(ImageUtils.getFallback(context))
            .into(new BitmapImageViewTarget(holder.image) {
                @Override
                protected void setResource(@Nullable Bitmap resource) {
                    super.setResource(resource);
                    if (resource == null) {
                        return;
                    }

                    int color = Palette.from(resource).generate().getMutedColor(0xFFFFFF);
                    holder.layout.setBackgroundColor(color);
                    holder.setIsRecyclable(true);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    super.onLoadCleared(placeholder);
                }
            });
    }

    protected abstract MediaCover getMediaCover(M item);

    protected abstract String getText(M item);

    protected abstract String getTitle(M item);

    @Override
    public int getItemCount() {
        return items.size();
    }
}