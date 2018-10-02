package com.connriverlines.connrail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.connriverlines.connrail.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.connriverlines.connrail.MainActivity.INTENT_UPDATE_DATA;
import static com.connriverlines.connrail.MainActivity.MSG_DATA_TAG;
import static com.connriverlines.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriverlines.connrail.MainActivity.MSG_UPDATE_CAR_DATA;

/**
 * Created by bbrown on 3/16/2018
 */

public class DialogFragmentSpotList extends DialogFragment {

    private CarData carData;
    private String sTown = null;
    private SpotList spotList;

    private ListView lvSpots;
    private Spinner spTown;

    static DialogFragmentSpotList newInstance() {
        return new DialogFragmentSpotList();
    }

    void setTown(String sx) {
        sTown = sx;
    }

    void setCarData(CarData cd) {
        carData = cd;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (carData == null) {
            return null;
        }

        builder.setTitle(R.string.setout_spot);

        builder.setView(createView());

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int msgType = intent.getIntExtra(MSG_TYPE_TAG, -1);
            if (msgType == MSG_UPDATE_CAR_DATA) { // if current car deleted, parent will finish and take this dialog with it
                String sMsgData = intent.getStringExtra(MSG_DATA_TAG);
                try {
                    carData =  new CarData(new JSONObject(sMsgData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            updateView();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(INTENT_UPDATE_DATA));
    }

    private void updateView() {

        //fill the spinner list of towns
        final ArrayList<String> townList = Utils.getTownList(true, getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, townList);
        spTown.setAdapter(adapter);

        if (sTown != null) {
            for (int ix = 1; ix < townList.size(); ix++) {
                if (sTown.equalsIgnoreCase(townList.get(ix))) {
                    spTown.setSelection(ix);
                    break;
                }

            }
        }

        spTown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    fillSpotList(null);
                } else {
                    fillSpotList(townList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private View createView() {
        View dialogView = View.inflate(getActivity(), R.layout.dialog_spot_list, null);

        // load all the controls
        lvSpots = (ListView) dialogView.findViewById(R.id.spotListView);
        spTown = (Spinner) dialogView.findViewById(R.id.spTown);

        updateView();

        // ListView Item Click Listener
        lvSpots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotData sd = spotList.getSpotData(position);
                carData.setCurrentLoc(sd.getID());
                done();
            }
        });

        return dialogView;
    }

    private void fillSpotList(String sTown) {
        // fill the spot list
        this.sTown = sTown;
        spotList = new SpotList(lvSpots, getActivity(), Utils.getSpotsInTown(sTown));
        spotList.resetList();
    }

    interface DialogListener {
        void updateResult();
    }

    private void done() {
        DialogListener activity = (DialogListener) getActivity();
        activity.updateResult();
        dismiss();
    }

}
