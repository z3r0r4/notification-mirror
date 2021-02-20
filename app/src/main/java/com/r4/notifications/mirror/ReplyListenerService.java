package com.r4.notifications.mirror;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import androidx.core.app.NotificationCompat;

//TODO make port modifiable

//TODO clean code:
//TODO clean logs

//TODO hand over to pinging instead of keeping the socket open
//for the time after notification with reply was transmitted
//ping every few minutes afterwards

/**
 * Class which listens to replies from the pc
 */
public class ReplyListenerService extends Service {
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 5646545;
    private static final String TAG = "ReplyListenerService";

    private ServerSocket serverSocket;
    private Socket socket;
    private Thread mThread;
    private boolean stopThread = false;
    /**
     * run socket that waits for connections and json strings, and takes actions on the notifications depending on the received data:
     * get Hostaddress
     * setup server socket
     * wait for new connecetions until thread is stopped or socket dies
     * read from connections
     * create networkpackage
     * processreply:
     * create Notification
     * take action on notification depending on networkpackage
     * <p>
     * close server socket
     * stop service (maybe not) (maybe rather restart thread)
     */
    Runnable ReplyReceiverRunnable = new Runnable() {
        /* TO USE THIS IN THE EMULATOR: THE PORTS HAVE TO BE FORWARDED
         * adb -s emulator-5554 forward tcp:9002 tcp:9001
         * tcp:port adresses on host machine tcp:port forwarded to on emulator
         *
         * Test with:
         *   echo "{'id':'9001','key':'0|com.r4.notifications.mirror|9001|null|10084','message':'ANSWER','isdismiss':'true','isreply':'','isaction':''}" | nc 127.0.0.1 9002
         * */
        @Override
        public void run() {

            Log.e(TAG + "run", "running a thread");
            try {
                InetAddress IP = Inet4Address.getLocalHost();//InetAddress.getByName("192.168.232.2");//InetAddress.getByName(MainActivity.sContext.getResources().getString(R.string.DefaultMirrorIP));
                int PORT = MainActivity.sContext.getResources().getInteger(R.integer.DefaultReceiverPORT);
                serverSocket = new ServerSocket(PORT, 0, IP);

                while (!stopThread) {
                    if (serverSocket != null) {
//                        serverSocket.setSoTimeout(10000);
                        Log.d(TAG, "waiting for server connections on" + IP + PORT);
                        socket = serverSocket.accept();
                        Log.e(TAG, "new Client!");

                        String read = (new BufferedReader(new InputStreamReader(socket.getInputStream()))).readLine();
//                        Log.e(TAG, read);
                        NetworkPackage netpkg = new NetworkPackage(read);
                        netpkg.log();
                        processReply(netpkg);        //could be done in new threads
                    } else break;
                }

            } catch (IOException e) {
                Log.e(TAG, "Socket connection failed", e);
            } catch (ExceptionInInitializerError e) {
                Log.e(TAG, "wrong ID for Key");
            }


            Log.e(TAG + "run", "ending thread");
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket failed to close");
            }
            stopSelf(); //maybe restart service to restart thread
        }
    };
    private SharedPreferences.Editor editor;

    /**
     * depening on the network package answer the notification
     * reply with message
     * act on action with actionname
     * dismiss notification
     *
     * @param netpkg from json extracted networkpackage containing the data for the actions on the notifications
     */
    private void processReply(NetworkPackage netpkg) {
        MirrorNotification mn = new MirrorNotification(netpkg);
        if (netpkg.isReply())
            mn.reply(netpkg.getMessage(), MainActivity.sContext);
        if (netpkg.isAction())
            mn.act(netpkg.getActionName());
        if (netpkg.isDismiss())
            mn.dismiss();
    }

    /**
     * create everything thats needed for the service to run
     * get access to the preferences
     * create thread from socket runnable
     * start thread
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences shPref = this.getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();

        mThread = new Thread(ReplyReceiverRunnable);
        mThread.start();
    }

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
        stop();
    }

    /**
     * set the receiver preference to true
     * post a notification for the foreground service to fullfill android requirements
     * start the service in forground
     * start the thread if it isnt started already
     * restart if killed on return
     *
     * @param intent  idk
     * @param flags   dc
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
                new NotificationCompat.Builder(this, MainActivity.FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID)
                        .setContentTitle("Notification Mirror Reply Listener Service")
                        .setContentText("Listening for Replies from the PC")
                        .setSmallIcon(R.drawable.ic_launcher_background) //very necessary
                        .setContentIntent(pendingIntent)
                        .setTicker("Notification Mirror Reply Listener Service: Listening for Replies from the PC")
                        .build();
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);

        if (!mThread.isAlive()) {
            Log.e(TAG, "thread seemingly dead: starting again");
            mThread = new Thread(ReplyReceiverRunnable);
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
        stop();
        return false;
    }

    /**
     * closes the socket, stops the thread and updates the preferences
     */
    private void stop() {
        try {
            serverSocket.close();   //socket.close();
        } catch (IOException e) {
            Log.e(TAG, "couldnt close Sockets");
        }
        stopThread = true;          //mThread.stop();
        editor.putBoolean("ReceiverStatus", false);
        editor.apply();
        Log.d(TAG, "Receiver inactive");
    }

    /**
     * not used
     * but needs to be implemented
     *
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