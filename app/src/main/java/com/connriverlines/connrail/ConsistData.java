package com.connriverlines.connrail;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by bbrown on 1/25/2018
 */

public class ConsistData implements Serializable {
    private int id;
    private String sName;
    private String sDescription;

    public ConsistData() {
        this.id = getNewId();
    }

    public ConsistData(String name, String desc) {
        this.id = getNewId();
        this.sName = name;
        this.sDescription = desc;
    }

    public ConsistData(JSONObject jsonData) {
        fromJSON(jsonData);
    }

    public void fromJSON(JSONObject jsonData) {
        try {
            id = jsonData.getInt("id");
            sName = jsonData.getString("na");
            sDescription = jsonData.getString("de");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("id", id);
            jsonData.put("na", sName);
            jsonData.put("de", sDescription);
            return jsonData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // loop though all consists looking for a unique id
    private int getNewId() {
        int id = 1;
        for (ConsistData cd : MainActivity.getConsistList()) {
            if (cd.id >= id) {
                id = cd.id + 1;
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

    public void setName(String s) {
        sName = s;
    }
    public String getName() {
        return sName;
    }

    public void setDescription(String s) {
        sDescription = s;
    }
    public String getDescription() {
        return sDescription;
    }
}
