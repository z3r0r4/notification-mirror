package com.r4.notifications.mirror;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

//not named Notifiaction to avoid naming conflicst with android
class MirrorNotification {
    private String id;
    private String appName;
    private boolean isCancel;
    //    private boolean isUpdate;
//    private boolean isClearable;
//    private String requestReplyId;
    private String time;
    private String title;
    private String text;
    private String ticker;
    private Notification.Action actions;
    private Notification.Action replyAction;

    public MirrorNotification(StatusBarNotification sbn) {
        //DATA EXTRACTION
    }

    //FOR POSTING
    public MirrorNotification(String id, String title, String text) {

    }

    //FOR POSTING AND REPLIES
    public MirrorNotification(String id, String title, String text, String replyActionName) {

    }

    //FOR POSTING AND REPLIES AND ACTIONS
    public MirrorNotification(String id, String title, String text, String replyActionName, String actionName) {

    }

    //FOR REPLIES
    public MirrorNotification(String id) {

    }

    public void act(String actionName) {//maybe MirrorWorker

    }

    public void reply(String message) {//maybe MirrorWorker

    }

    //TEST ONLY
    public void post() {

    }
}
