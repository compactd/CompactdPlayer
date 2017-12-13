package io.compactd.player.ui.library;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;

import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.utils.ImageUtils;

/**
 * Created by vinz243 on 12/12/2017.
 */

public class ArtistViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "ArtistViewHolder";
    private final TextView mArtistNameText;
    private final ImageView mArtistImage;
    private final View mArtistBackground;
    private final TextView mArtistSub;
    private CompactdArtist artist;

    ArtistViewHolder(View itemView) {
        super(itemView);
        mArtistNameText = itemView.findViewById(R.id.artist_name_text);
        mArtistImage     = itemView.findViewById(R.id.artistImage);
        mArtistBackground = itemView.findViewById(R.id.palette_color_container);
        mArtistSub = itemView.findViewById(R.id.artist_sub);
    }

    void bindArtist(CompactdArtist compactdArtist) {

        this.artist = compactdArtist;

        try {
            this.artist.fetch();
            mArtistNameText.setText(artist.getName());
            GlideApp
                .with(itemView)
                .load(new MediaCover(artist))
                .fallback(ImageUtils.getFallback(itemView.getContext()))
                .into(new BaseTarget<Drawable>() {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (errorDrawable == null) try {
                            errorDrawable = ImageUtils.getFallback(itemView.getContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Bitmap image = ImageUtils.drawableToBitmap(errorDrawable);
                        mArtistImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {

                        Bitmap image = ImageUtils.drawableToBitmap(resource);
                        mArtistImage.setImageDrawable(resource);
                        int color = Palette.from(image).generate().getMutedColor(0xFFFFFF);
                        mArtistBackground.setBackgroundColor(color);
                    }

                    @Override
                    public void getSize(SizeReadyCallback cb) {
                        cb.onSizeReady(mArtistImage.getWidth(), mArtistImage.getHeight());
                    }

                    @Override
                    public void removeCallback(SizeReadyCallback cb) {

                    }
                });
            mArtistSub.setText(artist.getAlbumCount() + " albums");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            mArtistNameText.setText(R.string.unknown_artist_name);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
