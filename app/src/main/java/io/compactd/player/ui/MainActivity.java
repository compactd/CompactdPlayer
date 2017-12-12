package io.compactd.player.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.couchbase.lite.CouchbaseLiteException;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.compactd.client.CompactdClient;
import io.compactd.client.CompactdException;
import io.compactd.client.CompactdSync;
import io.compactd.player.R;
import io.compactd.player.utils.PreferenceUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText mServerURLText;
    private Button mConnectButton;
    private ProgressBar mProgressBar;
    private EditText mUsername;
    private EditText mPassword;
    private Button mPreviousButton;
    private ConstraintLayout mLayout;
    private ConstraintLayout mFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mServerURLText  = findViewById(R.id.serverUrlText);
        mConnectButton  = findViewById(R.id.connectButton);
        mPreviousButton = findViewById(R.id.previousButton);
        mProgressBar    = findViewById(R.id.progressBar);
        mUsername       = findViewById(R.id.usernameEditText);
        mPassword       = findViewById(R.id.passwordEditText);

        mLayout      = findViewById(R.id.mainLayout);
        mFrameLayout = findViewById(R.id.frameLayout);

        mConnectButton.setOnClickListener(this);

        mPreviousButton.setOnClickListener(this);

        String remote = PreferenceUtil.getInstance(this).getRemoteUrl();

        if (!remote.isEmpty()) {
            CheckServerTask task = new CheckServerTask(this);
            task.execute(remote);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mServerURLText.setVisibility(View.VISIBLE);
            mConnectButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectButton:
                mServerURLText.setVisibility(View.GONE);
                mConnectButton.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                if (mUsername.getVisibility() == View.VISIBLE) {
                    mUsername.setVisibility(View.GONE);
                    mPassword.setVisibility(View.GONE);
                    ConnectAndSyncTask task = new ConnectAndSyncTask(this);
                    task.execute(new Pair<>(mUsername.getText().toString(),
                            mPassword.getText().toString()));
                } else {
                    CheckServerTask task = new CheckServerTask(this);
                    task.execute(mServerURLText.getText().toString());
                }
                break;
            case R.id.previousButton:
                showServerText();
                mUsername.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
        }
    }
    class ConnectAndSyncTask extends AsyncTask<Pair<String, String>, Float, Boolean> implements CompactdSync.SyncEventListener {
        static final int DURATION = 400;
        private Context context;
        private int progress = 0;

        ConnectAndSyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Pair<String, String>[] pairs) {
            String username = pairs[0].first;
            String password = pairs[0].second;

            CompactdClient instance = CompactdClient.getInstance();
            String token = PreferenceUtil.getInstance(context).getSessionToken();
            if (!token.isEmpty() && instance.isTokenValid(token)) {
                instance.setToken(token);
            }
            try {
                if (!instance.login(username, password)) {
                   return false;
               }
            } catch (IOException | JSONException | CompactdException e) {
                e.printStackTrace();
                return false;
            }

            CompactdSync compactdSync = CompactdSync.getInstance(context);
            compactdSync.setURL(instance.getUrl().toString());
            compactdSync.setToken(instance.getToken());
            compactdSync.addEventListener(this);
            compactdSync.start(instance.getPrefix());
            return true;
        }

        @Override
        protected void onPostExecute(Boolean res) {
            if (!res) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showCredentialForm();
                    }
                });
            }
        }

        @Override
        public void finished() {
            Context context = getApplicationContext();
            PreferenceUtil.getInstance(context).setSessionToken(CompactdClient.getInstance().getToken());
            PreferenceUtil.getInstance(context).setUsername(CompactdClient.getInstance().getUsername());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConstraintSet set = new ConstraintSet();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        AutoTransition autoTransition = new AutoTransition();
                        autoTransition.setDuration(DURATION);

                        TransitionManager.beginDelayedTransition(mLayout, autoTransition);
                    }
                    set.clone(mLayout);
                    set.setVerticalBias(R.id.frameLayout, 0.3f);
                    set.applyTo(mLayout);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(mFrameLayout, "alpha", 1.0f, 0.0f);
                    animator.setDuration(DURATION);
                    animator.start();

                }
            });
        }

        @Override
        protected void onProgressUpdate(final Float... values) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setIndeterminate(false);
                    int p = Math.round(values[0] * 100);
                    if (p > progress) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mProgressBar.setProgress(p, true);
                        } else {
                            mProgressBar.setProgress(p);
                        }
                        progress = p;
                    }
                }
            });
        }

        @Override
        public void databaseChanged(String database) {

        }

        @Override
        public void databaseSyncStarted(String database) {

        }

        @Override
        public void databaseSyncFinished(String database) {

        }

        @Override
        public void onCouchException(CouchbaseLiteException exc) {

        }

        @Override
        public void onURLException(MalformedURLException exc) {

        }

        @Override
        public void onProgress(float progress) {
            this.publishProgress(progress);
        }
    }
    class CheckServerTask extends AsyncTask<String, Void, Boolean> {

        private Context context;
        private URL url;

        CheckServerTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... args) {
            try {
                url = new URL(args[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }
            CompactdClient.getInstance().setUrl(url);
            return CompactdClient.getInstance().isServerValid();
        }

        @Override
        protected void onPostExecute(final Boolean res) {
            if (res) {
                PreferenceUtil.getInstance(context).setRemoteUrl(url.toString());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!res) {
                        showServerText();
                        mServerURLText.setError(getString(R.string.incompatible_server));
                    } else {
                        showCredentialForm();
                    }
                }
            });

        }
    }

    private void showCredentialForm() {
        CompactdClient client = CompactdClient.getInstance();
        if (client.isTokenValid(PreferenceUtil.getInstance(this).getSessionToken())) {
            mServerURLText.setVisibility(View.GONE);
            mConnectButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mUsername.setVisibility(View.GONE);
            mPassword.setVisibility(View.GONE);
            ConnectAndSyncTask task = new ConnectAndSyncTask(this);
            task.execute(new Pair<>(mUsername.getText().toString(),
                    mPassword.getText().toString()));
            return;
        }
        mConnectButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPassword.setVisibility(View.VISIBLE);
        mUsername.setVisibility(View.VISIBLE);
        mPreviousButton.setVisibility(View.VISIBLE);
    }

    private void showServerText() {
        mServerURLText.setVisibility(View.VISIBLE);
        mConnectButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }
}
