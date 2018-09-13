package com.connriver.connrail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Owner.OnDataUpdate, Remote.OnDataUpdate {

    // global defines ===============================================================
    public static final int CARDATA_SPOT_MAX = 4;
    public static final int NONE = -1;
    //public static final String TAG = "ConnRail";
    // global defines ===============================================================

    // global data
    private static final ArrayList<CarData> gCarDataList =  new ArrayList<>(); // global car data
    private static final ArrayList<SpotData> gSpotDataList =  new ArrayList<>(); // global spot data
    private static final ArrayList<ConsistData> gConsistDataList =  new ArrayList<>(); // global spot data
    private static int iSessionNumber = 0;
    public static boolean bShowInStorage = true;

    private static final int ALERT_LOCATION = 1;
    private static final int ALERT_SPOTS = 2;

    public static final int SOCKET_PORT = 8099;
    public static final int MSG_PING = 1;
    public static final int MSG_NO_PING = 2;
    public static final int MSG_REQUEST_FULL_DATA = 4;
    public static final int MSG_FULL_DATA = 5;
    public static final int MSG_REQUEST_SESSION_DATA = 6;
    public static final int MSG_SESSION_DATA = 7;
    public static final int MSG_FULL_CAR_DATA = 8;
    public static final int MSG_FULL_SPOT_DATA = 9;
    public static final int MSG_FULL_CONSIST_DATA = 10;

    public static final int MSG_DELETE_CAR_DATA = 11;
    public static final int MSG_DELETE_SPOT_DATA = 12;
    public static final int MSG_DELETE_CONSIST_DATA = 13;

    public static final int MSG_UPDATE_CAR_DATA = 14;
    public static final int MSG_UPDATE_SPOT_DATA = 15;
    public static final int MSG_UPDATE_CONSIST_DATA = 16;

    public static final String MSG_TYPE_TAG = "msg_type";
    public static final String MSG_DATA_TAG = "msg_data";

    public static final String INTENT_UPDATE_DATA = "intent_update_data";

    // intent data labels
    public static final String SPOT_DATA = "SpotData";
    public static final String CONSIST_DATA = "ConsistData";
    public static final String CAR_DATA = "CarData";
    public static final String TOWN_NAME = "TownName";
    public static final String CAR_INFO_PARENT = "Parent";
    public static final String PARENT_YARD = "YardMaster";
    public static final String PARENT_TRAIN = "TrainMaster";
    private static final String PREFS_NAME = "ConnRailPrefs";
    private static final String PREFS_SESSION_NUMBER = "SessionNumber";
    private static final String PREFS_USER_TYPE = "UserType";
    private static final String PREFS_OWNER_IP = "OwnerIP";
    public static final String CURRENT_TAB = "CurrentTab";

    // user type and data
    public static final int USER_TYPE_SINGLE = 1;
    public static final int USER_TYPE_OWNER = 2;
    public static final int USER_TYPE_REMOTE = 3;
    private static int iUserType = -1;
    private static final int GET_USER_TYPE = 1;
    public static final String USER_TYPE = "user_type";
    public static final String OWNER_IP = "owner_ip";
    private static String sOwnerIP = null;

    static final ArrayList<AlertData> alerts = new ArrayList<>();

    private TextView tvUserType;
    private TextView tvUserStatus;
    private TextView tvSession;
    private Button btnSession;

    private static Owner mOwner = null;
    private static Remote mRemote = null;

    public static int getSessionNumber() {
        return iSessionNumber;
    }

    private void incrSessionNumber() {
        iSessionNumber++;
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(PREFS_SESSION_NUMBER, iSessionNumber);
        editor.apply();

        // if an owner, send the new day/session number to all remotes
        if (iUserType == USER_TYPE_OWNER && mOwner != null) {
            mOwner.sendAll(MSG_SESSION_DATA, String.valueOf(iSessionNumber));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find all the ui elements
        tvUserType = (TextView) findViewById(R.id.tvUserType);
        tvUserStatus = (TextView) findViewById(R.id.tvUserStatus);
        tvSession = (TextView) findViewById(R.id.tvSessionNumber);

        btnSession = (Button)findViewById(R.id.btnSessionNumber);
        btnSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incrSessionNumber();
                showSessionNumber();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // load the saved user type and owner ip
        loadUserType();

        // show the current user type
        displayUserType();

        showSessionNumber();

        // on rotation need to refresh the callbacks to the new activity
        if (mOwner != null) {
            mOwner.setCallback(this);
        }
        if (mRemote != null) {
            mRemote.setCallback(this);
        }
    }

    public static ArrayList<CarData> getCarList() {
        return gCarDataList;
    }

    public static ArrayList<SpotData> getSpotList() {
        return gSpotDataList;
    }

    public static ArrayList<ConsistData> getConsistList() {
        return gConsistDataList;
    }

    public static void updateSpot(SpotData sdAddEditDel, boolean bDelete) {
        int id = sdAddEditDel.getID();
        for (SpotData sd : gSpotDataList) {
            if (sd.getID() == id) {
                if (bDelete) {
                    gSpotDataList.remove(sd);
                } else {
                    sd.fromJSON(sdAddEditDel.toJSON());
                }
                return;
            }
        }

        if (!bDelete) { // if not doing a delete and not updated above - add
            gSpotDataList.add(sdAddEditDel);
        }
    }

    public static void spotAddEditDelete(SpotData sdAddEdit, boolean bDelete) {
        int msgType = bDelete ? MSG_DELETE_SPOT_DATA : MSG_UPDATE_SPOT_DATA;

        if (iUserType != USER_TYPE_REMOTE) {
            updateSpot(sdAddEdit, bDelete); // update/add/delete list item directly
            DBUtils.saveSpotData(); //save to the DB

            // if an owner, send the spot change to all remotes
            if (iUserType == USER_TYPE_OWNER && mOwner != null) {
                mOwner.sendAll(msgType, sdAddEdit.toJSON().toString());
            }
        } else {
            // if remote, send the spot change to owner
            if (mRemote != null) {
                mRemote.send(msgType, sdAddEdit.toJSON().toString());
            }
        }
    }

    public static void updateCar(CarData cdAddEditDel, boolean bDelete) {
        int id = cdAddEditDel.getID();
        for (CarData cd : gCarDataList) {
            if (cd.getID() == id) {
                if (bDelete) {
                    gCarDataList.remove(cd);
                } else {
                    cd.fromJSON(cdAddEditDel.toJSON());
                }
                return;
            }
        }
        if (!bDelete) { // if not doing a delete and not updated above - add
            gCarDataList.add(cdAddEditDel);
        }
    }

    public static void carAddEditDelete(CarData cdAddEdit, boolean bDelete) {
        int msgType = bDelete ? MSG_DELETE_CAR_DATA : MSG_UPDATE_CAR_DATA;

        if (iUserType != USER_TYPE_REMOTE) {
            updateCar(cdAddEdit, bDelete); // update/add/delete list item
            DBUtils.saveCarData(); //save to the DB

            // if an owner, send the car change to all remotes
            if (iUserType == USER_TYPE_OWNER && mOwner != null) {
                mOwner.sendAll(msgType, cdAddEdit.toJSON().toString());
            }
        } else {
            // if remote, send the car change to owner
            if (mRemote != null) {
                mRemote.send(msgType, cdAddEdit.toJSON().toString());
            }
        }
    }

    public static void updateConsist(ConsistData cdAddEditDel, boolean bDelete) {
        int id = cdAddEditDel.getID();
        for (ConsistData cd : gConsistDataList) {
            if (cd.getID() == id) {
                if (bDelete) {
                    gConsistDataList.remove(cd);
                } else {
                    cd.fromJSON(cdAddEditDel.toJSON());
                }
                return;
            }
        }

        if (!bDelete) { // if not doing a delete and not updated above - add
            gConsistDataList.add(cdAddEditDel);
        }
    }

    public static void consistAddEditDelete(ConsistData cdAddEdit, boolean bDelete) {
        int msgType = bDelete ? MSG_DELETE_CONSIST_DATA : MSG_UPDATE_CONSIST_DATA;

        if (iUserType != USER_TYPE_REMOTE) {
            updateConsist(cdAddEdit, bDelete); // update/add/delete list item
            DBUtils.saveConsistData(); //save to the DB

            // if an owner, send the consist change to all remotes
            if (iUserType == USER_TYPE_OWNER && mOwner != null) {
                mOwner.sendAll(msgType, cdAddEdit.toJSON().toString());
            }
        } else {
            // if remote, send the consist change to owner
            if (mRemote != null) {
                mRemote.send(msgType, cdAddEdit.toJSON().toString());
            }
        }
    }

    private void loadUserType() {
        // get the session number locally
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ix = prefs.getInt(PREFS_USER_TYPE, USER_TYPE_SINGLE);
        String sx = "";
        if (ix == USER_TYPE_REMOTE) {
            sx = prefs.getString(PREFS_OWNER_IP, "");
        }
        handleTypeChange(ix, sx);
    }

    private void saveUserType() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(PREFS_USER_TYPE, iUserType);
        if (iUserType == USER_TYPE_REMOTE) {
            editor.putString(PREFS_OWNER_IP, sOwnerIP);
        }
        editor.apply();
    }

    private void requestAllData() {

        // remote user will get data when they connect to owner
        if (iUserType != USER_TYPE_REMOTE) {
            loadAllData();
        }
    }

    // locally load the day/session number and all DB tables (single/owner)
    private void loadAllData() {
        loadSessionNumber();
        loadDBData(this);
    }

    private void loadSessionNumber() {
        // get the session number locally
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        iSessionNumber = prefs.getInt(PREFS_SESSION_NUMBER, 1);
        showSessionNumber();
    }

    private void showSessionNumber() {
        tvSession.setText(String.valueOf(iSessionNumber));
    }

    public static void loadDBData(Context ctx) {
        DBUtils.init(ctx);

        // load car information from the db
        DBUtils.loadCarData();

        // load spot information from the db
        DBUtils.loadSpotData();

        // load consist information from the db
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
        int loc;

        for (CarData cd : gCarDataList) {
            loc = cd.getCurrentLoc();
            if ((loc == NONE || Utils.getSpotFromID(loc) == null) && !cd.getInStorage() && cd.getConsist() == NONE) {
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

        for (CarData cd : gCarDataList) {
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
        StaticListView lvAlerts = (StaticListView) findViewById(R.id.lvAlerts);
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

        AlertList al = new AlertList(lvAlerts, getBaseContext());
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
            case R.id.action_import_export:
                startActivity(new Intent(this, ImportExportActivity.class));
                break;
            case R.id.action_user_type:
                Intent intent = new Intent(this, UserTypeActivity.class);
                intent.putExtra(USER_TYPE, iUserType);
                intent.putExtra(OWNER_IP, sOwnerIP);
                startActivityForResult(intent, GET_USER_TYPE);
                break;

            case R.id.action_end:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_USER_TYPE) {
            if (resultCode == RESULT_OK) {
                int iType = data.getIntExtra(USER_TYPE, USER_TYPE_SINGLE);
                String sOwnIP = data.getStringExtra(OWNER_IP);

                handleTypeChange(iType, sOwnIP);
            }
        }
    }

    private void handleTypeChange(int iType, String sOwnIP) {

        // return if nothing has changed
        if (iType == iUserType) {
            if (iType != USER_TYPE_REMOTE || sOwnIP.equals(sOwnerIP)) {
                return;
            }
        }

        boolean bRequestData = true;

        // don't reload the DB info if a change between single/owner
        if ((iType == USER_TYPE_OWNER && iUserType == USER_TYPE_SINGLE) ||
            (iType == USER_TYPE_SINGLE && iUserType == USER_TYPE_OWNER )) {
            bRequestData = false;
        }

        iUserType = iType;
        sOwnerIP = sOwnIP;

        saveUserType();

        // close/destroy any owner or remote
        if (mOwner != null) {
            mOwner.close();
            mOwner = null;
        }
        if (mRemote != null) {
            mRemote.close();
            mRemote = null;
        }

        // if change to remote or owner - create new ones
        if (iUserType == USER_TYPE_REMOTE) {
            mRemote = new Remote(this, sOwnerIP);
        } else if (iUserType == USER_TYPE_OWNER) {
            mOwner = new Owner(this, this);
        }

        if (bRequestData) {
            requestAllData();
        }

        displayUserType();
    }

    private void displayUserType() {
        if (iUserType == USER_TYPE_REMOTE) {
            tvUserType.setText(getResources().getString(R.string.user_remote) + " - " +
                    getResources().getString(R.string.user_owner) + " " + getResources().getString(R.string.user_ip) + " " + sOwnerIP);
            tvUserStatus.setText(getResources().getString(R.string.remote_disconnected));
            btnSession.setEnabled(false); // remote can't increment the day/session
        } else if (iUserType == USER_TYPE_OWNER) {
            tvUserType.setText(getResources().getString(R.string.user_owner) +  " - " + getResources().getString(R.string.user_ip) + " " + mOwner.getIP(this));
            tvUserStatus.setText("");
            btnSession.setEnabled(true);
        } else {
            tvUserType.setText(R.string.user_single);
            tvUserStatus.setText("");
            btnSession.setEnabled(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.nav_help:
                startActivity(new Intent(this, HelpActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // broadcast a potential UI change to any activities that are listening
    private void updateUI(int iMsgType, String sMsgData) {
        Intent intent = new Intent(INTENT_UPDATE_DATA);
        intent.putExtra(MSG_TYPE_TAG, iMsgType);
        intent.putExtra(MSG_DATA_TAG, sMsgData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onOwnerDataUpdate(final int iMsgType, final String sData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (iMsgType) {
                    case MSG_PING:
                        tvUserStatus.setText(getResources().getString(R.string.remote_count) + " " + mOwner.getRemoteCount() );
                        break;
                    default:
                        // a change could trigger an alert so update alerts
                        showAlerts();

                        // broadcast changes to any running activities
                        updateUI(iMsgType, sData);

                }
            }
        });
    }

    private void remoteReconnect() {
        if (mRemote != null) {
            mRemote.close();
            mRemote = null;
        }

        mRemote = new Remote(this, sOwnerIP);
    }

    @Override
    public void onRemoteDataUpdate(final int iMsgType, final String sData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (iMsgType) {
                    case MSG_PING:
                        tvUserStatus.setText(getResources().getString(R.string.remote_connected));
                        break;
                    case MSG_NO_PING:
                        tvUserStatus.setText(getResources().getString(R.string.remote_disconnected));
                        remoteReconnect();
                        break;
                    case MSG_SESSION_DATA:
                        iSessionNumber = Integer.parseInt(sData);
                        showSessionNumber();
                        break;

                    default:
                        // a change could trigger an alert so update alerts
                        showAlerts();

                        // broadcast changes to any running activities
                        updateUI(iMsgType, sData);
                }
            }
        });
    }
}
