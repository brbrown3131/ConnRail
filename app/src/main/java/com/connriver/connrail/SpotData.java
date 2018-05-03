package com.connriver.connrail;

/**
 * Created by user on 1/25/2018.
 */

public class SpotData {
    private int id;
    private String sTown;
    private String sIndustry;
    private String sTrack;

    public SpotData() {

        this.id = getNewId();
    }

    // loop though all spots looking for a unique id
    private int getNewId() {
        int id = 1;
        SpotData sd;
        for (int ix = 0; ix < MainActivity.gSpotData.size(); ix++) {
            sd = MainActivity.gSpotData.get(ix);
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
