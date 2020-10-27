package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MAIN";

    //TEST
    public NotificationManagerCompat notificationManager;


    //    @RequiresApi(api = Build.VERSION_CODES.O)//END
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
        final MirrorNotification notification = new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", this);

        Button btnMsgTest = (Button) findViewById(R.id.btnMsgTest);
        btnMsgTest.setOnClickListener(v -> {
            notification.post(notificationManager, getApplicationContext());
            Log.d(TAG, "onClick: msgTest");
        });

        Button btnReply = (Button) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(v -> {
            try {
                notification.reply("AUTOREPLY", getApplicationContext());//TODO GET FROM BINDER INSTEAD
            } catch (PendingIntent.CanceledException e) {
                Log.e(TAG, "onClick: Reply Intent might be canceled already", e);
            }
            Log.d(TAG, "onClick: Reply");
        });

        Button btnGetListenerPermission = (Button) findViewById(R.id.btnGetListenerPermission);
        btnGetListenerPermission.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
            Log.d(TAG, "onClick: ActionNotificationListener");
        });
        Switch swListenerStatus = (Switch) findViewById(R.id.swListenerPermission);
        swListenerStatus.setChecked(checkListenerService());
        
        handleReplyIntent();
    }

    /* TEST ONLY */
    private void handleReplyIntent() {
        Intent intent = this.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        Log.d(TAG, "handleReplyIntent: Trying to get Replied Input");
        try {
            String inputString = remoteInput.getCharSequence("reply").toString();

            TextView replyTV = (TextView) findViewById(R.id.tV_repliedtext);
            replyTV.setText(inputString);

            Notification repliedNotification =   //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, "TestChannel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();
            notificationManager.notify(9001, repliedNotification);
        } catch (NullPointerException e) {
            Log.e(TAG, "handleReplyIntent: couldn't get Reply text, maybe wrong Intent", e);
        }
    }

    private boolean checkListenerService() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ListenerStatus", false);

    }
}