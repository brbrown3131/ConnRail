package com.connriverlines.connrail;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.connriverlines.connrail.R;

import static com.connriverlines.connrail.MainActivity.INTENT_UPDATE_DATA;
import static com.connriverlines.connrail.MainActivity.MSG_DELETE_CONSIST_DATA;
import static com.connriverlines.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriverlines.connrail.MainActivity.MSG_UPDATE_CONSIST_DATA;

public class ConsistListActivity extends AppCompatActivity {

    private ConsistList cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        ListView lv = (ListView) findViewById(R.id.lvMain);
        cl = new ConsistList(lv, getBaseContext(), MainActivity.getConsistList());

        cl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                manageConsist(position);
            }

        });

        final Button btnConsistAdd = (Button) findViewById(R.id.btnAdd);
        btnConsistAdd.setText(R.string.button_add_train);
        btnConsistAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addConsist();
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int msgType = intent.getIntExtra(MSG_TYPE_TAG, -1);
            if (msgType == MSG_DELETE_CONSIST_DATA || msgType == MSG_UPDATE_CONSIST_DATA) {
                cl.resetList();
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

        cl.resetList();
    }

    private boolean dupFound(String sName) {
        // check for duplicate and message if found
        for (ConsistData cd :  MainActivity.getConsistList()) {
            if (sName.equals(cd.getName())) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_duplicate_consist), this) ;
                return true;
            }
        }
        return false;
    }

    private boolean badName(String sName) {
        if (sName.isEmpty()) {
            Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.msg_bad_consist_name), this);
            return true;
        }
        return false;
    }

    private void addConsist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this, R.layout.dialog_consist_info, null);
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
                cl.resetList();
                ad.dismiss();
            }
        });
    }

    private void manageConsist(int index) {
        Intent intent = new Intent(getBaseContext(), ConsistActivity.class);
        intent.putExtra(MainActivity.CONSIST_DATA, cl.getConsistData(index));
        startActivity(intent);
    }
}
