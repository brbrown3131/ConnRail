package com.connriverlines.connrail;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.connriverlines.connrail.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConsistFragment extends Fragment {

    private ConsistList cl;
    private Listener mCallback;

    public interface Listener {
        void onConsistSelected(int id);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_list_add, container, false);

        ListView lv = (ListView) view.findViewById(R.id.lvMain);

        cl = new ConsistList(lv, getContext(), MainActivity.getConsistList());

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
        for (ConsistData cd : MainActivity.getConsistList()) {
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
        final View dialogView = View.inflate(getActivity(), R.layout.dialog_consist_info, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.new_consist);

        final TextInputEditText etName = (TextInputEditText) dialogView.findViewById(R.id.etConsistName);
        final TextInputEditText etDesc = (TextInputEditText) dialogView.findViewById(R.id.etConsistDesc);
        builder.setPositiveButton(R.string.button_ok, null);

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

        Button ok = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sName = Utils.trim(etName);
                if (badName(sName) || dupFound(sName)) {
                    return;
                }
                ConsistData cd = new ConsistData(sName, Utils.trim(etDesc));
                MainActivity.consistAddEditDelete(cd, false);
                ad.dismiss();
                cl.resetList();
            }
        });
    }


    private void setTrain(int pos) {
        ConsistData cd = cl.getConsistData(pos);
        mCallback.onConsistSelected(cd.getID());
    }

}
