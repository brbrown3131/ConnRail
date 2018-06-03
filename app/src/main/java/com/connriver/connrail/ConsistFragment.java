package com.connriver.connrail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
        View view = inflater.inflate(R.layout.activity_list_add, container, false);

        lv = (ListView) view.findViewById(R.id.lvMain);

        cl = new ConsistList(lv, getContext());

        cl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setTrain(position);

            }

        });

        final Button btnConsistAdd = (Button) view.findViewById(R.id.btnAdd);
        btnConsistAdd.setText(R.string.button_add_train);
        btnConsistAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addConsist();
            }
        });

        return view;

    }

    private boolean dupFound(String sName) {
        // check for duplicate and message if found
        ConsistData cd;
        for (int ix = 0; ix < MainActivity.gConsistData.size(); ix++) {
            cd = MainActivity.gConsistData.get(ix);
            if (sName.equals(cd.getName())) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_duplicate_consist), getActivity()) ;
                return true;
            }
        }
        return false;
    }

    private boolean badName(String sName) {
        if (sName.isEmpty()) {
            Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_bad_consist_name), getActivity());
            return true;
        }
        return false;
    }

    private void addConsist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_consist_info, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.new_consist);

        final EditText etName = (EditText) dialogView.findViewById(R.id.etConsistName);
        final EditText etDesc = (EditText) dialogView.findViewById(R.id.etConsistDesc);
        builder.setPositiveButton(R.string.button_ok, null);

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        final AlertDialog ad = builder.create();
        ad.show();

        ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Button ok = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sName = Utils.trim(etName);
                if (badName(sName) || dupFound(sName)) {
                    return;
                }
                ConsistData cd = new ConsistData(sName, Utils.trim(etDesc));
                MainActivity.gConsistData.add(cd);
                ad.dismiss();
                update();
            }
        });
    }

    private void update() {
        cl.resetList();
        DBUtils.saveConsistData();
    }

    private void setTrain(int pos) {
        ConsistData cd = MainActivity.gConsistData.get(pos);
        mCallback.onConsistSelected(cd.getID());
    }

}
