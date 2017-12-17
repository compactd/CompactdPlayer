package io.compactd.player.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import io.compactd.client.CompactdClient;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdTrack;

import static io.compactd.client.CompactdClient.*;

/**
 * Created by vinz243 on 17/12/2017.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {
    public static final String KEY_TRACK = "track_id";
    private MediaPlayer player;
    private CompactdTrack track;

    private final IBinder binder = new Binder() {
      public MediaPlayerService getService () {
          return MediaPlayerService.this;
      }
    };
    private int resumePosition;
    private AudioManager audioManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String trackId = intent.getExtras().getString(KEY_TRACK);
        track = CompactdTrack.findById(CompactdManager.getInstance(getApplicationContext()), trackId);

        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (trackId != null && !trackId.isEmpty() && track != null) {
            setupMediaPlayer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (player != null) {
            stopMedia();
            player.release();
        }

        removeAudioFocus();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void setupMediaPlayer () {
        player = new MediaPlayer();
        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnPreparedListener(this);
        player.reset();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            player.setAudioAttributes(attributes);
        } else {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        String url = CompactdClient.getInstance().getUrl() + track.getStreamingURL("original");

        try {
            Method setDataSource = player.getClass().getMethod("setDataSource", String.class, Map.class);
            setDataSource.setAccessible(true);
            setDataSource.invoke(player, url, CompactdClient.getInstance().getHeaders());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        player.prepareAsync();
    }

    public void playMedia () {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    public void stopMedia () {
        if (player == null) return;
        if (player.isPlaying()) {
            player.stop();
        }
    }

    public void pauseMedia () {
        if (player.isPlaying()) {
            player.pause();
            resumePosition = player.getCurrentPosition();
        }
    }

    void resumeMedia () {
        if (!player.isPlaying()) {
            player.seekTo(resumePosition);
            player.start();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player == null) setupMediaPlayer();
                else if (!player.isPlaying()) player.start();
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (player.isPlaying()) player.stop();
                player.release();
                player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying()) player.pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }
    }
    private boolean requestAudioFocus () {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        int res = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        return res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus () {
        return audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


}
