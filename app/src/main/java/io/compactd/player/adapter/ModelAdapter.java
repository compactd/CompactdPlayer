package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.GlideRequest;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.utils.ImageUtils;

/**
 * Created by vinz243 on 15/12/2017.
 */

public abstract class ModelAdapter<M extends CompactdModel> extends RecyclerView.Adapter<ItemViewHolder> implements ListPreloader.PreloadModelProvider<M> {
    private static final int MAX_CACHE_SIZE = 25;
    private static final String TAG = ModelAdapter.class.getSimpleName();
    private final LayoutType layoutType;
    private final LayoutInflater inflater;
    private final GlideRequest<Bitmap> fullRequest;
    private boolean tintBackground = true;
    private LinkedHashMap<String, Bitmap> cache = new LinkedHashMap<>();

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

    public boolean isTintBackground() {
        return tintBackground;
    }

    public void setTintBackground(boolean tintBackground) {
        this.tintBackground = tintBackground;
    }

    public enum LayoutType {
        GridItem, ListItem
    }

    protected final Context context;
    protected final List<M> items;

    ModelAdapter(Context context, LayoutType layoutType) {
        super();
        this.context = context;
        this.items   = new ArrayList<>();

        this.inflater    = LayoutInflater.from(context);
        this.layoutType  = layoutType;
        this.fullRequest = GlideApp.with(context).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.LOW);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        cache.clear();
    }

    public void swapItems (List<M> items) {
        this.items.clear();
        this.items.addAll(items);

        cache.clear();
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

        if (layoutType == LayoutType.GridItem) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int devicewidth = displaymetrics.widthPixels / 3;

            holder.image.getLayoutParams().width = devicewidth;
            holder.image.getLayoutParams().height = devicewidth;
        }


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


        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemSelected(current, position);
            }
        });

    }

    protected void onItemSelected(M current, int position) {

    }

    protected void loadImage(final M current, final ItemViewHolder holder) throws IOException {
        String cacheId = getCacheId(current);
        if (cache.containsKey(cacheId)) {
            Bitmap resource = cache.get(cacheId);
            if (tintBackground) {
                int color = Palette.from(resource).generate().getMutedColor(0xFFFFFF);
                holder.layout.setBackgroundColor(color);
            }

            holder.image.setImageBitmap(resource);

            return;
        }

        fullRequest.load(getMediaCover(current))
            .fallback(ImageUtils.getFallback(context))
            .into(new BitmapImageViewTarget(holder.image) {
                @Override
                public void getSize(final SizeReadyCallback cb) {

                    super.getSize(new SizeReadyCallback() {
                        @Override
                        public void onSizeReady(int width, int height) {
                            DisplayMetrics displaymetrics = new DisplayMetrics();
                            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

                            if (layoutType == LayoutType.GridItem) {
                                int devicewidth = displaymetrics.widthPixels / 3;

                                cb.onSizeReady(devicewidth, devicewidth);
                            } else {
                                int dp = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 64, displaymetrics );
                                cb.onSizeReady(dp, dp);
                            }
                        }
                    });


                }

                @Override
                protected void setResource(@Nullable Bitmap resource) {
                    super.setResource(resource);
                    if (resource == null) {
                        return;
                    }
                    if (tintBackground) {
                        int color = Palette.from(resource).generate().getMutedColor(0xFFFFFF);
                        holder.layout.setBackgroundColor(color);
                    }
                    cache.put(current.getId(), resource);
                    
                    verifyCache();
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    super.onLoadCleared(placeholder);
                }
            });
    }
    
    private void verifyCache() {
        if (cache.size() > MAX_CACHE_SIZE) {
            cache.remove(cache.entrySet().iterator().next().getKey());
        }
    }

    protected abstract String getCacheId(M item);

    protected abstract MediaCover getMediaCover(M item);

    protected abstract String getText(M item);

    protected abstract String getTitle(M item);

    @Override
    public int getItemCount() {
        return items.size();
    }
}
