package com.connriver.connrail;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class NetworkSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_settings);

        final Button btnImport = (Button) findViewById(R.id.btnImport);
        btnImport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                importDB();
            }
        });

        final Button btnExport = (Button) findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exportDB();
            }
        });
    }

    private void importDB() {
        //TODO

    }
    private void exportDB() {
        //TODO
    }
}
