package com.r4.notifications.mirror;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class NotificationReceiver extends NotificationListenerService {
    private final static String TAG = "Receiver";
    static NotificationReceiver _this;
    static Semaphore sem = new Semaphore(0);

    public Map<String, MirrorNotification> activeNotifications = new HashMap<>();
    public String lastKey;
    private SharedPreferences.Editor editor;

    //by https://gist.github.com/paulo-raca/471680c0fe4d8f91b8cde486039b0dcd
    public static NotificationReceiver get() { //BLACK MAGIC touch with 100m stick
        sem.acquireUninterruptibly();
        NotificationReceiver receiver = _this;
        sem.release();
        return receiver;
    }//TODO stop weird things while Listener is connecting
    //TODO maybe use Intent->Broadcast construct instead

    public void onListenerConnected() {
        editor.putBoolean("ListenerStatus", true);
        editor.apply();
        Log.e(TAG, "onListenerConnected");
        _this = this;
        sem.release();
    }

    public void onListenerDisconnected() {
        editor.putBoolean("ListenerStatus", false);
        editor.apply();
        Log.e(TAG, "onListenerDisconnected");
//        sem.acquireUninterruptibly(); //dont use or blackmagic will stop main from starting
//        _this = null;
    }

    @SuppressLint("CommitPrefEdits")
    public void onCreate() {
        editor = this.getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE).edit();
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!NotificationMirror.inFilter(sbn)) {
            Log.e(TAG, "onNotificationPosted: RECEIVED");
            MirrorNotification mn = new MirrorNotification(sbn);
            Log.d(TAG, "onNotificationPosted: " + mn.ticker);
            if (!activeNotifications.containsKey(mn.key)) {
                activeNotifications.put(mn.key, mn);
                NotificationMirror.mirror(activeNotifications.get(mn.key));
                lastKey = mn.key;
            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
//        mirrorCanceled(getData(sbn)); //mirror(getData(sbn).setCanceled())
//        resetData(getData(sbn)); //reset(getId(sbn));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public void checkAccess() {
        Log.e(TAG, "checkBinding: AAAAAAAAAAAAAAAAAAAAA GOT ACCESSS");
    }

    public MirrorNotification getLast() {
        return activeNotifications.get(lastKey);
    }
}
