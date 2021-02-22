package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "nm.MainActivity";

    public static Context sContext;

    private NotificationManagerCompat notificationManager;

    private NotificationGenerator notificationGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContext = getApplicationContext();

        /**declare buttons and views*/
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
        SwitchCompat swListenerStatus = findViewById(R.id.swSetListenerPermission);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnDismiss = findViewById(R.id.btnDismiss);

        //Shows a test notification when the "show notification" button is clicked
        notificationGenerator = new NotificationGenerator(this, "title", "content");
        if(notificationGenerator.isInitialized()) {
            btnMsgTest.setOnClickListener(v -> notificationGenerator.show());
        }

        /**check and show if listener is connected*/
        swListenerStatus.setClickable(false);
        showListenerServiceStatus();

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener(v -> openListenerSettings());

        /**handle replyintents from testnotification*/
        handleReplyIntent();


        //if (!getListenerServiceStatus()) return;
        /**add onclick to post Test Notification */
//        btnTestNotificationReceiverAccess.setOnClickListener(v -> getLastNotification());

        /**add onclick to reply to last notification */
        btnReply.setOnClickListener(v -> replyToLastNotification());

        /**add onclick to dismiss last notifiaction */
        btnDismiss.setOnClickListener(v -> dismissLastNotification());

        //createForegroundServiceNotificationChannel();

        /**make sure the reply receiver service is started and its shown*/
        ensureReceiverServiceState();

    }

    @Override
    protected void onStart() {
        super.onStart();
        /**check and show if listener is connected*/
        showListenerServiceStatus();
        ensureReceiverServiceState();
    }

    /**
     * tries to start or stop the receiver depending on the sharedpreferences
     */
    private void ensureReceiverServiceState() {
        if (getReplyReceiverServiceStatus()) startReplyListenerService();
        else stopReplyListenerService();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /**check and show if listener is connected*/
        showListenerServiceStatus();
        handleReplyIntent();
    }

    /**
     * replies to the last notification the listener stored
     * requires working listener
     */
    private void replyToLastNotification() {
        try {
            MirrorNotification notification = SystemNotificationReceiver.getactiveNotifications().get(SystemNotificationReceiver.lastKey);
            NotificationMirror.replyToNotification(notification, "AUTOREPLY", getApplicationContext());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReply", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }

    /**
     * dismisses the last notificaiton the listener stored
     */
    private void dismissLastNotification() {
        try {
            NotificationMirror.dismissNotification(SystemNotificationReceiver.getactiveNotifications().get(SystemNotificationReceiver.lastKey));
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickDismiss", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }

    /**
     * opens the ListenerServiceSettings
     */
    private void openListenerSettings() {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
    }

    /**
     * shows if the listener is connected
     */
    private void showListenerServiceStatus() {
        SwitchCompat swListenerStatus = findViewById(R.id.swSetListenerPermission);
        swListenerStatus.setChecked(getListenerServiceStatus());
    }

    /**
     * checks if the listener is connected
     *
     * @return state of the listener connection
     */
    private boolean getListenerServiceStatus() {
        ComponentName cn = new ComponentName(this, SystemNotificationReceiver.class);
        String flat = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    /**
     * gets the current state of the receiver preference
     *
     * @return boo if the receiver should be running
     */
    private boolean getReplyReceiverServiceStatus() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SystemNotificationReceiver.class.getSimpleName(), AppCompatActivity.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ReceiverStatus", false);
    }

    /**
     * starts the the foreground service that listens for replies to the messages
     */
    private void startReplyListenerService() {
//        Helper.toasted("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Intent i = new Intent(this, NetworkNotificationReceiver.class);
        this.startService(i);
    }

    /**
     * stops the the foreground service that listens for replies to the messages
     */
    private void stopReplyListenerService() {
        Intent i = new Intent(this, NetworkNotificationReceiver.class);
        this.stopService(i);
    }

    /**
     * Test only
     * handles an incoming intent and replies to the notification if its an intent from an reply to the notification
     */
    @Deprecated
    private void handleReplyIntent() {
        Intent intent = this.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        Log.d(TAG, "handleReplyIntent: Trying to get Reply Input");
        try {//TODO dont react to every intent (use broadcasts?)
            String inputString = remoteInput.getCharSequence("reply").toString();

            TextView replyTV = (TextView) findViewById(R.id.tvRepliedText);
            replyTV.setText(inputString);

            Notification repliedNotification =  //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, "TestChannel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();
            notificationManager.notify(9003, repliedNotification);
        } catch (NullPointerException e) {
            Log.e(TAG + "handleReplyIntent", "REPLY EXTRACTION FAILED, maybe not a replying intent");
//            Helper.toasted("Not a Reply Intent");
        }
    }
}