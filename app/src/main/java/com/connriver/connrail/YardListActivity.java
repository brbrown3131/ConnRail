package com.connriver.connrail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class YardListActivity extends AppCompatActivity {

    private ListView lv;
    private CarList cl;
    CarData cdSelected = null;
    String sCurrentTown = null;

    static final int SET_CAR_INFO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yard_list);

        lv = (ListView) findViewById(R.id.carListView);
        Spinner spTown = (Spinner) findViewById(R.id.spTown);
        //fill the spinner list of towns
        final ArrayList<String> townList = Utils.getTownList(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, townList);
        spTown.setAdapter(adapter);

        spTown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    sCurrentTown = null;
                } else {
                    sCurrentTown = townList.get(position);
                }
                fillCarList(sCurrentTown);
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

     // based on which town selected, display what cars are in that town
    private void fillCarList(String sx) {
        cl = new CarList(lv, getBaseContext(), Utils.getCarsInTown(sx));
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
            cdSelected.copyLocation(cd);
            DBUtils.saveCarData();
            cl.resetList();
        }
    }
}
