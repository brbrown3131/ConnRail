package com.connriver.connrail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 3/16/2018.
 */

public class CarLocationActivity extends AppCompatActivity
        implements TownFragment.Listener, ConsistFragment.Listener, StorageFragment.Listener{

    private CarData cd;
    private String sTown = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_location);

        // get the passed in CarData
        Intent intent = getIntent();
        cd = (CarData) intent.getSerializableExtra(MainActivity.CAR_DATA);
        sTown = intent.getStringExtra(MainActivity.TOWN_NAME);
        if (cd == null) {
            return;
        }

        setTitle(cd.getInfo());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_crosshairs_gps);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_train);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_archive);
    }

    // send the updated CarData back and finish
    private void done() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.CAR_DATA, cd);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onSpotSelected(int id) {
        cd.setCurrentLoc(id);
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
                    return new TownFragment();
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
