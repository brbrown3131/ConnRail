package com.connriver.connrail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.NONE;

public class ConsistActivity extends AppCompatActivity {

    private ListView lv;
    private ArrayList<CarData> availableList = new ArrayList<>();
    private CarList carsInConsist;
    private ConsistData consistData;
    int idConsist;
    private AlertDialog adAddCar = null;
    String sCurrentTown = null;
    private CarData cdSelected = null;

    static final int SET_CAR_INFO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consist);

        lv = (ListView) findViewById(R.id.consistView);

        //get the consist ID and name
        idConsist = getIntent().getIntExtra(MainActivity.CONSIST_ID, NONE);
        consistData = Utils.getConsistFromID(idConsist);
        if (consistData == null) {
            return;
        }

        Spinner spTown = (Spinner) findViewById(R.id.spTown);
        //fill the spinner list of towns
        final ArrayList<String> townList = Utils.getTownList();
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
                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                manageCar(position);
            }
        });

        final Button btnConsistAdd = (Button) findViewById(R.id.btnConsistAdd);
        btnConsistAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addToConsist();
            }
        });

        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.consist, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.action_delete).setEnabled(Utils.getConsistSize(idConsist) == 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit:
                editConsist();
                break;
            case R.id.action_delete:
                messageDelete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateView() {
        setTitle(consistData.getName());
        carsInConsist = new CarList(lv, this, Utils.getCarsInConsist(idConsist, sCurrentTown));
        carsInConsist.setShowCurr(false);

        carsInConsist.resetList();
    }

    private void editConsist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_consist_info, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.edit_consist);

        final EditText etName = (EditText) dialogView.findViewById(R.id.etConsistName);
        final EditText etDesc = (EditText) dialogView.findViewById(R.id.etConsistDesc);
        etName.setText(consistData.getName());
        etDesc.setText(consistData.getDescription());

        builder.setPositiveButton(R.string.button_ok, null);

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        final AlertDialog ad = builder.create();
        ad.show();

        Button ok = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sName = Utils.trim(etName);
                if (badName(sName) || dupFound(sName)) {
                    return;
                }
                consistData.setName(sName);
                consistData.setDescription(Utils.trim(etDesc));
                updateView();
                ad.dismiss();
            }
        });
    }

    private boolean dupFound(String sName) {
        // check for duplicate and message if found
        ConsistData cd;
        for (int ix = 0; ix < MainActivity.gConsistData.size(); ix++) {
            cd = MainActivity.gConsistData.get(ix);
            if (cd.getID() != consistData.getID() && sName.equals(cd.getName())) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_duplicate_consist), this) ;
                return true;
            }
        }
        return false;
    }

    private boolean badName(String sName) {
        if (sName.isEmpty()) {
            Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_bad_consist_name), this);
            return true;
        }
        return false;
    }

    private void deleteConsist() {
        // only delete empty consists
        if (Utils.getConsistSize(idConsist) != 0) {
            return;
        }
        MainActivity.gConsistData.remove(consistData);
        finish();
    }

    private void updateConsistList() {
        carsInConsist.resetList();
        DBUtils.saveConsistData();
        DBUtils.saveCarData();
    }

    private void addToConsist() {

        // only show cars not already in a consist, not being held and in the current town (null = all)
        availableList.clear();
        int sessNum = MainActivity.getSessionNumber();
        for (CarData cd : Utils.getAllCars(false)) {
            if (cd.getConsist() == NONE && sessNum >= cd.getHoldUntilDay()) {
                if (sCurrentTown == null) {
                    availableList.add(cd);
                } else {
                    int id = cd.getCurrentLoc();
                    SpotData sd = Utils.getSpotFromID(id);
                    if (sd != null && sd.getTown().equals(sCurrentTown)) {
                        availableList.add(cd);
                    }
                }
            }
        }

        //launch spot list dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_car_list, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.available_cars);

        ListView lv = (ListView) dialogView.findViewById(R.id.carListView);
        final CarList clAvail = new CarList(lv, getBaseContext(), availableList);
        clAvail.setShowCurr(true);
        clAvail.setShowDest(true);
        clAvail.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarData cd = clAvail.getCarData(position);
                cd.setConsist(idConsist);
                updateConsistList();
                updateView();
                if (adAddCar != null) {
                    adAddCar.dismiss();
                }
            }
        });

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        adAddCar = builder.create();
        adAddCar.show();

    }

    private void messageDelete() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getResources().getString(R.string.msg_delete_sure));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteConsist();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    // launch dialog to select the drop spot
    private void manageCar(int index) {
        cdSelected = carsInConsist.getCarData(index);

        // launch the car info screen
        Intent intent = new Intent(this, CarInfoActivity.class);
        intent.putExtra(MainActivity.CAR_DATA, cdSelected);
        intent.putExtra(MainActivity.CAR_INFO_PARENT, MainActivity.PARENT_TRAIN);
        intent.putExtra(MainActivity.TOWN_NAME, sCurrentTown);
        startActivityForResult(intent, SET_CAR_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_CAR_INFO && resultCode == RESULT_OK) {
            CarData cd = (CarData) data.getSerializableExtra(MainActivity.CAR_DATA);
            cdSelected.copyLocation(cd);
            updateConsistList();
            updateView();
        }
    }

}
