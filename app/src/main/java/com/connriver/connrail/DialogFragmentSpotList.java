package com.connriver.connrail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 3/16/2018.
 */

public class DialogFragmentSpotList extends DialogFragment {

    private CarData cd;
    private String sTown = null;
    private boolean bShowCurrentTown = true;
    SpotList spotList;

    private ListView lvSpots;
    private TextView tvCurr;
    private Spinner spTown;

    static DialogFragmentSpotList newInstance() {
        return new DialogFragmentSpotList();
    }

    public void setTown(String sx) {
        sTown = sx;
    }

    public String getTown() {
        return sTown;
    }

    public void setCarData(CarData cd) {
        this.cd = cd;
    }

    public void setShowCurrentTown(boolean bx) {
        bShowCurrentTown = bx;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (cd == null) {
            return null;
        }

        builder.setTitle(cd.getInfo());

        builder.setView(createView());

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private View createView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_spot_list, null);

        // load all the controls
        lvSpots = (ListView) dialogView.findViewById(R.id.spotListView);
        tvCurr = (TextView) dialogView.findViewById(R.id.tvCurrLoc);
        spTown = (Spinner) dialogView.findViewById(R.id.spTown);

        // if we want to display the list of towns to choose from
        if (bShowCurrentTown) {
            //fill the spinner list of towns
            final ArrayList<String> townList = Utils.getTownList();
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
        } else {
            tvCurr.setVisibility(View.GONE);
            spTown.setVisibility(View.GONE);
            fillSpotList(sTown);
        }


        // ListView Item Click Listener
        lvSpots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotData sd = spotList.getSpotData(position);
                cd.setCurrentLoc(sd.getID());
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

    public interface EditDialogListener {
        void updateResult();
    }

    private void done() {
        EditDialogListener activity = (EditDialogListener) getActivity();
        activity.updateResult();
        dismiss();
    }

}
