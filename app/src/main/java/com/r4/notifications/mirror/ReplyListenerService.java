package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;

/**
 * Class which listens to replies from the pc
 */
public class ReplyListenerService extends Service {
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 5646545;
    private static final String TAG = "ReplyListenerService";
    ServerSocket serverSocket;
    Socket socket;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG + "run", "running a thread");
            try {
                 serverSocket = new ServerSocket(9001, 3,InetAddress.getLocalHost()); //InetAddress.getByName("192.168.178.84"));
                while (true) {
                    Log.e(TAG,"waiting for server connections "+InetAddress.getLocalHost());
                    if (serverSocket != null) {
                        serverSocket.setSoTimeout(10000);
                        socket = serverSocket.accept();
                        Log.e(TAG, "new Client!");
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        //DO IN NEW THREADS
                        Log.e(TAG, dataInputStream.toString());
//                        if (netpkg.isReply())
//                            (new MirrorNotification(netpkg)).reply(netpkg.getMessage(), MainActivity.sContext);
//                        if (netpkg.isAction())
//                            (new MirrorNotification(netpkg)).act(netpkg.getActionName());
//                        if (netpkg.isDismiss())
//                            (new MirrorNotification(netpkg)).dismiss();
                        continue;
                    }
                }
//                NetworkPackage netpkg = receiveData();//DO IN WHILE

            } catch (IOException e) {
                Log.e(TAG, "Socket connection failed",e);
            }

            Log.e(TAG + "run", "ending a thread");
        }
    };
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();

        executorService = Executors.newFixedThreadPool(4);
//        receiveDataInBackground();
        new Thread(runnable).start();
        Log.e(TAG, "ended thread");
    }

    private void receiveDataInBackground() {

    }

    private NetworkPackage receiveData() {

        return new NetworkPackage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
        try {
            serverSocket.close();
//            socket.close();
        } catch (IOException e) {
            Log.e(TAG,"couldnt close Sockets");
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //Intent should contain the Socket address
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
//no one tells you to put this here and not externally -.-
        Intent notificationIntent = new Intent(this, ReplyListenerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification =
                new Notification.Builder(this, MainActivity.FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID)
                        .setContentTitle("Notification Mirror Reply Listener Service")
                        .setContentText("Listening for Replies from the PC")
                        .setSmallIcon(R.drawable.ic_launcher_background) //very necessary
                        .setContentIntent(pendingIntent)
                        .setTicker("Notification Mirror Reply Listener Service: Listening for Replies from the PC")
                        .build();
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
}