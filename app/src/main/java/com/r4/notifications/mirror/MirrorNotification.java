package com.r4.notifications.mirror;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

//not named Notifiaction to avoid naming conflicst with android
class MirrorNotification {

    protected final String TAG = getClass().getSimpleName();

    private int id;
    private String key; //maybe only use one of them
    private String appName; //packageName
    private boolean isCancel;   //TODO onNotificationRemoved
    //    private boolean isUpdate;
//    private boolean isClearable;
//    private String requestReplyId;
    private String time;
    private String title;
    private String text;
    private String ticker;  //for compatability?
    private Notification.Action actions;
    private Notification.Action replyAction;
//    private String replyID;//if replyactions can be uniquely identified by the notifi.id this isnt needed

    public MirrorNotification(StatusBarNotification sbn) {
        //DATA EXTRACTION
        id = sbn.getId();
        key = getNotificationKey(sbn);
        appName = sbn.getPackageName();
        time = Long.toString(sbn.getPostTime());
        title = getTitle(sbn);
        text = getText(sbn);
        ticker = getTickerText(sbn);
        actions = getActions(sbn);
        replyAction = getReplyAction(actions);
//        replyID = getReplyID(replyAction);
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


    /* MAYBE EXTRACT TO HELPER CLASS */

    private String getNotificationKey(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return sbn.getKey(); //sbn.getId();
        else {
            String packageName = sbn.getPackageName() != null ? sbn.getPackageName() : ""; //RIP ELVIS IS DEAD
            String tag = sbn.getTag() != null ? sbn.getTag() : "";      //statusBarNotification.getTag() ?: "";
            int id = sbn.getId();
            return packageName + ":" + tag + ":" + id; //sbn.getId();
        }
    }

    private String getTitle(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return null;

        Bundle extras = sbn.getNotification().extras;

        if (!extras.containsKey(Notification.EXTRA_MESSAGES)) //any cleaner way than ifs?
            return null;

        if (extras.getString(Notification.EXTRA_CONVERSATION_TITLE) != null)
            return extras.getString(Notification.EXTRA_CONVERSATION_TITLE);

        if (extras.getString(Notification.EXTRA_TITLE) != null)
            return extras.getString(Notification.EXTRA_TITLE);

        if (extras.getString(Notification.EXTRA_TITLE_BIG) != null)
            return extras.getString(Notification.EXTRA_TITLE_BIG);

        Log.e(TAG, "getTitle: couldn't get the Title from the Notification", new NullPointerException());
        return null;
    }

    private String getText(StatusBarNotification sbn) { //getMessage
        Bundle extras = sbn.getNotification().extras;

        if (extras.getString(Notification.EXTRA_TEXT) != null)
            return extras.getString(Notification.EXTRA_TEXT);

        if (extras.getString(Notification.EXTRA_BIG_TEXT) != null)
            return extras.getString(Notification.EXTRA_BIG_TEXT);

        if (extras.getString(Notification.EXTRA_SUMMARY_TEXT) != null)
            return extras.getString(Notification.EXTRA_SUMMARY_TEXT);
        //ALSO POSSIBLE EXTRA_INFO_TEXT, EXTRA_SUB_TEXT

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return null;

        if (!extras.containsKey(Notification.EXTRA_MESSAGES))
            return null;

        boolean isGroupConversation = extras.getBoolean(NotificationCompat.EXTRA_IS_GROUP_CONVERSATION);
        Parcelable[] messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages == null)
            return null;

        String text = "";
        for (Bundle message : (Bundle[]) messages) { //for (Parcelable p : ms) Bundle m = (Bundle) p;
            if (isGroupConversation && message.containsKey("sender"))
                text = (String) message.get("sender") + ": ";
            text += message.get("text") + "\n";
        }
        if (text.equals(""))
            Log.e(TAG, "getText: Couldn't get Text / Message", new NullPointerException());
        return text;
    }

    private String getTickerText(StatusBarNotification sbn) {
        if (sbn.getNotification().tickerText != null)
            return sbn.getNotification().tickerText.toString();

        if (getTitle(sbn) != null && getText(sbn) != null)
            return getTitle(sbn) + ": " + getText(sbn);

        if (getTitle(sbn) != null)
            return getTitle(sbn);

        if (getText(sbn) != null)
            return getText(sbn);

        Log.e(TAG, "getTickerText: Couldn't get TickerText", new NullPointerException());
        return null;
    }

    private Notification.Action getActions(StatusBarNotification sbn) {
        return null;
    }

    private Notification.Action getReplyAction(Notification.Action actions) {
        return null;
    }

    /* TEST ONLY */
    public void post() {

    }
}
