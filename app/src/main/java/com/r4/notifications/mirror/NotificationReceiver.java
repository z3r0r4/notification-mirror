package com.r4.notifications.mirror;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends NotificationListenerService {

    private Map<String, MirrorNotification> activeNotifications = new HashMap<>();//either store in this service(use binder to access) or store in shared storage?

    public void onListenerConnected() {
        //TODO put information in shared settings storage to display in Main
    }

    public void onListenerDisconnected() {
        //put information in shared settings storage to display in Main
    }

    public void onCreate() {

    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!NotificationMirror.inFilter(sbn)) {
            MirrorNotification mn = new MirrorNotification(sbn);

            if (!activeNotifications.containsKey(mn.key)) {
                activeNotifications.put(mn.key, mn); //maybe use getNotificationKey as static instead of getId //setData(extractData(sbn)); //setData(MirrorNotification(sbn))
                NotificationMirror.mirror(activeNotifications.get(mn.key));                    //mirror(getData(sbn));
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
}
