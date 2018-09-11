package com.connriver.connrail;

import android.content.Context;

/**
 * Created by bbrown on 3/16/2018
 * Abstraction of utilities to load/save data
 */

class DBUtils {
    private static DBHandler dbh;

    static void init(Context ctx) {
        dbh = new DBHandler(ctx);
    }

    static String getDbName() {
        return dbh.getDbName();
    }

    static void loadCarData() {
        dbh.loadCarData();
    }
    static void saveCarData() {
        dbh.saveCarData();
    }

    static void loadSpotData() {
        dbh.loadSpotData();
    }
    static void saveSpotData() {
        dbh.saveSpotData();
    }

    static void loadConsistData() {
        dbh.loadConsistData();
    }
    static void saveConsistData() {
        dbh.saveConsistData();
    }
}
