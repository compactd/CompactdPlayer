package io.compactd.player.ui.library;

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

import io.compactd.client.models.CompactdAlbum;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.GlideRequest;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.utils.ImageUtils;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumViewHolder> implements ListPreloader.PreloadModelProvider {
    private static final String TAG = AlbumsAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    private final GlideRequest<Bitmap> fullRequest;

    public AlbumsAdapter(Context context) {
        this.mContext = context;
        this.albums = new ArrayList<>();
        this.mInflater = LayoutInflater.from(context);
        this.fullRequest = GlideApp.with(context).asBitmap();
    }

    private final Context mContext;

    public List<CompactdAlbum> getAlbums() {
        return albums;
    }

    public void swapAlbums (List<CompactdAlbum> albums) {
        this.albums.clear();
        this.albums.addAll(albums);
        notifyDataSetChanged();
    }

    private final List<CompactdAlbum> albums;

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_card, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onViewRecycled(AlbumViewHolder holder) {
        super.onViewRecycled(holder);
        GlideApp.with(mContext).clear(holder.albumImageView);
        holder.albumImageView.setImageDrawable(null);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, int position) {
        final CompactdAlbum current = albums.get(position);

        // holder.setIsRecyclable(false);
        holder.bindAlbum(albums.get(position));

        holder.albumImageView.setImageDrawable(null);
        try {
            current.fetch();
            holder.albumNameText.setText(current.getName());
            fullRequest.load(new MediaCover(current))
                    .fallback(ImageUtils.getFallback(mContext))
                    .into(new ImageViewTarget<Bitmap>(holder.albumImageView) {
                        @Override
                        protected void setResource(@Nullable Bitmap resource) {
                            if (resource == null) {
                                // holder.setIsRecyclable(true);
                                return;
                            }

                            holder.albumImageView.setImageBitmap(resource);
                            int color = Palette.from(resource).generate().getMutedColor(0xFFFFFF);
                            holder.cardView.setBackgroundColor(color);

                            // holder.setIsRecyclable(true);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            // holder.setIsRecyclable(true);
                        }
                    });
            holder.albumSub.setText(current.getTrackCount() + " tracks");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            holder.albumNameText.setText(R.string.unknown_artist_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getItemId(int position) {
        return albums.get(position).getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return this.albums.size();
    }

    @NonNull
    @Override
    public List getPreloadItems(int position) {
        return this.albums.subList(position, position + 1);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(Object item) {
        MediaCover cover = (MediaCover) item;
        return fullRequest.clone().thumbnail(0.2f).load(cover);
    }
}
