package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

class NotificationMirror {
    private final static String TAG = "NotificationMirror";

    /**
     * sends the given notification via tcp and the Mirror Class over the network to the specified socket address
     * logs the contents of the notification
     *
     * @param notification notification to
     * @param IP           of the socket
     * @param PORT         of the socket
     */
    public static void mirror(MirrorNotification notification, String IP, int PORT) {
        Mirror mirror = new Mirror(IP, PORT);
        mirror.execute(notification);
    }

    /**
     * sends a notification over the network which dismisses the target notification
     *
     * @param notification notification to be dismissed
     */
    public static void mirrorCancel(MirrorNotification notification, String IP, int PORT) {

    }

    public static void executeNotificationAction(MirrorNotification mirrorNotification, String actionName) {

    }

    public static void dismissNotification(MirrorNotification mirrorNotification) {
        NotificationManager notificationManager = (NotificationManager) MainActivity.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mirrorNotification.getId());
    }

    public static void postNotification(MirrorNotification mirrorNotification, NotificationManagerCompat notificationManager, Context context) {
        Notification notification = new NotificationCompat.Builder(context, "TestChannel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(mirrorNotification.getTitle())
                .setContentText(mirrorNotification.getText())
//                .setContentIntent(pIntent)
//                .setPriority(NotificationCompat.PRIORITY_MAX) //For lower androids without channels
//                .setAutoCancel(true) //close onclick
                .addAction(mirrorNotification.getReplyAction())
                .build();
        notificationManager.notify(9001, notification);
    }

    public static void replyToNotification(MirrorNotification mirrorNotification, String message, Context context) {
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
