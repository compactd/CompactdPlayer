package io.compactd.player.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.couchbase.lite.CouchbaseLiteException;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.adapter.PlaylistItemAdapter;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helper.MusicPlayerRemote;
import io.compactd.player.service.MediaPlayerService;
import io.compactd.player.ui.activities.SlidingMusicActivity;
import io.compactd.player.ui.views.WidthFitSquareLayout;

public class PlayerFragment extends Fragment implements MediaPlayerService.MediaListener, MediaPlayerService.PlaybackListener, Toolbar.OnMenuItemClickListener {

    private static final String TAG = PlayerFragment.class.getSimpleName();
    @BindView(R.id.player_toolbar)
    Toolbar toolbar;

    @BindView(R.id.cover_view)
    ImageView coverView;

    @BindView(R.id.layout)
    LinearLayout layout;

    @BindView(R.id.player_play_pause_fab)
    FloatingActionButton playPauseFab;

    @BindView(R.id.player_prev_button)
    ImageButton prevButton;

    @BindView(R.id.player_next_button)
    ImageButton nextButton;

    @BindView(R.id.player_repeat_button)
    ImageButton repeatButton;

    @BindView(R.id.player_shuffle_button)
    ImageButton shuffleButton;

    @BindView(R.id.player_progress_slider)
    SeekBar progressSlider;

    @BindView(R.id.player_song_total_time)
    TextView songTotalTime;

    @BindView(R.id.player_song_current_progress)
    TextView songCurrentProgress;

    @BindView(R.id.player_footer_frame)
    LinearLayout playerFooterFrame;

    @BindView(R.id.player_sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    @BindView(R.id.cover_layout)
    WidthFitSquareLayout widthFitSquareLayout;

    @BindView(R.id.current_image)
    ImageView currentImage;

    @BindView(R.id.current_text)
    TextView currentText;

    @BindView(R.id.current_title)
    TextView currentTitle;

    @BindView(R.id.player_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.background_view)
    View backgroundView;

    private Unbinder unbinder;
    private boolean monitorPlayback = false;
    private Runnable progressRunnable;
    private Handler handler;
    private PlaylistItemAdapter tracksAdapter;

    public PlayerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = statusBarHeight;
        }

        toolbar.inflateMenu(R.menu.menu_player);

        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SlidingMusicActivity) getActivity()).onBackPressed();
            }
        });

        prevButton.setColorFilter(Color.BLACK);
        playPauseFab.setColorFilter(Color.BLACK);
        playPauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(getContext());
            if (remote.isPlaying()) {
                remote.pauseMedia();
            } else {
                remote.playMedia();
            }
            }
        });
        nextButton.setColorFilter(Color.BLACK);


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerRemote.getInstance(getContext()).skipToNext();
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerRemote.getInstance(getContext()).rewind();
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(getContext());
                remote.setShuffling(!remote.isShuffling());
                shuffleButton.setSelected(remote.isShuffling());
            }
        });

        progressSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(getContext());

                if (remote.isPlaying() && fromUser)  {
                    remote.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setupPanelHeight();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        tracksAdapter = new PlaylistItemAdapter(getContext());
        recyclerView.setAdapter(tracksAdapter);

        return rootView;
    }

    private void setupPanelHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int topMargin = getResources().getDimensionPixelSize(resourceId);

        final int availablePanelHeight = slidingUpPanelLayout.getHeight() - layout.getHeight() + topMargin;
        final int minPanelHeight = getResources().getDimensionPixelSize(R.dimen.min_player_panel_height) + topMargin;
        if (availablePanelHeight < minPanelHeight) {
            widthFitSquareLayout.getLayoutParams().height = widthFitSquareLayout.getHeight() - (minPanelHeight - availablePanelHeight);
            widthFitSquareLayout.forceSquare(false);
        }
        slidingUpPanelLayout.setPanelHeight(Math.max(minPanelHeight, availablePanelHeight));

       // ((SlidingMusicActivity) getActivity()).setAntiDragView(fragment.slidingUpPanelLayout.findViewById(R.id.player_panel));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(getContext());
        remote.addMediaListener(this);
        remote.addPlaybackListener(this);

        if (remote.isPlaying()) {
            onMediaLoaded(remote.getCurrent());
            onQueueChanged(remote.getPlaylist(0));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MusicPlayerRemote remote = MusicPlayerRemote.getInstance(getContext());
        remote.removePlaybackListener(this);
        remote.removeMediaListener(this);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMediaLoaded(CompactdTrack track) {
        final String tag = "(" + track.getId() + ") ";
        Log.d(TAG, "onMediaLoaded: " + track + "; " + track.getAlbum());
        try {
            track.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        currentText.setText(track.getArtist().getName());
        currentTitle.setText(track.getName());
        currentImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        coverView.setImageBitmap(null);

        GlideApp.with(this)
            .asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
            .load(new MediaCover(track.getAlbum()))
            .dontAnimate()
            .into(new BitmapImageViewTarget(coverView) {
                @Override
                public void getSize(SizeReadyCallback cb) {
                    super.getSize(new SizeReadyCallback() {
                        @Override
                        public void onSizeReady(int width, int height) {}
                    });
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int pixels = displaymetrics.widthPixels;
                    cb.onSizeReady(pixels, pixels);
                }

                @Override
                protected void setResource(Bitmap resource) {
                    if (resource == null) return;
                    int color = Palette.from(resource).generate().getLightMutedColor(Color.WHITE);
                    playerFooterFrame.setBackgroundColor(color);
                    backgroundView.setBackgroundColor(color);

                    super.setResource(resource);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    Log.d(TAG, "onLoadFailed: ");
                }

                @Override
                public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    Log.d(TAG, "onResourceReady: " + tag + resource);
                }

                @Override
                protected Object clone() throws CloneNotSupportedException {
                    Log.d(TAG, "clone: " + tag);
                    return super.clone();
                }
            });

    }

    @Override
    public void onMediaEnded(CompactdTrack track, @Nullable CompactdTrack next) {

    }

    @Override
    public void onMediaSkipped(CompactdTrack skipped, @Nullable CompactdTrack next) {
        MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(getContext());
        tracksAdapter.swapDataSet(remote.getPlaylist(1));
    }

    @Override
    public void onMediaRewinded(CompactdTrack rewinded, CompactdTrack previous) {
        MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(getContext());
        tracksAdapter.swapDataSet(remote.getPlaylist(1));

    }

    @Override
    public void onQueueChanged(List<CompactdTrack> queue) {

        tracksAdapter.swapDataSet(queue.subList(1, queue.size()));
    }

    @Override
    public void onMediaReady(CompactdTrack track) {

    }

    @Override
    public void onPrepareMedia(CompactdTrack track) {

    }

    @Override
    public void onPlayerDestroyed() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onPlaybackPaused() {
        playPauseFab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    @Override
    public void onPlaybackProgress(CompactdTrack track, int position, int duration) {

        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
        songTotalTime.setText(format.format(duration));
        songCurrentProgress.setText(format.format(position));
        progressSlider.setMax(duration / 1000);
        progressSlider.setProgress(position / 1000);
    }

    @Override
    public void onPlaybackResumed() {
        playPauseFab.setImageResource(R.drawable.ic_pause_white_24dp);

    }

    @Override
    public void onPlaybackRewinded() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        MusicPlayerRemote remote = MusicPlayerRemote.getInstance(getContext());
        switch (item.getItemId()) {
            case R.id.menu_item_clear_queue:
                remote.clearQueue();
                return true;
            case R.id.menu_item_stop:
                remote.destroyMedia();
                return true;
        }
        return false;
    }
}
