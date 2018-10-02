package com.connriverlines.connrail;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bbrown on 7/6/2018
 */

class Remote {

    private final String sIP;
    private Socket mSocket = null;
    private OnDataUpdate mCallback = null;
    private final Timer timer;
    private boolean bConnected = false;
    private boolean bRequestAllData = true;
    private static final int TIMEOUT_INTERVAL = 10000;
    private boolean bReceivedSpotInfo = false;
    private boolean bReceivedConsistInfo = false;
    private boolean bReceivedCarInfo = false;

    Remote(OnDataUpdate callback, String sx) {
        sIP = sx;
        mCallback = callback;

        // create a new worker thread and start
        Thread listenThread = new Thread(new ListenThread());
        listenThread.start();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!bConnected) {
                    if (mCallback != null) {
                        mCallback.onRemoteDataUpdate(MainActivity.MSG_NO_PING, null);
                    }
                }
                bConnected = false;
            }
        }, TIMEOUT_INTERVAL, TIMEOUT_INTERVAL);

    }

    void setCallback(OnDataUpdate callback) {
        mCallback = callback;
    }

    void close() {
        // kill the connected timer
        timer.cancel();

        CloseSocket cs = new CloseSocket();
        cs.execute();
    }

    private String buildMessage(int iMsgType, String sData) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(MainActivity.MSG_TYPE_TAG, iMsgType);
            jsonData.put(MainActivity.MSG_DATA_TAG, sData);
            return jsonData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ListenThread extends Thread {

        @Override
        public void run() {
            try {
                //create a new socket, get the input stream and listen
                mSocket = new Socket(sIP, MainActivity.SOCKET_PORT);
                DataInputStream dis = new DataInputStream(mSocket.getInputStream());
                String sData;

                while (true) {
                    sData = dis.readUTF();

                    int iMsgType = 0;
                    String sMsgData = null;
                    try {
                        final JSONObject jsonData;
                        jsonData = new JSONObject(sData);

                        iMsgType = jsonData.getInt(MainActivity.MSG_TYPE_TAG);
                        sMsgData = jsonData.getString(MainActivity.MSG_DATA_TAG);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // do something with the owner message
                    handleOwnerMsg(iMsgType, sMsgData);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // handle receiving a message from the owner
    private void handleOwnerMsg(int iMsgType, String sMsgData) {

        switch (iMsgType) {
            case MainActivity.MSG_PING:
                bConnected = true;
                if (bRequestAllData) { // if flagged to request owner data - send requests
                    bRequestAllData = false;

                    // turn off flags that we have all the table data
                    bReceivedSpotInfo = false;
                    bReceivedConsistInfo = false;
                    bReceivedCarInfo = false;

                    //send the owner session#/data table requests
                    send(MainActivity.MSG_REQUEST_SESSION_DATA, "");
                    send(MainActivity.MSG_REQUEST_FULL_DATA, "");
                }
                break;

            case MainActivity.MSG_FULL_SPOT_DATA:
                parseSpotData(sMsgData);
                return;

            case MainActivity.MSG_FULL_CONSIST_DATA:
                parseConsistData(sMsgData);
                return;

            case MainActivity.MSG_FULL_CAR_DATA:
                parseCarData(sMsgData);
                return;

            case MainActivity.MSG_DELETE_SPOT_DATA:
                try {
                    MainActivity.updateSpot(new SpotData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case MainActivity.MSG_UPDATE_SPOT_DATA:
                try {
                    MainActivity.updateSpot(new SpotData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case MainActivity.MSG_DELETE_CONSIST_DATA:
                try {
                    MainActivity.updateConsist(new ConsistData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case MainActivity.MSG_UPDATE_CONSIST_DATA:
                try {
                    MainActivity.updateConsist(new ConsistData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case MainActivity.MSG_DELETE_CAR_DATA:
                try {
                    MainActivity.updateCar(new CarData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case MainActivity.MSG_UPDATE_CAR_DATA:
                try {
                    MainActivity.updateCar(new CarData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

        }

        // pass the message on to the UI unless a return from switch above
        if (mCallback != null) {
            mCallback.onRemoteDataUpdate(iMsgType, sMsgData);
        }
    }

    private void postParse() {
        // when we get all 3 tables, tell the mainactivity
        if (bReceivedSpotInfo && bReceivedConsistInfo && bReceivedCarInfo) {
            if (mCallback != null) {
                mCallback.onRemoteDataUpdate(MainActivity.MSG_FULL_DATA, "");
            }
        }
    }

    private void parseSpotData(String sData) {
        try {
            JSONArray jArray = new JSONArray(sData);

            MainActivity.getSpotList().clear();
            for (int ix = 0; ix < jArray.length(); ix++) {
                MainActivity.getSpotList().add(new SpotData(jArray.getJSONObject(ix)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bReceivedSpotInfo = true;
        postParse();
    }

    private void parseConsistData(String sData) {
        try {
            JSONArray jArray = new JSONArray(sData);

            MainActivity.getConsistList().clear();
            for (int ix = 0; ix < jArray.length(); ix++) {
                MainActivity.getConsistList().add(new ConsistData(jArray.getJSONObject(ix)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bReceivedConsistInfo = true;
        postParse();
    }

    private void parseCarData(String sData) {
        try {
            JSONArray jArray = new JSONArray(sData);

            MainActivity.getCarList().clear();
            for (int ix = 0; ix < jArray.length(); ix++) {
                MainActivity.getCarList().add(new CarData(jArray.getJSONObject(ix)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bReceivedCarInfo = true;
        postParse();
    }

    void send(int iMsgType, String sData) {
        SendSocket ss = new SendSocket(buildMessage(iMsgType, sData));
        ss.execute();
    }

    private class SendSocket extends AsyncTask<Void, Void, Void> {
        private final String sOut;

        SendSocket(String sx) {
           sOut = sx;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mSocket != null) {
                try {
                    DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
                    dos.writeUTF(sOut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private class CloseSocket extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (mSocket != null) {
                try {
                    mSocket.close();
                    mSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    interface OnDataUpdate {
        void onRemoteDataUpdate(int msgType, String sData);
    }

}
