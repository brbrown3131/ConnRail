package com.connriver.connrail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import static com.connriver.connrail.MainActivity.INTENT_UPDATE_DATA;
import static com.connriver.connrail.MainActivity.MSG_DELETE_SPOT_DATA;
import static com.connriver.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_SPOT_DATA;

public class SpotListActivity extends AppCompatActivity {

    private SpotList sl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        ListView lv = (ListView) findViewById(R.id.lvMain);

        sl = new SpotList(lv, getBaseContext(), MainActivity.getSpotList());
        sl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item value
                launchAddEdit(sl.getSpotData(position));
            }

        });

        final Button btnSpotAdd = (Button) findViewById(R.id.btnAdd);
        btnSpotAdd.setText(R.string.button_add_spot);
        btnSpotAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchAddEdit(null);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int msgType = intent.getIntExtra(MSG_TYPE_TAG, -1);
            if (msgType == MSG_DELETE_SPOT_DATA || msgType == MSG_UPDATE_SPOT_DATA) {
                sl.resetList();
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
        sl.resetList();
    }

    private void launchAddEdit(SpotData sd) {
        Intent intent = new Intent(getBaseContext(), SpotAddEditActivity.class);
        intent.putExtra(MainActivity.SPOT_DATA, sd);
        startActivity(intent);
    }
}
