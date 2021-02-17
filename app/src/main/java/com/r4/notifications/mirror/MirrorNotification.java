package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

import androidx.core.app.NotificationManagerCompat;

class MirrorNotification implements Serializable {

    private final static String TAG = "MirrorNotification";
    //TODO add Documentation
    public int id;
    public String key;
    public String appName; //packageName
    public boolean isCancel;   //TODO onNotificationRemoved
    //    public boolean isUpdate;
//    public boolean isClearable;
//    public boolean isReplyable; //TODO add marker for json that specificies repliability
//    public boolean isActionable;
    public String time;
    public String title;
    public String text;
    public String ticker;
    public transient Notification.Action replyAction;   //theres only one replyaction
    public transient List<Notification.Action> actions; //excludes repliable Actions

    public MirrorNotification(StatusBarNotification sbn) {
        id = sbn.getId();
        key = NotificationExtractor.getNotificationKey(sbn);
        appName = sbn.getPackageName();
        time = Long.toString(sbn.getPostTime());
        title = NotificationExtractor.getTitle(sbn);
        text = NotificationExtractor.getText(sbn);
        ticker = NotificationExtractor.getTickerText(sbn);
        actions = NotificationExtractor.getActions(sbn);
        replyAction = NotificationExtractor.getReplyAction(sbn);
    }

    //FOR REPLIES
    @Deprecated
    public MirrorNotification(String id) {
    }

    //FOR POSTING
    @Deprecated
    public MirrorNotification(String id, String title, String text) {
    }

    //FOR POSTING AND REPLIES
    @Deprecated
    public MirrorNotification(String id, String title, String text, String replyActionName, Context context) { //posting
        this.title = title;
        this.text = text;

        RemoteInput remoteInput = new RemoteInput.Builder("reply")
                .setLabel("Enter Text Boss")
                .build();
        Intent replyIntent = new Intent(context, MainActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(context, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.replyAction =
                new Notification.Action.Builder(android.R.drawable.ic_dialog_info, "Reply", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
    }

    //FOR POSTING AND REPLIES AND ACTIONS
    @Deprecated
    public MirrorNotification(String id, String title, String text, String replyActionName, String actionName, Context context) {
    }

    @Deprecated
    public static NotificationManagerCompat createNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.sContext);
        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
        return notificationManager;
    }

    public void act(String actionName) {//maybe MirrorWorker

    }

    public void reply(String message, Context context) {//maybe MirrorWorker
        Logger log = () -> {
            Log.e(TAG + "reply", "NO REPLYACTIONS or REMOTEINPUTS");
            Helper.toasted("Not Repliable");
        };
        if (this.replyAction == null || this.replyAction.getRemoteInputs().length == 0) {
            log.e();
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        for (RemoteInput remoteIn : this.replyAction.getRemoteInputs())
            bundle.putCharSequence(remoteIn.getResultKey(), message);

        RemoteInput.addResultsToIntent(this.replyAction.getRemoteInputs(), intent, bundle);
        try {
            replyAction.actionIntent.send(context, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG + "reply", "REPLY FAILED" + e.getLocalizedMessage());
            Helper.toasted("Couldnt reply to Notification");
        }
    }

    /* TEST ONLY */
    @Deprecated
    public void post(NotificationManagerCompat notificationManager, Context context) {
        Notification notification = new Notification.Builder(context, "TestChannel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(this.title)
                .setContentText(this.text)
//                .setContentIntent(pIntent)
//                .setPriority(NotificationCompat.PRIORITY_MAX) //For lower androids without channels
//                .setAutoCancel(true) //close onclick
                .addAction(this.replyAction)
                .build();
        notificationManager.notify(9001, notification);
    }

    @Deprecated
    public void dismiss(NotificationManagerCompat notificationManager){
        notificationManager.cancel(id);
    }

    interface Logger {
        void e();
    }
}