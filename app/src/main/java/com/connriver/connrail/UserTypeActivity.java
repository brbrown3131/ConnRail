package com.connriver.connrail;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.USER_TYPE_OWNER;
import static com.connriver.connrail.MainActivity.USER_TYPE_REMOTE;
import static com.connriver.connrail.MainActivity.OWNER_IP;
import static com.connriver.connrail.MainActivity.USER_TYPE_SINGLE;
import static com.connriver.connrail.MainActivity.USER_TYPE;

/**
 * Created by bbrown on 7/26/2018
 */

public class UserTypeActivity extends AppCompatActivity {
    private RadioButton single, owner, remote;
    private String sRem;
    private int iType;
    private final ArrayList<String> listIP = new ArrayList<>();
    private AutoCompleteTextView actvIP;
    private ImageButton ib;
    private Button btnOK;
    private static final String ITYPE = "iType";
    private static final String SREM = "sRem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        iType = intent.getIntExtra(USER_TYPE, USER_TYPE_SINGLE);
        sRem = intent.getStringExtra(OWNER_IP);

        // if re-create after screen rotation
        if (savedInstanceState != null) {
            iType = savedInstanceState.getInt(ITYPE);
            sRem = savedInstanceState.getString(SREM);
        }

        setContentView(R.layout.activity_user_type);

        single = (RadioButton) findViewById(R.id.rbSingle);
        owner = (RadioButton) findViewById(R.id.rbOwner);
        remote = (RadioButton) findViewById(R.id.rbRemote);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        single.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doSingle();
            }
        });

        owner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doOwner();
            }
        });

        remote.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doRemote();
            }
        });

        btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doOK();
            }
        });

        loadIPs();
        actvIP = (AutoCompleteTextView) findViewById(R.id.actvTest);
        actvIP.setThreshold(1);
        ArrayAdapter<String> ad = new ArrayAdapter <>(this, android.R.layout.simple_dropdown_item_1line, listIP);
        actvIP.setAdapter(ad);

        actvIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateOKButton();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        ib = (ImageButton) findViewById(R.id.btnDown);
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actvIP.showDropDown();
            }
        });

        init();
    }

    private void updateOKButton() {
        btnOK.setEnabled(iType != USER_TYPE_REMOTE || Patterns.IP_ADDRESS.matcher(actvIP.getText().toString().trim()).matches());
    }

    // on screen rotate save the current info
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putInt(ITYPE, iType);
        savedState.putString(SREM, sRem);
    }


    private void init() {
        if (iType == USER_TYPE_OWNER) {
            doOwner();
        } else if (iType == USER_TYPE_REMOTE) {
            doRemote();
        } else {
            doSingle();
        }
    }
    private void setButtons() {
        single.setChecked(iType == USER_TYPE_SINGLE);
        owner.setChecked(iType == USER_TYPE_OWNER);
        remote.setChecked(iType == USER_TYPE_REMOTE);
        updateOKButton();
    }

    private void doSingle() {
        iType = USER_TYPE_SINGLE;
        setButtons();
        actvIP.setText("");
        actvIP.setEnabled(false);
        ib.setEnabled(false);
    }

    private void doOwner() {
        iType = USER_TYPE_OWNER;
        setButtons();
        actvIP.setText("");
        actvIP.setEnabled(false);
        ib.setEnabled(false);
    }

    private void doRemote() {
        iType = USER_TYPE_REMOTE;
        setButtons();
        actvIP.setText(sRem);
        actvIP.setEnabled(true);
        ib.setEnabled(true);
    }

    private void doOK() {
        Intent intent = new Intent();
        intent.putExtra(USER_TYPE, iType);
        if (iType == USER_TYPE_REMOTE) {
            String sIP = actvIP.getText().toString().trim();
            if (sIP.length() > 0) {
                saveIP(sIP);
            }
            intent.putExtra(OWNER_IP, sIP);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private static final String IP_OWNER_DATA = "OwnerIpData";
    private static final String IP_PREFIX = "OwnerIp-";
    private static final int IP_TOTAL = 5;

    // save the last 5 entered ip addresses
    private void saveIP(String sIP) {
        //remove any earlier copies
        for (String sx : listIP ) {
            if (sx.equals(sIP)) {
                listIP.remove(sx);
                break;
            }
        }

        // if already 5 remove the last one
        if (listIP.size() == IP_TOTAL) {
            listIP.remove(IP_TOTAL - 1);
        }

        // add to top of list
        listIP.add(0, sIP);

        // write the list
        SharedPreferences.Editor editor = getSharedPreferences(IP_OWNER_DATA, MODE_PRIVATE).edit();
        for (int ix = 0; ix < listIP.size(); ix++) {
            editor.putString(IP_PREFIX + ix, listIP.get(ix));
        }
        editor.apply();
    }

    // load the last 5 ip addresses
    private void loadIPs() {
        SharedPreferences prefs = getSharedPreferences(IP_OWNER_DATA, MODE_PRIVATE);

        String sx;
        for (int ix = 0; ix < IP_TOTAL; ix++) {
            sx = prefs.getString(IP_PREFIX + ix, null);
            if (sx == null) {
                break;
            } else {
                listIP.add(sx);
            }
        }
    }

}
