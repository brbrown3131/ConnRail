package com.connriver.connrail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import java.util.ArrayList;

public class CarLocationListActivity extends AppCompatActivity {

    private ListView lv;
    private CarList cl;
    private String sTown = null;
    CarData cdSelected = null;

    static final int SET_LOCATION = 1;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("BBB", "CarLocationListActivity OnResume");
        ArrayList<CarData> carList = Utils.getAllCars(true);
        for (CarData cd : carList) {
            Log.d("BBB", "Car:" + cd.getInfo() + " = " + (cd.getInStorage() ? "Stored" : ""));
        }
    }

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

    private void selectSpot(int ixCarData) {
        // get which car is selected
        cdSelected = cl.getCarData(ixCarData);
        if (cdSelected == null){
            return;
        }

        // launch car location activity with the current car data and town selected (if any)
        Intent intent = new Intent(this, CarLocationActivity.class);
        intent.putExtra(MainActivity.CAR_DATA, cdSelected);
        intent.putExtra(MainActivity.TOWN_NAME, sTown);
        startActivityForResult(intent, SET_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_LOCATION && resultCode == RESULT_OK) {
            CarData cd = (CarData) data.getSerializableExtra(MainActivity.CAR_DATA);
            cdSelected.copyLocation(cd);
            DBUtils.saveCarData();
            resetList();
        }
    }
}
