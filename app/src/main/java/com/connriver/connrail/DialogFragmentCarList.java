package com.connriver.connrail;

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

import android.widget.ListView;


import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.INTENT_UPDATE_DATA;
import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 3/16/2018
 */

public class DialogFragmentCarList extends DialogFragment {

    private CarList clAvail;
    private ListView lvCars;
    private String sTown = null;
    private final ArrayList<CarData> availableList = new ArrayList<>();
    private int idConsist = NONE;

    static DialogFragmentCarList newInstance() {
        return new DialogFragmentCarList();
    }

    public void setTown(String sx) {
        sTown = sx;
    }

    public void setConsistID(int id) {
        idConsist = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.available_cars);

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
        // only show cars not already in a consist, not being held and in the current town (null = all)
        availableList.clear();
        int sessNum = MainActivity.getSessionNumber();
        for (CarData cd : Utils.getAllCars(false)) {
            if (cd.getConsist() == NONE && sessNum >= cd.getHoldUntilDay()) {
                if (sTown == null) {
                    availableList.add(cd);
                } else {
                    int id = cd.getCurrentLoc();
                    SpotData sd = Utils.getSpotFromID(id);
                    if (sd != null && sd.getTown().equals(sTown)) {
                        availableList.add(cd);
                    }
                }
            }
        }

        clAvail = new CarList(lvCars, getActivity(), availableList);
        clAvail.setShowCurr(true);
        clAvail.setShowDest(true);
        clAvail.resetList();
    }

    private View createView() {
        View dialogView = View.inflate(getActivity(), R.layout.dialog_car_list, null);

        // load all the controls
        lvCars = (ListView) dialogView.findViewById(R.id.carListView);

        updateView();

        // ListView Item Click Listener
        lvCars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarData cd = clAvail.getCarData(position);
                cd.setConsist(idConsist);
                MainActivity.carAddEditDelete(cd, false);
                done();
            }
        });

        return dialogView;
    }


    public interface DialogListener {
        void updateResult();
    }

    private void done() {
        DialogListener activity = (DialogListener) getActivity();
        activity.updateResult();
        dismiss();
    }

}
