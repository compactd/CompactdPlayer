package io.compactd.player.ui.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codekidlabs.storagechooser.StorageChooser;
import com.couchbase.lite.CouchbaseLiteException;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.CompactdManager;
import io.compactd.client.CompactdPreset;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.client.models.SyncOptions;
import io.compactd.player.util.FormatUtil;
import io.compactd.player.util.PreferenceUtil;

public class SyncActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String SYNC_CHANNEL_ID = "sync_channel";
    public static int NOTIFICATION_ID = 0x5de;

    @BindView(R.id.sync_fab)
    FloatingActionButton fab;

    @BindView(R.id.destination_text)
    TextView destinationText;

    @BindView(R.id.destination_layout)
    FrameLayout destinationLayout;

    @BindView(R.id.nomedia_switch)
    Switch noMediaSwitch;

    @BindView(R.id.preset_layout)
    LinearLayout presetLayout;

    @BindView(R.id.preset_text)
    TextView presetText;

    @BindView(R.id.size_view)
    TextView sizeText;

    @BindView(R.id.main_layout)
    CoordinatorLayout mainLayout;

    private Unbinder unbinder;
    private SyncOptions mOptions;
    private NotificationManager mNotificationManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private boolean mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        mOptions = new SyncOptions();

        unbinder = ButterKnife.bind(this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        setTitle("Sync");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        noMediaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mOptions.setNoMedia(b);
            }
        });

        String preset = PreferenceUtil.getInstance(SyncActivity.this).getSyncPreset();
        CompactdPreset compactdPreset = CompactdPreset.from(preset);
        mOptions.setPreset(compactdPreset);
        presetText.setText(getResources().getText(compactdPreset.getDesc()));

        presetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<String> names = CompactdPreset.names(getResources());
                new MaterialDialog.Builder(SyncActivity.this)
                        .title(R.string.preset_dialog_title)
                        .items(names)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                CompactdPreset preset = CompactdPreset.from(getResources(), text.toString());
                                presetText.setText(preset.getDesc());
                                mOptions.setPreset(preset);
                                updateSizeStatus();
                            }
                        })
                        .show();
            }
        });

        String dest = PreferenceUtil.getInstance(SyncActivity.this).getSyncDestination();
        destinationText.setText(dest);
        mOptions.setDestination(dest);

        destinationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StorageChooser chooser = new StorageChooser.Builder()
                        .withActivity(SyncActivity.this)
                        .withFragmentManager(getFragmentManager())
                        .shouldResumeSession(true)
                        .withMemoryBar(true)
                        .setType(StorageChooser.DIRECTORY_CHOOSER).build();

                chooser.show();

                chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                    @Override
                    public void onSelect(String path) {
                        path = path + "/Music";
                        destinationText.setText(path);
                        mOptions.setDestination(path);
                        PreferenceUtil.getInstance(SyncActivity.this).setSyncDestination(path);
                    }
                });
                updateSizeStatus();
            }
        });

        fab.setOnClickListener(this);

        updateSizeStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressWarnings("deprecation")
    private long getAvailableBlocks(StatFs statFs) {
        long availableBlocks;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            availableBlocks = statFs.getAvailableBlocksLong();
        } else {
            availableBlocks = statFs.getAvailableBlocks();
        }

        return availableBlocks;
    }

    private void updateSizeStatus() {
        fab.hide();
        sizeText.setText(R.string.size_view_loader);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final long size = CompactdTrack.computeRequiredStorage(CompactdManager.getInstance(SyncActivity.this), mOptions);
                    StatFs stat = new StatFs(mOptions.getDestination());
                    final long bytesAvailable = (long)stat.getBlockSize() * getAvailableBlocks(stat);
                    final String req = FormatUtil.humanReadableByteCount(size, true);
                    final String left = FormatUtil.humanReadableByteCount(bytesAvailable, true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        sizeText.setText(getString(R.string.size_view_text, req, left));

                        if (size < bytesAvailable) {
                            fab.show();
                            sizeText.setError(null);
                        } else {
                            sizeText.setError(getString(R.string.nospace_left));
                        }

                        }
                    });
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();

                }

            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onClick(View view) {
        fab.hide();
        noMediaSwitch.setEnabled(false);
        presetLayout.setEnabled(false);
        destinationLayout.setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel();
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SYNC_CHANNEL_ID);
        builder.setContentTitle(getString(R.string.sync_notification_title));
        builder.setContentText(getString(mOptions.getPreset().getDesc()));
        builder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        builder.setProgress(100, 0, true);

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

        final Snackbar snackbar = Snackbar.make(mainLayout, R.string.sync_in_progress, Snackbar.LENGTH_INDEFINITE);

        snackbar.show();

        final Handler handler = new Handler();
        Collection<CompactdTrack> items = null;
        try {
            items = CompactdTrack.getSyncList(CompactdManager.getInstance(this), mOptions);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }

        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        for (final CompactdTrack track : items) {
            if (!track.isAvailableOffline()) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        track.setStorageOptions(mOptions);
                        track.sync();
                    }
                });
            }
        }

        final Timer timer = new Timer();

        snackbar.setAction("Cancel", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                timer.purge();
                threadPoolExecutor.shutdownNow();
                snackbar.dismiss();
                fab.show();
                noMediaSwitch.setEnabled(true);
                presetLayout.setEnabled(true);
                destinationLayout.setEnabled(true);
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder.setProgress((int) threadPoolExecutor.getTaskCount(),
                            (int) threadPoolExecutor.getCompletedTaskCount(), false);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());

                    snackbar.setText("Syncing tracks ("+ threadPoolExecutor.getCompletedTaskCount() + "/"+ threadPoolExecutor.getTaskCount()+")");

                    if (threadPoolExecutor.getTaskCount() == threadPoolExecutor.getCompletedTaskCount()) {

                        snackbar.dismiss();
                        fab.show();
                        timer.cancel();
                        timer.purge();

                        noMediaSwitch.setEnabled(true);
                        presetLayout.setEnabled(true);
                        destinationLayout.setEnabled(true);
                        mNotificationManager.cancel(NOTIFICATION_ID);

                        if (mBackground) {
                            finish();
                        }
                    }
                }
            });
            }
        }, 250, 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(SYNC_CHANNEL_ID,
                getString(R.string.sync_channel_name), NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onBackPressed() {
        if (threadPoolExecutor == null || threadPoolExecutor.isTerminated()) {
            finish();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBackground = false;
    }
}
