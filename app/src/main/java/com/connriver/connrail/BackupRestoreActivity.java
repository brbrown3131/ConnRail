package com.connriver.connrail;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupRestoreActivity extends AppCompatActivity {

    private static final int PATH_SELECT_BACKUP = 0;
    private static final int PATH_SELECT_RESTORE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        final Button btnRestore = (Button) findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                restoreDB();
            }
        });

        final Button btnBackup = (Button) findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backupDB();
            }
        });
    }

    private void messageOverWrite() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getResources().getString(R.string.msg_overwrite_sure));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showReadChooser();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    private void restoreDB() {

        // if permission.READ_EXTERNAL_STORAGE not allowed - return
        if (!isReadAllowed()) {
            return;
        }
        messageOverWrite();
    }

    private void showReadChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PATH_SELECT_RESTORE);
    }

    private void backupDB() {

        // if permission.WRITE_EXTERNAL_STORAGE not allowed - return
        if (!isWriteAllowed()) {
            return;
        }

        showWriteChooser();
    }

    private void showWriteChooser() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, DBUtils.getDbName() + ".bak");
        startActivityForResult(intent, PATH_SELECT_BACKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PATH_SELECT_BACKUP:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // copy current db to selected uri
                    backupCopy(uri);
                }
                break;
            case PATH_SELECT_RESTORE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // copy current db to selected uri
                    restoreCopy(uri);
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void backupCopy(Uri uri) {
        try {
            InputStream is = new FileInputStream(getDbPath());
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.output_error), this);
                return;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
        Utils.messageBox(getResources().getString(R.string.success), getResources().getString(R.string.backup_done), this);
    }

    private void restoreCopy(Uri uri) {

        try {
            int length;
            byte[] buffer = new byte[1024];

            // open for single read to see if file is empty
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.input_error), this);
                return;
            }

            // check for empty file and error/return
            if (is.read(buffer) <= 0) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.restore_empty), this);
                is.close();
                return;
            }
            is.close(); //close the file and reopen for actual import


            is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Utils.messageBox(getResources().getString(R.string.error), getResources().getString(R.string.input_error), this);
                return;
            }

            OutputStream os = new FileOutputStream(getDbPath());
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        // reinitialize with new data
        MainActivity.loadDBData(getBaseContext());

        Utils.messageBox(getResources().getString(R.string.success), getResources().getString(R.string.restore_done), this);
    }

    private String getDbPath() {
        Context ctx = getBaseContext();
        File fDB = ctx.getDatabasePath(DBUtils.getDbName());
        return fDB.getAbsolutePath();
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
