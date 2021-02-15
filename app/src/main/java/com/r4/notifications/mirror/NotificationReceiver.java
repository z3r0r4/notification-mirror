package com.r4.notifications.mirror;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends NotificationListenerService {
    private final static String TAG = "Receiver";

    public static Map<String, MirrorNotification> activeNotifications = new HashMap<>();
    public static String lastKey;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;

    public static Map<String, MirrorNotification> getactiveNotifications() {
        if (lastKey == null) throw new NullPointerException();
        return activeNotifications;
    }

    public void onListenerConnected() {
        editor.putBoolean("ListenerStatus", true);
        editor.apply();
        Log.e(TAG, "onListenerConnected");
    }

    public void onListenerDisconnected() {
        editor.putBoolean("ListenerStatus", false);
        editor.apply();
        Log.e(TAG, "onListenerDisconnected");
    }

    @SuppressLint("CommitPrefEdits")
    public void onCreate() {
        shPref = this.getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!NotificationMirror.inFilter(sbn)) {
            Log.e(TAG, "onNotificationPosted: RECEIVED");
            MirrorNotification mn = new MirrorNotification(sbn);
            Log.d(TAG, "onNotificationPosted: " + mn.ticker);
//            if (!activeNotifications.containsKey(mn.key)) {
            activeNotifications.put(mn.key, mn);
            Log.e(TAG, "Mirroring: " + shPref.getBoolean("MirrorState", false));
            if (shPref.getBoolean("MirrorState", false))
                NotificationMirror.mirror(activeNotifications.get(mn.key), shPref.getString("HOST_IP", "192.168.178.84"), shPref.getInt("HOST_PORT", 9001));
            lastKey = mn.key;
//            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
//        mirrorCanceled(getData(sbn)); //mirror(getData(sbn).setCanceled())
//        resetData(getData(sbn)); //reset(getId(sbn));
    }
}
