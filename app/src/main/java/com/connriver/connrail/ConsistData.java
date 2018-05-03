package com.connriver.connrail;

/**
 * Created by user on 1/25/2018.
 */

public class ConsistData {
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

    // loop though all consists looking for a unique id
    private int getNewId() {
        int id = 1;
        ConsistData cd;
        for (int ix = 0; ix < MainActivity.gConsistData.size(); ix++) {
            cd = MainActivity.gConsistData.get(ix);
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
