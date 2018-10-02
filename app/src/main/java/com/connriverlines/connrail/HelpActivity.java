package com.connriverlines.connrail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.connriverlines.connrail.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView wv = (WebView) findViewById(R.id.helpView);
        wv.getSettings().setBuiltInZoomControls(true);

        wv.loadUrl("file:///android_asset/help.html");


    }
}
