package com.connriver.connrail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class CarListActivity extends AppCompatActivity {

    private ListView lv;
    private CarList cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        lv = (ListView) findViewById(R.id.lvMain);
        cl = new CarList(lv, getBaseContext(), MainActivity.gCarData);
        cl.setShowCurr(false);
        cl.setShowDest(false);
        cl.resetList();

        // ListView Item Click Listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchAddEdit(position);
            }

        });

        final Button btnCarAdd = (Button) findViewById(R.id.btnAdd);
        btnCarAdd.setText(R.string.button_add_car);
        btnCarAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchAddEdit(-1);
            }
        });

    }

    @Override
    protected void onResume() {
        cl.resetList();
        super.onResume();
    }

    private void launchAddEdit(int index) {
        Intent intent = new Intent(getBaseContext(), CarAddEdit.class);
        intent.putExtra(MainActivity.CAR_DATA_INDEX, index);
        startActivity(intent);
    }
}
