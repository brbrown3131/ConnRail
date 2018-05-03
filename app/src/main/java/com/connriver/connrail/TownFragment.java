package com.connriver.connrail;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TownFragment extends Fragment {
    private ListView lv;
    SpotList sl;
    private String sCurrentTown = null;
    Listener mCallback;

    public interface Listener {
        public void onSpotSelected(int id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Listener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_town, container, false);

        Spinner spTown = (Spinner) view.findViewById(R.id.spTown);

        //fill the spinner list of towns
        final ArrayList<String> townList = Utils.getTownList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, townList);
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

    private void updateView() {
        sl = new SpotList(lv, getActivity(), Utils.getSpotsInTown(sCurrentTown));
        sl.resetList();
    }

    private void setSpot(int pos) {
        SpotData sd = sl.getSpotData(pos);
        mCallback.onSpotSelected(sd.getID());
    }

}
