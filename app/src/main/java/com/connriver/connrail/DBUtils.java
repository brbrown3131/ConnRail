package com.connriver.connrail;

import android.content.Context;

/**
 * Created by bbrown on 3/16/2018.
 * Abstraction of utilities to load/save data
 */

public class DBUtils {
    private static Context context;
    private static DBHandler dbh;

    public static void init(Context ctx) {
        context = ctx;
        dbh = new DBHandler(context);
    }

    //====================================

    public static void setDbName(String sx) {
        dbh.setDbName(sx);
    }

    public static String getDbName() {
        return dbh.getDbName();
    }

    //====================================
    public static void loadCarData() {
        dbh.loadCarData();
    }

    public static void saveCarData() {
        dbh.saveCarData();
    }
//====================================
    public static void loadSpotData() {
        dbh.loadSpotData();
    }

    public static void saveSpotData() {
        dbh.saveSpotData();
    }

//====================================
    public static void loadConsistData() {
        dbh.loadConsistData();
    }
    public static void saveConsistData() {
        dbh.saveConsistData();
    }
//====================================

}
