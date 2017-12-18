package io.compactd.player.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import io.compactd.client.models.CompactdTrack;
import io.compactd.player.service.MediaPlayerService;

/**
 * Created by vinz243 on 17/12/2017.
 */

public class MusicPlayerRemote {
    private static MusicPlayerRemote sInstance;
    public MediaPlayerService mediaPlayer;
    private boolean serviceBound = false;
    private List<ConnectionCallback> mCallbacks = new ArrayList<>();

    private interface ConnectionCallback {
        void onReady ();
    }

    private MusicPlayerRemote(Context context) {
        bindService(context);
    }

    private void bindService(Context context) {
        ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
                mediaPlayer = binder.getService();
                serviceBound = true;
                for (int i = 0; i < mCallbacks.size(); i++) {
                    mCallbacks.remove(i).onReady();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mediaPlayer = null;
                serviceBound = false;
            }
        };

        Intent intent = new Intent(context, MediaPlayerService.class);
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void waitUntilReady(ConnectionCallback cb) {
        if (isServiceBound()) {
            cb.onReady();
            return;
        }
        mCallbacks.add(cb);
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

    public CompactdTrack getCurrent () {
        if (isServiceBound()) {
            return mediaPlayer.getCurrentTrack();
        }
        return null;
    }

    public int getProgress () {
        return isServiceBound() ? mediaPlayer.getProgress() : 0;
    }

    public int getDuration () {
        return isServiceBound() ? mediaPlayer.getDuration() : 0;
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
}
