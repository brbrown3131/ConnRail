package com.connriver.connrail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class SpotListActivity extends AppCompatActivity {

    private ListView lv;
    SpotList sl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        lv = (ListView) findViewById(R.id.lvMain);

        sl = new SpotList(lv, getBaseContext(), MainActivity.gSpotData);
        sl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item value
                launchAddEdit(position);
            }

        });

        final Button btnSpotAdd = (Button) findViewById(R.id.btnAdd);
        btnSpotAdd.setText(R.string.button_add_spot);
        btnSpotAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchAddEdit(-1);
            }
        });
    }

    @Override
    protected void onResume() {
        sl.resetList();
        DBUtils.saveSpotData();
        super.onResume();
    }

    private void launchAddEdit(int index) {
        Intent intent = new Intent(getBaseContext(), SpotAddEdit.class);
        intent.putExtra(MainActivity.SPOT_DATA_INDEX, index);
        startActivity(intent);
    }
}
