package io.compactd.player.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.service.MediaPlayerService;

import static io.compactd.player.ui.activities.SlidingMusicActivity.DELAY_MILLIS;

public class PlayerFragment extends Fragment implements MediaPlayerService.MediaListener {

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
    private Unbinder unbinder;
    private MusicPlayerRemote remote;
    private boolean monitorPlayback = false;
    private Runnable progressRunnable;
    private Handler handler;

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
        unbinder = ButterKnife.bind(this, rootView);

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = statusBarHeight;
        }
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        prevButton.setColorFilter(Color.BLACK);
        playPauseFab.setColorFilter(Color.BLACK);
        playPauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remote.mediaPlayer.isPlaying()) {
                    remote.mediaPlayer.pauseMedia();
                } else {
                    remote.mediaPlayer.playMedia();
                }
            }
        });
        nextButton.setColorFilter(Color.BLACK);

        progressSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (remote.mediaPlayer.isPlaying())  {
                    remote.mediaPlayer.seekTo(remote.mediaPlayer.getDuration() * i / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());

        handler = new Handler();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressSlider.setProgress(remote.getProgress() / remote.getDuration() * 100, true);
                } else {
                    progressSlider.setProgress(remote.getProgress() / remote.getDuration() * 100);
                }
                SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
                songTotalTime.setText(format.format(remote.getDuration()));
                songCurrentProgress.setText(format.format(remote.getProgress()));
                if (monitorPlayback) {
                    handler.postDelayed(progressRunnable, DELAY_MILLIS);
                }
            }
        };

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        remote = MusicPlayerRemote.getInstance(context);
        remote.addMediaListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onLoad(CompactdTrack track) {
        GlideApp.with(this)
            .asBitmap()
            .load(new MediaCover(track.getAlbum()))
            .into(new BitmapImageViewTarget(coverView) {
                @Override
                protected void setResource(Bitmap resource) {
                    if (resource == null) return;
                    int color = Palette.from(resource).generate().getLightMutedColor(Color.TRANSPARENT);
                    playerFooterFrame.setBackgroundColor(color);

                    super.setResource(resource);
                }
            });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onFinish(CompactdTrack track) {

    }

    @Override
    public void onPlay() {
        playPauseFab.setImageResource(R.drawable.ic_pause_white_24dp);
        if (!monitorPlayback) {
            monitorPlayback = true;
            handler.postDelayed(progressRunnable, DELAY_MILLIS);
        }
    }

    @Override
    public void onRewind() {

        if (!monitorPlayback) {
            monitorPlayback = true;
            handler.postDelayed(progressRunnable, DELAY_MILLIS);
        }
    }

    @Override
    public void onPlaybackPause() {
        monitorPlayback = false;
        playPauseFab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    @Override
    public void onSkip() {

        if (!monitorPlayback) {
            monitorPlayback = true;
            handler.postDelayed(progressRunnable, DELAY_MILLIS);
        }
    }

    @Override
    public void onProgress(int progress, CompactdTrack track) {

    }
}
