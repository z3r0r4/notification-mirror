package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.gson.annotations.Until;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.core.content.ContextCompat.getSystemService;

/**
 * @since 20210719
 * works the mirror and manages what shall be send(mirrored over the nextwork)
 * synchronious
 */

class NotificationMirror {
    private final static String TAG = "nm.NotificationMirror";

    //singelton instance of the notification mirror
    private static NotificationMirror singleton_instance = null;


    //ip of the PC that receives the notifications via tcp
    private String hostname;
    //port of the PC that receives the notifications via tcp
    private int hostPort;
    //handles the execution in the background
    ExecutorService executor;

    /**
     * Constructor for the Notification Mirror
     *
     * @param context application context
     */
    private NotificationMirror(Context context) {
        //update the host credentials from the shared preferences
        updateHostCredentials(context);

        //create a single thread executor, since only one of the same ports can be opened anyways
        this.executor = Executors.newSingleThreadExecutor();

        //initialize the notificationManager
//        notificationManager = NotificationManagerCompat.from(context);

    }

    /**
     * Returns an instance of the singleton object.
     * The object will be initialized upon the first call of this function.
     *
     * @param context application context
     * @return singleton instance of the notification mirror
     */
    public static NotificationMirror getSingleInstance(Context context) {
        if (singleton_instance == null) {
            singleton_instance = new NotificationMirror(context);
        }
        return singleton_instance;
    }

    /**
     * sends the given notification via tcp and the Mirror Class over the network to the specified socket address
     * logs the contents of the notification
     *
     * @param mirrorNotification notification to
     */
    public void mirrorFromDevice(MirrorNotification mirrorNotification) {
        executor.execute(new NetworkNotificationRunnable(mirrorNotification, hostname, hostPort));
    }
//     * @param notification notification to
//     * @param IP           of the socket
//     * @param PORT         of the socket
//     */
//    public static void mirror(MirrorNotification notification, String IP, int PORT) {
//        notification.log();
//
//        Mirror mirror = new Mirror(IP, PORT);
//        mirror.execute(notification);
//    }
    /**
     * sends a notification over the network which dismisses the target notification
     *
     * @param notification notification to be dismissed
     */
    public static void mirrorDismissFromDevice(MirrorNotification mirrorNotification) {

    }

    /**
     * checks if the notification is one that is sensible to store
     * excludes charging state updates, low battery warnings and mobile data warnings
     *
     * @param sbn notification to be checked
     * @return if the notification may pass the filter
     */
    public static boolean inFilter(StatusBarNotification sbn) {//here?
        Notification notification = sbn.getNotification();

        if ((notification.flags & Notification.FLAG_FOREGROUND_SERVICE) != 0
                || (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0
                || (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0
                || (notification.flags & NotificationCompat.FLAG_GROUP_SUMMARY) != 0
        )
            return true;
        if (sbn.getPackageName().equals("android"))
            return true;
        if (sbn.getPackageName().equals("com.android.systemui")) {
            if (sbn.getTag().equals("low_battery"))
                return true;
            else if (sbn.getTag().equals("charging_state"))
                return true;
            else if (sbn.getTag().contains("NetworkPolicy"))
                return true;
        }
        return false;
    }


    /**
     * Updates the IP Address and the Port of the device that the notifications will
     * be mirrored to.
     * They will be loaded from the shared preferences if available, otherwise the default
     * values will be used.
     *
     * @param context application context
     */
    public void updateHostCredentials(Context context) {
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getSingleInstance(context);

        //assign ip and port from the shared preferences or from the default value
        hostname = sharedPreferencesManager.getMirrorIP(context);
        hostPort = sharedPreferencesManager.getMirrorPort(context);
    }

    /**
     * reacts to a answer from the pc
     * replies to notifications
     * actions
     * dismisses
     *
     * @param networkPackage
     */
    private void onReceive(NetworkPackage networkPackage) {//maybe not here
//        MirrorNotification notification = getNotification(networkPackage.getID());
//        if (networkPackage.isReply())
//            notification.reply(networkPackage.getMessage());
//        else if (networkPackage.isAction())
//            notification.act(networkPackage.getActionName());
    }




}
