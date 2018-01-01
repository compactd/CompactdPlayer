package io.compactd.player.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.github.zafarkhaja.semver.Version;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchFilter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.compactd.client.CompactdClient;
import io.compactd.client.CompactdException;
import io.compactd.client.CompactdManager;
import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.client.models.CompactdModel;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.adapter.ItemSearchAdapter;
import io.compactd.player.helper.MusicPlayerRemote;
import io.compactd.player.ui.fragments.AlbumsFragment;
import io.compactd.player.ui.fragments.ArtistsFragment;
import io.compactd.player.ui.fragments.ModelFragment;
import io.compactd.player.ui.fragments.TracksFragment;

public class LibraryActivity extends SlidingMusicActivity implements NavigationView.OnNavigationItemSelectedListener {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggleButton;
    private NavigationView mNavigationView;
    private SearchView mSearchView;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(this);
        mSearchView = findViewById(R.id.searchView);

        if (CompactdClient.getInstance().isOffline()) {
            mNavigationView.getMenu().findItem(R.id.item_sync).setEnabled(false);
        }

        mToggleButton = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);

        mDrawerLayout.addDrawerListener(mToggleButton);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mToggleButton.syncState();

        final TextView appText = mNavigationView.getHeaderView(0).findViewById(R.id.app_text);

        final CompactdClient client = CompactdClient.getInstance();
        new Thread(
            new Runnable() {

                @Override
                public void run() {
                    if (client.isOffline()) {
                        appText.setText(R.string.offline_text);
                        return;
                    }
                    try {
                        final Version version = client.getServerVersion();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                appText.setText(getString(R.string.app_text_content, version.toString()));
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (CompactdException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        TextView serverText = mNavigationView.getHeaderView(0).findViewById(R.id.server_text);
        if (client.isOffline()) {
            serverText.setVisibility(View.INVISIBLE);
        } else {
            serverText.setText(client.getUrl().getHost());
        }

        setupSearchView();
    }

    private void setupSearchView() {
        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(this);

       // mSearchView.setVersionMargins(SearchView.VersionMargins.TOOLBAR_SMALL);
        mSearchView.setHint("Search");
        mSearchView.setVoice(false);
        mSearchView.close(false);

        final ItemSearchAdapter searchAdapter = new ItemSearchAdapter(this);
        mSearchView.setAdapter(searchAdapter);

        mSearchView.setOnMenuClickListener(new SearchView.OnMenuClickListener() {

            @Override
            public void onMenuClick() {
                mDrawerLayout.openDrawer(Gravity.START, true);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mHistoryDatabase.addItem(new SearchItem(query));
                mSearchView.close(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapter.setQuery(newText);
                return true;
            }
        });

        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onOpen() {
                return true;
            }

            @Override
            public boolean onClose() {
                return true;
            }
        });

        searchAdapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.close(true);
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                setupResults(searchAdapter);
            }
        }).start();
    }

    private void setupResults(ItemSearchAdapter adapter) {
        List<CompactdModel> items = new ArrayList<>();
        Manager manager = CompactdManager.getInstance(this);

        try {
            items.addAll(CompactdArtist.findAll(manager, CompactdModel.FindMode.OnlyIds));
            items.addAll(CompactdAlbum.findAll(manager, CompactdModel.FindMode.OnlyIds));
           // items.addAll(CompactdTrack.findAll(manager, CompactdModel.FindMode.OnlyIds));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        adapter.setItems(items);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.item_sync:
                if (CompactdClient.getInstance().isOffline()) {
                    return false;
                }
                startActivity(
                        new Intent(this, SyncActivity.class)
                );
                return true;
            case R.id.action_showhidden:
                item.setChecked(!item.isChecked());
                mSectionsPagerAdapter.setShowHidden(item.isChecked());

                return true;

        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayerRemote remote = MusicPlayerRemote.getInstance(this);
        remote.removePlaybackListener(this);
        remote.destroyMedia();

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();
        private boolean mShowHidden = false;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return ArtistsFragment.newInstance(ModelFragment.GRID_LAYOUT, null);
                case 1:
                    return AlbumsFragment.newInstance(ModelFragment.GRID_LAYOUT, null);
                case 2:
                    TracksFragment tracksFragment = TracksFragment.newInstance(ModelFragment.VERTICAL_LAYOUT, null);
                    tracksFragment.setShowHidden(mShowHidden);
                    return tracksFragment;
                default:
                    return ArtistsFragment.newInstance(ModelFragment.GRID_LAYOUT, null);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mRegisteredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mRegisteredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment (int pos) {
            return mRegisteredFragments.get(pos);
        }

        public void setShowHidden(boolean showHidden) {
            TracksFragment fragment = (TracksFragment) getRegisteredFragment(2);
            if (fragment != null)  {
                fragment.setShowHidden(showHidden);
            }
            mShowHidden = showHidden;
        }
    }
}
