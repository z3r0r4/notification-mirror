package com.r4.notifications.mirror;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

class NotificationMirror {
    private final static String TAG = "NotifiactionMirror";

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
// TODO   send NotificationData over windows to network
    }

    public static void mirrorCancel(MirrorNotification notification) {
//    send Update to cancel over network
    }

    public static boolean inFilter(StatusBarNotification sbn) {//here?
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

    /* Get DATA out of the service */


//    public static NotificationReceiver sReceiver;
//    public static boolean mBound = false;
//
//    public static ServiceConnection mConnection = new ServiceConnection() {
//        // Called when the connection with the service is established
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // Because we have bound to an explicit
//            // service that is running in our own process, we can
//            // cast its IBinder to a concrete class and directly access it.
//            NotificationReceiver.LocalBinder binder = (NotificationReceiver.LocalBinder) service;
//            sReceiver = binder.getService();
//            mBound = true;
//        }
//
//        // Called when the connection with the service disconnects unexpectedly
//        public void onServiceDisconnected(ComponentName className) {
//            Log.e(TAG, "onServiceDisconnected");
//            mBound = false;
//        }
//    };
//    public static MirrorNotification getNotification(String id, Context context) {
//        sReceiver.checkBinding();
//        return null; //use Binder to access Data in ListenerService
//    }
}
