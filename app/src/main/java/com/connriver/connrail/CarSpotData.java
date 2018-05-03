package com.connriver.connrail;

import java.io.Serializable;

/**
 *  Created by bbrown on 3/9/2018.
    List item for each car - where it is going, how many days to hold there and lading/instructions */

public class CarSpotData implements Serializable {
    private int iD; // id of the spot/location
    private int iHoldDays; // how many days to hold it
    private String sLading; // what's in the car going to this location
    private String sInstructions; // any special instructions

    public CarSpotData(int id, int ihold) {
        iD = id;
        iHoldDays = ihold;
        sLading = "";
        sInstructions = "";
    }

    // copy constructor
    CarSpotData(CarSpotData csd) {
        iD = csd.iD;
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
        return this.iD == that.iD &&
               this.iHoldDays == that.iHoldDays &&
               this.sLading.equals(that.sLading) &&
               this.sInstructions.equals(that.sInstructions);
    }

    public int getID() {
        return iD;
    }
    public void setID(int id) {
        iD = id;
    }

    public int getHoldDays() {
        return iHoldDays;
    }
    public void setHoldDays(int days) {
        iHoldDays = days;
    }

    public String getLading() {
        return sLading;
    }
    public void setLading(String sx) {
        sLading = sx;
    }

    public String getInstructions() {
        return sInstructions;
    }
    public void setInstructions(String sx) {
        sInstructions = sx;
    }
}
