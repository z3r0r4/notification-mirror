package com.r4.notifications.mirror;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends NotificationListenerService {
    private final static String TAG = "Notification Receiver";

    public static Map<String, MirrorNotification> activeNotifications = new HashMap<String, MirrorNotification>();
    public static String lastKey;
    private static String lastlastKey;

    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;

    /**
     * checks if any notificaitons have been received yet and returns
     *
     * @return a map of the currently active notifications accessible by their key
     */
    public static Map<String, MirrorNotification> getactiveNotifications() {
        if (lastKey == null) throw new NullPointerException();
        return activeNotifications;
    }

    /**
     * set the listener status preference as ensabled
     */
    public void onListenerConnected() {
        editor.putBoolean("ListenerStatus", true);
        editor.apply();
        Log.d(TAG, "Listener Connected");
    }

    /**
     * set the listener status preference as disabled
     */
    public void onListenerDisconnected() {
        editor.putBoolean("ListenerStatus", false);
        editor.apply();
        Log.e(TAG, "Listener Disconnected");
        Helper.toasted("LISTENER Disconnected");
    }

    /**
     * init the shared preferences to store listener status
     */
    public void onCreate() {
        shPref = this.getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();
    }

    /**
     * extract Data from Statusbarnotification*
     * check if the notification is already known by key (= sdk>26 unique || else packageName + ":" + tag + ":" + id)
     * store the notification with the its key as key
     * mirror notification if thats active
     * store key of the last caought notification
     * store the key of the one before that if there was one
     */
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!NotificationMirror.inFilter(sbn)) {
            Log.d(TAG + "onNotificationPosted", "RECEIVED Notification");
            MirrorNotification mn = new MirrorNotification(sbn);
            Log.d(TAG + "onNotificationPosted", " Ticker: " + mn.ticker);
//            if (!activeNotifications.containsKey(mn.key)) { //disallow updates
            activeNotifications.put(mn.key, mn);
            Log.d(TAG + "onNotificationPosted", "Mirroring Notification: " + shPref.getBoolean("MirrorState", false));
            if (shPref.getBoolean("MirrorState", false))
                NotificationMirror.mirror(activeNotifications.get(mn.key), shPref.getString("HOST_IP", Resources.getSystem().getString(R.string.DefaultMirrorIP)), shPref.getInt("HOST_PORT", Resources.getSystem().getInteger(R.integer.DefaultMirrorPORT)));
            if (lastKey != null) lastlastKey = lastKey;
            lastKey = mn.key;
//            }
        }
    }

    /**
     * extract the data from the sbn
     * check if the notification is stored in the activenotifications map under its key
     * set the previous key as the last key if htere was one else set last key null
     * remove the notification from the map if
     * mirror the dismissal if active
     */
    public void onNotificationRemoved(StatusBarNotification sbn) {
        MirrorNotification mn = new MirrorNotification(sbn);
        if (activeNotifications.containsKey(mn.key)) {
            if (lastlastKey != null) {
                lastKey = lastlastKey;
                lastlastKey = null;
            }
            activeNotifications.remove(mn.key);
//        if (shPref.getBoolean("MirrorState", false)) NotificationMirror.dismiss();
            Log.d(TAG + "onNotificationRemoved", "Removed notification");
        }
    }
}
