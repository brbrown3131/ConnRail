package com.connriverlines.connrail;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.connriverlines.connrail.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TownFragment extends Fragment {
    private ListView lv;
    private SpotList sl;
    private String sCurrentTown = null;
    private Listener mCallback;
    private Spinner spTown;
    private ArrayList<String> townList;

    public interface Listener {
        void onSpotSelected(int id, String sTown);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sCurrentTown = getArguments().getString(MainActivity.TOWN_NAME);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_town, container, false);

        spTown = (Spinner) view.findViewById(R.id.spTown);

        fillTownList();

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

        lv = (ListView) view.findViewById(R.id.lvSpots);
        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setSpot(position);
            }

        });

        return view;
    }

    private void fillTownList() {
        //fill the spinner list of towns
        townList = Utils.getTownList(true, getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, townList);
        spTown.setAdapter(adapter);
        if (sCurrentTown != null) {
            for (int ix = 1; ix < townList.size(); ix++) {
                if (townList.get(ix).equals(sCurrentTown)) {
                    spTown.setSelection(ix);
                    break;
                }
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fillTownList();
            updateView();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter(MainActivity.INTENT_UPDATE_DATA));
    }

    private void updateView() {
        sl = new SpotList(lv, getActivity(), Utils.getSpotsInTown(sCurrentTown));
        sl.resetList();
    }

    private void setSpot(int pos) {
        SpotData sd = sl.getSpotData(pos);
        mCallback.onSpotSelected(sd.getID(), sCurrentTown);
    }

}
