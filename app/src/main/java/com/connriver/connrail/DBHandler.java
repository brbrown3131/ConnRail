package com.connriver.connrail;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.*;

/**
 * Created by user on 1/24/2018.
 */

public class DBHandler {
    private SQLiteDatabase DB;
    private static String DBName = "layout1"; //TODO allow more than one - setting name
    private static final int version = '1';
    private static Context currentContext;
    private static final String CAR_DATA_TABLE = "CarData";
    private static final String SPOT_DATA_TABLE = "SpotData";
    private static final String CONSIST_DATA_TABLE = "ConsistData";

    // TODO allow separate server - pc, cloud

    // allow the default name to be changed
    public void setDbName(String sx) {
        DBName = sx;
    }
    public String getDbName() {
        return DBName;
    }

    public DBHandler(Context context) {
        initDB(context);
    }

    // escape any apostrophes
    private String PX(String sx) {
        return sx.replace("'","''");
    }

    private void initDB(Context context) {
        currentContext = context;

        try {
            DB = currentContext.openOrCreateDatabase(DBName, 0, null);
        } catch (SQLiteException e) {
        }
        if (DB == null) {
            return;
        }

        //BBB
        //DB.execSQL("DROP TABLE " + CAR_DATA_TABLE); //to kill a table
        //DB.execSQL("DROP TABLE " + SPOT_DATA_TABLE); //to kill a table

        DB.execSQL("CREATE TABLE IF NOT EXISTS " + CAR_DATA_TABLE +
                    " (Initials TEXT, Number TEXT, Type TEXT, Notes TEXT," +
                    " Spot1 INTEGER, Hold1 INTEGER, Lading1 TEXT, Instructions1 Text," +
                    " Spot2 INTEGER, Hold2 INTEGER, Lading2 TEXT, Instructions2 Text," +
                    " Spot3 INTEGER, Hold3 INTEGER, Lading3 TEXT, Instructions3 Text," +
                    " Spot4 INTEGER, Hold4 INTEGER, Lading4 TEXT, Instructions4 Text," +
                    " ixSpot INTEGER, ixSpotDay INTEGER, idConsist INTEGER, inStorage INTEGER, idCurrentLoc INTEGER);");

        DB.execSQL("CREATE TABLE IF NOT EXISTS " + SPOT_DATA_TABLE +
                   " (id INTEGER, Town TEXT, Industry TEXT, Track TEXT);");

        DB.execSQL("CREATE TABLE IF NOT EXISTS " + CONSIST_DATA_TABLE +
                " (id INTEGER, Name TEXT, Description TEXT);");
    }

    // Car Data ===================================================================================

