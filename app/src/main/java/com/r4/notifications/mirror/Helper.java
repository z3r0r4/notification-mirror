package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

class Helper {
    public static final String TESTCHANNEL_ID = "TestChannelID01";
    public static final String TESTCHANNEL_NAME = "TestChannel01";
    public static final String TESTCHANNEL_DESCRIPTION = "TestChannel";

    protected static void toasted(String text){
        Toast.makeText(MainActivity.sContext, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Returns a test notification that can be used to test if all functionalities of the
     * app are working properly
     *
     * @param context the application context
     * @return test notification
     */
    protected static Notification createTestNotification(Context context) {
        androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("reply")
                .setLabel("Enter Text Boss")
                .build();
        Intent replyIntent = new Intent(context, MainActivity.class);
        PendingIntent replyPendingIntent = PendingIntent.getActivity(
                context,
                1,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_dialog_info,
                "Reply", replyPendingIntent
        )
                .addRemoteInput(remoteInput)
                .build();

        return new NotificationCompat.Builder(context, TESTCHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(replyAction)
                .build();
    }
}
