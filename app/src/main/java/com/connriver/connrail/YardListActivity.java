package com.connriver.connrail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.INTENT_UPDATE_DATA;

public class YardListActivity extends AppCompatActivity {

    private ListView lv;
    private CarList cl;
    private CarData cdSelected = null;
    private String sCurrentTown = null;
    private Spinner spTown;
    private ArrayList<String> townList;

    private static final int SET_CAR_INFO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yard_list);

        lv = (ListView) findViewById(R.id.carListView);

        spTown = (Spinner) findViewById(R.id.spTown);

        fillTownList();

        updateList();

        spTown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    sCurrentTown = null;
                } else {
                    sCurrentTown = townList.get(position);
                }
                updateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cdSelected = cl.getCarData(position);
                showCarInfo();
            }
        });
    }

    private void fillTownList() {
        //fill the spinner list of towns
        townList = Utils.getTownList(true, this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, townList);
        spTown.setAdapter(adapter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // just update the list on any change
            updateList();
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
        updateList();
    }

    // based on which town selected, display what cars are in that town
    private void updateList() {
        cl = new CarList(lv, getBaseContext(), Utils.getCarsInTown(sCurrentTown));
        cl.resetList();
    }

    private void showCarInfo() {
        // launch the car info screen
        Intent intent = new Intent(this, CarInfoActivity.class);
        intent.putExtra(MainActivity.CAR_DATA, cdSelected);
        intent.putExtra(MainActivity.CAR_INFO_PARENT, MainActivity.PARENT_YARD);
        intent.putExtra(MainActivity.TOWN_NAME, sCurrentTown);
        startActivityForResult(intent, SET_CAR_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_CAR_INFO && resultCode == RESULT_OK) {
            CarData cd = (CarData) data.getSerializableExtra(MainActivity.CAR_DATA);

            MainActivity.carAddEditDelete(cd, false);

            updateList();
        }
    }

}
