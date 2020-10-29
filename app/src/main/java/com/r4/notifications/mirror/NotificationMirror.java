package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.StatusBarNotification;
import android.util.Log;

class NotificationMirror {
    private final static String TAG = "NotifiactionMirror";

    public static void mirror(MirrorNotification notification) {
        Log.e(TAG, "MirrorNotification: "
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
        Mirror mirror = new Mirror();
        mirror.execute(notification);
// TODO send NotificationData over windows to network
    }

    public static void mirrorCancel(MirrorNotification notification) {
//    send Update to cancel over network
    }

    public static boolean inFilter(StatusBarNotification sbn) {//here?
        Notification notification = sbn.getNotification();

//        if ((notification.flags & Notification.FLAG_FOREGROUND_SERVICE) != 0
//                || (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0
//                || (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0
//                || (notification.flags & NotificationCompat.FLAG_GROUP_SUMMARY) != 0)
//            return true;
        if (sbn.getPackageName().equals("com.android.systemui")) {
            if (sbn.getTag().equals("low_battery"))
                return true;
            else if (sbn.getTag().equals("charging_state"))
                return true;
            else return sbn.getTag().contains("NetworkPolicy");
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
