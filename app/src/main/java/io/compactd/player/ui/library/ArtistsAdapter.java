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

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.GlideRequest;
import io.compactd.player.glide.GlideRequests;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.utils.ImageUtils;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistViewHolder> implements ListPreloader.PreloadModelProvider {
    private static final String TAG = ArtistsAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    private final GlideRequest<Bitmap> fullRequest;

    ArtistsAdapter(Context context) {
        this.mContext = context;
        this.artists = new ArrayList<>();
        this.mInflater = LayoutInflater.from(context);
        this.fullRequest = GlideApp.with(context).asBitmap();
    }

    private final Context mContext;

    public List<CompactdArtist> getArtists() {
        return artists;
    }

    void swapArtists (List<CompactdArtist> artists) {
        this.artists.clear();
        this.artists.addAll(artists);
        notifyDataSetChanged();
    }

    private final List<CompactdArtist> artists;

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_artist_item, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onViewRecycled(ArtistViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d(TAG, "onViewRecycled: ");
        GlideApp.with(mContext).clear(holder.artistImageView);
        holder.artistImageView.setImageDrawable(null);
    }

    @Override
    public void onBindViewHolder(final ArtistViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        final CompactdArtist current = artists.get(position);

        // holder.setIsRecyclable(false);
        holder.bindArtist(artists.get(position));

        holder.artistImageView.setImageDrawable(null);
        try {
            current.fetch();
            holder.artistNameText.setText(current.getName());
            fullRequest.load(new MediaCover(current))
                    .fallback(ImageUtils.getFallback(mContext))
                    .into(new ImageViewTarget<Bitmap>(holder.artistImageView) {
                        @Override
                        protected void setResource(@Nullable Bitmap resource) {
                            if (resource == null) {
                                // holder.setIsRecyclable(true);
                                return;
                            }

                            holder.artistImageView.setImageBitmap(resource);
                            int color = Palette.from(resource).generate().getMutedColor(0xFFFFFF);
                            holder.artistBackground.setBackgroundColor(color);

                            // holder.setIsRecyclable(true);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            // holder.setIsRecyclable(true);
                        }
                    });
            holder.artistSub.setText(current.getAlbumCount() + " albums");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            holder.artistNameText.setText(R.string.unknown_artist_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getItemId(int position) {
        return artists.get(position).getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return this.artists.size();
    }

    @NonNull
    @Override
    public List getPreloadItems(int position) {
        return this.artists.subList(position, position + 1);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(Object item) {
        MediaCover cover = (MediaCover) item;
        return fullRequest.clone().thumbnail(0.2f).load(cover);
    }
}
