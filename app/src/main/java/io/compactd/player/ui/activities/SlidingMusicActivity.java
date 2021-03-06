package io.compactd.player.ui.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import butterknife.Unbinder;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.helper.MusicPlayerRemote;
import io.compactd.player.service.MediaPlayerService;
import io.compactd.player.util.PreferenceUtil;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by vinz243 on 18/12/2017.
 */

public abstract class SlidingMusicActivity extends AppCompatActivity implements
        SlidingUpPanelLayout.PanelSlideListener,
        MediaPlayerService.MediaListener, MediaPlayerService.PlaybackListener {

    public static final int DELAY_MILLIS = 1000;
    public static final String TAG = SlidingMusicActivity.class.getSimpleName();

    private Unbinder unbinder;
    private Runnable progressRunnable;
    private Handler handler;
    private boolean monitorPlayback;
    private ImageView playbackImage;
    private ImageView caretView;
    private SlidingUpPanelLayout panelLayout;
    private FrameLayout mSlidingContent;
    private MaterialProgressBar miniProgress;
    private TextView trackTitle;
    private FrameLayout layoutContainer;
    private FrameLayout fragment;
    private int statusBarColor;
    private FrameLayout statusBarDummy;
    private LinearLayout dragView;
    private ProgressBar mediaLoadProgress;
    private Switch localPlaybackSwitch;


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
        statusBarDummy  = findViewById(R.id.status_bar_dummy);
        dragView        = findViewById(R.id.dragView);

        localPlaybackSwitch = findViewById(R.id.local_playback_switch);

        mediaLoadProgress = findViewById(R.id.media_load_progress);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarDummy.getLayoutParams().height = getResources().getDimensionPixelSize(resourceId);
        }

        panelLayout.addPanelSlideListener(this);

        playbackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(SlidingMusicActivity.this);
                if (remote.isPlaying()) {
                    remote.pauseMedia();
                } else {
                    remote.playMedia();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        localPlaybackSwitch.setChecked(PreferenceUtil.getInstance(this).isLocalPlayback());
        localPlaybackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceUtil.getInstance(SlidingMusicActivity.this).setLocalPlayback(isChecked);
            }
        });

        TextView aboutText = findViewById(R.id.about_text);
        aboutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT)
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutAppName("CompactdPlayer")
                        .withAboutDescription(getString(R.string.about_desc))
                        .start(SlidingMusicActivity.this);
            }
        });
    }

    public void setShowStatusBarDummy (boolean b) {
        statusBarDummy.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void onPlaybackProgress (int progress, int duration) {

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
    protected void onPause() {
        super.onPause();

        MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(this);

        remote.removeMediaListener(this);
        remote.removePlaybackListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MusicPlayerRemote remote  = MusicPlayerRemote.getInstance(this);

        remote.addMediaListener(this);
        remote.addPlaybackListener(this);

        if (remote.isPlaying()) {
            showPlayer();
            onMediaLoaded(remote.getCurrent());
        } else {
            hidePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void hidePlayer ()  {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    void showPlayer () {
        SlidingUpPanelLayout.PanelState state = panelLayout.getPanelState();
        if (state == SlidingUpPanelLayout.PanelState.HIDDEN) {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        caretView.setRotation(180 * slideOffset);
        fadeView(dragView, slideOffset);

        // fadeView(fragment, 1 - slideOffset);
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
    public void onMediaLoaded(CompactdTrack track) {

        trackTitle.setText(track.getName());
        mediaLoadProgress.setVisibility(View.GONE);
        playbackImage.setVisibility(View.VISIBLE);
        showPlayer();
    }

    @Override
    public void onMediaEnded(CompactdTrack track, @Nullable CompactdTrack next) {

    }

    @Override
    public void onMediaSkipped(CompactdTrack skipped, @Nullable CompactdTrack next) {

    }

    @Override
    public void onMediaRewinded(CompactdTrack rewinded, CompactdTrack previous) {

    }

    @Override
    public void onQueueChanged(List<CompactdTrack> queue) {

    }

    @Override
    public void onMediaReady(CompactdTrack track) {

    }

    @Override
    public void onPlayerDestroyed() {
        hidePlayer();
    }

    @Override
    public void onPlaybackPaused() {
        playbackImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
    }

    @Override
    public void onPlaybackProgress(CompactdTrack track, int position, int duration) {
        miniProgress.setMax(duration);
        miniProgress.setProgress(position);
    }

    @Override
    public void onPlaybackResumed() {
        playbackImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_24dp));

    }

    @Override
    public void onPlaybackRewinded() {
        miniProgress.setProgress(0);
    }

    @Override
    public void onBackPressed() {
        if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)  {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
          finishAffinity();
        }
    }

    @Override
    public void onPrepareMedia(CompactdTrack track) {
        playbackImage.setVisibility(View.GONE);
        mediaLoadProgress.setVisibility(View.VISIBLE);
        trackTitle.setText(track.getName());
        showPlayer();
    }
}
