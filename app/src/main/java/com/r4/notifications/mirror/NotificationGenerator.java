package com.r4.notifications.mirror;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static androidx.core.content.ContextCompat.getSystemService;

public class NotificationGenerator {
    private static final String TAG = "nm.NotificationGenerator";

    static int GENERATOR_ID = 0;

    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManager;
    String CHANNEL_ID;
    Context context;
    boolean initialized = false;


    public NotificationGenerator(Context context, String textTitle, String textContent) {
        CHANNEL_ID = "TestChannel_" + GENERATOR_ID++;
        this.context = context;

        //create the notification channel
        createNotificationChannel(CHANNEL_ID, CHANNEL_ID);

        //create the notification
        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //initialize the notificationManager
        notificationManager = NotificationManagerCompat.from(context);

        initialized = true;
    }


    private void createNotificationChannel(String channelName, String channelDescription) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(context, NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void show() {
        notificationManager.notify(4, builder.build());
    }

    public void show(String title, String content) {
        builder.setContentTitle(title).setContentText(content);
        notificationManager.notify(4, builder.build());

        Log.d(TAG, "Test Notification sent!");
    }

    public boolean isInitialized()
    {
        return initialized;
    }
}
