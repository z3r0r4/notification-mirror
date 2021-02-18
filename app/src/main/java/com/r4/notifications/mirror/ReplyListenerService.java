package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;

/**
 * Class which listens to replies from the pc
 */
public class ReplyListenerService extends Service {
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 5646545;
    private static final String TAG = "ReplyListenerService";
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();

        executorService = Executors.newFixedThreadPool(4);
        receiveDataInBackground();
        Log.e(TAG, "ended thread");
    }

    private void receiveDataInBackground() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG + "run", "running a thread");
                NetworkPackage netpkg = receiveData();
                if (netpkg.isReply())
                    (new MirrorNotification(netpkg)).reply(netpkg.getMessage(), MainActivity.sContext);
                if (netpkg.isAction())
                    (new MirrorNotification(netpkg)).act(netpkg.getActionName());
                if (netpkg.isDismiss())
                    (new MirrorNotification(netpkg)).dismiss();
                Log.e(TAG + "run", "ending a thread");
            }
        });

    }

    private NetworkPackage receiveData() {

        return new NetworkPackage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();

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