    public void loadCarData() {
        Cursor cursor = DB.rawQuery("SELECT * FROM " + CAR_DATA_TABLE, null);
        CarData cd;
        ArrayList<CarSpotData> listSpots = new ArrayList<>(); // local list of delivery spots
        if (cursor.moveToFirst())
        {
            do {
                cd = new CarData();

                cd.setInitials(cursor.getString(cursor.getColumnIndex("Initials")));
                cd.setNumber(cursor.getString(cursor.getColumnIndex("Number")));
                cd.setType(cursor.getString(cursor.getColumnIndex("Type")));
                cd.setNotes(cursor.getString(cursor.getColumnIndex("Notes")));

                // load the site ids/holds
                listSpots.clear();
                for (int ix = 1; ix <= CARDATA_SPOT_MAX; ix++) {
                    int iSpot = cursor.getInt(cursor.getColumnIndex("Spot" + ix));
                    int iHold = cursor.getInt(cursor.getColumnIndex("Hold" + ix));
                    String sLading = cursor.getString(cursor.getColumnIndex("Lading" + ix));
                    String sInst = cursor.getString(cursor.getColumnIndex("Instructions" + ix));
                    CarSpotData csd = new CarSpotData(iSpot, iHold);
                    csd.setLading(sLading);
                    csd.setInstructions(sInst);
                    if (iSpot != NONE) {
                        listSpots.add(csd);
                    }
                }
                cd.setCarSpotData(listSpots);

                cd.setSpotIndex(cursor.getInt(cursor.getColumnIndex("ixSpot")));
                cd.setCurrentLoc(cursor.getInt(cursor.getColumnIndex("idCurrentLoc")));
                cd.setHoldUntilDay(cursor.getInt(cursor.getColumnIndex("ixSpotDay")));
                cd.setConsist(cursor.getInt(cursor.getColumnIndex("idConsist")));
                if (0 != cursor.getInt(cursor.getColumnIndex("inStorage"))) {
                    cd.setInStorage();
                }

                MainActivity.gCarData.add(cd);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void saveCarData() {
        int isInStorage;
        ArrayList<CarSpotData> listSpots; // local list of delivery spots
        CarSpotData dummySpot = new CarSpotData(NONE, NONE);

        DB.execSQL("DELETE FROM " + CAR_DATA_TABLE);
        for (CarData cd : MainActivity.gCarData) {
            isInStorage = cd.getInStorage() ? 1 : 0;

            listSpots = cd.getCarSpotDataCopy();

            // add dummy/NONE spots to the end so there are CARDATA_SPOT_MAX count
            for (int ix = listSpots.size(); ix < MainActivity.CARDATA_SPOT_MAX; ix++) {
                listSpots.add(dummySpot);
            }

            DB.execSQL("INSERT INTO " + CAR_DATA_TABLE + " Values ('" + PX(cd.getInitials()) + "','" + PX(cd.getNumber()) + "','" +
                        PX(cd.getType()) + "','" + PX(cd.getNotes()) + "','" +
                        listSpots.get(0).getID() + "','" + listSpots.get(0).getHoldDays() + "','" +  PX(listSpots.get(0).getLading()) + "','" + PX(listSpots.get(0).getInstructions()) + "','" +
                        listSpots.get(1).getID() + "','" + listSpots.get(1).getHoldDays() + "','" +  PX(listSpots.get(1).getLading()) + "','" + PX(listSpots.get(1).getInstructions()) + "','" +
                        listSpots.get(2).getID() + "','" + listSpots.get(2).getHoldDays() + "','" +  PX(listSpots.get(2).getLading()) + "','" + PX(listSpots.get(2).getInstructions()) + "','" +
                        listSpots.get(3).getID() + "','" + listSpots.get(3).getHoldDays() + "','" +  PX(listSpots.get(3).getLading()) + "','" + PX(listSpots.get(3).getInstructions()) + "','" +
                        cd.getSpotIndex() + "','" + cd.getHoldUntilDay() + "','" + cd.getConsist() + "','" + isInStorage + "','" + cd.getCurrentLoc() + "');");
        }
    }

    // Spot Data ===================================================================================

    public void loadSpotData() {
        Cursor cursor = DB.rawQuery("SELECT * FROM " + SPOT_DATA_TABLE, null);
        SpotData sd;
        if (cursor.moveToFirst())
        {
            do {
                sd = new SpotData();

                sd.setID(cursor.getInt(cursor.getColumnIndex("id")));
                sd.setTown(cursor.getString(cursor.getColumnIndex("Town")));
                sd.setIndustry(cursor.getString(cursor.getColumnIndex("Industry")));
                sd.setTrack(cursor.getString(cursor.getColumnIndex("Track")));

                MainActivity.gSpotData.add(sd);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void saveSpotData() {
        SpotData sd;
        DB.execSQL("DELETE FROM " + SPOT_DATA_TABLE);
        for (int ix = 0; ix < MainActivity.gSpotData.size(); ix++) {
            sd = MainActivity.gSpotData.get(ix);
            DB.execSQL("INSERT INTO " + SPOT_DATA_TABLE + " Values ('" + sd.getID() + "','" + PX(sd.getTown()) + "','" + PX(sd.getIndustry()) + "','" + PX(sd.getTrack()) + "');");
        }
    }

    // Consist Data ===================================================================================

    public void loadConsistData() {
        Cursor cursor = DB.rawQuery("SELECT * FROM " + CONSIST_DATA_TABLE, null);

        ConsistData cd;
        if (cursor.moveToFirst())
        {
            do {
                cd = new ConsistData();

                cd.setID(cursor.getInt(cursor.getColumnIndex("id")));
                cd.setName(cursor.getString(cursor.getColumnIndex("Name")));
                cd.setDescription(cursor.getString(cursor.getColumnIndex("Description")));

                MainActivity.gConsistData.add(cd);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void saveConsistData() {
        ConsistData cd;
        DB.execSQL("DELETE FROM " + CONSIST_DATA_TABLE);
        for (int ix = 0; ix < MainActivity.gConsistData.size(); ix++) {
            cd = MainActivity.gConsistData.get(ix);
            DB.execSQL("INSERT INTO " + CONSIST_DATA_TABLE + " Values ('" + cd.getID() + "','" + PX(cd.getName()) + "','" + PX(cd.getDescription()) + "');");
        }
    }
}
