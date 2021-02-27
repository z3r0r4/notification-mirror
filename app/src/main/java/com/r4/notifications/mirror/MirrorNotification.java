package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

class MirrorNotification implements Serializable {

    private final static String TAG = "nm.MirrorNotification";
    public int id;
    //    public String tag;
    public String key;
    public String appName; //packageName
    public String title;
    public String text;
    public String ticker;
    public String time;
    public transient NotificationCompat.Action replyAction;   //theres only one replyaction
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
     * @param sbn caught statusbarnotification
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
     * creates a Notification from a networkpackage which can be used to dismiss or reply or act to a notification
     * basically a copy constructor
     *
     * @param netpkg the json package specifiying the key of the notification
     */
    public MirrorNotification(NetworkPackage netpkg) throws ExceptionInInitializerError {
        try {
            MirrorNotification mn = DeviceNotificationReceiver.getactiveNotifications().get(netpkg.getKey());
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

        androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("reply")
                .setLabel("Enter Text Boss")
                .build();
        Intent replyIntent = new Intent(context, MainActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(context, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.replyAction =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_dialog_info, "Reply", replyPendingIntent)
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

    //

    /**
     * interface for custom implementation logging in each method
     */
    interface Logger {
        void e();
    }

    //Getter and Setter

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getAppName() {
        return appName;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getTicker() {
        return ticker;
    }

    public String getTime() {
        return time;
    }

    public NotificationCompat.Action getReplyAction() {
        return replyAction;
    }

    public List<Notification.Action> getActions() {
        return actions;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public boolean isReplyable() {
        return isReplyable;
    }

    public boolean isActionable() {
        return isActionable;
    }
}