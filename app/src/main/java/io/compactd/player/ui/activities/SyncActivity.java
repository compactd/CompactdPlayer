package io.compactd.player.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codekidlabs.storagechooser.StorageChooser;
import com.couchbase.lite.CouchbaseLiteException;

import java.text.DecimalFormat;
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
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.client.models.SyncOptions;
import io.compactd.player.utils.FormatUtil;
import io.compactd.player.utils.PreferenceUtil;

public class SyncActivity extends SlidingMusicActivity implements View.OnClickListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        mOptions = new SyncOptions();

        unbinder = ButterKnife.bind(this);
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

    private void updateSizeStatus() {
        fab.hide();
        sizeText.setText(R.string.size_view_loader);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final long size = CompactdTrack.computeRequiredStorage(CompactdManager.getInstance(SyncActivity.this), mOptions);
                    StatFs stat = new StatFs(mOptions.getDestination());
                    final long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
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

        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        for (final CompactdTrack track : items) {
            if (!track.isAvailableOffline()) {
                executor.execute(new Runnable() {
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
            executor.shutdownNow();
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
                snackbar.setText("Syncing tracks ("+executor.getCompletedTaskCount() + "/"+executor.getTaskCount()+")");
                if (executor.getTaskCount() == executor.getCompletedTaskCount()) {
                    snackbar.dismiss();
                    fab.show();
                    timer.cancel();
                    timer.purge();
                    noMediaSwitch.setEnabled(true);
                    presetLayout.setEnabled(true);
                    destinationLayout.setEnabled(true);
                }
                }
            });
            }
        }, 250, 1000);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        finish();
    }
}
