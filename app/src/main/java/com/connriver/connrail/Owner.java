package com.connriver.connrail;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.WIFI_SERVICE;
import static com.connriver.connrail.MainActivity.MSG_DELETE_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_DELETE_CONSIST_DATA;
import static com.connriver.connrail.MainActivity.MSG_DELETE_SPOT_DATA;
import static com.connriver.connrail.MainActivity.MSG_FULL_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_FULL_CONSIST_DATA;
import static com.connriver.connrail.MainActivity.MSG_REQUEST_FULL_DATA;
import static com.connriver.connrail.MainActivity.MSG_REQUEST_SESSION_DATA;
import static com.connriver.connrail.MainActivity.MSG_SESSION_DATA;
import static com.connriver.connrail.MainActivity.MSG_FULL_SPOT_DATA;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_CAR_DATA;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_CONSIST_DATA;
import static com.connriver.connrail.MainActivity.MSG_UPDATE_SPOT_DATA;
import static com.connriver.connrail.MainActivity.TAG;
import static com.connriver.connrail.MainActivity.MSG_DATA_TAG;
import static com.connriver.connrail.MainActivity.MSG_PING;
import static com.connriver.connrail.MainActivity.MSG_TYPE_TAG;
import static com.connriver.connrail.MainActivity.MSG_UPDATE;
import static com.connriver.connrail.MainActivity.SOCKET_PORT;
import static com.connriver.connrail.MainActivity.getSessionNumber;

/**
 * Created by user on 7/6/2018.
 */

public class Owner {
    private Thread newSocketThread = null;
    private ServerSocket mOwnerSocket = null;
    private static ArrayList<Socket> listSockets = new ArrayList<>();
    private OnDataUpdate mCallback = null;
    private Timer timer;
    private PowerManager.WakeLock wl;
    private WifiManager.WifiLock wfl;
    private Context mContext;
    private static final int PING_INTERVAL = 5000;

    // start a thread that listens on a port for new remote socket connections
    // for each new socket listen for a message

