package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import static androidx.core.content.ContextCompat.getSystemService;

class NotificationMirror {
    private final static String TAG = "nm.NotificationMirror";

    //singelton instance of the notification mirror
    private static NotificationMirror singleton_instance = null;

    //member variables
    private final NotificationManagerCompat notificationManager;
    //ID of the next test notification
    private int currentTestNotificationId = 0;
    //ip of the PC that receives the notifications via tcp
    private String hostIP;
    //port of the PC that receives the notifications via tcp
    private int hostPort;

    /**
     * Constructor for the Notification Mirror
     *
     * @param context application context
     */
    private NotificationMirror(Context context) {
        //update the host credentials from the shared preferences
        updateHostCredentials(context);

        //initialize the notificationManager
        notificationManager = NotificationManagerCompat.from(context);
    }

    /**
     * Returns an instance of the singleton object.
     * The object will be initialized upon the first call of this function.
     *
     * @param context application context
     * @return singleton instance of the notification mirror
     */
    public static NotificationMirror getInstance(Context context) {
        if (singleton_instance == null) {
            singleton_instance = new NotificationMirror(context);
        }
        return singleton_instance;
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
     * sends the given notification via tcp and the Mirror Class over the network to the specified socket address
     * logs the contents of the notification
     *
     * @param mirrorNotification notification to
     */
    public void mirrorFromDevice(MirrorNotification mirrorNotification) {
        //create a new mirror task and start it to send the notification via tcp.
        Mirror mirror = new Mirror(hostIP, hostPort);
        mirror.execute(mirrorNotification);
    }

    /**
     * Mirrors the Notifications that come over the network to the device.
     * Displays the notification in the statusbar.
     *
     * @param mirrorNotification the notification to be displayed
     * @param channelID          the channel where the notification will be sent in
     * @param context            application context
     */
    public void mirrorFromNetwork(MirrorNotification mirrorNotification, String channelID, Context context) {
        Notification notification = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(mirrorNotification.getTitle())
                .setContentText(mirrorNotification.getText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(mirrorNotification.getReplyAction())
                .build();
        notificationManager.notify(mirrorNotification.getId(), notification);
    }

    /**
     * Displays a Notification in the statusbar.
     *
     * @param notification
     */
    public void showTestNotification(Notification notification) {

        notificationManager.notify(currentTestNotificationId, notification);
        currentTestNotificationId++;
    }

    /**
     * sends a notification over the network which dismisses the target notification
     *
     * @param notification notification to be dismissed
     */
    public void mirrorCancel(MirrorNotification notification, String IP, int PORT) {

    }

    /**
     * Exectutes the action with the given action name
     *
     * @param mirrorNotification
     * @param actionName
     */
    public void executeNotificationAction(MirrorNotification mirrorNotification, String actionName) {

    }

    /**
     * dismisses the given notification from the statusbar and active notification list (not directly but through the notification listener)
     *
     * @param mirrorNotification
     */
    public void dismissNotification(MirrorNotification mirrorNotification) {
        notificationManager.cancel(mirrorNotification.getId());
    }

    /**
     * Replies to a notification that has a reply action.
     *
     * @param mirrorNotification the notification to be replied to
     * @param message            the reply message
     * @param context            application context
     */
    public void replyToNotification(MirrorNotification mirrorNotification, String message, Context context) {
        MirrorNotification.Logger log = () -> {
            Log.e(TAG + "reply", "NO REPLYACTIONS or REMOTEINPUTS");
            Helper.toasted("Not Repliable");
        };

        final NotificationCompat.Action replyAction = mirrorNotification.getReplyAction();

        if (replyAction == null || replyAction.getRemoteInputs().length == 0) {
            Log.e(TAG, "no actions or remote inputs to reply to");
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        for (androidx.core.app.RemoteInput remoteIn : replyAction.getRemoteInputs())
            bundle.putCharSequence(remoteIn.getResultKey(), message);

        RemoteInput.addResultsToIntent(replyAction.getRemoteInputs(), intent, bundle);
        try {
            replyAction.actionIntent.send(context, 0, intent); //SET
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG + "reply", "REPLY FAILED" + e.getLocalizedMessage());
            Helper.toasted("Couldnt reply to Notification");
        }
    }

    /**
     * Create a Notification to speperate different kinds of notifications.
     * E.g the device notifications and the test notifications.
     *
     * @param channelName        the name of the channel
     * @param channelDescription a short description of the channel
     * @param channelID          the unique ID of the channel
     * @param context            application context
     */
    public void createNotificationChannel(String channelName, String channelDescription, String channelID, Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(context, NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
        //get the default ip and port from the resources
        String defaultMirrorIP = MainActivity.sContext.getResources().getString(R.string.DefaultMirrorIP);
        int defaultMirrorPort = MainActivity.sContext.getResources().getInteger(R.integer.DefaultMirrorPORT);

        //get access to the shared preferences where the ip and port are saved
        SharedPreferences sharedPreferences = context.getSharedPreferences(DeviceNotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);

        //assign ip and port from the shared preferences or from the default value
        hostIP = sharedPreferences.getString("HOST_IP", defaultMirrorIP);
        hostPort = sharedPreferences.getInt("HOST_PORT", defaultMirrorPort);
    }

}
