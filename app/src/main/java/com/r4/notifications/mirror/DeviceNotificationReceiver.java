package com.r4.notifications.mirror;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class DeviceNotificationReceiver extends NotificationListenerService {
    private final static String TAG = "nm.DeviceNotificationReceiver";

    public static Map<String, MirrorNotification> activeNotifications = new HashMap<>();
    public static String lastKey;
    private static String lastlastKey;

    private UserSettingsManager userSettingsManager;

    /**
     * checks if any notificaitons have been received yet and returns
     *
     * @return a map of the currently active notifications accessible by their key
     */
    public static Map<String, MirrorNotification> getactiveNotifications() {
        if (lastKey == null) {
            throw new NullPointerException();
        }
        return activeNotifications;
    }

    /**
     * Returns the last Notification that had been added to the map
     *
     * @return last received notification
     */
    public static MirrorNotification getLastNotification() {
        if(lastKey == null) {
            throw new NullPointerException();
        }
        return activeNotifications.get(lastKey);
    }

    /**
     * set the listener status preference as ensabled
     */
    public void onListenerConnected() {
        userSettingsManager.setDeviceNotificationReceiverStatus(true);
        Log.d(TAG, "Listener Connected");
    }

    /**
     * set the listener status preference as disabled
     */
    public void onListenerDisconnected() {
        userSettingsManager.setDeviceNotificationReceiverStatus(false);
        Log.e(TAG, "Listener Disconnected");
        Helper.toasted(getApplicationContext(),"LISTENER Disconnected");
    }

    /**
     * init the shared preferences to store listener status
     */
    public void onCreate() {
        userSettingsManager = UserSettingsManager.getInstance(getApplicationContext());
    }

    /**
     * extract Data from status bar notification*
     * check if the notification is already known by key (= sdk>26 unique || else packageName + ":" + tag + ":" + id)
     * store the notification with its key as key
     * mirror notification if that is active
     * store key of the last caught notification
     * store the key of the one before that if there was one
     */
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!NotificationFilter.isBlacklisted(sbn)) {
            Log.d(TAG + "onNotificationPosted", "RECEIVED Notification");
            MirrorNotification mirrorNotification = new MirrorNotification(sbn);
            mirrorNotification.log();
            Log.d(TAG + "onNotificationPosted", " Ticker: " + mirrorNotification.ticker);
//            if (!activeNotifications.containsKey(mn.key)) { //disallow updates
            activeNotifications.put(mirrorNotification.key, mirrorNotification);
            Log.d(TAG + "onNotificationPosted", "Mirroring Notification: " + userSettingsManager.getMirrorState());

            //mirror the notification if the MirrorState is set to true
            if (userSettingsManager.getMirrorState()) {
                NotificationMirror.getInstance(getApplicationContext()).mirrorFromDevice(
                        activeNotifications.get(mirrorNotification.key)
                );
            }
            if (lastKey != null) {
                lastlastKey = lastKey;
            }
            lastKey = mirrorNotification.key;
//          }
        }
    }

    /**
     * extract the data from the sbn.
     * check if the notification is stored in the activenotifications map under its key.
     * set the previous key as the last key if there was one, otherwise set last key to null.
     * remove the notification from the map if
     * mirror the dismissal if active
     */
    public void onNotificationRemoved(StatusBarNotification sbn) {
        MirrorNotification mirrorNotification = new MirrorNotification(sbn);
        if (activeNotifications.containsKey(mirrorNotification.key)) {
            if (lastlastKey != null) {
                lastKey = lastlastKey;
                lastlastKey = null;
            }
            activeNotifications.remove(mirrorNotification.key);
//        if (shPref.getBoolean("MirrorState", false)) NotificationMirror.dismiss();
            Log.d(TAG + "onNotificationRemoved", "Removed notification");
        }
    }
}