    public Owner(Context context, OnDataUpdate callback) {

        mContext = context;
        mCallback = callback;

        // the owner has to stay alive during sleep mode
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "keep_app_alive");
        wl.acquire();

        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "keep_wifi_alive");
        wfl.acquire();

        // start the connected timer that sends pings to any listening remotes
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Owner Timer tick - count = " + getRemoteCount());

                // send a ping to all attached remotes
                sendAll(MSG_PING, "");

                // send a ping to the UI so it can display the remote count
                if (mCallback != null) {
                    mCallback.onOwnerDataUpdate(MSG_PING, "");
                }
            }
        }, 0, PING_INTERVAL);

        // create a new worker thread and start
        newSocketThread = new Thread(new NewSocketThread());
        newSocketThread.start();
    }

    public void close() {

        timer.cancel();

        wfl.release();
        wl.release();

        // close any the remote sockets
        for (Socket socket : listSockets) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.d(TAG, "Owner Remote Socket Close Exception");
            }
        }

        // close the owner socket - this will trigger an exception in the accept thread and end it
        if (mOwnerSocket != null) {
            try {
                mOwnerSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Owner Socket Close Exception");
            }
        }
    }

    private class NewSocketThread extends Thread {

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                mOwnerSocket = new ServerSocket(SOCKET_PORT);

                while (true) {
                    // wait for a new socket connection
                    Socket socket = mOwnerSocket.accept();

                    // add it to the list of available sockets
                    addSocket(socket);
                }
            } catch (IOException e) {
                Log.d(TAG, "Owner Socket Accept Exception");
            }
        }
    }

    private void addSocket(Socket socket) {
        // add the socket to the list
        for (Socket sx : listSockets) {
            if (sx.getInetAddress().equals(socket.getInetAddress())) {
                try {
                    sx.close();
                } catch (IOException e) {
                    Log.d(TAG, "Owner Close Exception");
                    e.printStackTrace();
                }
                listSockets.remove(sx);
            }
        }
        listSockets.add(socket);

        // start a thread to listen for any data from the remote
        Thread listenThread = new Thread(new ListenThread(socket));
        listenThread.start();

        // send the new remote the initial data
        send(socket, MSG_PING, "");
    }

    private class ListenThread extends Thread {

        Socket mSocket;
        ListenThread(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            try {
                //create a new socket, get the input stream and listen
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
                        Log.d(TAG, "Owner JSON Exception");
                    }

                    Log.d(TAG, "Owner Got message. Type = " + iMsgType + " data:" + sMsgData);

                    handleRemoteMsg(mSocket, iMsgType, sMsgData);
                }


            } catch (IOException e) {
                Log.d(TAG, "Owner readUTF Exception");
            }
        }
    }

    // handle receiving a message from a remote
    private void handleRemoteMsg(Socket socket, int iMsgType, String sMsgData) {

        switch (iMsgType) {
            case MSG_REQUEST_SESSION_DATA:
                send(socket, MSG_SESSION_DATA, Integer.toString(getSessionNumber()));
                return;
            case MSG_REQUEST_FULL_DATA:
                sendAllTables(socket);
                return;

            case MSG_DELETE_SPOT_DATA:
                try {
                    MainActivity.spotAddEditDelete(new SpotData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }

                break;

            case MSG_UPDATE_SPOT_DATA:
                try {
                    MainActivity.spotAddEditDelete(new SpotData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }
                break;

            case MSG_DELETE_CONSIST_DATA:
                try {
                    MainActivity.consistAddEditDelete(new ConsistData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }

                break;

            case MSG_UPDATE_CONSIST_DATA:
                try {
                    MainActivity.consistAddEditDelete(new ConsistData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }
                break;

            case MSG_DELETE_CAR_DATA:
                try {
                    MainActivity.carAddEditDelete(new CarData(new JSONObject(sMsgData)), true);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }

                break;

            case MSG_UPDATE_CAR_DATA:
                try {
                    MainActivity.carAddEditDelete(new CarData(new JSONObject(sMsgData)), false);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception");
                }
                break;
        }

        // pass the message on to the UI unless a return from switch above
        if (mCallback != null) {
            mCallback.onOwnerDataUpdate(iMsgType, sMsgData);
        }
    }

    // send all the tables to a given remote/socket
    private void sendAllTables(Socket socket) {
        send(socket, MSG_FULL_SPOT_DATA, buildSpotTable());
        send(socket, MSG_FULL_CONSIST_DATA, buildConsistTable());
        send(socket, MSG_FULL_CAR_DATA, buildCarTable());
    }

    private String buildSpotTable() {
        JSONArray jArray = new JSONArray();
        for (SpotData sd : MainActivity.getSpotList()) {
            jArray.put(sd.toJSON());
        }
        return jArray.toString();
    }

    private String buildConsistTable() {
        JSONArray jArray = new JSONArray();
        for (ConsistData cd : MainActivity.getConsistList()) {
            jArray.put(cd.toJSON());
        }
        return jArray.toString();
    }

    private String buildCarTable() {
        JSONArray jArray = new JSONArray();
        for (CarData cd : MainActivity.getCarList()) {
            jArray.put(cd.toJSON());
        }
        return jArray.toString();
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

    // send to a specific remote
    private void send(Socket socket, int msgType, String data) {
        SendSocket ss = new SendSocket(socket, buildMessage(msgType, data));
        ss.execute();
    }

    private class SendSocket extends AsyncTask<Void, Void, Void> {

        private String sOut;
        private Socket targetSocket = null;

        public SendSocket(Socket socket, String sx) {
            targetSocket = socket;
            sOut = sx;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DataOutputStream dos = new DataOutputStream(targetSocket.getOutputStream());
                Log.d(TAG, "Sending:" + sOut);
                dos.writeUTF(sOut);
            } catch (IOException e) {
                Log.d(TAG, "Owner writeUTF Exception");
            }

            return null;
        }
    }

    // send the update to all remotes
    public void sendAll(int msgType, String data) {
        SendAllSockets sas = new SendAllSockets(buildMessage(msgType, data));
        sas.execute();
    }

    private class SendAllSockets extends AsyncTask<Void, Void, Void> {

        private String sOut;

        public SendAllSockets(String sx) {
            sOut = sx;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (Socket sx : listSockets) {
                try {
                    DataOutputStream dos = new DataOutputStream(sx.getOutputStream());
                    Log.d(TAG, "Owner Sending:" + sOut);
                    dos.writeUTF(sOut);
                } catch (IOException e) {
                    Log.d(TAG, "Owner Sending Exception");
                    e.printStackTrace();
                    listSockets.remove(sx);
                }
            }

            return null;
        }
    }

    public String getIP() {
        String ret = findIP_WIFI();
        if (ret == null) {
            ret = findIP_Internet();
        }
        return ret;
    }

    public int getRemoteCount() {
        return listSockets.size();
    }

    private String findIP_WIFI() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return ipAddressString;
    }

    private String findIP_Internet() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.d(TAG, "Owner Socket Exception");
        }
        return "Not Found";
    }


    public interface OnDataUpdate {
        void onOwnerDataUpdate(int msgType, String sData);
    }

}
