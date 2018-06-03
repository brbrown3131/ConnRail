package com.connriver.connrail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // global defines ===============================================================
    public static final int CARDATA_SPOT_MAX = 4;
    public static final int NONE = -1;
    public static final String TAG = "ConnRail";
    // global defines ===============================================================

    // global data
    public static ArrayList<CarData> gCarData = null; // global car data
    public static ArrayList<SpotData> gSpotData = null; // global spot data
    public static ArrayList<ConsistData> gConsistData = null; // global spot data
    private static int iSessionNumber = 0;
    public static boolean bShowInStorage = true;

    public static final int ALERT_LOCATION = 1;
    public static final int ALERT_SPOTS = 2;

    // intent data labels
    public static final String CAR_DATA_INDEX = "CarDataIndex";
    public static final String SPOT_DATA_INDEX = "SpotDataIndex";
    public static final String CONSIST_ID = "ConsistId";
    public static final String CAR_DATA = "CarData";
    public static final String TOWN_NAME = "TownName";
    public static final String CAR_INFO_PARENT = "Parent";
    public static final String PARENT_YARD = "YardMaster";
    public static final String PARENT_TRAIN = "TrainMaster";
    public static final String PREFS_NAME = "ConnRailPrefs";
    public static final String SESSION_NUMBER = "SessionNumber";
    public static final String CURRENT_TAB = "CurrentTab";

    private ListView lvAlerts;
    private static ArrayList<AlertData> alerts = new ArrayList<>();

    public static int getSessionNumber() {
        return iSessionNumber;
    }

    public void incrSessionNumber() {
        iSessionNumber++;
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(SESSION_NUMBER, iSessionNumber);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the context for utils
        Utils.init(this);

        // get the session number
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        iSessionNumber = prefs.getInt(SESSION_NUMBER, 0);

        final TextView tvSession = (TextView) findViewById(R.id.tvSessionNumber);
        tvSession.setText(Integer.toString(getSessionNumber()));

        Button btnSession = (Button)findViewById(R.id.btnSessionNumber);
        btnSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incrSessionNumber();
                tvSession.setText(Integer.toString(getSessionNumber()));
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateData(this);
    }

    public static void updateData(Context ctx) {
        DBUtils.init(ctx, DBUtils.MODE_SINGLE_USER);

        // load car information from the db
        gCarData = new ArrayList<CarData>();
        DBUtils.loadCarData();

        // load spot information from the db
        gSpotData = new ArrayList<>();
        DBUtils.loadSpotData();

        // load consist information from the db
        gConsistData = new ArrayList<>();
        DBUtils.loadConsistData();

        // remove all dead spots
        Utils.removeAllDeadSpots();
    }

    @Override
    protected void onResume() {
        // show any system alerts - current locations not set, cars < 2 spots, etc.
        showAlerts();

        super.onResume();
    }

    // add an alert to the list if any cars do not have a current location defined
    private AlertData alertLocations() {
        int count = 0;

        for (CarData cd : gCarData) {
            if (cd.getCurrentLoc() == NONE && !cd.getInStorage() && cd.getConsist() == NONE) {
                count++;
            }
        }

        if (count > 0) {
            String sx = getResources().getString(R.string.no_current_defined) + ": " + count + " " +
                    (count == 1 ? getResources().getString(R.string.car) : getResources().getString(R.string.cars));
            return new AlertData(sx, AlertData.ALERT_ERROR, ALERT_LOCATION);
        }
        return null;
    }

    // add an alert to the list if any cars have bad spots/destinations
    private AlertData alertSpots() {
        int count = 0;

        for (CarData cd : gCarData) {
            if (cd.invalidSpots()) {
                count++;
            }
        }

        if (count > 0) {
            String sx = getResources().getString(R.string.err_spots_defined) + ": " + count + " " +
                    (count == 1 ? getResources().getString(R.string.car) : getResources().getString(R.string.cars));
            return new AlertData(sx, AlertData.ALERT_ERROR, ALERT_SPOTS);
        }
        return null;
    }

    private void showAlerts() {
        lvAlerts = (ListView) findViewById(R.id.lvAlerts);
        alerts.clear();

        AlertData ad;

        // check locations set
        ad = alertLocations();
        if (ad != null) {
            alerts.add(ad);
        }

        // check all spots valid
        ad = alertSpots();
        if (ad != null) {
            alerts.add(ad);
        }

        // no problems
        if (alerts.size() == 0) {
            ad = new AlertData(getResources().getString(R.string.no_alerts), AlertData.ALERT_INFO, NONE);
            alerts.add(ad);
        }

        AlertList al = new AlertList(lvAlerts, getBaseContext(), alerts);
        al.resetList();

        // ListView Item Click Listener
        lvAlerts.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertData ad = alerts.get(position);

                if (ad.getID() == ALERT_LOCATION) {
                    startActivity(new Intent(getApplication(), CarLocationListActivity.class));
                }
                if (ad.getID() == ALERT_SPOTS) {
                    startActivity(new Intent(getApplication(), CarListActivity.class));
                }

            }

        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_network_settings:
                startActivity(new Intent(this, NetworkSettings.class));
                break;
            case R.id.action_end:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_consist:
                startActivity(new Intent(this, ConsistListActivity.class));
                break;
            case R.id.nav_yard_list:
                startActivity(new Intent(this, YardListActivity.class));
                break;
            case R.id.nav_car_status:
                startActivity(new Intent(this, CarLocationListActivity.class));
                break;
            case R.id.nav_car_list:
                startActivity(new Intent(this, CarListActivity.class));
                break;
            case R.id.nav_spot_list:
                startActivity(new Intent(this, SpotListActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, About.class));
                break;
            case R.id.nav_help:
                startActivity(new Intent(this, HelpActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
