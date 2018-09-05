package com.connriver.connrail;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static com.connriver.connrail.MainActivity.MSG_DELETE_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_DELETE_CONSIST_DATA;
import static com.connriver.connrail.MainActivity.MSG_DELETE_SPOT_DATA;
import static com.connriver.connrail.MainActivity.MSG_FULL_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_FULL_CONSIST_DATA;
import static com.connriver.connrail.MainActivity.MSG_FULL_SPOT_DATA;
import static com.connriver.connrail.MainActivity.MSG_REQUEST_FULL_DATA;
import static com.connriver.connrail.MainActivity.MSG_REQUEST_SESSION_DATA;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_CONSIST_DATA;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_SPOT_DATA;
import static com.connriver.connrail.MainActivity.TAG;
import static com.connriver.connrail.MainActivity.MSG_DATA_TAG;
import static com.connriver.connrail.MainActivity.MSG_NO_PING;
import static com.connriver.connrail.MainActivity.MSG_PING;
import static com.connriver.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriver.connrail.MainActivity.SOCKET_PORT;

/**
 * Created by user on 7/6/2018.
 */

public class Remote {

    private String sIP;
    private Socket mSocket = null;
    private Thread listenThread = null;
    private OnDataUpdate mCallback = null;
    private Timer timer;
    private boolean bConnected = false;
    private boolean bRequestAllData = true;
    private static final int TIMEOUT_INTERVAL = 10000;

    public Remote(OnDataUpdate callback, String sx) {
        sIP = sx;
        mCallback = callback;

        // create a new worker thread and start
        listenThread = new Thread(new ListenThread());
        listenThread.start();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!bConnected) {
                    if (mCallback != null) {
                        mCallback.onRemoteDataUpdate(MSG_NO_PING, null);
                    }
                }
                bConnected = false;
            }
        }, TIMEOUT_INTERVAL, TIMEOUT_INTERVAL);

    }

    public void close() {
        // kill the connected timer
        timer.cancel();

        CloseSocket cs = new CloseSocket();
        cs.execute();
    }

    private String buildMessage(int iMsgType, String sData) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(MSG_TYPE_TAG, iMsgType);
            jsonData.put(MSG_DATA_TAG, sData);
            return jsonData.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    private class ListenThread extends Thread {

        @Override
        public void run() {
            try {
                //create a new socket, get the input stream and listen
                mSocket = new Socket(sIP, SOCKET_PORT);
                DataInputStream dis = new DataInputStream(mSocket.getInputStream());
                String sData;

                while (true) {
                    sData = dis.readUTF();

                    int iMsgType = 0;
                    String sMsgData = null;
                    try {
                        final JSONObject jsonData;
                        jsonData = new JSONObject(sData);

                        iMsgType = jsonData.getInt(MSG_TYPE_TAG);
                        sMsgData = jsonData.getString(MSG_DATA_TAG);

                    } catch (JSONException e) {
                        Log.d(TAG, "Remote JSON Exception");
                    }

                    Log.d(TAG, "Got data:" + "Type:" + iMsgType + " Data:" + sMsgData);

                    // do something with the owner message
                    handleOwnerMsg(iMsgType, sMsgData);
                }

            } catch (IOException e) {
                Log.d(TAG, "Remote readUTF Exception");
                e.printStackTrace();
            }
        }
    }

    // handle receiving a message from the owner
    private void handleOwnerMsg(int iMsgType, String sMsgData) {

        switch (iMsgType) {
            case MSG_PING:
                bConnected = true;
                if (bRequestAllData) { // if flagged to request owner data - send requests //TODO - auto send on connect?
                    bRequestAllData = false;
                    send(MSG_REQUEST_SESSION_DATA, "");
                    send(MSG_REQUEST_FULL_DATA, "");
                }
                break;

            case MSG_FULL_SPOT_DATA:
                parseSpotData(sMsgData);
                return;

            case MSG_FULL_CONSIST_DATA:
                parseConsistData(sMsgData);
                return;

            case MSG_FULL_CAR_DATA:
                parseCarData(sMsgData);
                return;

            case MSG_DELETE_SPOT_DATA:
                try {
                    MainActivity.updateSpot(new SpotData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }

                break;

            case MSG_UPDATE_SPOT_DATA:
                try {
                    MainActivity.updateSpot(new SpotData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }
                break;

            case MSG_DELETE_CONSIST_DATA:
                try {
                    MainActivity.updateConsist(new ConsistData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }

                break;

            case MSG_UPDATE_CONSIST_DATA:
                try {
                    MainActivity.updateConsist(new ConsistData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }
                break;

            case MSG_DELETE_CAR_DATA:
                try {
                    MainActivity.updateCar(new CarData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }

                break;

            case MSG_UPDATE_CAR_DATA:
                try {
                    MainActivity.updateCar(new CarData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }
                break;

        }

        // pass the message on to the UI unless a return from switch above
        if (mCallback != null) {
            mCallback.onRemoteDataUpdate(iMsgType, sMsgData);
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
            Log.d(TAG, "Remote JSON Exception");
        }
    }

    private void parseConsistData(String sData) {
        try {
            JSONArray jArray = new JSONArray(sData);

            MainActivity.getConsistList().clear();
            for (int ix = 0; ix < jArray.length(); ix++) {
                MainActivity.getConsistList().add(new ConsistData(jArray.getJSONObject(ix)));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Remote JSON Exception");
        }
    }

    private void parseCarData(String sData) {
        try {
            JSONArray jArray = new JSONArray(sData);

            MainActivity.getCarList().clear();
            for (int ix = 0; ix < jArray.length(); ix++) {
                MainActivity.getCarList().add(new CarData(jArray.getJSONObject(ix)));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Remote JSON Exception");
        }
    }

    public void send(int iMsgType, String sData) {
        SendSocket ss = new SendSocket(buildMessage(iMsgType, sData));
        ss.execute();
    }

    private class SendSocket extends AsyncTask<Void, Void, Void> {
        private String sOut;

        public SendSocket(String sx) {
           sOut = sx;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mSocket != null) {
                try {
                    DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
                    Log.d(TAG, "Sending:" + sOut);
                    dos.writeUTF(sOut);
                } catch (IOException e) {
                    Log.d(TAG, "Remote writeUTF Exception");
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
                    Log.d(TAG, "Remote Socket Close Exception");
                }
            }

            return null;
        }
    }

    public interface OnDataUpdate {
        void onRemoteDataUpdate(int msgType, String sData);
    }

}
