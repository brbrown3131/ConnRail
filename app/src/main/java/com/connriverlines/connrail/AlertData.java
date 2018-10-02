package com.connriverlines.connrail;

/**
 * Created by bbrown on 1/19/2018
 */

class AlertData {

    public static final int ALERT_INFO = 0;
    public static final int ALERT_WARNING = 1;
    public static final int ALERT_ERROR = 2;
    private final String sMessage;
    private int iLevel;
    private final int id;

    public AlertData(String sx, int iLev, int id) {
        sMessage = sx;
        this.id = id;
        setLevel(iLev);
    }

    public String getMessage() {
        return sMessage;
    }

    public int getLevel() {
        return iLevel;
    }

    private void setLevel(int ix) {
        if (ix == ALERT_INFO || ix == ALERT_WARNING || ix == ALERT_ERROR) {
            iLevel = ix;
        } else {
            iLevel = ALERT_ERROR;
        }
    }

    public int getID() {
        return id;
    }
}
