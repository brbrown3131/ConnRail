package com.connriver.connrail;

import android.content.Context;

/**
 * Created by bbrown on 3/16/2018.
 * Abstraction of utilities to load/save data
 */

public class DBUtils {
    private static Context context;
    private static int iMode;
    private static DBHandler dbh;

    public static final int MODE_SINGLE_USER = 1;

    public static void init(Context ctx, int mode) {
        context = ctx;
        iMode = mode;

        if (iMode == MODE_SINGLE_USER) {
            dbh = new DBHandler(context);
        }
    }

    public static void loadCarData() {
        if (iMode == MODE_SINGLE_USER) {
            dbh.loadCarData();
        }
    }

    public static void saveCarData() {
        if (iMode == MODE_SINGLE_USER) {
            dbh.saveCarData();
        }
    }
//====================================
    public static void loadSpotData() {
        if (iMode == MODE_SINGLE_USER) {
            dbh.loadSpotData();
        }
    }
    public static void saveSpotData() {
        if (iMode == MODE_SINGLE_USER) {
            dbh.saveSpotData();
        }
    }

//====================================
    public static void loadConsistData() {
        if (iMode == MODE_SINGLE_USER) {
            dbh.loadConsistData();
        }
    }
    public static void saveConsistData() {
        if (iMode == MODE_SINGLE_USER) {
            dbh.saveConsistData();
        }
    }
//====================================
    public static void loadSystemInfo() {
    }
    public static void saveSystemInfo() {
    }
}
