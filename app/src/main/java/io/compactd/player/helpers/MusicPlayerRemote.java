package io.compactd.player.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdTrack;
import io.compactd.player.service.MediaPlayerService;

/**
 * Created by vinz243 on 17/12/2017.
 */

@SuppressWarnings("SynchronizeOnNonFinalField")
public class MusicPlayerRemote {
    public static final String TAG = MusicPlayerRemote.class.getSimpleName();
    private static MusicPlayerRemote sInstance;
    private MediaPlayerService mediaPlayer;
    private boolean serviceBound = false;
    private final List<ConnectionCallback> mCallbacks = new ArrayList<>();
    private ServiceConnection mServiceConnection;

    public void stopMedia () {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.stopMedia();
            }
        });
    }

    public void destroyMedia (Context context) {
        mediaPlayer.destroy();
        context.unbindService(mServiceConnection);
    }

    public List<CompactdTrack> getPlaylist(int skipItems) {
        List<CompactdTrack> queue = mediaPlayer.getQueue();
        return queue.subList(skipItems, queue.size());
    }

    public void skipToNext() {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.skipToNext();
            }
        });
    }

    public void rewind () {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.skipToPrevious();
            }
        });
    }

    private interface ConnectionCallback {
        void onReady ();
    }

    private MusicPlayerRemote(Context context) {
        bindService(context);
    }

    private void bindService(Context context) {
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: " + componentName + "; " + iBinder);
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
                mediaPlayer = binder.getService();
                serviceBound = true;

                synchronized (mCallbacks)  {

                    for (ConnectionCallback cb:
                            mCallbacks) {
                        cb.onReady();
                    }

                    mCallbacks.clear();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: " + componentName);
                mediaPlayer = null;
                serviceBound = false;
            }
        };
        Intent intent = new Intent(context, MediaPlayerService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void waitUntilReady(ConnectionCallback cb) {
        if (isServiceBound()) {
            cb.onReady();
            return;
        }
        synchronized (mCallbacks) {
            mCallbacks.add(cb);
        }
    }

    public void openQueue (final List<CompactdTrack> tracks, final int position, final boolean play) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.openQueue(tracks, position, play);
            }
        });
    }

    public boolean isServiceBound() {
        return serviceBound;
    }

    public static MusicPlayerRemote getInstance (Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlayerRemote(context);
        }

        return sInstance;
    }

    public void seekTo (int ms) {
        if (!isServiceBound()) return;
        synchronized (mediaPlayer)  {
            mediaPlayer.seekTo(ms);
        }
    }


    public CompactdTrack getCurrent () {
        synchronized (mediaPlayer)  {
            if (isServiceBound()) {
                return mediaPlayer.getCurrentTrack();
            }
            return null;

        }
    }

    public void pauseMedia () {
        if (!isServiceBound()) return;

        synchronized (mediaPlayer)  {
            mediaPlayer.pauseMedia();
        }
    }

    public void playMedia () {
        if (!isServiceBound()) return;

        synchronized (mediaPlayer)  {
            mediaPlayer.playMedia();
        }
    }

    public boolean isPlaying () {
        if (!isServiceBound()) return false;

        synchronized (mediaPlayer)  {
            return mediaPlayer.isPlaying();
        }
    }

    public int getProgress () {
        return isServiceBound() ? mediaPlayer.getProgress() : 0;
    }

    public int getDuration () {
        synchronized (mediaPlayer)  {
            return isServiceBound() ? mediaPlayer.getDuration() : 0;
        }

    }

    public void skipTracks (final int pos) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.skipTracks(pos);
            }
        });
    }

    public void addMediaListener (final MediaPlayerService.MediaListener l) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.addMediaListener(l);
            }
        });
    }

    public void removeMediaListener (final MediaPlayerService.MediaListener l) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.removeMediaListener(l);
            }
        });
    }

    public void addPlaybackListener (final MediaPlayerService.PlaybackListener l) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.addPlaybackListener(l);
            }
        });
    }

    public void removePlaybackListener (final MediaPlayerService.PlaybackListener l) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.removePlaybackListener(l);
            }
        });
    }

    public void setShuffling (final boolean flag) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.setShuffling(flag);
            }
        });
    }

    public boolean isShuffling () {
        return mediaPlayer != null && mediaPlayer.isShuffling();
    }

    public void clearQueue () {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.clearQueue();
            }
        });
    }
}
