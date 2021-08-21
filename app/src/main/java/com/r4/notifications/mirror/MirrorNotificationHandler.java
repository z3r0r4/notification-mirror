package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static androidx.core.content.ContextCompat.getSystemService;

/**
 * provides actions on and for notifications
 */

class MirrorNotificationHandler {

    private final static String TAG = "nm.MirrorNotificationHandler";

    //singelton instance of the notification mirror
    private static MirrorNotificationHandler singleton_instance = null;

    //member variables
    private final NotificationManagerCompat notificationManager;
    //ID of the next test notification
    private int currentTestNotificationId = 0;

    private MirrorNotificationHandler(Context context) {
        //initialize the notificationManager
        notificationManager = NotificationManagerCompat.from(context);

    }
    /**
     * Returns an instance of the singleton object.
     * The object will be initialized upon the first call of this function.
     *
     * @param context application context
     * @return singleton instance of the notification mirror
     */
    public static MirrorNotificationHandler getSingleInstance(Context context) {
        if (singleton_instance == null) {
            singleton_instance = new MirrorNotificationHandler(context);
        }
        return singleton_instance;
    }

    public void postNotification(int id, Notification notification) {
        notificationManager.notify(id, notification);
    }

    /**
     * Exectutes the action with the given action name
     *
     * @param mirrorNotification notification whose action to execute
     * @param actionName name of the action to be executed
     */
    public void executeNotificationAction(MirrorNotification mirrorNotification, String actionName) {

    }

    /**
     * dismisses the given notification from the status bar and active notification list (not directly but through the notification listener)
     *
     * @param mirrorNotification notification to be dismissed
     */
    public void dismissNotification(MirrorNotification mirrorNotification) {
        notificationManager.cancel(mirrorNotification.getId());
    }

    /**
     * Replies to a notification that has a reply action.
     *
     * @param mirrorNotification the notification to be replied to
     * @param message            the reply message
     * @param context            application context
     */
    public void replyToNotification(MirrorNotification mirrorNotification, String message, Context context) {
        MirrorNotification.Logger log = () -> {
            Log.e(TAG + "reply", "NO REPLYACTIONS or REMOTEINPUTS");
            Helper.toasted("Not Repliable");
        };
        Log.e(TAG,"REPPPPPLIYIINGNGNGN");
        final NotificationCompat.Action replyAction = mirrorNotification.getReplyAction();

        if (replyAction == null || replyAction.getRemoteInputs().length == 0) {
            Log.e(TAG, "no actions or remote inputs to reply to");
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        for (androidx.core.app.RemoteInput remoteIn : replyAction.getRemoteInputs())
            bundle.putCharSequence(remoteIn.getResultKey(), message);

        androidx.core.app.RemoteInput.addResultsToIntent(replyAction.getRemoteInputs(), intent, bundle); //maybe not androidx
        try {
            replyAction.actionIntent.send(context, 0, intent); //SET
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG + "reply", "REPLY FAILED" + e.getLocalizedMessage());
            Helper.toasted("Couldnt reply to Notification");
        }
    }
    /**
     * dismisses the last notificaiton the listener stored
     */
    public void dismissLastNotification() {
        try {
//            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).dismiss();
            dismissNotification(DeviceNotificationReceiver.getLastNotification());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickDismiss", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }
    /**
     * Displays a Notification in the status bar.
     *
     * @param notification test notification to be shown
     */
    @Deprecated
    public void postTestNotification(Notification notification) {
        notificationManager.notify(currentTestNotificationId, notification);
        currentTestNotificationId++;
    }
    @Deprecated
    public void updateTestNotification(Notification notification) {
        postNotification(currentTestNotificationId - 1, notification);
    }

    /**
     * Create a Notification to speperate different kinds of notifications.
     * E.g the device notifications and the test notifications.
     *
     * @param channelName        the name of the channel
     * @param channelDescription a short description of the channel
     * @param channelID          the unique ID of the channel
     * @param context            application context
     */
    @Deprecated
    public static void createNotificationChannel(String channelName, String channelDescription, String channelID, Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(context, NotificationManager.class); //TODO use the whole class scope one
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
