package com.r4.notifications.mirror;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkNotificationRunnable implements Runnable {
    private static final String TAG = "nm.NetworkNotificationRunnable";
    private final MirrorNotification notification;
    private final String hostname;
    private final int port;

    /**
     *
     * @param notification Notification to be sent via tcp
     * @param hostname address of the PC that receives the notification
     * @param port port on which the receiving PC listens
     */
    public NetworkNotificationRunnable(MirrorNotification notification, String hostname, int port) {
        this.notification = notification;
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Connect to the receiving PC and send the notification as a json string
     */
    @Override
    public void run() {
        try {
            Log.d(TAG, "Trying to Connect to Socket " + hostname + ":" + port);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), 10000);
            Log.d(TAG, "Socket Connected");

            String json = new Gson().toJson(notification);

            PrintWriter writer = new PrintWriter(socket.getOutputStream());//,true);
            writer.write(json);
            writer.flush();
            writer.close();
            socket.close();
            Log.d(TAG + "doInBackground", "Notification successfully Mirrored");
        } catch (IOException e) {
            Log.e(TAG + "doInBackground", "SOCKET CONNECTION FAILED\n" + hostname + ":" + port,e);
        }
    }
}
