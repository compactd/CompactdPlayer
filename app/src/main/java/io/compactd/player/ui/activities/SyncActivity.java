package io.compactd.player.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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

import org.w3c.dom.Text;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.CompactdManager;
import io.compactd.client.CompactdPreset;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.helpers.OfflineItem;
import io.compactd.player.helpers.SyncOptions;
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
            }
        });

        fab.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onClick(View view) {
        fab.hide();
        final Snackbar bar = Snackbar.make(mainLayout, R.string.sync_in_progress, Snackbar.LENGTH_INDEFINITE);

        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        final ProgressBar item = new ProgressBar(this);
        item.setIndeterminate(true);
        contentLay.addView(item,0);

        bar.show();

        final Handler handler = new Handler();
        List<CompactdTrack> all = null;
        try {
            all = CompactdTrack.findAll(CompactdManager.getInstance(this), CompactdModel.FindMode.OnlyIds);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        final ThreadPoolExecutor tpe = OfflineItem.sync(mOptions, all);

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                item.setIndeterminate(false);
                item.setMax((int) tpe.getTaskCount());
                item.setProgress((int) tpe.getCompletedTaskCount());
                if (tpe.getTaskCount() == tpe.getCompletedTaskCount()) {
                    bar.dismiss();
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 250, 1000);
    }
}
