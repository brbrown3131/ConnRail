package com.connriver.connrail;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.CARDATA_SPOT_MAX;
import static com.connriver.connrail.MainActivity.INTENT_UPDATE_DATA;
import static com.connriver.connrail.MainActivity.MSG_DATA_TAG;
import static com.connriver.connrail.MainActivity.MSG_DELETE_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_CAR_DATA;

public class CarAddEditActivity extends AppCompatActivity {
    private TextInputEditText etInit;
    private TextInputEditText etNum;
    private AutoCompleteTextView actvType;
    private TextInputEditText etNotes;
    private Button btnAddSpot;
    private Button btnSave;
    private CarData cdEdit = null;
    private boolean bCarSpotDataChanged = false;

    private StaticListView lvCarSpots;
    private ArrayList<CarSpotData> listCarSpotData = null;

    private AlertDialog adAddSpot = null;

    private static final String SPOTLIST = "SpotList";
    private static final String DATACHANGED = "DataChanged";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_car_add_edit);

        etInit = (TextInputEditText) findViewById(R.id.etCarInitials);
        etNum = (TextInputEditText) findViewById(R.id.etCarNumber);
        actvType = (AutoCompleteTextView) findViewById(R.id.actvCarType);
        actvType.setThreshold(1);
        String[] types = getResources().getStringArray(R.array.car_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        actvType.setAdapter(adapter);

        etNotes = (TextInputEditText) findViewById(R.id.etCarNotes);
        lvCarSpots = (StaticListView) findViewById(R.id.lvCarSpots);

        // automatically show the keyboard on a new but not an edit
        etInit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (etInit.getText().length() == 0) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    }
                }
            }
        });

        etInit.addTextChangedListener(new TextWatcher() {
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

        etNum.addTextChangedListener(new TextWatcher() {
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

        // automatically show the list if blank
        actvType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && actvType.getText().toString().trim().length() == 0) {
                    actvType.showDropDown();
                }
            }
        });

        etNotes.addTextChangedListener(new TextWatcher() {
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

        actvType.addTextChangedListener(new TextWatcher() {
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

        btnAddSpot = (Button) findViewById(R.id.btnSpotAdd);
        btnAddSpot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAddSpotClicked(v);
            }
        });

        btnSave = (Button) findViewById(R.id.btnCarSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        Button btnDelete = (Button) findViewById(R.id.btnCarDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onDeleteClicked();
            }
        });

        cdEdit = (CarData) getIntent().getSerializableExtra(MainActivity.CAR_DATA);
        if (cdEdit == null) {
            cdEdit = new CarData();
        } else {
            btnDelete.setEnabled(true);
            showData();
        }

        // if re-create after screen rotation, load the saved copy of car spot and boolean
        if (savedInstanceState != null) {
            listCarSpotData = (ArrayList<CarSpotData>)savedInstanceState.getSerializable(SPOTLIST);
            bCarSpotDataChanged = savedInstanceState.getBoolean(DATACHANGED);
            updateSaveButton();
        }

        // create/init the list of car spots
        // get a copy of the CarSpotData list from the CarData
        if (listCarSpotData == null) {
            listCarSpotData = cdEdit.getCarSpotDataCopy();
        }

        // ListView Item Click Listener
        updateSpotList();
        lvCarSpots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSpotSelected(position);
            }
        });
    }

    private void showData() {
        etInit.setText(cdEdit.getInitials());
        etNum.setText(cdEdit.getNumber());
        actvType.setText(cdEdit.getType());
        etNotes.setText(cdEdit.getNotes());
    }

    private void updateSpotData() {
        listCarSpotData = cdEdit.getCarSpotDataCopy();
        updateSpotList();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (cdEdit == null) {
                return;
            }
            int msgType = intent.getIntExtra(MSG_TYPE_TAG, -1);
            if (msgType == MSG_DELETE_CAR_DATA || msgType == MSG_UPDATE_CAR_DATA) {
                String sMsgData = intent.getStringExtra(MSG_DATA_TAG);
                try {
                    CarData cd =  new CarData(new JSONObject(sMsgData));
                    if (cd.getID() == cdEdit.getID()) {
                        if (msgType == MSG_DELETE_CAR_DATA) {
                            finish();
                        } else {
                            cdEdit = cd;
                            showData();
                            updateSpotData();
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

    // on screen rotate save the current spot list
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putSerializable(SPOTLIST, listCarSpotData);
        savedState.putBoolean(DATACHANGED, bCarSpotDataChanged);
    }

    private void updateSpotList() {
        CarSpotDataAdapter adapter = new CarSpotDataAdapter(this, listCarSpotData);
        lvCarSpots.setAdapter(adapter);

        setListHeight(adapter);

        btnAddSpot.setEnabled(listCarSpotData.size() < CARDATA_SPOT_MAX);
    }

    private void setListHeight(CarSpotDataAdapter adapter) {
        int numberOfItems = adapter.getCount();

        // Get total height of all items.
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = adapter.getView(itemPos, null, lvCarSpots);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }

        // Get total height of all item dividers.
        int totalDividersHeight = lvCarSpots.getDividerHeight() * (numberOfItems - 1);

        // Set list height.
        ViewGroup.LayoutParams params = lvCarSpots.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        lvCarSpots.setLayoutParams(params);
    }

    private void onSpotSelected(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_spot_days, null);
        builder.setView(dialogView);
        builder.setTitle(Utils.trim(etInit) + " " + Utils.trim(etNum));

        TextView tvSpotTown = (TextView) dialogView.findViewById(R.id.tvSpotTown);
        TextView tvSpotIndustry = (TextView) dialogView.findViewById(R.id.tvSpotIndustry);
        TextView tvSpotTrack = (TextView) dialogView.findViewById(R.id.tvSpotTrack);
        final TextView tvSpotLading = (TextView) dialogView.findViewById(R.id.tvSpotLading);
        final TextView tvSpotInst = (TextView) dialogView.findViewById(R.id.tvSpotInstructions);
        final ImageView ivEdit = (ImageView) dialogView.findViewById(R.id.btnEdit);
        final LinearLayout llEdit = (LinearLayout) dialogView.findViewById(R.id.llLadingInst);
        final NumberPicker npDays = (NumberPicker) dialogView.findViewById(R.id.npDays);

        final CarSpotData csd = listCarSpotData.get(position);
        final CarSpotData csdOriginal = new CarSpotData(csd);
        int id = csd.getID();
        if (id == -1) {
            return;
        }
        SpotData sd = Utils.getSpotFromID(id);
        if (sd == null) {
            return;
        }

        ivEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editLadingInstruct(tvSpotLading, tvSpotInst);
            }
        });

        llEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editLadingInstruct(tvSpotLading, tvSpotInst);
            }
        });

        tvSpotTown.setText(sd.getTown());
        tvSpotIndustry.setText(sd.getIndustry());
        tvSpotTrack.setText(sd.getTrack());
        tvSpotLading.setText(csd.getLading());
        tvSpotInst.setText(csd.getInstructions());

        npDays.setMaxValue(7);
        npDays.setMinValue(0);
        npDays.setWrapSelectorWheel(false);
        npDays.setValue(csd.getHoldDays());

        builder.setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        csd.setHoldDays(npDays.getValue());
                        csd.setLading(tvSpotLading.getText().toString());
                        csd.setInstructions(tvSpotInst.getText().toString());

                        if (!csd.equals(csdOriginal)) {
                            bCarSpotDataChanged = true;
                        }
                        updateSpotList();
                        updateSaveButton();
                    }
                });
        builder.setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteSpot(position);
                    }
                });
        AlertDialog ad = builder.create();
        ad.show();
    }

    private void editLadingInstruct(final TextView tvLading, final TextView tvInstruct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_car_spot_info, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.lading_title);

        final TextInputEditText etLading = (TextInputEditText) dialogView.findViewById(R.id.etLading);
        final TextInputEditText etInstruct = (TextInputEditText) dialogView.findViewById(R.id.etInstruct);
        etLading.setText(tvLading.getText());
        etInstruct.setText(tvInstruct.getText());

        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                tvLading.setText(Utils.trim(etLading));
                tvInstruct.setText(Utils.trim(etInstruct));
            }
        });

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        final AlertDialog ad = builder.create();
        ad.show();
        Window win = ad.getWindow();
        if (win != null) {
            win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    // enable the save button if all requirements met
    private void updateSaveButton() {
        String sInit = Utils.trim(etInit);
        String sNum = Utils.trim(etNum);
        String sNotes = Utils.trim(etNotes);
        String sType = actvType.getText().toString().trim();
        btnSave.setEnabled(sInit.length() > 0 && sNum.length() > 0 && sType.length() > 0 &&
                (!sInit.equals(cdEdit.getInitials()) ||
                !sNum.equals(cdEdit.getNumber()) ||
                !sNotes.equals(cdEdit.getNotes()) ||
                !sType.equals(cdEdit.getType()) ||
                bCarSpotDataChanged));
    }

    private void deleteSpot(int pos) {
        listCarSpotData.remove(pos);
        bCarSpotDataChanged = true;

        updateSpotList();
        updateSaveButton();
    }

    private void messageSave(String sMessage) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(sMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.button_save),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (saveCarAbort()) {
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

    private void onAddSpotClicked(View view) {
        if (lvCarSpots.getCount() >= CARDATA_SPOT_MAX) {
            return;
        }

        // hide the keyboard, if showing
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //launch spot list dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_spot_list, null);
        builder.setView(dialogView);
        builder.setTitle(getResources().getString(R.string.select_spot));

        dialogView.findViewById(R.id.tvCurrLoc).setVisibility(View.GONE);
        dialogView.findViewById(R.id.spTown).setVisibility(View.GONE);

        ListView lv = (ListView) dialogView.findViewById(R.id.spotListView);
        final SpotList sl = new SpotList(lv, getBaseContext(), Utils.getSpotsInTown(null));
        sl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotData sd = sl.getSpotData(position);
                CarSpotData csd = new CarSpotData(sd.getID(), 0); // 0 days by default
                listCarSpotData.add(csd);
                bCarSpotDataChanged = true;
                updateSpotList();
                updateSaveButton();
                if (adAddSpot != null) {
                    adAddSpot.dismiss();
                }
            }
        });

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        adAddSpot = builder.create();
        adAddSpot.show();
    }

    private boolean dupFound() {
        // check for duplicate and message if found
        String init = Utils.trim(etInit);
        String num = Utils.trim(etNum);
        for (CarData cd : MainActivity.getCarList()) {
            if (cd.getID() != cdEdit.getID()) { // ignore the currently edited
                if (init.equals(cd.getInitials()) && num.equals(cd.getNumber())) {
                    Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_duplicate_car), this);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean invalidSpots() {
        int size = listCarSpotData.size();

        // return true if there are fewer than 2 spots
        if (size < 2) {
            Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_minimum_two), this);
            return true;
        }

        // return true if first and last same spot
        if (listCarSpotData.get(0).getID() == listCarSpotData.get(size - 1).getID()) {
            Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_first_last), this);
            return true;
        }

        // return true if same spot twice in a row
        int prevID = listCarSpotData.get(0).getID();
        for (int ix = 1; ix < size; ix++) {
            if (listCarSpotData.get(ix).getID() == prevID) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_diff_seq), this);
                return true;
            }
            prevID = listCarSpotData.get(ix).getID();
        }


        return false; //ok
    }

    private boolean saveCarAbort() {
        // check for duplicate and message if found
        if (dupFound()) {
            return true;
        }

        // check for valid spots and message if found
        if (invalidSpots()) {
            return true;
        }

        cdEdit.setInitials(Utils.trim(etInit));
        cdEdit.setNumber(Utils.trim(etNum));
        cdEdit.setType(actvType.getText().toString().trim());
        cdEdit.setNotes(Utils.trim(etNotes));
        cdEdit.setCarSpotData(listCarSpotData);

        MainActivity.carAddEditDelete(cdEdit, false);

        return false; // save successful
    }

    private void onSaveClicked() {
        if (saveCarAbort()) {
            return;
        }
        finish();
    }

    private void onDeleteClicked() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getResources().getString(R.string.msg_delete_sure));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCar();
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

    private void deleteCar() {
        MainActivity.carAddEditDelete(cdEdit, true);
        finish();
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
            messageSave(getResources().getString(R.string.msg_save_car_changes));
            return false;
        } else {
            return true;
        }
    }

    private static class ViewHolder {
        private TextView tvSpotTown;
        private TextView tvSpotIndustry;
        private TextView tvSpotTrack;
        private TextView tvSpotDays; // days to hold car at this spot
    }

    private class CarSpotDataAdapter extends ArrayAdapter<CarSpotData> {
        private CarSpotDataAdapter(Context context, ArrayList<CarSpotData> carSpotData) {
            super(context, 0, carSpotData);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spot_list, parent, false);

                holder = new ViewHolder();
                holder.tvSpotTown = (TextView) convertView.findViewById(R.id.tvSpotTown);
                holder.tvSpotIndustry = (TextView) convertView.findViewById(R.id.tvSpotIndustry);
                holder.tvSpotTrack = (TextView) convertView.findViewById(R.id.tvSpotTrack);
                holder.tvSpotDays = (TextView) convertView.findViewById(R.id.tvSpotDays);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CarSpotData csd = getItem(position);
            if (csd == null) {
                return convertView;
            }
            SpotData sd = Utils.getSpotFromID(csd.getID());

            if (sd == null) {
                holder.tvSpotTown.setText(getResources().getString(R.string.status_unknown));
                holder.tvSpotIndustry.setText("");
                holder.tvSpotTrack.setText("");

            } else {
                holder.tvSpotTown.setText(sd.getTown());
                holder.tvSpotIndustry.setText(sd.getIndustry());
                holder.tvSpotTrack.setText(sd.getTrack());
                holder.tvSpotDays.setText(String.valueOf(csd.getHoldDays()) + " " + (csd.getHoldDays() == 1 ? getResources().getString(R.string.text_day) : getResources().getString(R.string.text_days)));
            }

            return convertView;
        }
    }
}
