package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "MAIN";

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
        btnMsgTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notification.post(notificationManager, getApplicationContext());//TODO add Test Notification
                Log.d(TAG, "onClick: msgTest");
            }
        });


//            final MirrorNotification notification2 = new MirrorNotification("123456");
        Button btnReply = (Button) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    notification.reply("AUTOREPLY", getApplicationContext());
                } catch (PendingIntent.CanceledException e) {
                    Log.e(TAG, "onClick: Reply Intent might be canceled already", e);
                }
                Log.d(TAG, "onClick: Reply");
            }
        });

        Button btnGetListenerPermission = (Button) findViewById(R.id.btnGetListenerPermission);
        btnGetListenerPermission.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
                Log.d(TAG, "onClick: ActionNotificationListener");
            }
        });
        handleReplyIntent();
    }

    private boolean checkPermission() {
        String theList = android.provider.Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        String[] theListList = theList.split(":");
        String me = (new ComponentName(this, NotificationReceiver.class)).flattenToString();
        for (String next : theListList) {
            if (me.equals(next)) return true;
        }
        return false;
    }

    private void handleReplyIntent() {
        Intent intent = this.getIntent();

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        try {
            Log.d(TAG, "handleReplyIntent: Trying to get Replied Input");
            TextView myTextView = (TextView) findViewById(R.id.tV_repliedtext);
            String inputString = remoteInput.getCharSequence("reply").toString();
            myTextView.setText(inputString);
            Notification repliedNotification =                                      //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, "TestChannel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();

            notificationManager.notify(9001, repliedNotification);
        } catch (Exception e) {
            Log.e(TAG, "handleReplyIntent: No Results Android lvl maybe low", e);
        }
    }
}