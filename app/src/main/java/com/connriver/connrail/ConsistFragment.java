package com.connriver.connrail;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConsistFragment extends Fragment {

    private ListView lv;
    private ConsistList cl;
    Listener mCallback;

    public interface Listener {
        public void onConsistSelected(int id);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consist, container, false);

        lv = (ListView) view.findViewById(R.id.lvTrains);
        cl = new ConsistList(lv, getContext());

        cl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setTrain(position);

            }

        });

        return view;

    }

    private void setTrain(int pos) {
        ConsistData cd = MainActivity.gConsistData.get(pos);
        mCallback.onConsistSelected(cd.getID());
    }

}
