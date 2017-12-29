package io.compactd.player.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.compactd.client.CompactdClient;
import io.compactd.client.models.ArtworkSize;
import io.compactd.client.models.CompactdArtwork;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;

/**
 * Created by vinz243 on 17/12/2017.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {
    public static final String KEY_TRACK = "track_id";
    private static final String CHANNEL_ID = "player_channel";

    private static final String PACKAGE_NAME = "io.compactd.player";
    public static final String ACTION_TOGGLE_PAUSE = PACKAGE_NAME + ".togglepause";
    public static final String ACTION_PLAY = PACKAGE_NAME + ".play";
    public static final String ACTION_PLAY_PLAYLIST = PACKAGE_NAME + ".play.playlist";
    public static final String ACTION_PAUSE = PACKAGE_NAME + ".pause";
    public static final String ACTION_STOP = PACKAGE_NAME + ".stop";
    public static final String ACTION_SKIP = PACKAGE_NAME + ".skip";
    public static final String ACTION_REWIND = PACKAGE_NAME + ".rewind";
    public static final String ACTION_QUIT = PACKAGE_NAME + ".quitservice";

    private static final int NOTIFICATION_ID = 420;
    
    private static final int INTENT_PAUSE = 0;
    private static final int INTENT_PLAY = 1;
    private static final int INTENT_REWIND = 3;
    private static final int INTENT_SKIP = 4;

    private static final String TAG = MediaPlayerService.class.getName();
    public static final int MIN_REWIND_DURATION = 4000;
    public static final int FINISH_EVENT_TRESHOLD = 120;
    public static final int UPDATE_PROGRESS_INTERVAL = 500;
    public static final int UPDATE_TICK = 1000;

    private MediaPlayer player;
    private List<CompactdTrack> playlist = new ArrayList<>();

    private MediaSessionCompat mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaControllerCompat.TransportControls transportControls;

    private final IBinder binder = new LocalBinder();
    private int resumePosition;
    private AudioManager audioManager;
    private Handler handler = new Handler();
    private boolean playerReady = false;
    private boolean shuffling;

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public List<CompactdTrack> getQueue() {
        ArrayList<CompactdTrack> tracks = new ArrayList<>();
        tracks.addAll(playlist.subList(position, playlist.size()));
        return tracks;
    }

    public boolean isShuffling() {
        return shuffling;
    }

    public void setShuffling(boolean shuffling) {
        this.shuffling = shuffling;
        if (shuffling) {
            CompactdTrack current = playlist.remove(position);
            Collections.shuffle(playlist);
            playlist.add(position, current);
            fireQueueChanged(playlist);
        }
    }

    public void clearQueue() {
        CompactdTrack current = getCurrentTrack();
        position = 0;
        playlist.clear();
        playlist.add(current);
        fireQueueChanged(playlist);
    }

    public abstract class AbsMediaListener implements MediaListener {

        @Override
        public void onMediaLoaded(CompactdTrack track) {

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
    }

    public interface MediaListener {
        /**
         * Called everytime a new media is loaded
         * @param track
         */
        void onMediaLoaded(CompactdTrack track);

        /**
         * Once a media has finished played and will pass to the next track if any
         * @param track the finished track
         * @param next the next track that is going to play, null if no playlist
         */
        void onMediaEnded(CompactdTrack track, @Nullable CompactdTrack next);

        /**
         * When a media is skipped
         * @param skipped the skipped track
         * @param next the next track on the playlist that is going to play if any
         */
        void onMediaSkipped (CompactdTrack skipped, @Nullable CompactdTrack next);

        /**
         * Called when a media is rewinded
         * @param rewinded the media rewinded
         * @param previous the previous media, which is going to play now
         */
        void onMediaRewinded (CompactdTrack rewinded, CompactdTrack previous);

        /**
         * Called whne the queu changed
         * @param queue the queue
         */
        void onQueueChanged (List<CompactdTrack> queue);

        void onMediaReady (CompactdTrack track);

        void onPrepareMedia (CompactdTrack track);

        void onPlayerDestroyed ();
    }

    public interface PlaybackListener {
        void onPlaybackPaused();
        void onPlaybackProgress(CompactdTrack track, int position, int duration);
        void onPlaybackResumed();
        void onPlaybackRewinded ();
    }

    private final List<MediaListener> mMediaListeners = new ArrayList<>();
    private final List<PlaybackListener> mPlaybackListeners = new ArrayList<>();

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };

    private int position;
    private NotificationChannel notificationChannel;

    public void addMediaListener (MediaListener listener)  {
        synchronized (mMediaListeners)  {
            mMediaListeners.add(listener);
        }
    }

    public void removeMediaListener (MediaListener listener) {
        synchronized (mMediaListeners) {
            mMediaListeners.remove(listener);
        }
    }

    public void addPlaybackListener (PlaybackListener listener) {
        synchronized (mPlaybackListeners)  {
            mPlaybackListeners.add(listener);
            if (!isMonitoringPlayback() && player.isPlaying()) {
                handler.postDelayed(updateProgress, UPDATE_TICK);
            }
        }
    }

    public void removePlaybackListener (PlaybackListener listener) {
        synchronized (mPlaybackListeners) {
            mPlaybackListeners.remove(listener);
        }
    }

    private boolean isMonitoringPlayback() {
        return !mPlaybackListeners.isEmpty();
    }


    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            final CompactdTrack current = playlist.get(position);
            if (player != null) {
                firePlaybackProgress(getCurrentTrack(), getProgress(), getDuration());
                if (isMonitoringPlayback() && player.isPlaying()) {
                    new Handler().postDelayed(updateProgress, UPDATE_PROGRESS_INTERVAL);
                }
            }
        }
    };

    private void firePlaybackProgress(CompactdTrack currentTrack, int progress, int duration) {
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onPlaybackProgress(currentTrack, progress, duration);
        }
    }

    private void firePlaybackPaused () {
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onPlaybackPaused();
        }
    }

    private void firePlaybackResumed () {
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onPlaybackResumed();
        }
    }

    private void firePlaybackRewind () {
        for (PlaybackListener listener : mPlaybackListeners) {
            listener.onPlaybackRewinded();
        }
    }

    private void fireMediaLoaded (CompactdTrack track) {
        Log.d(TAG, "fireMediaLoaded: " + track);
        new Throwable().printStackTrace();
        for (MediaListener listener : mMediaListeners) {
            listener.onMediaLoaded(track);
        }
    }

    private void fireMediaSkipped (CompactdTrack track, CompactdTrack next) {
        for (MediaListener listener : mMediaListeners) {
            listener.onMediaSkipped(track, next);
        }
    }

    private void fireMediaRewinded (CompactdTrack track, CompactdTrack prev) {
        for (MediaListener listener : mMediaListeners) {
            listener.onMediaRewinded(track, prev);
        }
    }

    private void fireQueueChanged (List<CompactdTrack> tracks) {
        List<CompactdTrack> readonly = Collections.unmodifiableList(tracks);
        for (MediaListener listener : mMediaListeners) {
            listener.onQueueChanged(readonly);
        }
    }

    private void firePrepareMedia (CompactdTrack track) {
        Log.d(TAG, "firePrepareMedia: " + track);

        new Throwable().printStackTrace();
        for (MediaListener listener : mMediaListeners) {
            listener.onPrepareMedia(track);
        }
    }

    private void fireMediaReady (CompactdTrack track) {
        for (MediaListener listener : mMediaListeners) {
            listener.onMediaReady(track);
        }
    }
    private void firePlayerDestroyed ()  {
        for (MediaListener listener : mMediaListeners) {
            listener.onPlayerDestroyed();
        }
    }

    enum PlaybackStatus {
        PLAYING, PAUSED
    }

    private void registerBecomingnNoisyReceiver () {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        registerReceiver(becomingNoisyReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!requestAudioFocus()) {
            stopSelf();
        }

        setupMediaPlayer();
        setupMediaSession();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel();
        }

        handleIncomingActions(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
            playMedia();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
            pauseMedia();
        } else if (actionString.equalsIgnoreCase(ACTION_SKIP)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_REWIND)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel() {
        if (notificationChannel == null) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            notificationChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.player_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
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
        if (player != null) return;

        player = new MediaPlayer();
        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnPreparedListener(this);
        player.reset();

        registerBecomingnNoisyReceiver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            player.setAudioAttributes(attributes);
        } else {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }


    }

    private void setupMediaSession() {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    public void skipToPrevious() {
        if (player.getCurrentPosition() > MIN_REWIND_DURATION) {
            player.seekTo(0);
            firePlaybackRewind();
            return;
        }
        CompactdTrack skipped = playlist.get(position);
        if (position == 0) {
            position = playlist.size() - 1;
        } else {
            position = position+ - 1;
        }
        updatePlaylist();
        fireMediaRewinded(skipped, playlist.get(position));
    }

    public void skipTracks (int n) {
        jumpTo(position + n);
    }

    private void jumpTo(int pos) {
        CompactdTrack current = getCurrentTrack();
        if (pos < playlist.size()) {
            position = pos;
            updatePlaylist();
            fireMediaSkipped(current, getCurrentTrack());
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        
    }

    public void skipToNext() {
        CompactdTrack skipped = playlist.get(position);
        if (position == playlist.size() - 1) {
            position = 0;
        } else {
            position = position + 1;
        }
        updatePlaylist();
        fireMediaSkipped(skipped, playlist.get(position));
    }

    private void buildNotification (PlaybackStatus status) {
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (status == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause_white_24dp;
            //create the pause action
            play_pauseAction = playbackAction(INTENT_PAUSE);
        } else if (status == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_arrow_black_24dp;
            //create the play action
            play_pauseAction = playbackAction(INTENT_PLAY);
        }

        Bitmap largeIcon = getArtwork(playlist.get(position));

        // Create a new Notification
        CompactdTrack activeAudio = playlist.get(position);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                // Attach our MediaSession token
                .setMediaSession(mediaSession.getSessionToken())
                // Show our playback controls in the compact notification view.
                .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_audiotrack_black_24dp)
                // Set Notification content information
                .setContentText(activeAudio.getArtist().getName())
                .setContentTitle(activeAudio.getName())
                .setContentInfo(activeAudio.getAlbum().getName())
                // Add playback actions
                .addAction(R.drawable.ic_skip_previous_white_24dp, "previous", playbackAction(INTENT_REWIND))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_skip_next_white_24dp, "next", playbackAction(INTENT_SKIP));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public void destroy () {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        firePlayerDestroyed();
        player.stop();
    }

    private PendingIntent playbackAction(int action) {
        Intent intent = new Intent(this, MediaPlayerService.class);

        switch (action) {
            case INTENT_PAUSE:
                intent.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, action, intent, 0);
            case INTENT_PLAY:
                intent.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, action, intent, 0);
            case INTENT_REWIND:
                intent.setAction(ACTION_REWIND);
                return PendingIntent.getService(this, action, intent, 0);
            case INTENT_SKIP:
                intent.setAction(ACTION_SKIP);
                return PendingIntent.getService(this, action, intent, 0);
        }
        return null;
    }

    public void openQueue(final List<CompactdTrack> tracks, final int startPosition, final boolean startPlaying) {
        CompactdTrack track = getCurrentTrack();

        playlist.clear();
        playlist.addAll(tracks);

        if (isShuffling()) {
            Collections.shuffle(playlist);
        }

        position = startPosition;

        fireQueueChanged(playlist);
        updatePlaylist();

        fireMediaSkipped(track, getCurrentTrack());
    }

    public void nextTrack () {
        CompactdTrack skipped = getCurrentTrack();
        position = position + 1;
        updatePlaylist();
        fireMediaSkipped(skipped, getCurrentTrack());
    }

    public CompactdTrack getCurrentTrack() {

        if (playlist.isEmpty()) return null;
        return playlist.get(position);
    }

    private void updatePlaylist() {
        if (playlist.isEmpty()) return;

        final CompactdTrack track = playlist.get(position);

        try {
            track.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        firePrepareMedia(track);

        player.setOnCompletionListener(null);
        playerReady = false;
        player.reset();

        setDataSource(track);
        player.prepareAsync();

        Bitmap bitmap = getArtwork(track);

        final MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist().getName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAlbum().getName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long) (track.getDuration() * 1000))
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, position + 1)
                .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, track.getAlbum().getYear())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
        mediaSession.setMetadata(builder.build());

        buildNotification(PlaybackStatus.PAUSED);

    }

    private Bitmap getArtwork(CompactdTrack track) {
        CompactdArtwork artwork = new CompactdArtwork(track.getManager(), track.getAlbum().getId());

        return BitmapFactory.decodeStream(artwork.getImage(ArtworkSize.LARGE));
    }

    private void setDataSource(CompactdTrack track) {

        CompactdClient client = CompactdClient.getInstance();
        if (client.isOffline()) {
            try {
                player.setDataSource(track.getStorageLocation());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String url = client.getUrl() + track.getStreamingURL("original");

        setDataSource(url);
    }

    private void setDataSource(String url) {
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
    }

    public void playMedia () {
        if (!player.isPlaying()) {
            if (!playerReady) {
                updatePlaylist();
            }
            player.start();
            firePlaybackResumed();
            buildNotification(PlaybackStatus.PLAYING);
            handler.postDelayed(updateProgress, UPDATE_TICK);
        }
    }

    public void stopMedia () {
        if (player != null && player.isPlaying()) {
            player.stop();
            buildNotification(PlaybackStatus.PAUSED);
        }
    }

    public void pauseMedia () {
        if (player != null && player.isPlaying()) {
            player.pause();
            resumePosition = player.getCurrentPosition();
            firePlaybackPaused();
            buildNotification(PlaybackStatus.PAUSED);
        }
    }

    void resumeMedia () {
        if (player != null && !player.isPlaying()) {
            player.seekTo(resumePosition);
            player.start();
            firePlaybackResumed();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.d(TAG, "onBufferingUpdate: " + mediaPlayer + "; " + i);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion: " + mediaPlayer);
        stopMedia();
        if (position >= playlist.size()) {
            stopSelf();

            firePlayerDestroyed();
            playerReady = false;
        } else {
            position = position + 1;
            updatePlaylist();
        }
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
        Log.d(TAG, "onPrepared: " + mediaPlayer);
        playerReady = true;
        player.setOnCompletionListener(this);
        fireMediaLoaded(getCurrentTrack());
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onSeekComplete: " + mediaPlayer);
        firePlaybackProgress(getCurrentTrack(), getProgress(), getDuration());
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player == null) setupMediaPlayer();
                else if (!player.isPlaying()) resumeMedia();
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (player.isPlaying()) stopMedia();
                player.release();
                player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying()) pauseMedia();
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


    public int getProgress () {
        if (playerReady) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration () {
        return player.getDuration();
    }

    public void seekTo (int ms)  {
        Log.d(TAG, "seekTo: " + ms + "/" + player.getDuration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            player.seekTo(ms, MediaPlayer.SEEK_CLOSEST);
        } else {
            player.seekTo(ms);

        }
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService () {
            return MediaPlayerService.this;
        }
    }
}
