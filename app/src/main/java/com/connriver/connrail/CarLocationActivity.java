package com.connriver.connrail;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static com.connriver.connrail.MainActivity.INTENT_UPDATE_DATA;

/**
 * Created by bbrown on 3/16/2018.
 */

public class CarLocationActivity extends AppCompatActivity
        implements TownFragment.Listener, ConsistFragment.Listener, StorageFragment.Listener{

    private CarData cd;
    private String sCurrentTown = null;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_location);

        // get the passed in CarData
        Intent intent = getIntent();
        cd = (CarData) intent.getSerializableExtra(MainActivity.CAR_DATA);
        int iTab = intent.getIntExtra(MainActivity.CURRENT_TAB, 0);
        sCurrentTown = intent.getStringExtra(MainActivity.TOWN_NAME);
        if (cd == null) {
            return;
        }

        setTitle(cd.getInfo());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(iTab);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_crosshairs_gps);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_train);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_archive);
    }

    // send the updated CarData back and finish
    private void done() {
        int iTab = viewPager.getCurrentItem();
        Intent intent = new Intent();
        intent.putExtra(MainActivity.CAR_DATA, cd);
        intent.putExtra(MainActivity.CURRENT_TAB, iTab);
        intent.putExtra(MainActivity.TOWN_NAME, sCurrentTown);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onSpotSelected(int id, String sTown) {
        cd.setCurrentLoc(id);
        sCurrentTown = sTown;
        done();
    }

    @Override
    public void onConsistSelected(int id) {
        cd.setConsist(id);
        done();
    }

    @Override
    public void onStorageSelected() {
        cd.setInStorage();
        done();
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            switch (position) {
                case 0:
                    return getString(R.string.tab_town);
                case 1:
                    return getString(R.string.tab_train);
                case 2:
                    return getString(R.string.tab_storage);
                default:
                    return null;
            }
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.TOWN_NAME, sCurrentTown);
                    android.support.v4.app.Fragment frag = new TownFragment();
                    frag.setArguments(bundle);
                    return frag;
                case 1:
                    return new ConsistFragment();
                case 2:
                    return new StorageFragment();

                default:
                    return null;
            }
        }
    }
}
