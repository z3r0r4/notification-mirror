package com.r4.notifications.mirror;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

class Mirror extends AsyncTask<String, Void, Void> {
    private final static String TAG = "Mirror";

    Socket mSocket;
    DataOutputStream mDataOutputStream;
    PrintWriter mPrintWriter;

    @Override
    protected Void doInBackground(String... strings) {

        String message = strings[0];
        try {
//            mSocket.checkConnect();
            Log.d(TAG, "doInBackground: sending");
            mSocket = new Socket();
            InetAddress inetAddress = InetAddress.getByName("192.168.178.10");
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, 9999);
//            mSocket.bind(socketAddress);
            mSocket.connect(socketAddress, 5000);
            mPrintWriter = new PrintWriter(mSocket.getOutputStream(), true);
            mPrintWriter.println("This is a message sent to the server");
            //            mPrintWriter.write(message);
//            mPrintWriter.flush();
            mPrintWriter.close();
            mSocket.close();
            Log.d(TAG, "doInBackground: SENNT");

        } catch (IOException e) {
            Log.e(TAG, "doInBackground: COULDNT CONNCET", e);
        }
        return null;
    }
}
