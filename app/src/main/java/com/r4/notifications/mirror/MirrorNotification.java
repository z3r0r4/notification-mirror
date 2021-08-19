package com.r4.notifications.mirror;

import android.app.Notification;
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

/**
 * @since 20210719
 * stores data from a Android StatusbarNotification extracted using the NotificationExtractor
 * reply and action and dismiss on this
 */

class MirrorNotification implements Serializable {

    private final static String TAG = "MirrorNotification";
    public int id;
    //    public String tag;
    public String key;
    public String appName; //packageName
    public String title;
    public String text;
    public String ticker;
    public String time;
    public transient Notification.Action replyAction;   //theres only one replyaction
    public transient List<Notification.Action> actions; //excludes repliable Actions

    public boolean isCancel;   //TODO onNotificationRemoved
    //    public boolean isUpdate;
    //    public boolean isClearable;
    public boolean isReplyable;
    public boolean isActionable;

    /**
     * Extracts every interesting information out of a statusbarnotification
     * for more complex operations the extractor class is used
     * <p>
     * id           : unique identifier of a notification for the notificationmanager.notify method (i.e. 9001)
     * //* tag          : The tag supplied to android.app.NotificationManager#notify(int, Notification), or null if no tag was specified. ()
     * key          : "A unique instance key for this notification record" (0|com.r4.notifications.mirror|9001|null|10084)
     * appName      : The packageName that the notification belongs to (com.r4.notifications.mirror)
     * title        : sbn.getNotification().extras.getString("android.title")||.getString(Notification.EXTRA_TITLE)||getString(Notification.EXTRA_TITLE_BIG)||.getString(Notification.EXTRA_CONVERSATION_TITLE) (i.e. TestNotification)
     * text         : .getString(Notification.EXTRA_TEXT||EXTRA_BIG_TEXT||EXTRA_SUMMARY_TEXT)||.getParcelableArray(Notification.EXTRA_MESSAGES) (i.e. Testing)
     * ticker       : sbn.getNotification().tickerText||getTitle(sbn) + ": " + getText(sbn)||getTitle||getText (i.e. TestNotification: Testing)
     * actions      : multiple: sbn.getNotification().actions Except those with a remote input cuz those are repliable
     * replyaction  : single: sbn.getNotification().actions with remoteInput and resultKey.contains("reply"||"android.intent.extra.text"||"input")
     * <p>
     * isActionable : boo if there are actions
     * isReplyable  : boo if a replyaction was found
     *
     * @param sbn caought statusbarnotification
     */
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

        if (actions != null) isActionable = true;
        if (replyAction != null) isReplyable = true;
    }

    /**
     * Test only
     * creates a notification that can be posted and replied to
     *
     * @param id
     */
    @Deprecated
    public MirrorNotification(String id) {
    }

    /**
     * Test only
     * creates a notification that can be posted
     *
     * @param id
     * @param title
     * @param text
     */
    @Deprecated
    public MirrorNotification(String id, String title, String text) {
    }

    /**
     * creates a Notification from a networkpackage which can be used to dismiss or reply or act to a notification
     * basically a copy constructor
     *
     * @param netpkg the json package specifiying the key of the notification
     */
    public MirrorNotification(NetworkPackage netpkg) throws ExceptionInInitializerError {
        try {
            MirrorNotification mn = NotificationReceiver.getactiveNotifications().get(netpkg.getKey());
            mn.log();
            if (mn.id != netpkg.getID()) { //crash here if two pkgs are send afteranother bith with a dismiss
                Log.e(TAG, "wrong ID for notifiaction retrived by KEY");
                throw new ExceptionInInitializerError();
            }
            this.id = mn.id;
            this.key = mn.key;
            this.appName = mn.appName;
            this.title = mn.title;
            this.text = mn.text;
            this.ticker = mn.ticker;
            this.time = mn.time;
            this.replyAction = mn.replyAction;
            this.actions = mn.actions;
            this.isCancel = mn.isCancel;
            this.isReplyable = mn.isReplyable;
            this.isActionable = mn.isActionable;
        } catch (NullPointerException e) {
            Log.e(TAG, "couldnt finde Notification, maybe got dismissed");
        }
    }

    /**
     * Test only
     * Creates a notification that can be posted and replied to
     *
     * @param id              unique id for the notifiactionmanager
     * @param title           title of the notifiacation thats shown in the sb
     * @param text            text of the notification that is shown in the sb
     * @param replyActionName name of the action that can be replied to after posting
     * @param context         trash
     */
    @Deprecated
    public MirrorNotification(String id, String title, String text, String replyActionName, Context context) { //TODO use replyactionname
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

    /**
     * Test only
     * Creates a notification that can be posted, replied and actioned upon to
     *
     * @param id              unique id for the notifiactionmanager
     * @param title           title of the notifiacation thats shown in the sb
     * @param text            text of the notification that is shown in the sb
     * @param replyActionName name of the action that can be replied to after posting
     * @param actionName      name of the additonal action
     * @param context         trash
     */
    @Deprecated
    public MirrorNotification(String id, String title, String text, String replyActionName, String actionName, Context context) {
    }

    public void log() {
        Log.d(TAG + "MirrorNotification", "to be mirrored:"
                + "\nID     :" + this.id
                + "\nkey    :" + this.key
                + "\nappName:" + this.appName
                + "\ntime   :" + this.time
                + "\ntitle  :" + this.title
                + "\ntext   :" + this.text
                + "\nticker :" + this.ticker
                + "\nactions:" + this.isActionable
                + "\nrepAct :" + this.isReplyable
        );
    }

    /**
     * execute the action of a notification by name
     * writes ? times
     *
     * @param actionName name of the action of the notification to be executed
     */
    public void act(String actionName) {

    }

    /**
     * replies to a notification using its replyaction and its remote inputs
     * writes once
     *
     * @param message which should be replied
     * @param context trash
     */
    public void reply(String message, Context context) {//maybe MirrorWorker
        Logger log = () -> {
            Log.e(TAG + "reply", "NO REPLYACTIONS or REMOTEINPUTS");
            Helper.toasted("Not Repliable");
        };
        if (this.replyAction == null || this.replyAction.getRemoteInputs().length == 0) {
            Log.e(TAG, "no actions or remote inputs to reply to");
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        for (RemoteInput remoteIn : this.replyAction.getRemoteInputs())
            bundle.putCharSequence(remoteIn.getResultKey(), message);

        RemoteInput.addResultsToIntent(this.replyAction.getRemoteInputs(), intent, bundle);
        try {
            replyAction.actionIntent.send(context, 0, intent); //SET
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG + "reply", "REPLY FAILED" + e.getLocalizedMessage());
            Helper.toasted("Couldnt reply to Notification");
        }
    }

    /**
     * Test only
     * posts this notification to the channel of a notification manager
     *
     * @param notificationManager with the channel its posted to
     * @param context             trash
     */
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

    /**
     * dismisses this notification posted which is identified by its id
     * <p>
     * //     * @param notificationManager with the channel the notification was posted to
     */
    public void dismiss() {
        NotificationManager notificationManager = (NotificationManager) MainActivity.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    /**
     * interface for custom implementation logging in each method
     */
    interface Logger {
        void e();
    }
}