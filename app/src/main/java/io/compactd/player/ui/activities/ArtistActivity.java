package io.compactd.player.ui.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.couchbase.lite.CouchbaseLiteException;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helper.CompactdParcel;
import io.compactd.player.helper.MusicPlayerRemote;
import io.compactd.player.ui.fragments.AlbumsFragment;
import io.compactd.player.ui.fragments.ModelFragment;
import io.compactd.player.ui.fragments.TracksFragment;

public class ArtistActivity extends SlidingMusicActivity implements Toolbar.OnMenuItemClickListener {

    public static final String BUNDLE_ARTIST_KEY = "artist";
    private static final String TAG = ArtistActivity.class.getSimpleName();

    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.artist_cover_view)
    ImageView artistCoverView;

    @BindView(R.id.title)
    TextView titleView;

    private Unbinder unbinder;
    private CompactdArtist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        setTheme(R.style.AppTheme_TranslucentStatusBar);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        supportPostponeEnterTransition();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        CompactdParcel artist = bundle.getParcelable(BUNDLE_ARTIST_KEY);


        try {
            assert artist != null;
            CompactdArtist model = (CompactdArtist) artist.getModel(CompactdArtist.class, CompactdManager.getInstance(this));
            setArtist(model);

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = manager.beginTransaction();

            AlbumsFragment albumsFragment = AlbumsFragment.newInstance(ModelFragment.HORIZONTAL_LAYOUT, model.getId());
            TracksFragment tracksFragment = TracksFragment.newInstance(ModelFragment.VERTICAL_LAYOUT, model.getId());
            fragmentTransaction.add(R.id.albums_frame, albumsFragment);
            fragmentTransaction.add(R.id.tracks_frame, tracksFragment);
            fragmentTransaction.commit();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        setShowStatusBarDummy(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }

    private void setArtist(CompactdArtist model) {
        mArtist = model;
        try {
            model.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }
        toolbar.setTitle(model.getName());
        titleView.setText(model.getName());
        Glide.with(this).load(new MediaCover(model)).into(new DrawableImageViewTarget(artistCoverView) {
            @Override
            public void onResourceReady(Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                supportStartPostponedEnterTransition();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_play_shuffled:
                try {
                    List<CompactdTrack> queue = mArtist.getTracks(CompactdModel.FindMode.OnlyIds);
                    Collections.shuffle(queue);
                    MusicPlayerRemote.getInstance(this).openQueue(queue, 0, true);
                    MusicPlayerRemote.getInstance(this).setShuffling(true);
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            case R.id.action_play_next:
                try {
                    List<CompactdTrack> queue = mArtist.getTracks(CompactdModel.FindMode.OnlyIds);
                    MusicPlayerRemote.getInstance(this).insert(queue);
                    return true;
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }


        }
        return false;
    }
}
