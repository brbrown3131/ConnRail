package com.connriver.connrail;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

        // if permission.READ_EXTERNAL_STORAGE not allowed - return
        if (!isReadAllowed()) {
            return;
        }

        // copy backup to current db
        rawCopy(getBackupPath(), getDbPath());

        // reinitialize with new data
        MainActivity.updateData(getBaseContext());

        Utils.messageBox(null, getResources().getString(R.string.import_done) + getBackupPath(), this);
    }

    private void exportDB() {

        // if permission.WRITE_EXTERNAL_STORAGE not allowed - return
        if (!isWriteAllowed()) {
            return;
        }

        // copy current db to backup
        rawCopy(getDbPath(), getBackupPath());

        Utils.messageBox(null, getResources().getString(R.string.export_done) + getBackupPath(), this);
    }

    private void rawCopy(String src, String dest) {

        // raw copy of file data
        try {
            FileChannel fcSrc = new FileInputStream(src).getChannel();
            FileChannel fcDest = new FileOutputStream(dest).getChannel();
            fcDest.transferFrom(fcSrc, 0, fcSrc.size());
            fcSrc.close();
            fcDest.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String getDbPath() {
        Context ctx = getBaseContext();
        File fDB = ctx.getDatabasePath(DBUtils.getDbName());
        return fDB.getAbsolutePath();
    }

    private String getBackupPath() {
        // get the external storage directory
        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // path for the destination file
        String sx = sd.getAbsolutePath() + "/" + DBUtils.getDbName() + ".db";
        return sx;
    }

    private boolean isWriteAllowed() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
    private boolean isReadAllowed() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

}
