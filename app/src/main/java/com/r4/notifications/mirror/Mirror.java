package com.r4.notifications.mirror;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
//TODO receive replies in Background service
//TODO reply then
class Mirror extends AsyncTask<MirrorNotification, Void, Void> {
    private final static String TAG = "Mirror";

    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private ObjectOutputStream outputStream = null;
    private PrintWriter mWriter;
    private String HOST_IP = "192.168.178.10";
    private int HOST_PORT = 9999;

    @Override
    protected Void doInBackground(MirrorNotification... mnts) {

        MirrorNotification notification = mnts[0];
        try {
            Log.d(TAG, "Connecting to Socket");
            mSocket = new Socket(HOST_IP, HOST_PORT);
//            mSocket.checkConnect();
            Log.d(TAG, "Socket Connected");
//            InetAddress inetAddress = InetAddress.getByName("192.168.178.10");
//            SocketAddress socketAddress = new InetSocketAddress(inetAddress, 9999);
//            mSocket.bind(socketAddress);
//            mSocket.connect(socketAddress, 5000);

            String jason = new Gson().toJson(notification);
//            outputStream = new ObjectOutputStream(mSocket.getOutputStream());
//            outputStream.writeObject(notification);
//            outputStream.flush();
//            outputStream.close();
            mWriter = new PrintWriter(mSocket.getOutputStream());//,true);
//            mPrintWriter.println(message);
            mWriter.write(jason);
            mWriter.flush();
            mWriter.close();
            mSocket.close();
            Log.d(TAG, "doInBackground: SENNT");
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: COULDNT CONNCET", e);
        }
        return null;
    }
}