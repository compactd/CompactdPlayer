package io.compactd.player.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.couchbase.lite.Manager;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.ui.views.ItemViewHolder;
import io.compactd.player.util.NavigationUtil;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * Created by Vincent on 01/01/2018.
 */

public class ItemSearchAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    public static final int ARTIST_LENGTH = 2;
    public static final int ALBUM_LENGTH = 3;
    private final List<String> items = new ArrayList<>();
    private String mQuery = "";
    private final List<ExtractedResult> results = new ArrayList<>();
    private Context mContext;

    public void setListener(View.OnClickListener mListener) {
        this.mListener = mListener;
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public ItemSearchAdapter(Context mContext) {
        this.mContext = mContext;
        setHasStableIds(true);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(root);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final ExtractedResult result = results.get(position);
        String id = result.getString();

        Manager manager = CompactdManager.getInstance(mContext);

        switch (id.split("\\/").length) {
            case ARTIST_LENGTH:
                final CompactdArtist artist = CompactdArtist.findById(manager, id, true);

                holder.title.setText(artist.getName());
                holder.text.setText(mContext.getResources().getString(R.string.artist_item_text, artist.getAlbumCount()));
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavigationUtil.goToArtist((Activity) mContext, artist);
                        mListener.onClick(v);
                    }
                });

                loadImage(holder.image, new MediaCover(artist));
                break;
            case ALBUM_LENGTH:
                final CompactdAlbum album = CompactdAlbum.findById(manager, id, true);
                holder.title.setText(album.getName());
                holder.text.setText(album.getArtist().getName());
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavigationUtil.goToAlbum((Activity) mContext, album);
                        mListener.onClick(v);
                    }
                });
                loadImage(holder.image, new MediaCover(album));
                break;

        }

        holder.overflowImage.setVisibility(View.GONE);
    }

    private void loadImage(ImageView image, MediaCover mediaCover) {
        GlideApp.with(mContext).load(mediaCover).into(image);
    }


    public void setItems (List<CompactdModel> items) {
        this.items.clear();
        this.items.addAll(Collections2.transform(items, new Function<CompactdModel, String>() {
            @Nullable
            @Override
            public String apply(@Nullable CompactdModel input) {
                return input.getId();
            }
        }));
        updateResults();
    }

    public void setQuery (String query) {
        mQuery = query;
        updateResults();
    }

    private void updateResults() {
        results.clear();

        List<ExtractedResult> extractedResults = FuzzySearch.extractSorted(mQuery, items, 30);
        results.addAll(extractedResults.subList(0, Math.min(5, extractedResults.size())));

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    @Override
    public long getItemId(int position) {
        return results.get(position).getString().hashCode();
    }
}
