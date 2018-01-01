package io.compactd.player.helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
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
    private final Context mContext;

    public void stopMedia () {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.stopMedia();
            }
        });
    }

    public void destroyMedia () {
        if (serviceBound) {
            mediaPlayer.destroy();
            mContext.unbindService(mServiceConnection);
            mediaPlayer = null;
            serviceBound = false;
        }
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
        mContext = context.getApplicationContext();
    }

    private void bindService(Context context) {
        if (isServiceBound()) return;

        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: " + componentName + "; " + iBinder);
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
                mediaPlayer = binder.getService();

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
        context.getApplicationContext().startService(intent);
        context.getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    private void waitUntilReady(final ConnectionCallback cb, int maxWait) {
        if (isServiceBound() && mediaPlayer != null) {
            cb.onReady();
            return;
        }
        synchronized (MusicPlayerRemote.class) {
            mCallbacks.add(cb);
            if (!isServiceBound()) {
                bindService(mContext);
            }
        }
        if (maxWait > 0) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (mCallbacks) {
                        if (mCallbacks.contains(cb)) {
                            mCallbacks.remove(cb);
                        }

                    }
                }
            }, maxWait);
        }
    }

    public void waitUntilReady (ConnectionCallback cb) {
        waitUntilReady(cb, 0);
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
        if (mediaPlayer == null) return null;
        return mediaPlayer.getCurrentTrack();
    }

    public void pauseMedia () {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.pauseMedia();
            }
        }, 50);
    }

    public void playMedia () {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.playMedia();
            }
        }, 150);

    }

    public boolean isPlaying () {
        if (mediaPlayer == null) return false;

        return mediaPlayer.isPlaying();
    }

    public int getProgress () {
        return mediaPlayer != null ? mediaPlayer.getProgress() : 0;
    }

    public int getDuration () {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void skipTracks (final int pos) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.skipTracks(pos);
            }
        }, 100);
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

    public void insert (final List<CompactdTrack> queue) {
        waitUntilReady(new ConnectionCallback() {
            @Override
            public void onReady() {
                mediaPlayer.insert(queue);
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
        }, 500);
    }
}
