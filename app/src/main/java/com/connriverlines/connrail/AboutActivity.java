package com.connriverlines.connrail;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.connriverlines.connrail.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvVersion = (TextView) findViewById(R.id.about_version);
        String sx = tvVersion.getText().toString();

        String sVers;
        try {
            sVers = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            sVers = getResources().getString(R.string.error);
        }

        tvVersion.setText(sx + " " + sVers);
    }
}
