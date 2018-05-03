package com.connriver.connrail;

import java.util.ArrayList;

import static com.connriver.connrail.MainActivity.CARDATA_SPOT_MAX;
import static com.connriver.connrail.MainActivity.NONE;

/**
 * Created by bbrown on 1/19/2018.
 */

public class AlertData {

    public static final int ALERT_INFO = 0;
    public static final int ALERT_WARNING = 1;
    public static final int ALERT_ERROR = 2;
    private String sMessage;
    private int iLevel;
    private int id;

    public AlertData(String sx, int iLev, int id) {
        sMessage = sx;
        this.id = id;
        setLevel(iLev);
    }

    public String getMessage() {
        return sMessage;
    }
    public void setMessage(String sx) {
        sMessage = sx;
    }

    public int getLevel() {
        return iLevel;
    }
    public void setLevel(int ix) {
        if (ix == ALERT_INFO || ix == ALERT_WARNING || ix == ALERT_ERROR) {
            iLevel = ix;
        } else {
            iLevel = ALERT_ERROR;
        }
    }

    public int getID() {
        return id;
    }
    public void setID(int ix) {
        id = ix;
    }
}
