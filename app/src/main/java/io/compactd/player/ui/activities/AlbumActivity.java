package io.compactd.player.ui.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.couchbase.lite.CouchbaseLiteException;

import org.w3c.dom.Text;

import java.lang.reflect.InvocationTargetException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.player.R;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.ui.fragments.AlbumsFragment;
import io.compactd.player.ui.fragments.ModelFragment;
import io.compactd.player.ui.fragments.TracksFragment;

public class AlbumActivity extends SlidingMusicActivity {

    public static final String BUNDLE_ALBUM_KEY = "album";

    @BindView(R.id.album_cover_view)
    ImageView albumCoverView;

    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;

    @BindView(R.id.title)
    TextView titleView;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        supportPostponeEnterTransition();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        CompactdParcel album = bundle.getParcelable(BUNDLE_ALBUM_KEY);


        try {
            assert album != null;
            CompactdAlbum model = (CompactdAlbum) album.getModel(CompactdAlbum.class, CompactdManager.getInstance(this));
            setAlbum(model);

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = manager.beginTransaction();

            TracksFragment tracksFragment = TracksFragment.newInstance(ModelFragment.VERTICAL_LAYOUT, model.getId());
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
    public boolean onSupportNavigateUp() {
        this.onBackPressed();
        return true;
    }

    private void setAlbum(CompactdAlbum model) {
        try {
            model.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }

        Glide.with(this).load(new MediaCover(model)).into(new DrawableImageViewTarget(albumCoverView) {
            @Override
            public void onResourceReady(Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                supportStartPostponedEnterTransition();
            }
        });
        setTitle("");
        titleView.setText(model.getName());
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
}
