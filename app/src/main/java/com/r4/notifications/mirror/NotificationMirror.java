package com.r4.notifications.mirror;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

class NotificationMirror {
    private final static String TAG = "NotifiactionMirror";

    public static void mirror(MirrorNotification notification, String IP, int PORT) {
        Log.d(TAG +"MirrorNotification", "to be mirrored:"
                        + "\nID     :" + notification.id
                        + "\nkey    :" + notification.key
                        + "\nappName:" + notification.appName
                        + "\ntime   :" + notification.time
                        + "\ntitle  :" + notification.title
                        + "\ntext   :" + notification.text
                        + "\nticker :" + notification.ticker
//                + "\nactions:" + actions.size()
//                + "\nrepAct :" + replyAction.title
        );

        Mirror mirror = new Mirror(IP, PORT);
        mirror.execute(notification);
    }

    public static void mirrorCancel(MirrorNotification notification) {
//    send Update to cancel over network
    }

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

    private void onReceive(NetworkPackage networkPackage) {//maybe not here
//        MirrorNotification notification = getNotification(networkPackage.getID());
//        if (networkPackage.isReply())
//            notification.reply(networkPackage.getMessage());
//        else if (networkPackage.isAction())
//            notification.act(networkPackage.getActionName());
    }
}
