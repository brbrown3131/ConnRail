package com.connriver.connrail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.CARDATA_SPOT_MAX;

public class CarAddEdit extends AppCompatActivity {
    private int ixEdit = -1;
    private EditText etInit;
    private EditText etNum;
    private SpinnerPlus spType;
    private EditText etNotes;
    private Button btnAddSpot;
    private Button btnSave;
    private Button btnDelete;
    private CarData cdEdit = null;
    private boolean bTypeFocusIn = false;
    private boolean bCarSpotDataChanged = false;

    private ListView lvCarSpots;
    private ArrayList<CarSpotData> listCarSpotData = null;

    private AlertDialog adAddSpot = null;

    private static String SPOTLIST = "SpotList";
    private static String DATACHANGED = "DataChanged";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_car_add_edit);

        etInit = (EditText) findViewById(R.id.etCarInitials);
        etNum = (EditText) findViewById(R.id.etCarNumber);
        spType = (SpinnerPlus) findViewById(R.id.spinCarType);
        etNotes = (EditText) findViewById(R.id.etCarNotes);
        lvCarSpots = (ListView) findViewById(R.id.lvCarSpots);

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

        etNum.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionID, KeyEvent event) {
                if (actionID == EditorInfo.IME_ACTION_NEXT) {
                    bTypeFocusIn = true;
                    spType.performClick();
                    return true;
                }
                return false;
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

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateSaveButton();
                if (bTypeFocusIn) {
                    etNotes.requestFocus();
                    bTypeFocusIn = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
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

        btnDelete = (Button) findViewById(R.id.btnCarDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onDeleteClicked();
            }
        });

        ixEdit = getIntent().getIntExtra(MainActivity.CAR_DATA_INDEX, -1);
        if (ixEdit == -1) {
            cdEdit = new CarData();
        } else {
            btnDelete.setEnabled(true);
            cdEdit = MainActivity.gCarData.get(ixEdit);
            etInit.setText(cdEdit.getInitials());
            etNum.setText(cdEdit.getNumber());

            int ixType = 0;
            String sType = cdEdit.getType();
            for ( ; ixType < spType.getCount(); ixType++) {
                if (spType.getItemAtPosition(ixType).equals(sType)) {
                    break;
                }
            }
            if (ixType == spType.getCount()) {
                ixType--;
            }
            spType.setSelection(ixType);

            etNotes.setText(cdEdit.getNotes());
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void onSpotSelected(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_spot_days, null);
        builder.setView(dialogView);
        builder.setTitle(Utils.trim(etInit) + " " + Utils.trim(etNum));

        TextView tvSpotTown = (TextView) dialogView.findViewById(R.id.tvSpotTown);
        TextView tvSpotIndustry = (TextView) dialogView.findViewById(R.id.tvSpotIndustry);
        TextView tvSpotTrack = (TextView) dialogView.findViewById(R.id.tvSpotTrack);
        final TextView tvSpotLading = (TextView) dialogView.findViewById(R.id.tvSpotLading);
        final TextView tvSpotInst = (TextView) dialogView.findViewById(R.id.tvSpotInstructions);
        final ImageView ivEdit = (ImageView) dialogView.findViewById(R.id.btnEdit);
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
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_car_spot_info, null);
        builder.setView(dialogView);

        final EditText etLading = (EditText) dialogView.findViewById(R.id.etLading);
        final EditText etInstruct = (EditText) dialogView.findViewById(R.id.etInstruct);
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
        ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // enable the save button if all requirements met
    private void updateSaveButton() {
        String sInit = Utils.trim(etInit);
        String sNum = Utils.trim(etNum);
        String sNotes = Utils.trim(etNotes);
        String sType = spType.getSelectedItem().toString();
        btnSave.setEnabled(sInit.length() > 0 && sNum.length() > 0 &&
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
                        if (!saveCar()) {
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
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_spot_list, null);
        builder.setView(dialogView);
        builder.setTitle(getResources().getString(R.string.select_spot));

        dialogView.findViewById(R.id.tvCurrLoc).setVisibility(View.GONE);
        dialogView.findViewById(R.id.spTown).setVisibility(View.GONE);

        ListView lv = (ListView) dialogView.findViewById(R.id.spotListView);
        SpotList sl = new SpotList(lv, getBaseContext(), Utils.getSpotsInTown(null));
        sl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotData sd = MainActivity.gSpotData.get(position);
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
        CarData cd;
        String init = Utils.trim(etInit);
        String num = Utils.trim(etNum);
        for (int ix = 0; ix < MainActivity.gCarData.size(); ix++) {
            if (ix != ixEdit) { // ignore the currently edited
                cd = MainActivity.gCarData.get(ix);
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

    private boolean saveCar() {
        // check for duplicate and message if found
        if (dupFound()) {
            return false;
        }

        // check for valid spots and message if found
        if (invalidSpots()) {
            return false;
        }

        cdEdit.setInitials(Utils.trim(etInit));
        cdEdit.setNumber(Utils.trim(etNum));
        cdEdit.setType(spType.getSelectedItem().toString());
        cdEdit.setNotes(Utils.trim(etNotes));

        cdEdit.setCarSpotData(listCarSpotData);

        if (ixEdit == -1) { // add new
            MainActivity.gCarData.add(cdEdit);
        }

        DBUtils.saveCarData();

        return true; // save successful
    }

    private void onSaveClicked() {
        if (!saveCar()) {
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
        if (ixEdit != -1) {
            MainActivity.gCarData.remove(ixEdit);
        }
        DBUtils.saveCarData();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            CarSpotData csd = getItem(position);
            SpotData sd = Utils.getSpotFromID(csd.getID());

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

            if (sd == null) {
                holder.tvSpotTown.setText(getResources().getString(R.string.status_unknown));
                holder.tvSpotIndustry.setText("");
                holder.tvSpotTrack.setText("");

            } else {
                holder.tvSpotTown.setText(sd.getTown());
                holder.tvSpotIndustry.setText(sd.getIndustry());
                holder.tvSpotTrack.setText(sd.getTrack());
                holder.tvSpotDays.setText(Integer.toString(csd.getHoldDays()) + " " + (csd.getHoldDays() == 1 ? getResources().getString(R.string.text_day) : getResources().getString(R.string.text_days)));
            }

            return convertView;
        }
    }
}
