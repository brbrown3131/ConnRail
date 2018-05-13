package com.connriver.connrail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by user on 1/19/2018.
 */

public class CarData implements Serializable{
    private String sInitials;
    private String sNumber;
    private String sType;
    private String sNotes;
    private ArrayList<CarSpotData> listSpots = new ArrayList<>(); // list of delivery spots
    private int ixSpot; // index of target/current spot.
    private int ixHoldUntilDay; // day/session when the car is free to be moved.
    private int idConsist; // which train/consist this car is part of, -1 if none
    private int idCurrentLoc; // current location of car - spot or hold/yard (interim location)
    private boolean bIsInStorage; // true if car is in storage

    public CarData() {
        ixSpot = NONE; // no spot by default
        idConsist = NONE; // not in a consist by default
        idCurrentLoc = NONE;
        ixHoldUntilDay = NONE;
    }

    public void copyLocation(CarData source) {
        ixSpot = source.ixSpot;
        ixHoldUntilDay = source.ixHoldUntilDay;
        idConsist = source.idConsist;
        idCurrentLoc = source.idCurrentLoc;
        bIsInStorage = source.bIsInStorage;
    }

    // car is moved to storage - clear any existing consists or current locations
    public void setInStorage() {
        bIsInStorage = true;
        idConsist = NONE;
        idCurrentLoc = NONE;
        ixHoldUntilDay = NONE;
    }

    public boolean getInStorage() {
        return bIsInStorage;
    }

    public void setInitials(String s) {
        sInitials = s;
    }
    public String getInitials() {
        return sInitials;
    }

    public void setNumber(String s) {
        sNumber = s;
    }
    public String getNumber() {
        return sNumber;
    }

    public void setType(String s) {
        sType = s;
    }
    public String getType() {
        return sType;
    }

    public void setNotes(String s) {
        sNotes = s;
    }
    public String getNotes() {
        return sNotes;
    }

    public int getSpotCount() {
        return listSpots.size();
    }

    public boolean usesSpotID(int id) {
        for (CarSpotData csd : listSpots) {
            if (csd.getID() == id) {
                return true;
            }
        }
        return false;
    }

    // get a copy of the car list data for editing
    public ArrayList<CarSpotData> getCarSpotDataCopy() {
        ArrayList<CarSpotData> list = new ArrayList<>();
        for (CarSpotData csd : listSpots) {
            CarSpotData csdCopy = new CarSpotData(csd);
            list.add(csdCopy);
        }
        return list;
    }

    // replace the car spot data
    public void setCarSpotData(ArrayList<CarSpotData> list) {
        if (list.size() > MainActivity.CARDATA_SPOT_MAX) {
            return;
        }
        listSpots.clear();
        for (CarSpotData csd : list) {
            listSpots.add(csd);
        }
    }

    // set the current consist - clear in storage and current location
    public void setConsist(int consist) {
        idConsist = consist;
        if (consist != NONE) {
            bIsInStorage = false;
            idCurrentLoc = NONE;
            ixHoldUntilDay = NONE;
        }
    }
    public int getConsist() {
        return idConsist;
    }

    public void setSpotIndex(int ix) {
        ixSpot = ix;
    }
    public int getSpotIndex() {
        return ixSpot;
    }

    public void setHoldUntilDay(int ix) {
        ixHoldUntilDay = ix;
    }
    public int getHoldUntilDay() {
        return ixHoldUntilDay;
    }

    public void setCurrentLoc(int idLoc) {
        //if same/current location selected - do nothing
        if (idLoc == idCurrentLoc) {
            return;
        }
        idCurrentLoc = idLoc;
        bIsInStorage = false;
        idConsist = NONE;

        //TODO - moved car temporarily from hold spot, need to put it back -  may need a current target/next target?

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

    public int getCurrentLoc() {
        return idCurrentLoc;
    }

    public String getInfo() {
        return sInitials + " " + sNumber + " [" + sType + "]";
    }

    public CarSpotData getNextSpot() {
        if (listSpots.isEmpty()) {
            return null;
        }
        if (ixSpot == NONE || ixSpot >= listSpots.size()) {
            ixSpot = 0;
        }

        return listSpots.get(ixSpot);
    }

    // remove any deleted spots from the car's spotlist
    public void removeDeadSpots() {
        Iterator<CarSpotData> it = listSpots.iterator();
        while (it.hasNext()) {
            CarSpotData csd = it.next();
            if (Utils.getSpotFromID(csd.getID()) == null) {
                it.remove();
            }
        }
    }

    public boolean invalidSpots() {
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
