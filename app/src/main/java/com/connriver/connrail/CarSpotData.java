package com.connriver.connrail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 *  Created by bbrown on 3/9/2018
    List item for each car - where it is going, how many days to hold there and lading/instructions */

class CarSpotData implements Serializable {
    private int id; // id of the spot/location
    private int iHoldDays; // how many days to hold it
    private String sLading; // what's in the car going to this location
    private String sInstructions; // any special instructions

    CarSpotData(int id, int iHold) {
        this.id = id;
        iHoldDays = iHold;
        sLading = "";
        sInstructions = "";
    }

    CarSpotData(JSONObject jsonData) {
        try {
            id = jsonData.getInt("id");
            iHoldDays = jsonData.getInt("iHoldDays");
            sLading = jsonData.getString("sLading");
            sInstructions = jsonData.getString("sInstructions");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONObject toJSON() {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("id", id);
            jsonData.put("iHoldDays", iHoldDays);
            jsonData.put("sLading", sLading);
            jsonData.put("sInstructions", sInstructions);
            return jsonData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // copy constructor
    CarSpotData(CarSpotData csd) {
        id = csd.id;
        iHoldDays = csd.iHoldDays;
        sLading = csd.sLading;
        sInstructions = csd.sInstructions;
    }

    // equals comp.
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CarSpotData)) {
            return false;
        }

        CarSpotData that = (CarSpotData) other;

        // Custom equality check here.
        return this.id == that.id &&
               this.iHoldDays == that.iHoldDays &&
               this.sLading.equals(that.sLading) &&
               this.sInstructions.equals(that.sInstructions);
    }

    public int getID() {
        return id;
    }

    int getHoldDays() {
        return iHoldDays;
    }
    void setHoldDays(int days) {
        iHoldDays = days;
    }

    String getLading() {
        return sLading;
    }
    void setLading(String sx) {
        sLading = sx;
    }

    String getInstructions() {
        return sInstructions;
    }
    void setInstructions(String sx) {
        sInstructions = sx;
    }
}
