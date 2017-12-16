package io.compactd.player.ui.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;

import java.lang.reflect.InvocationTargetException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.adapter.ModelAdapter;
import io.compactd.player.glide.GlideApp;
import io.compactd.player.glide.MediaCover;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.adapter.AlbumsAdapter;

public class ArtistActivity extends AppCompatActivity {

    public static final String BUNDLE_ARTIST_KEY = "artist";
    private static final String TAG = ArtistActivity.class.getSimpleName();

    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.artist_cover_view)
    ImageView artistCoverView;

    @BindView(R.id.artist_albums)
    RecyclerView artistAlbums;

    @BindView(R.id.title)
    TextView titleView;

    private Unbinder unbinder;
    private AlbumsAdapter albumsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setTheme(R.style.AppTheme_TranslucentStatusBar);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        artistAlbums.setLayoutManager(layoutManager);

        albumsAdapter = new AlbumsAdapter(this, ModelAdapter.LayoutType.GridItem);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        CompactdParcel artist = bundle.getParcelable(BUNDLE_ARTIST_KEY);
        try {
            assert artist != null;
            setArtist((CompactdArtist) artist.getModel(CompactdArtist.class, CompactdManager.getInstance(this)));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private void setArtist(CompactdArtist model) {
        try {
            model.fetch();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }
        Log.d(TAG, "setArtist: "+ model);
        titleView.setText(model.getName());
        artistAlbums.setAdapter(albumsAdapter);
        GlideApp.with(this).load(new MediaCover(model)).into(artistCoverView);

        try {
            albumsAdapter.swapItems(model.getAlbums(CompactdModel.FindMode.OnlyIds));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
