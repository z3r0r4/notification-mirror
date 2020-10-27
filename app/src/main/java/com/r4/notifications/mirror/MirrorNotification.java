package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

class MirrorNotification {

    private final static String TAG = "MirrorNotification";

    public int id;
    public String key;
    public String appName; //packageName
    public boolean isCancel;   //TODO onNotificationRemoved
    //    public boolean isUpdate;
//    public boolean isClearable;
    public String time;
    public String title;
    public String text;
    public String ticker;  //for compatability?
    public Notification.Action replyAction;    //theres only one replyaction
    private List<Notification.Action> actions; //excludes repliable Actions

    public MirrorNotification(StatusBarNotification sbn) { //extraction //not useable for posts: problematic //nvm think it works
        //DATA EXTRACTION
        id = sbn.getId();
        key = getNotificationKey(sbn);
        appName = sbn.getPackageName();
        time = Long.toString(sbn.getPostTime());
        title = getTitle(sbn);
        text = getText(sbn);
        ticker = getTickerText(sbn);
        actions = getActions(sbn);
        replyAction = getReplyAction(sbn);
    }


    //FOR REPLIES
    public MirrorNotification(String id) {

    }

    //FOR POSTING
    public MirrorNotification(String id, String title, String text) {

    }

    //FOR POSTING AND REPLIES
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
    public MirrorNotification(String id, String title, String text, String replyActionName, String actionName, Context context) {

    }

    private static String getNotificationKey(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return sbn.getKey(); //sbn.getId();
        else {
            String packageName = sbn.getPackageName() != null ? sbn.getPackageName() : ""; //RIP ELVIS IS DEAD
            String tag = sbn.getTag() != null ? sbn.getTag() : "";      //statusBarNotification.getTag() ?: "";
            int id = sbn.getId();
            return packageName + ":" + tag + ":" + id;
        }
    }

    private static String getTitle(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "getTitle: couldn't get the Title from the Notification", new NullPointerException());
            return null;
        }

        Bundle extras = sbn.getNotification().extras;

        if (extras.containsKey("android.title")) //extras.getString("android.title") != null
            return extras.getString("android.title");

        if (!extras.containsKey(Notification.EXTRA_MESSAGES)) {
            Log.e(TAG, "getTitle: couldn't get the Title from the Notification", new NullPointerException());
            return null;
        }
        if (extras.getString(Notification.EXTRA_TITLE) != null)
            return extras.getString(Notification.EXTRA_TITLE);

        if (extras.getString(Notification.EXTRA_TITLE_BIG) != null)
            return extras.getString(Notification.EXTRA_TITLE_BIG);

        if (extras.getString(Notification.EXTRA_CONVERSATION_TITLE) != null)
            return extras.getString(Notification.EXTRA_CONVERSATION_TITLE);

        Log.e(TAG, "getTitle: couldn't get the Title from the Notification", new NullPointerException());
        return null;
    }


    /* MAYBE EXTRACT TO HELPER CLASS */
    //can all be static

    private static String getText(StatusBarNotification sbn) { //getMessage
        Bundle extras = sbn.getNotification().extras;

        if (extras.getString(Notification.EXTRA_TEXT) != null)      //ALSO POSSIBLE EXTRA_INFO_TEXT, EXTRA_SUB_TEXT
            return extras.getString(Notification.EXTRA_TEXT);

        if (extras.getString(Notification.EXTRA_BIG_TEXT) != null)
            return extras.getString(Notification.EXTRA_BIG_TEXT);

        if (extras.getString(Notification.EXTRA_SUMMARY_TEXT) != null)
            return extras.getString(Notification.EXTRA_SUMMARY_TEXT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.d(TAG, "getText: Couldn't get Text / Message", new NullPointerException());
            return null;
        }

        if (!extras.containsKey(Notification.EXTRA_MESSAGES)) { //extras.getString(Notification.EXTRA_MESSAGES) == null
            Log.d(TAG, "getText: Couldn't get Text / Message", new NullPointerException());
            return null;
        }

        boolean isGroupConversation = extras.getBoolean(NotificationCompat.EXTRA_IS_GROUP_CONVERSATION);
        Parcelable[] messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages == null) {
            Log.d(TAG, "getText: Couldn't get Text / Message", new NullPointerException());
            return null;
        }

        String text = "";
        for (Bundle message : (Bundle[]) messages) { //for (Parcelable p : ms) Bundle m = (Bundle) p;
            if (isGroupConversation && message.containsKey("sender"))
                text = (String) message.get("sender") + ": ";
            text += message.get("text") + "\n";
        }
        if (text.equals(""))
            Log.d(TAG, "getText: Couldn't get Text / Message", new NullPointerException());
        return text;
    }

    private static String getTickerText(StatusBarNotification sbn) {
        if (sbn.getNotification().tickerText != null)
            return sbn.getNotification().tickerText.toString();

        if (getTitle(sbn) != null && getText(sbn) != null)
            return getTitle(sbn) + ": " + getText(sbn);

        if (getTitle(sbn) != null)
            return getTitle(sbn);

        if (getText(sbn) != null)
            return getText(sbn);

        Log.d(TAG, "getTickerText: Couldn't get TickerText", new NullPointerException());
        return null;
    }

    private static List<Notification.Action> getActions(StatusBarNotification sbn) {//actionextraction
        Notification notification = sbn.getNotification();

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
        Log.d(TAG, "getActions: lame, couldn't get any action", new NullPointerException());
        return null;
    }

    //RETURNS THE ACTUAL REPLY ACTION WITH THE FiTTING REMOTE INPUT doesnt store all of the actions like smth called k** (still gotta search for the right remoteInput, wehn replying tho)
    private static Notification.Action getReplyAction(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "getReplyAction: couldn't get any ReplyActions", new NullPointerException());
            return null;
        }

        Notification notification = sbn.getNotification();
        if (notification.actions != null && notification.actions.length > 0) {
            for (Notification.Action action : notification.actions) {
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
        Log.d(TAG, "getReplyAction: couldn't get any ReplyActions", new NullPointerException());
        return null;
    }

    public void act(String actionName) {//maybe MirrorWorker

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public void reply(String message, Context context) throws PendingIntent.CanceledException {//maybe MirrorWorker
        if (this.replyAction == null || this.replyAction.getRemoteInputs().length == 0) {
            Log.e(TAG, "reply: couldn't get ReplyAction or RemoteInputs");
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
            Log.e(TAG, "reply: Couldn't send" + e.getLocalizedMessage());
        }
    }

    /* TEST ONLY */
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
}
