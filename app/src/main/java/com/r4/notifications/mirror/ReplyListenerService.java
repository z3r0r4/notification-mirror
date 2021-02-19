package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import androidx.annotation.Nullable;
//TODO make port modifiable

//TODO clean code:
//TODO extract methods
//TODO clean logs
//TODO

//TODO document

/**
 * Class which listens to replies from the pc
 */
public class ReplyListenerService extends Service {
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 5646545;
    private static final String TAG = "ReplyListenerService";
    ServerSocket serverSocket;
    Socket socket;
    Thread mThread;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;

    //    private ExecutorService executorService;
    private boolean stopThread = false;
    /**
     * run socket that waits for connections and json strings, and takes actions on the notifications depending on the received data:
     * get Hostaddress
     * setup server socket
     * wait for new connecetions until thread is stopped or socket dies
     * read from connections
     * create networkpackage
     * create Notification
     * take action on notification depending on networkpackage
     *
     * close server socket
     * stop service (maybe not) (maybe rather restart thread)
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* TO USE THIS IN THE EMULATOR: THE PORTS HAVE TO BE FORWARDED
             * adb -s emulator-5554 forward tcp:9002 tcp:9001
             * tcp:port adresses on host machine tcp:port forwarded to on emulator
             *
             * Test with:
             *   echo "{'id':'9001','key':'0|com.r4.notifications.mirror|9001|null|10084','message':'ANSWER','isdismiss':'true','isreply':'','isaction':''}" | nc 127.0.0.1 9002
             * */
            Log.e(TAG + "run", "running a thread");
            try {
                InetAddress IP = Inet4Address.getLocalHost();//InetAddress.getByName("192.168.232.2");//InetAddress.getByName("192.168.178.84");
                serverSocket = new ServerSocket(9001, 0, IP);
                while (true) {
                    Log.d(TAG, "waiting for server connections " + IP);
                    if (serverSocket != null && !stopThread) {
//                        serverSocket.setSoTimeout(10000);
                        Log.d(TAG, "set TImeout");
                        socket = serverSocket.accept();
                        Log.e(TAG, "new Client!");
//                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        // Client established connection.
                        // Create input and output streams
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String input = in.readLine();
                        Log.e(TAG, input);
                        NetworkPackage netpkg = new NetworkPackage(input);
                        netpkg.log();
                        //DO IN NEW THREADS

                        MirrorNotification mn = new MirrorNotification(netpkg);
                        if (netpkg.isReply())
                            mn.reply(netpkg.getMessage(), MainActivity.sContext);
                        if (netpkg.isAction())
                            mn.act(netpkg.getActionName());
                        if (netpkg.isDismiss())
                            mn.dismiss();
                    } else {
                        break;
                    }
                }
//                NetworkPackage netpkg = receiveData();//DO IN WHILE

            } catch (IOException e) {
                Log.e(TAG, "Socket connection failed", e);
            } catch (ExceptionInInitializerError e) {
                Log.e(TAG, "wrong ID for Key");
            }
            Log.e(TAG + "run", "ending thread");
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //maybe restart service to restart thread
            stopSelf();
        }
    };

    /**
     * create everything thats needed for the service to run
     * get access to the preferences
     * create thread from socket runnable
     * start thread
     */
    @Override
    public void onCreate() {
        super.onCreate();
        shPref = this.getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();

        mThread = new Thread(runnable);
        mThread.start();
//        executorService = Executors.newFixedThreadPool(4);
//        receiveDataInBackground();
    }

    private void receiveDataInBackground() {

    }

//    private NetworkPackage receiveData() {
//
//        return new NetworkPackage();
//    }

    /**
     * destroys and kills the servie and its threads
     * close the socket
     * indicate the thread to stop
     * update receiver preference
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
        try {
            serverSocket.close();
//            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "couldnt close Sockets");
        }
//        mThread.stop();
        stopThread = true;
        editor.putBoolean("ReceiverStatus", false);
        editor.apply();
        Log.d(TAG, "Receiver inactive");
    }

    /**
     * set the receiver preference to true
     * post a notification for the foreground service to fullfill android requirements
     * start the service in forground
     * start the thread if it isnt started already
     * restart if killed on return
     * @param intent idk
     * @param flags dc
     * @param startId maybe?
     * @return StartSticky int
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //Intent should contain the Socket address
        editor.putBoolean("ReceiverStatus", true);
        editor.apply();
        Log.d(TAG, "Receiver active");

//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
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

        if (!mThread.isAlive()) {
            Log.e(TAG, "thread seemingly dead: starting again");
            mThread = new Thread(runnable);
            mThread.start();
            if (mThread.isAlive())
                Toast.makeText(MainActivity.sContext, "thread restarted", Toast.LENGTH_SHORT).show();
        }
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * stops the service by closing the socket, ending the thread in which the socket was running and sets the receiver preference to false
     *
     * @param service idk
     * @return false
     */
    @Override
    public boolean stopService(Intent service) {
        try {
            serverSocket.close();
//            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "couldnt close Sockets");
        }
//        mThread.stop();
        stopThread = true;
        editor.putBoolean("ReceiverStatus", true);
        editor.apply();
        Log.d(TAG, "Receiver active");
        return false;
    }

    /**
     * not used
     * but needs to be implemented
     * @param intent idk
     * @return null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
}