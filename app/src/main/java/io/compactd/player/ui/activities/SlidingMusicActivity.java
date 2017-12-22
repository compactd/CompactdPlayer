package io.compactd.player.ui.activities;

import android.app.Activity;
import android.app.FragmentContainer;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.service.MediaPlayerService;

/**
 * Created by vinz243 on 18/12/2017.
 */

public abstract class SlidingMusicActivity extends AppCompatActivity implements
        SlidingUpPanelLayout.PanelSlideListener,
        MediaPlayerService.MediaListener {

    public static final int DELAY_MILLIS = 1000;
    public static final String TAG = SlidingMusicActivity.class.getSimpleName();

    private Unbinder unbinder;
    private MusicPlayerRemote remote;
    private Runnable progressRunnable;
    private Handler handler;
    private boolean monitorPlayback;
    private ImageView playbackImage;
    private ImageView caretView;
    private SlidingUpPanelLayout panelLayout;
    private FrameLayout mSlidingContent;
    private ProgressBar miniProgress;
    private TextView trackTitle;
    private FrameLayout layoutContainer;
    private FrameLayout fragment;
    private int statusBarColor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.sliding_music_layout);

        playbackImage   = findViewById(R.id.playback_image);
        caretView       = findViewById(R.id.caret_image);
        panelLayout     = findViewById(R.id.sliding_layout);
        mSlidingContent = findViewById(R.id.sliding_content);
        miniProgress    = findViewById(R.id.mini_progress);
        trackTitle      = findViewById(R.id.track_title);
        layoutContainer = findViewById(R.id.layout_container);
        fragment        = findViewById(R.id.player_container);

        panelLayout.addPanelSlideListener(this);

        remote = MusicPlayerRemote.getInstance(this);
        remote.addMediaListener(this);

        handler = new Handler();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                miniProgress.setMax(remote.getDuration());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    miniProgress.setProgress(remote.getProgress(), true);
                } else {
                    miniProgress.setProgress(remote.getProgress());
                }
                if (monitorPlayback) {
                    handler.postDelayed(progressRunnable, DELAY_MILLIS);
                }
            }
        };
        handler.postDelayed(progressRunnable, DELAY_MILLIS);

        playbackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remote.mediaPlayer.isPlaying()) {
                    remote.mediaPlayer.pauseMedia();
                } else {
                    remote.mediaPlayer.playMedia();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

    }

    @Override
    public void setContentView(int layoutResID) {
        if (layoutResID == R.layout.sliding_music_layout) {
            getWindow().setContentView(layoutResID);
        } else {
            getLayoutInflater().inflate(layoutResID, mSlidingContent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        caretView.setRotation(180 * slideOffset);
        fadeView(miniProgress, slideOffset);
        fadeView(playbackImage, slideOffset);
        fadeView(fragment, 1 - slideOffset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int c = getWindow().getStatusBarColor();
        }
    }

    public void fadeView (View view, float offset) {
        if (offset == 1)  {
            view.setVisibility(View.INVISIBLE);
        } else if (view.getVisibility() == View.INVISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        view.setAlpha(1 - offset);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

    }

    @Override
    public void onLoad(CompactdTrack track) {
        trackTitle.setText(track.getName());
        if (!monitorPlayback) {
            monitorPlayback = true;
            handler.postDelayed(progressRunnable, DELAY_MILLIS);
        }
    }

    @Override
    public void onFinish(CompactdTrack track) {
        if (monitorPlayback) {
            monitorPlayback = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        playbackImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        if (monitorPlayback) {
            monitorPlayback = false;
        }
    }

    @Override
    public void onPlay() {
        playbackImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_24dp));
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
    public void onSkip() {
        if (!monitorPlayback) {
            monitorPlayback = true;
            handler.postDelayed(progressRunnable, DELAY_MILLIS);
        }
    }

    @Override
    public void onProgress(int progress, CompactdTrack track) {
        miniProgress.setMax(remote.getDuration());
        miniProgress.setProgress(progress);
    }
}
