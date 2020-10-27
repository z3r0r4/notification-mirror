package com.r4.notifications.mirror;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

class NotificationMirror {
    protected final static String TAG = "NotifiactionMirror";

    public static void mirror(MirrorNotification notification) {
        Log.d(TAG, "MirrorNotification: "
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
//    send NotificationData over windows to network
    }

    public static void mirrorCancel(MirrorNotification notification) {
//    send Update to cancel over network
    }

    public static boolean inFilter(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();

        if ((notification.flags & Notification.FLAG_FOREGROUND_SERVICE) != 0
                || (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0
                || (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0
                || (notification.flags & NotificationCompat.FLAG_GROUP_SUMMARY) != 0)
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

    /*
    Get DATA out of the service
     */
    private MirrorNotification getNotification(String id) {
        return null; //use Binder to access Data in ListenerService
    }

    private void onReceive(NetworkPackage networkPackage) {//maybe not here
        MirrorNotification notification = getNotification(networkPackage.getID());
//        if (networkPackage.isReply())
//            notification.reply(networkPackage.getMessage());
//        else if (networkPackage.isAction())
//            notification.act(networkPackage.getActionName());
    }
}
