package com.connriverlines.connrail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;

import com.connriverlines.connrail.R;

import java.util.ArrayList;

import static com.connriverlines.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 3/16/2018
 */

class Utils {

    public static SpotData getSpotFromID(int id) {
        if (id == NONE) {
            return null;
        }

        for (SpotData sd : MainActivity.getSpotList()) {
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

        for (ConsistData cd : MainActivity.getConsistList()) {
            if (cd.getID() == id) {
                return cd;
            }
        }
        return null;
    }

    // get the list of all towns
    static ArrayList<String> getTownList(boolean bAddAll, Context context) {
        ArrayList<String> townList = new ArrayList<>();
        if (bAddAll) {
            townList.add(context.getResources().getString(R.string.all));
        }
        String sLast = "";
        String sTown;
        for (SpotData sd : MainActivity.getSpotList()) {
            sTown = sd.getTown();
            // the spots will be in alphabetical by town so only add on name change
            if (!sTown.equalsIgnoreCase(sLast)) {
                townList.add(sd.getTown());
                sLast = sTown;
            }
        }
        return townList;
    }

    // get all cars - boolean to include cars in storage
    static ArrayList<CarData> getAllCars(boolean bInStorage) {
        ArrayList<CarData> carList = new ArrayList<>();

        if (bInStorage) {
            return MainActivity.getCarList();
        }

        for (CarData cd : MainActivity.getCarList()) {
            if (!cd.getInStorage()) {
                carList.add(cd);
            }
        }
        return carList;
    }

    // get the list of cars for a given town or all towns. no cars in a consist or storage
    static ArrayList<CarData> getCarsInTown(String sTown) {
        ArrayList<CarData> carList = new ArrayList<>();

        for (CarData cd : MainActivity.getCarList()) {
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

    static int getConsistSize(int id) {
        int count = 0;

        for (CarData cd : MainActivity.getCarList()) {
            if (cd.getConsist() == id) {
                count++;
            }
        }

        return count;
    }

    // get the list of cars for a given consistID and destination town (null for ignore town)
    static ArrayList<CarData> getCarsInConsist(int id, String sTown) {
        ArrayList<CarData> carList = new ArrayList<>();

        for (CarData cd : MainActivity.getCarList()) {
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
    static ArrayList<SpotData> getSpotsInTown(String sTown) {
        ArrayList<SpotData> spotList = new ArrayList<>();

        // if town is null return the whole list
        if (sTown == null) {
            return MainActivity.getSpotList();
        }

        for (SpotData sd : MainActivity.getSpotList()) {
            if (sTown.equalsIgnoreCase(sd.getTown())) {
                spotList.add(sd);
            }
        }
        return spotList;
    }

    static String trim(TextInputEditText et) {
        return et.getText().toString().trim();
    }

    static void messageBox(String sTitle, String sMessage, Context ctx) {
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
    static void removeAllDeadSpots() {
        for (CarData cd : MainActivity.getCarList()) {

            // if a car had spots removed - update
            if (cd.removeDeadSpots()) {
                MainActivity.carAddEditDelete(cd, false);
            }
        }
    }

}
