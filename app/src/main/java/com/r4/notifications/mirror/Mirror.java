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
class Mirror extends AsyncTask<MirrorNotification, Void, Void> { //deprecated
    private final static String TAG = "Mirror";

    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private ObjectOutputStream outputStream = null;
    private PrintWriter mWriter;
    private String HOST_IP = "192.168.178.10";
    private int HOST_PORT = 9001;

    @Override
    protected Void doInBackground(MirrorNotification... mnts) {

        MirrorNotification notification = mnts[0];
        Log.e(TAG, "doInBackground: Starting Async");
        try {
            Log.d(TAG, "Connecting to Socket");
            mSocket = new Socket();//HOST_IP, HOST_PORT);
//            mSocket.checkConnect();
//            InetAddress inetAddress = InetAddress.getByName("192.168.178.10");
//            SocketAddress socketAddress = new InetSocketAddress(inetAddress, 9999);
//            mSocket.bind(socketAddress);
            mSocket.connect(new InetSocketAddress(HOST_IP, HOST_PORT), 5000);
            Log.d(TAG, "Socket Connected");

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
        Log.e(TAG, "doInBackground: Ending Async");
        return null;
    }
}