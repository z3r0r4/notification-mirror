package com.r4.notifications.mirror;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.LinkedList;
import java.util.List;

public class NotificationExtractor {
    private static final String TAG = "nm.NotificationExtractor";

    /**
     * extracts the key          : "A unique instance key for this notification record" (0|com.r4.notifications.mirror|9001|null|10084)
     *
     * @param sbn statusbarnotification of which the key should be extracted
     * @return the key (i.e. 0|com.r4.notifications.mirror|9001|null|10084)
     */
    protected static String getNotificationKey(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return sbn.getKey(); //sbn.getId();
        else {
            String packageName = sbn.getPackageName() != null ? sbn.getPackageName() : ""; //RIP ELVIS IS DEAD
            String tag = sbn.getTag() != null ? sbn.getTag() : "";      //statusBarNotification.getTag() ?: "";
            int id = sbn.getId();
            return packageName + ":" + tag + ":" + id;
        }
    }

    /**
     * extracts the title        : sbn.getNotification().extras.getString("android.title")||.getString(Notification.EXTRA_TITLE)||getString(Notification.EXTRA_TITLE_BIG)||.getString(Notification.EXTRA_CONVERSATION_TITLE) (i.e. TestNotification)
     *
     * @param sbn statusbarnotification of which the title should be extracted
     * @return the title of the notification (i.e. TestNotification)
     */
    protected static String getTitle(StatusBarNotification sbn) {
        MirrorNotification.Logger log = () -> Log.e(TAG + "getTitle", "TITLE EXTRACTION FAILED");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            log.e();
            return null;
        }

        Bundle extras = sbn.getNotification().extras;

        if (extras.containsKey("android.title")) //extras.getString("android.title") != null
        {
            return extras.getString("android.title");
        }

        if (!extras.containsKey(Notification.EXTRA_MESSAGES)) {
            log.e();
            return null;
        }
        if (extras.getString(Notification.EXTRA_TITLE) != null) {
            return extras.getString(Notification.EXTRA_TITLE);
        }

        if (extras.getString(Notification.EXTRA_TITLE_BIG) != null) {
            return extras.getString(Notification.EXTRA_TITLE_BIG);
        }

        if (extras.getString(Notification.EXTRA_CONVERSATION_TITLE) != null) {
            return extras.getString(Notification.EXTRA_CONVERSATION_TITLE);
        }

