package com.connriver.connrail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class SpotAddEdit extends AppCompatActivity {
    private int ixEdit = -1;
    private EditText etTown;
    private EditText etIndustry;
    private EditText etTrack;
    private Button btnSave;
    private Button btnDelete;
    private SpotData sdEdit = null;

    private int len(CharSequence cs) {
        return cs.toString().trim().length();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_spot_add_edit);

        etTown = (EditText) findViewById(R.id.etSpotTown);
        etIndustry = (EditText) findViewById(R.id.etSpotIndustry);
        etTrack = (EditText) findViewById(R.id.etSpotTrack);

        btnSave = (Button) findViewById(R.id.btnSpotSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        btnDelete = (Button) findViewById(R.id.btnSpotDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                messageDelete();
            }
        });

        ixEdit = getIntent().getIntExtra(MainActivity.SPOT_DATA_INDEX, -1);
        if (ixEdit != -1) {
            btnDelete.setEnabled(true);
            sdEdit = MainActivity.gSpotData.get(ixEdit);
            etTown.setText(sdEdit.getTown());
            etIndustry.setText(sdEdit.getIndustry());
            etTrack.setText(sdEdit.getTrack());
        }

        // automatically show the keyboard on a new but not an edit
        etTown.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (etTown.getText().length() == 0) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    }
                }
            }
        });

        etTown.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSave.setEnabled(len(s) > 0);
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
                btnSave.setEnabled(Utils.len(etTown) > 0);
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
                btnSave.setEnabled(Utils.len(etTown) > 0);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


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
                        if (!saveSpot()) {
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
        if (!saveSpot()) {
            return;
        }
        finish();
    }

    private boolean dupFound() {
        // check for duplicate and message if found
        SpotData sd;
        String sTown = Utils.trim(etTown);
        String sInd = Utils.trim(etIndustry);
        String sTrack = Utils.trim(etTrack);
        for (int ix = 0; ix < MainActivity.gSpotData.size(); ix++) {
            if (ix != ixEdit) { // ignore the currently edited
                sd = MainActivity.gSpotData.get(ix);
                if (sTown.equals(sd.getTown()) && sInd.equals(sd.getIndustry()) && sTrack.equals(sd.getTrack())) {
                    Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_duplicate_spot), this);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean saveSpot() {
        // check for duplicate and message if found
        if (dupFound()) {
            return false;
        }

        if (sdEdit == null) { //new
            sdEdit = new SpotData();
        }

        sdEdit.setTown(Utils.trim(etTown));
        sdEdit.setIndustry(Utils.trim(etIndustry));
        sdEdit.setTrack(Utils.trim(etTrack));

        if (ixEdit == -1) { //new
            MainActivity.gSpotData.add(sdEdit);
        }
        return true;
    }

    // get how many cars are currently using this spot
    private int getUseCount(int idSpot) {
        int count = 0;
        for (CarData cd : MainActivity.gCarData) {
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
        if (ixEdit != -1) {
            MainActivity.gSpotData.remove(ixEdit);
        }
        Utils.removeAllDeadSpots();
        finish();
    }

}
