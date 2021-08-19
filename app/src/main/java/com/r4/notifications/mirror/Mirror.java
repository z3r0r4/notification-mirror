package com.r4.notifications.mirror;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @since 20210719
 * sends jsonfied {@link MirrorNotification} 's over the network sockets
 * async
 */
class Mirror extends AsyncTask<MirrorNotification, Void, Void> { //deprecated but who cares
    private final static String TAG = "Mirror";

    private String HOST_IP = MainActivity.sContext.getResources().getString(R.string.DefaultMirrorIP);
    private int HOST_PORT = MainActivity.sContext.getResources().getInteger(R.integer.DefaultMirrorPORT);

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
            Log.e(TAG + "doInBackground", "SOCKET CONNECTION FAILED\n" + HOST_IP + ":" + HOST_PORT,e);
//            Helper.toasted("Couldnt connect to Socket to mirror Notification");
        }
        Log.d(TAG + "doInBackground", "Ending Asynctask, Closed Socket");
        return null;
    }
}