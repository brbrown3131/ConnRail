package com.connriverlines.connrail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.connriverlines.connrail.R;

import java.util.ArrayList;

import static com.connriverlines.connrail.MainActivity.INTENT_UPDATE_DATA;

public class CarLocationListActivity extends AppCompatActivity {

    private ListView lv;
    private CarList cl;
    private String sTown = null;
    private int iTab = 0;

    private static final int SET_LOCATION = 1;

    private void resetList() {
        // get all the cars. town selected will only be used to pass to spot selection
        ArrayList<CarData> carList = Utils.getAllCars(MainActivity.bShowInStorage);

        cl = new CarList(lv, getBaseContext(), carList);
        cl.setShowDest(false);
        cl.resetList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_status);
        lv = (ListView) findViewById(R.id.carListView);

        resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // launch the spot pick dialog
                selectSpot(position);
            }
        });

        final CheckBox cb = (CheckBox) findViewById(R.id.cbShowStored);
        cb.setChecked(MainActivity.bShowInStorage);
        cb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.bShowInStorage = cb.isChecked();
                resetList();
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // just reset the list on any change
            resetList();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(INTENT_UPDATE_DATA));
    }

    private void selectSpot(int ixCarData) {
        // get which car is selected
        CarData cdSelected = cl.getCarData(ixCarData);
        if (cdSelected == null){
            return;
        }

        // launch car location activity with the current car data and town selected (if any)
        Intent intent = new Intent(this, CarLocationActivity.class);
        intent.putExtra(MainActivity.CAR_DATA, cdSelected);
        intent.putExtra(MainActivity.TOWN_NAME, sTown);
        intent.putExtra(MainActivity.CURRENT_TAB, iTab);
        startActivityForResult(intent, SET_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_LOCATION && resultCode == RESULT_OK) {
            CarData cd = (CarData) data.getSerializableExtra(MainActivity.CAR_DATA);
            iTab = data.getIntExtra(MainActivity.CURRENT_TAB, 0);
            sTown = data.getStringExtra(MainActivity.TOWN_NAME);

            MainActivity.carAddEditDelete(cd, false);

            resetList();
        }
    }
}
