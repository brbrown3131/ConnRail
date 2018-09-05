package com.connriver.connrail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by user on 1/25/2018.
 */

public class SpotData implements Serializable {
    private int id;
    private String sTown = "";
    private String sIndustry = "";
    private String sTrack = "";

    public SpotData() {
        this.id = getNewId();
    }

    public SpotData(JSONObject jsonData) {
        try {
            id = jsonData.getInt("id");
            sTown = jsonData.getString("tn");
            sIndustry = jsonData.getString("in");
            sTrack = jsonData.getString("tk");
        } catch (JSONException e) {
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("id", id);
            jsonData.put("tn", sTown);
            jsonData.put("in", sIndustry);
            jsonData.put("tk", sTrack);
            return jsonData;
        } catch (JSONException e) {
            return null;
        }
    }

    // loop though all spots looking for a unique id
    private int getNewId() {
        int id = 1;
        for (SpotData sd : MainActivity.getSpotList()) {
            if (sd.id >= id) {
                id = sd.id + 1;
            }
        }
        return id;
    }

    // override unique id ( on load from DB)
    public void setID(int id) {
        this.id = id;
    }
    public int getID() {
        return this.id;
    }

    public void setTown(String s) {
        sTown = s;
    }
    public String getTown() {
        return sTown;
    }

    public void setIndustry(String s) {
        sIndustry = s;
    }
    public String getIndustry() {
        return sIndustry;
    }

    public void setTrack(String s) {
        sTrack= s;
    }
    public String getTrack() {
        return sTrack;
    }

}
