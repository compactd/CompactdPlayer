package io.compactd.player.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.zafarkhaja.semver.Version;

import org.json.JSONException;

import java.io.IOException;

import io.compactd.client.CompactdClient;
import io.compactd.client.CompactdException;
import io.compactd.client.models.CompactdTrack;
import io.compactd.player.R;
import io.compactd.player.helpers.MusicPlayerRemote;
import io.compactd.player.service.MediaPlayerService;
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

        mToggleButton = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);

        mDrawerLayout.addDrawerListener(mToggleButton);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mToggleButton.syncState();

        final TextView appText = mNavigationView.getHeaderView(0).findViewById(R.id.app_text);

        new Thread(
            new Runnable() {

                @Override
                public void run() {
                    try {
                        final Version version = CompactdClient.getInstance().getServerVersion();
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
        serverText.setText(CompactdClient.getInstance().getUrl().getHost());
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.item_sync) {
            startActivity(
                    new Intent(this, SyncActivity.class)
            );
            return true;
        } else {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

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
                    return TracksFragment.newInstance(ModelFragment.VERTICAL_LAYOUT, null);
                default:
                    return ArtistsFragment.newInstance(ModelFragment.GRID_LAYOUT, null);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
