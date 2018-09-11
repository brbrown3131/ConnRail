package com.connriver.connrail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 1/19/2018
 */

public class CarData implements Serializable {
    private int id;
    private String sInitials = "";
    private String sNumber = "";
    private String sType = "";
    private String sNotes = "";
    private final ArrayList<CarSpotData> listSpots = new ArrayList<>(); // list of delivery spots
    private int ixSpot; // index of target/current spot.
    private int ixHoldUntilDay; // day/session when the car is free to be moved.
    private int idConsist; // which train/consist this car is part of, -1 if none
    private int idCurrentLoc; // current location of car - spot or hold/yard (interim location)
    private boolean bIsInStorage = false; // true if car is in storage

    public CarData() {
        id = getNewId();
        ixSpot = NONE; // no spot by default
        idConsist = NONE; // not in a consist by default
        idCurrentLoc = NONE;
        ixHoldUntilDay = NONE;
    }

    public CarData(JSONObject jsonData) {
        try {
            id = jsonData.getInt("id");
            sInitials = jsonData.getString("in");
            sNumber = jsonData.getString("nu");
            sType = jsonData.getString("ty");
            sNotes = jsonData.getString("no");

            JSONArray jsonList = (JSONArray) jsonData.get("ls");
            for (int ix = 0; ix < jsonList.length(); ix++) {
                listSpots.add(new CarSpotData(jsonList.getJSONObject(ix)));
            }

            ixSpot = jsonData.getInt("sp");
            ixHoldUntilDay = jsonData.getInt("ho");
            idConsist = jsonData.getInt("co");
            idCurrentLoc = jsonData.getInt("cl");
            bIsInStorage = jsonData.getBoolean("is");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONObject toJSON() {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("id", id);
            jsonData.put("in", sInitials);
            jsonData.put("nu", sNumber);
            jsonData.put("ty", sType);
            jsonData.put("no", sNotes);

            JSONArray jsArray = new JSONArray();
            for (CarSpotData csd : listSpots) {
                jsArray.put(csd.toJSON());
            }
            jsonData.put("ls", jsArray);

            jsonData.put("sp", ixSpot);
            jsonData.put("ho", ixHoldUntilDay);
            jsonData.put("co", idConsist);
            jsonData.put("cl", idCurrentLoc);
            jsonData.put("is", bIsInStorage);

            return jsonData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // loop though all cars looking for a unique id
    private int getNewId() {
        int id = 1;
        for (CarData cd : MainActivity.getCarList()) {
            if (cd.id >= id) {
                id = cd.id + 1;
            }
        }
        return id;
    }

    public int getID() {
        return this.id;
    }

    // car is moved to storage - clear any existing consists or current locations
    void setInStorage() {
        bIsInStorage = true;
        idConsist = NONE;
        idCurrentLoc = NONE;
        ixHoldUntilDay = NONE;
    }

    boolean getInStorage() {
        return bIsInStorage;
    }

    void setInitials(String s) {
        sInitials = s;
    }
    String getInitials() {
        return sInitials;
    }

    public void setNumber(String s) {
        sNumber = s;
    }
    public String getNumber() {
        return sNumber;
    }

    void setType(String s) {
        sType = s;
    }
    String getType() {
        return sType;
    }

    void setNotes(String s) {
        sNotes = s;
    }
    String getNotes() {
        return sNotes;
    }

    boolean usesSpotID(int id) {
        for (CarSpotData csd : listSpots) {
            if (csd.getID() == id) {
                return true;
            }
        }
        return false;
    }

    // get a copy of the car list data for editing
    ArrayList<CarSpotData> getCarSpotDataCopy() {
        ArrayList<CarSpotData> list = new ArrayList<>();
        for (CarSpotData csd : listSpots) {
            CarSpotData csdCopy = new CarSpotData(csd);
            list.add(csdCopy);
        }
        return list;
    }

    // replace the car spot data
    void setCarSpotData(ArrayList<CarSpotData> list) {
        if (list.size() > MainActivity.CARDATA_SPOT_MAX) {
            return;
        }
        listSpots.clear();
        for (CarSpotData csd : list) {
            listSpots.add(csd);
        }
    }

    // set the current consist - clear in storage and current location
    void setConsist(int consist) {
        idConsist = consist;
        if (consist != NONE) {
            bIsInStorage = false;
            idCurrentLoc = NONE;
            ixHoldUntilDay = NONE;
        }
    }
    int getConsist() {
        return idConsist;
    }

    void setSpotIndex(int ix) {
        ixSpot = ix;
    }
    int getSpotIndex() {
        return ixSpot;
    }

    void setHoldUntilDay(int ix) {
        ixHoldUntilDay = ix;
    }
    int getHoldUntilDay() {
        return ixHoldUntilDay;
    }

    void setCurrentLoc(int idLoc) {
        //if same/current location selected - do nothing
        if (idLoc == idCurrentLoc) {
            return;
        }
        idCurrentLoc = idLoc;
        bIsInStorage = false;
        idConsist = NONE;

        // default the hold days to 0 - can move car again in current session
        ixHoldUntilDay = MainActivity.getSessionNumber();

        CarSpotData spot = getNextSpot(); // get the target spot
        if (spot != null && spot.getID() == idLoc) { // if there is a target spot and the car was moved to the target spot

            // set the cars hold until day to current session/day + predefined days for this spot
            ixHoldUntilDay = MainActivity.getSessionNumber() + spot.getHoldDays();

            // go to the next spot
            ixSpot++;
        }
    }

    int getCurrentLoc() {
        return idCurrentLoc;
    }

    String getInfo() {
        if (sType.length() > 0) {
            return sInitials + " " + sNumber + " [" + sType + "]";
        } else {
            return sInitials + " " + sNumber;
        }
    }

    CarSpotData getNextSpot() {
        if (listSpots.isEmpty()) {
            return null;
        }
        if (ixSpot == NONE || ixSpot >= listSpots.size()) {
            ixSpot = 0;
        }

        return listSpots.get(ixSpot);
    }

    CarSpotData getCurrentSpot() {
        if (listSpots.isEmpty()) {
            return null;
        }
        if (ixSpot == NONE) {
            return null;
        }
        CarSpotData csd;
        if (ixSpot == 0) {
            csd = listSpots.get(listSpots.size() - 1);
        } else {
            csd = listSpots.get(ixSpot - 1);
        }

        if (csd.getID() == idCurrentLoc) {
            return csd;
        } else {
            return null;
        }
    }

    // remove any deleted spots from the car's spotlist
    void removeDeadSpots() {
        Iterator<CarSpotData> it = listSpots.iterator();
        while (it.hasNext()) {
            CarSpotData csd = it.next();
            if (Utils.getSpotFromID(csd.getID()) == null) {
                it.remove();
            }
        }
    }

    boolean invalidSpots() {
        int size = listSpots.size();

        // return true if there are fewer than 2 spots
        if (size < 2) {
            return true;
        }

        // return true if first and last same spot
        if (listSpots.get(0).getID() == listSpots.get(size - 1).getID()) {
            return true;
        }

        // return true if same spot twice in a row
        int prevID = listSpots.get(0).getID();
        for (int ix = 1; ix < size; ix++) {
            if (listSpots.get(ix).getID() == prevID) {
                return true;
            }
            prevID = listSpots.get(ix).getID();
        }

        return false; //ok
    }

}
