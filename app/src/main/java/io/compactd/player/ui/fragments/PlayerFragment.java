package io.compactd.player.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.service.MediaPlayerService;

public class PlayerFragment extends Fragment implements MediaPlayerService.MediaListener {

    @BindView(R.id.player_toolbar)
    Toolbar toolbar;

    @BindView(R.id.cover_view)
    ImageView coverView;

    @BindView(R.id.layout)
    LinearLayout layout;

    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, rootView);

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = statusBarHeight;
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        MusicPlayerRemote.getInstance(context).addMediaListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onLoad(CompactdTrack track) {
        GlideApp.with(this).load(new MediaCover(track.getAlbum())).into(coverView);
    }

    @Override
    public void onFinish(CompactdTrack track) {

    }

    @Override
    public void onPlay() {

    }

    @Override
    public void onRewind() {

    }

    @Override
    public void onSkip() {

    }

    @Override
    public void onProgress(int progress, CompactdTrack track) {

    }
}
