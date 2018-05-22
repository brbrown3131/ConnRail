package com.connriver.connrail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 3/16/2018.
 */

public class Utils {

    private static Context context;

    public static void init(Context ctx) {
        context = ctx;
    }

    public static void dumpSpotData() {
        Log.d("DUMP", "Dump Spot ------------------------------");
        for (SpotData sd : MainActivity.gSpotData) {
            Log.d("DUMP", sd.getID() + " " + sd.getTown() + " " + sd.getIndustry() + " " + sd.getTrack());
        }
    }

    public static void dumpCarData() {
        Log.d("DUMP", "Dump Car------------------------------");
        for (CarData cd : MainActivity.gCarData) {
            Log.d("DUMP", cd.getInitials() + " " + cd.getNumber() + "=Loc:" + cd.getCurrentLoc() + " Consist:" + cd.getConsist() + (cd.getInStorage() ? " Stored" : ""));
        }
    }

    public static SpotData getSpotFromID(int id) {
        if (id == NONE) {
            return null;
        }

        for (SpotData sd : MainActivity.gSpotData) {
            if (sd.getID() == id) {
                return sd;
            }
        }
        return null;
    }

    public static ConsistData getConsistFromID(int id) {
        if (id == NONE) {
            return null;
        }

        for (ConsistData cd : MainActivity.gConsistData) {
            if (cd.getID() == id) {
                return cd;
            }
        }
        return null;
    }

    // get the list of all towns
    public static ArrayList<String> getTownList() {
        ArrayList<String> townList = new ArrayList<>();
        townList.add(context.getResources().getString(R.string.all));
        String sLast = "";
        String sTown;
        for (SpotData sd : MainActivity.gSpotData) {
            sTown = sd.getTown();
            // the spots will be in alphabetical by town so only add on name change
            if (!sTown.equalsIgnoreCase(sLast)) {
                townList.add(sd.getTown());
                sLast = sTown;
            }
        }
        return townList;
    }

    // get the list of cars in storage
    public static ArrayList<CarData> getCarsInStorage() {
        ArrayList<CarData> carList = new ArrayList<>();

        for (CarData cd : MainActivity.gCarData) {
            if (cd.getInStorage()) {
                carList.add(cd);
            }
        }
        return carList;
    }

    // get all cars - boolean to include cars in storage
    public static ArrayList<CarData> getAllCars(boolean bInStorage) {
        ArrayList<CarData> carList = new ArrayList<>();

        if (bInStorage) {
            return MainActivity.gCarData;
        }

        for (CarData cd : MainActivity.gCarData) {
            if (!cd.getInStorage()) {
                carList.add(cd);
            }
        }
        return carList;
    }

    // get the list of cars for a given town or all towns. no cars in a consist or storage
    public static ArrayList<CarData> getCarsInTown(String sTown) {
        ArrayList<CarData> carList = new ArrayList<>();

        for (CarData cd : MainActivity.gCarData) {
            if (cd.getInStorage() || cd.getConsist() != NONE) {
                continue;
            }

            if (sTown == null) {
                carList.add(cd);
            } else {
                int spotID = cd.getCurrentLoc();
                SpotData sd = getSpotFromID(spotID);
                if (sd == null) {
                    continue;
                }
                if (sTown.equalsIgnoreCase(sd.getTown())) {
                    carList.add(cd);
                }
            }
        }
        return carList;
    }

    public static int getConsistSize(int id) {
        int count = 0;

        for (CarData cd : MainActivity.gCarData) {
            if (cd.getConsist() == id) {
                count++;
            }
        }

        return count;
    }

    // get the list of cars for a given consistID and destination town (null for ignore town)
    public static ArrayList<CarData> getCarsInConsist(int id, String sTown) {
        ArrayList<CarData> carList = new ArrayList<>();

        for (CarData cd : MainActivity.gCarData) {
            if (cd.getConsist() == id) {
                if (sTown == null) {
                    carList.add(cd);
                } else {
                    CarSpotData csd = cd.getNextSpot();
                    if (csd != null) {
                        SpotData sd = Utils.getSpotFromID(csd.getID());
                        if (sd != null && sTown.equals(sd.getTown())) {
                            carList.add(cd);
                        }
                    }
                }
            }
        }
        return carList;
    }

    // get the list of spots for a given town
    public static ArrayList<SpotData> getSpotsInTown(String sTown) {
        ArrayList<SpotData> spotList = new ArrayList<>();

        // if town is null return the whole list
        if (sTown == null) {
            return MainActivity.gSpotData;
        }

        for (SpotData sd : MainActivity.gSpotData) {
            if (sTown.equalsIgnoreCase(sd.getTown())) {
                spotList.add(sd);
            }
        }
        return spotList;
    }

    public static int len(EditText et) {
        return trim(et).length();
    }

    public static String trim(EditText et) {
        return et.getText().toString().trim();
    }

    public static void messageBox(String sTitle, String sMessage, Context ctx) {
        AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle(sTitle);
        alertDialog.setMessage(sMessage);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ctx.getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    // remove any deleted spots from the car's spotlist
    public static void removeAllDeadSpots() {
        for (CarData cd : MainActivity.gCarData) {
            cd.removeDeadSpots();
        }
    }

}
