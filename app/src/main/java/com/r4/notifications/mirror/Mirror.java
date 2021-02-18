package com.r4.notifications.mirror;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

//TODO Background SocketServer service
//for the time after notification with reply was transmitted
//ping every few minutes afterwards
//TODO wait for secret
//TODO receive replies in Background service
//TODO reply then
class Mirror extends AsyncTask<MirrorNotification, Void, Void> { //deprecated but who cares
    private final static String TAG = "Mirror";

    private final ObjectOutputStream outputStream = null;

    private String HOST_IP = "192.168.178.84"; //DEFAULT VALUES
    private int HOST_PORT = 9001; //DEFAULT VALUES

    /**
     * needed
     */
    protected Mirror() {
    }

    /**
     * initalize the Socket address
     *
     * @param IP
     * @param PORT
     */
    protected Mirror(String IP, int PORT) {
        this.HOST_IP = IP;
        this.HOST_PORT = PORT;
    }

    /**
     * send the notification as json over tcp to the socket address
     * catch and log if the address isnt reachable
     *
     * @param mnts notification to be sent
     * @return nothing
     */
    @Override
    protected Void doInBackground(MirrorNotification... mnts) {

        MirrorNotification notification = mnts[0];
        Log.d(TAG, "doInBackground: Starting Async Socket Connection to mirror");
        try {
            Log.d(TAG, "Trying to Connect to Socket " + HOST_IP + ":" + HOST_PORT);
            Socket socket = new Socket();//HOST_IP, HOST_PORT);
            socket.connect(new InetSocketAddress(HOST_IP, HOST_PORT), 10000);
            Log.d(TAG, "Socket Connected");

            String jason = new Gson().toJson(notification);

            PrintWriter writer = new PrintWriter(socket.getOutputStream());//,true);
            writer.write(jason);
            writer.flush();
            writer.close();
            socket.close();
            Log.d(TAG + "doInBackground", "Notification successfully Mirrored");
        } catch (IOException e) {
            Log.e(TAG + "doInBackground", "SOCKET CONNECTION FAILED\n" + HOST_IP + ":" + HOST_PORT);
//            Helper.toasted("Couldnt connect to Socket to mirror Notification");
        }
        Log.d(TAG + "doInBackground", "Ending Asynctask, Closed Socket");
        return null;
    }
}