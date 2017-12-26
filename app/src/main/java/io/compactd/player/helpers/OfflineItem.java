package io.compactd.player.helpers;

import com.couchbase.lite.CouchbaseLiteException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.compactd.client.CompactdClient;
import io.compactd.client.CompactdRequest;
import io.compactd.client.models.CompactdTrack;

/**
 * Created by Vincent on 26/12/2017.
 */

public class OfflineItem {
    private CompactdTrack mTrack;
    private SyncOptions mOptions;

    public OfflineItem(CompactdTrack mTrack, SyncOptions mOptions) {
        this.mTrack = mTrack;
        this.mOptions = mOptions;
    }

    public String getDestination () {
        String dir = mOptions.getDestination();
        try {
            mTrack.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
        String artist = mTrack.getArtist().getName();
        String album = mTrack.getAlbum().getName();
        String track = mTrack.getName();
        String number = String.format(Locale.getDefault(),"%02d", mTrack.getNumber());

        return dir + (dir.endsWith("/") ? "" : "/") + artist + " - " + album + "/" + number + " - " + track + ".mp3";
    }

    public boolean isSynced () {
        return new File(getDestination()).exists();
    }

    public Runnable getSyncRunnable () {
        return new Runnable() {
            @Override
            public void run() {
                String uri = mTrack.getId().replaceAll("^library\\/", "");

                CompactdClient client = CompactdClient.getInstance();

                URL baseURL = client.getUrl();
                String endpoint = "/api/boombox/" + uri + "/" + mOptions.getPreset().getPreset();
                String token = client.getToken();

                CompactdRequest req = new CompactdRequest(baseURL, endpoint, token);

                try {
                    req.saveToDisk(getDestination());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static ThreadPoolExecutor sync (SyncOptions opts, List<CompactdTrack> items) {

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        for (CompactdTrack item : items) {
            OfflineItem offlineItem = new OfflineItem(item, opts);
            if (!offlineItem.isSynced()) {
                threadPoolExecutor.execute(offlineItem.getSyncRunnable());
            }
        }

        return threadPoolExecutor;
    }
}
