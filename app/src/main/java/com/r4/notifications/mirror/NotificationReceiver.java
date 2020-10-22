package com.r4.notifications.mirror;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Map;

class NotificationReceiver extends NotificationListenerService {

    private Map<String, MirrorNotification> activeNotifications = new HashMap<>();

    public void onListenerConnected() {
        //put information in shared settings storage to display in Main
    }

    public void onListenerDisconnected() {
        //put information in shared settings storage to display in Main
    }

    public void onCreate() {

    }

    public void onNotificationPosted(StatusBarNotification sbn) {
//        setData(extractData(sbn)); //setData(MirrorNotification(sbn))//TODO IMPLEMENT DATA EXTRACTION
//        mirror(getData(sbn));
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
//        mirrorCanceled(getData(sbn));
//        resetData(getData(sbn)); //reset(getId(sbn));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public MirrorNotification getData(String id) {
        return null;
    }

    private void setData(MirrorNotification notification) {
        //either store in this service(use binder to access) or store in shared storage?
    }
}