        log.e();
        return null;
    }

    /**
     * extracts the text         : .getString(Notification.EXTRA_TEXT||EXTRA_BIG_TEXT||EXTRA_SUMMARY_TEXT)||.getParcelableArray(Notification.EXTRA_MESSAGES) (i.e. Testing)
     *
     * @param sbn statusbarnotification of which the text should be extracted
     * @return the text of the notification body (i.e. Testing)
     */
    protected static String getText(StatusBarNotification sbn) { //getMessage
        Bundle extras = sbn.getNotification().extras;
        MirrorNotification.Logger log = () -> Log.e(TAG + "getText", "TEXT / MSG EXTRACTION FAILED");

        if (extras.getString(Notification.EXTRA_TEXT) != null)      //ALSO POSSIBLE EXTRA_INFO_TEXT, EXTRA_SUB_TEXT
            return extras.getString(Notification.EXTRA_TEXT);

        if (extras.getString(Notification.EXTRA_BIG_TEXT) != null)
            return extras.getString(Notification.EXTRA_BIG_TEXT);

        if (extras.getString(Notification.EXTRA_SUMMARY_TEXT) != null)
            return extras.getString(Notification.EXTRA_SUMMARY_TEXT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            log.e();
            return null;
        }

        if (!extras.containsKey(Notification.EXTRA_MESSAGES)) { //extras.getString(Notification.EXTRA_MESSAGES) == null
            log.e();//, new NullPointerException());
            return null;
        }

        boolean isGroupConversation = extras.getBoolean(NotificationCompat.EXTRA_IS_GROUP_CONVERSATION);
        Parcelable[] messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages == null) {
            log.e();//, new NullPointerException());
            return null;
        }

        String text = "";
        for (Parcelable p : messages) { //for (Bundle message : (Bundle[]) messages) { //TODO TEST and fix maybe
            Bundle message = (Bundle) p;
            if (isGroupConversation && message.containsKey("sender"))
                text = (String) message.get("sender") + ": ";
            text += message.get("text") + "\n";
        }
        if (text.equals(""))
            log.e();//, new NullPointerException());
        return text;
    }

    /**
     * extracts the ticker       : sbn.getNotification().tickerText||getTitle(sbn) + ": " + getText(sbn)||getTitle||getText
     *
     * @param sbn statusbarnotification of which the Tickertext should be extracted
     * @return the text of the ticker summary or a composite of title and text (i.e. TestNotification: Testing)
     */
    protected static String getTickerText(StatusBarNotification sbn) {
        MirrorNotification.Logger log = () -> Log.e(TAG + "getTickerText", "TICKERTEXT EXTRACTION FAILED");

        if (sbn.getNotification().tickerText != null)
            return sbn.getNotification().tickerText.toString();

        if (getTitle(sbn) != null && getText(sbn) != null)
            return getTitle(sbn) + ": " + getText(sbn);

        if (getTitle(sbn) != null)
            return getTitle(sbn);

        if (getText(sbn) != null)
            return getText(sbn);

        log.e();
        return null;
    }

    /**
     * extracts the actions      : multiple: sbn.getNotification().actions Except those with a remote input cuz those are repliable
     *
     * @param sbn statusbarnotification of which the actions should be extracted
     * @return a list of the actions of the given notifiation
     */
    protected static List<Notification.Action> getActions(StatusBarNotification sbn) {//actionextraction
        Notification notification = sbn.getNotification();
        MirrorNotification.Logger log = () -> Log.e(TAG + "getActions", "ACTION EXTRACTION FAILED");

        if (notification.actions != null && notification.actions.length > 0) {      //CHECK IF ACTIONS EXIST
            LinkedList<Notification.Action> localActions = new LinkedList<Notification.Action>();
            for (Notification.Action action : notification.actions) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)// Check whether it is a REPLY ACTION. We have special treatment for them
                    if (action.getRemoteInputs() != null && action.getRemoteInputs().length > 0)
                        continue;

                localActions.add(action);
            }
            return localActions;
        }
        log.e();   //lame, couldn't get any action");//, new NullPointerException());
        return null;
    }

    /**
     * extracts one replyaction  : sbn.getNotification().actions with remoteInput and resultKey.contains("reply"||"android.intent.extra.text"||"input")
     *
     * @param sbn statusbarnotification of which the replyaction should be extracted
     * @return the replyaction of the given notification
     */
    //RETURNS THE ACTUAL REPLY ACTION WITH THE FiTTING REMOTE INPUT doesnt store all of the actions like smth called k** (still gotta search for the right remoteInput, when replying tho)
    protected static NotificationCompat.Action getReplyAction(StatusBarNotification sbn) {
        MirrorNotification.Logger log = () -> Log.e(TAG + "getReplyAction", "REPLYACTION EXTRACTION FAILED");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            log.e();
            return null;
        }

        Notification notification = sbn.getNotification();
        if (notification.actions != null && notification.actions.length > 0) {
            for (NotificationCompat.Action action : getActions(notification)) {
                if (action != null && action.getRemoteInputs() != null) {
                    for (RemoteInput remoteInput : action.getRemoteInputs()) {//kde version stores all remoteInputs and uses a different replyfunction
                        String resultKey = remoteInput.getResultKey().toLowerCase();
                        if (resultKey.contains("reply"))
                            return action;
                        else if (resultKey.contains("android.intent.extra.text"))
                            return action;
                        else if (resultKey.contains("input"))
                            return action;
                    }
                }
            }
        }
        log.e();
        return null;
    }

    /**
     * gets the actions of a Notification
     * @param notification
     * @return NotificationCompat.Action[] array of the passed notification
     */
    private static NotificationCompat.Action[] getActions(Notification notification) {
        NotificationCompat.Action[] actions = new NotificationCompat.Action[NotificationCompat.getActionCount(notification)];
        for (int i = 0; i < NotificationCompat.getActionCount(notification); i++) {
            actions[i] = NotificationCompat.getAction(notification, i);
        }
        return actions;
    }
}