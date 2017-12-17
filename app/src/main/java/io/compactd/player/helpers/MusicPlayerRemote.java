package io.compactd.player.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.List;

import io.compactd.client.models.CompactdTrack;
import io.compactd.player.service.MediaPlayerService;

/**
 * Created by vinz243 on 17/12/2017.
 */

public class MusicPlayerRemote {
    private static MusicPlayerRemote sInstance;
    private MediaPlayerService mediaPlayer;
    private boolean serviceBound = false;

    MusicPlayerRemote (Context context) {
        bindService(context);
    }

    private void bindService(Context context) {
        ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
                mediaPlayer = binder.getService();
                serviceBound = true;
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

    public void openQueue (List<CompactdTrack> tracks, int position, boolean play) {
        if (mediaPlayer != null) {
            mediaPlayer.openQueue(tracks, position, play);
        }
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
}
