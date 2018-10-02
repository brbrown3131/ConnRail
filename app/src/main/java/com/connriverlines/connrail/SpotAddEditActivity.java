package com.connriverlines.connrail;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.connriverlines.connrail.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.connriverlines.connrail.MainActivity.INTENT_UPDATE_DATA;
import static com.connriverlines.connrail.MainActivity.MSG_DATA_TAG;
import static com.connriverlines.connrail.MainActivity.MSG_DELETE_SPOT_DATA;
import static com.connriverlines.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriverlines.connrail.MainActivity.MSG_UPDATE_SPOT_DATA;

public class SpotAddEditActivity extends AppCompatActivity {
    private AutoCompleteTextView actvTown;
    private TextInputEditText etIndustry;
    private TextInputEditText etTrack;
    private Button btnSave;
    private SpotData sdEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_spot_add_edit);

        actvTown = (AutoCompleteTextView) findViewById(R.id.actvSpotTown);
        actvTown.setThreshold(1);
        final ArrayList<String> townList = Utils.getTownList(false, this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, townList);
        actvTown.setAdapter(adapter);

        etIndustry = (TextInputEditText) findViewById(R.id.etSpotIndustry);
        etTrack = (TextInputEditText) findViewById(R.id.etSpotTrack);

        btnSave = (Button) findViewById(R.id.btnSpotSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        Button btnDelete = (Button) findViewById(R.id.btnSpotDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messageDelete();
            }
        });

        sdEdit = (SpotData) getIntent().getSerializableExtra(MainActivity.SPOT_DATA);
        if (sdEdit != null) {
            btnDelete.setEnabled(true);
            showData();
        }

        // automatically show the keyboard on a new but not an edit
        actvTown.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (actvTown.getText().length() == 0) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                        actvTown.dismissDropDown();
                    }
                }
            }
        });

        actvTown.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButton();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etIndustry.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButton();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etTrack.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButton();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void showData() {
        actvTown.setText(sdEdit.getTown());
        etIndustry.setText(sdEdit.getIndustry());
        etTrack.setText(sdEdit.getTrack());
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (sdEdit == null) {
                return;
            }
            int msgType = intent.getIntExtra(MSG_TYPE_TAG, -1);
            if (msgType == MSG_DELETE_SPOT_DATA || msgType == MSG_UPDATE_SPOT_DATA) {
                String sMsgData = intent.getStringExtra(MSG_DATA_TAG);
                try {
                    SpotData sd =  new SpotData(new JSONObject(sMsgData));
                    if (sd.getID() == sdEdit.getID()) {
                        //TODO - toast or msg about delete/update?
                        if (msgType == MSG_DELETE_SPOT_DATA) {
                            finish();
                        } else {
                            sdEdit = sd;
                            showData();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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

    // enable the save button if all requirements met
    private void updateSaveButton() {
        String sTown = actvTown.getText().toString().trim();
        String sIndustry = Utils.trim(etIndustry);
        String sTrack = Utils.trim(etTrack);
        btnSave.setEnabled(sTown.length() > 0 &&
                (sdEdit == null || !sTown.equals(sdEdit.getTown()) || !sIndustry.equals(sdEdit.getIndustry()) || !sTrack.equals(sdEdit.getTrack())));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!checkSave()) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (checkSave()) {
            super.onBackPressed();
        }
    }

    //return true if no changes or user wants to discard changes
    //return false if user wants to stay on page
    private boolean checkSave() {
        if (btnSave.isEnabled()) {
            messageSave();
            return false;
        } else {
            return true;
        }
    }

    private void messageSave() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getResources().getString(R.string.msg_save_spot_changes));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.button_save),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (saveSpotAbort()) {
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            finish();
                        }
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.button_dont_save),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
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

    private void onSaveClicked() {
        if (saveSpotAbort()) {
            return;
        }
        finish();
    }

    private boolean dupFound() {
        // check for duplicate and message if found
        String sTown = actvTown.getText().toString().trim();
        String sInd = Utils.trim(etIndustry);
        String sTrack = Utils.trim(etTrack);
        for (SpotData sd : MainActivity.getSpotList()) {
            if (sdEdit == null || sdEdit.getID() != sd.getID()) { // ignore the currently edited
                if (sTown.equals(sd.getTown()) && sInd.equals(sd.getIndustry()) && sTrack.equals(sd.getTrack())) {
                    Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_duplicate_spot), this);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean saveSpotAbort() {
        // check for duplicate and message if found
        if (dupFound()) {
            return true;
        }

        SpotData sd = new SpotData();
        if (sdEdit != null) {
            sd.setID(sdEdit.getID());
        }

        sd.setTown(actvTown.getText().toString().trim());
        sd.setIndustry(Utils.trim(etIndustry));
        sd.setTrack(Utils.trim(etTrack));

        MainActivity.spotAddEditDelete(sd, false);
        return false;
    }

    // get how many cars are currently using this spot
    private int getUseCount(int idSpot) {
        int count = 0;
        for (CarData cd : MainActivity.getCarList()) {
            if (cd.usesSpotID(idSpot)) {
                count++;
            }
        }
        return count;
    }

    private void messageDelete() {
        int iUse = getUseCount(sdEdit.getID());
        String sx;
        if (iUse == 0) {
            sx = getResources().getString(R.string.msg_delete_sure);
        } else {
            sx = getResources().getString(R.string.msg_car_use) + " " + iUse + " " +
                    (iUse == 1 ? getResources().getString(R.string.car) : getResources().getString(R.string.cars)) +
                    ". " + getResources().getString(R.string.msg_delete_sure);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(sx);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSpot();
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

    private void deleteSpot() {
        if (sdEdit != null) {
            MainActivity.spotAddEditDelete(sdEdit, true);
        }

        Utils.removeAllDeadSpots();

        finish();
    }

}
