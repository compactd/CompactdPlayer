package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import java.util.List;

import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.GlideRequest;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.util.ImageUtil;

/**
 * Created by vinz243 on 15/12/2017.
 */

public abstract class ModelAdapter<M extends CompactdModel> extends RecyclerView.Adapter<ItemViewHolder> implements ListPreloader.PreloadModelProvider<M> {
    private static final String TAG = ModelAdapter.class.getSimpleName();

    protected final LayoutType layoutType;
    private final LayoutInflater inflater;
    private final GlideRequest<Bitmap> fullRequest;

    protected final Context context;
    protected final List<M> items;

    private boolean mTintBackground = true;
    private boolean mShowMenu = true;

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

    public boolean tintBackground() {
        return mTintBackground;
    }

    public void setTintBackground(boolean tintBackground) {
        this.mTintBackground = tintBackground;
    }

    public boolean showMenu () {
        return mShowMenu;
    }

    public void setShowMenu(boolean mShowMenu) {
        this.mShowMenu = mShowMenu;
    }


    public enum LayoutType {
        GridItem, ListItem
    }


    ModelAdapter(Context context, LayoutType layoutType) {
        super();
        this.context = context;
        this.items   = new ArrayList<>();

        this.inflater = LayoutInflater.from(context);
        this.layoutType  = layoutType;
        this.fullRequest = GlideApp.with(context).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.LOW);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
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
        final M model = items.get(position);
        holder.image.setImageDrawable(null);
        holder.setIsRecyclable(false);

        if (layoutType == LayoutType.GridItem) {
            int width = getGridImageSize();

            holder.image.getLayoutParams().width = width;
            holder.image.getLayoutParams().height = width;
        }

        holder.overflowImage.setVisibility(showMenu() ? View.VISIBLE : View.GONE);

        if (showMenu()) {
            holder.overflowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu menu = inflateMenu(view, model);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return onMenuOptionSelected(item, model, holder);
                        }
                    });
                    menu.show();
                }
            });
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
                onItemSelected(current, position, holder);
            }
        });

        updateStatus(holder, model);

    }

    private int getGridImageSize() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels / 3 - 2 * context.getResources().getDimensionPixelSize(R.dimen.grid_item_margin);
    }

    private int getListImageSize () {
        return context.getResources().getDimensionPixelSize(R.dimen.list_item_image_width);
    }

    public int getImageSize () {
        if (layoutType == LayoutType.ListItem) {
            return getListImageSize();
        }
        return getGridImageSize();
    }

    protected void onItemSelected(M current, int position, ItemViewHolder holder) {

    }

    protected void loadImage(final M current, final ItemViewHolder holder) throws IOException {

        fullRequest.load(getMediaCover(current))
            .fallback(ImageUtil.getFallback(context))
            .into(new BitmapImageViewTarget(holder.image) {
                @Override
                public void getSize(final SizeReadyCallback cb) {
                    int size = getImageSize();
                    cb.onSizeReady(size, size);

                    super.getSize(new SizeReadyCallback() {
                        @Override
                        public void onSizeReady(int width, int height) {
                        }
                    });
                }

                @Override
                protected void setResource(@Nullable Bitmap resource) {
                    super.setResource(resource);
                    if (resource == null) {
                        return;
                    }
                    if (mTintBackground) {
                        int color = Palette.from(resource).generate().getMutedColor(0xFFFFFF);
                        holder.layout.setBackgroundColor(color);
                    }

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

    protected abstract PopupMenu inflateMenu(View view, M model);

    protected abstract boolean onMenuOptionSelected(MenuItem item, M model, ItemViewHolder holder);

    protected int getStatusResource (M item) {
        return 0;
    }

    void updateStatus (ItemViewHolder holder, M item) {
        if (holder.statusImage != null) {
            int resource = getStatusResource(item);
            if (resource == 0) {
                holder.statusImage.setVisibility(View.GONE);
            } else {
                holder.statusImage.setVisibility(View.VISIBLE);
                holder.statusImage.setImageDrawable(ContextCompat.getDrawable(context, resource));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
