package com.r4.notifications.mirror;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

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
            mSocket = new Socket("192.168.178.10", 9999);
            mPrintWriter = new PrintWriter(mSocket.getOutputStream(),true);
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
