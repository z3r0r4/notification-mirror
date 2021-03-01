package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "nm.MainActivity";

    //public static Context sContext;
    public static NotificationMirror notificationMirror;

    private NotificationManagerCompat notificationManager;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;
    private Notification testNotification;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        
        shPref = context.getSharedPreferences(DeviceNotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();

        //get an instance of the notification mirror
        notificationMirror = NotificationMirror.getInstance(context);
        //create the notification channel for the test notifications
        notificationMirror.createNotificationChannel(Helper.TESTCHANNEL_NAME, Helper.TESTCHANNEL_DESCRIPTION, Helper.TESTCHANNEL_ID, context);
        //create a basic test notification that can be posted
        testNotification = Helper.createTestNotification(this);

        //load the notification filter that classifies which notifications to mirror
        NotificationFilter.loadFilter(context);

        /**declare buttons and views*/
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnSaveConnection = findViewById(R.id.btnSave);
        Button btnNetTest = findViewById(R.id.btnNetTest);

        SwitchCompat swListenerStatus = findViewById(R.id.swSetListenerPermission);
        SwitchCompat swMirrorState = findViewById(R.id.swMirrorState);
        SwitchCompat swRunReplyReceiverService = findViewById(R.id.swRunReplyReceiverService);

        EditText etIP = findViewById(R.id.etIP);
        EditText etPORT = findViewById(R.id.etPort);

        TextView tvMirrorIP = findViewById(R.id.tvMirrorIP);
        TextView tvMirrorPORT = findViewById(R.id.tvMirrorPORT);
        TextView tvReceiverIP = findViewById(R.id.tvReplyIP);
        TextView tvReceiverPORT = findViewById(R.id.tvReplyPORT);

        /**check and show if listener is connected*/
        swListenerStatus.setClickable(false);
        showListenerServiceStatus();

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener(v -> openListenerSettings());

        /**check if notifications should be mirrored*/
        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));

        /**add onclick to set mirroring state on toggle*/
        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> setMirrorState(isChecked));

        /**add onclick to start listener service*/
        swRunReplyReceiverService.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) startReplyListenerService();
            else stopReplyListenerService();
        });

        //post the test notification when the "send test notification" button is clicked
        btnMsgTest.setOnClickListener(v -> notificationMirror.showTestNotification(testNotification));

        /**add onclick to reply to last notification */
        btnReply.setOnClickListener(v -> replyToLastNotification());

        /**add onclick to dismiss last notifiaction */
        btnDismiss.setOnClickListener(v -> dismissLastNotification());

        /**add onclick to mirror last notification*/
        btnNetTest.setOnClickListener(v -> mirrorLastNotification());

        /**save IP:PORT from edittexts*/
        btnSaveConnection.setOnClickListener(v -> {
            setSocketAddress(etIP, etPORT);
            showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);
            notificationMirror.updateHostCredentials(this);
        });

        /**handle replyintents from testnotification*/
        handleReplyIntent();

        /**show Mirror Socket Address in Textviews*/
        showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);
        /**show Receiver Socket Address in Textviews*/
        showReceiverSocketAddress(tvReceiverIP, tvReceiverPORT);
        /**make sure the reply receiver service is started and its shown*/
        showReceiverServiceStatus();
        ensureReceiverServiceState();

    }

    @Override
    protected void onStart() {
        super.onStart();
        /**check and show if listener is connected*/
        showListenerServiceStatus();
        ensureReceiverServiceState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /**check and show if listener is connected*/
        showListenerServiceStatus();
        handleReplyIntent();
    }

    /**
     * tries to start or stop the receiver depending on the sharedpreferences
     */
    private void ensureReceiverServiceState() {
        if (getReplyReceiverServiceStatus()) startReplyListenerService();
        else stopReplyListenerService();
    }

    /**
     * Parse the editTexts as Socket address and store them or defaults in the sharedpreferences
     *
     * @param etIP
     * @param etPORT
     */
    private void setSocketAddress(EditText etIP, EditText etPORT) {
        String IP = context.getResources().getString(R.string.DefaultMirrorIP);
        IP = etIP.getText().toString().equals("") ? IP : etIP.getText().toString();

        int PORT = context.getResources().getInteger(R.integer.DefaultMirrorPORT);
        try {
            PORT = Integer.parseInt(etPORT.getText().toString()) != 0 ? Integer.parseInt(etPORT.getText().toString()) : PORT;
        } catch (NumberFormatException e) {
            Helper.toasted(context,"Input an Integer");
        }

        editor.putString("HOST_IP", IP);
        editor.putInt("HOST_PORT", PORT);
        editor.apply();
        Log.d(TAG, "changed HOST_IP to:" + IP);
        Log.d(TAG, "changed HOST_PORT to:" + PORT);
    }

    /**
     * Show the Socket Address the mirror tries to connect to in the Textviews
     *
     * @param tvIP
     * @param tvPORT
     */
    private void showMirrorSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(shPref.getString("HOST_IP", context.getResources().getString(R.string.DefaultMirrorIP)));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", context.getResources().getInteger(R.integer.DefaultMirrorPORT))));
    }

    /**
     * Show the Socket Address the Receiver listens to in the Textviews
     *
     * @param tvIP
     * @param tvPORT
     */
    private void showReceiverSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(context.getResources().getString(R.string.DefaultReceiverIP));//shPref.getString("HOST_IP",
        tvPORT.setText(String.valueOf(getResources().getInteger(R.integer.DefaultReceiverPORT)));//String.valueOf(shPref.getInt("HOST_PORT",
    }

    /**
     * Get the last notification the listener stored
     * requires a working listener
     */
    private void getLastNotification() {
        try {
            Log.d("Test By extracting Last Notification", DeviceNotificationReceiver.getactiveNotifications().get(DeviceNotificationReceiver.lastKey).toString());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReceiverAccess", "no Noficications yet, or Listener broke");
            Helper.toasted(context,"No Notifications yet, check Listener connection");
        }
    }

    /**
     * store and log if the notifications should be mirrored
     *
     * @param isChecked
     */
    private void setMirrorState(boolean isChecked) {
        editor.putBoolean("MirrorState", isChecked);
        editor.apply();
        if (isChecked)
            Log.d(TAG, "onCreate: Mirroring now");
        if (!isChecked)
            Log.d(TAG, "onCreate: NOT Mirroring now");
    }

    /**
     * replies to the last notification the listener stored
     * requires working listener
     */
    private void replyToLastNotification() {
        try {
            MirrorNotification notification = DeviceNotificationReceiver.getLastNotification();
            notificationMirror.replyToNotification(notification, "AUTOREPLY", context);
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReply", "no Noficications yet, or Listener broke");
            Helper.toasted(context,"No Notifications yet, check Listener connection");
        }
    }

    /**
     * sends the last notification over the network
     */
    private void mirrorLastNotification() {
        try {
            notificationMirror.mirrorFromDevice(DeviceNotificationReceiver.getLastNotification());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickNetTest", "no Noficications yet, or Listener broke");
            Helper.toasted(context,"No Notifications yet, check Listener connection");
        }
    }

    /**
     * dismisses the last notificaiton the listener stored
     */
    private void dismissLastNotification() {
        try {
            notificationMirror.dismissNotification(DeviceNotificationReceiver.getLastNotification());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickDismiss", "no Noficications yet, or Listener broke");
            Helper.toasted(context,"No Notifications yet, check Listener connection");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(DeviceNotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
            return sharedPreferences.getBoolean("ListenerStatus", false);
        } else {
            ComponentName cn = new ComponentName(this, DeviceNotificationReceiver.class);
            String flat = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
            return flat != null && flat.contains(cn.flattenToString());
        }
    }

    /**
     * sets the state of the receiver preference as switch state
     */
    private void showReceiverServiceStatus() {
        SwitchCompat swReceiverStatus = findViewById(R.id.swRunReplyReceiverService);
        swReceiverStatus.setChecked(getReplyReceiverServiceStatus());
    }


    /**
     * gets the current state of the receiver preference
     *
     * @return boo if the receiver should be running
     */
    private boolean getReplyReceiverServiceStatus() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(DeviceNotificationReceiver.class.getSimpleName(), AppCompatActivity.MODE_PRIVATE);
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
     * handles an incoming intent and replies to the notification if its an intent from an reply to the notification.
     * TODO: add the feature to replace the 'replied-to-notification' with a notification that says 'it was replied to'
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
            //notificationManager.notify(9003, repliedNotification);
        } catch (NullPointerException e) {
            Log.e(TAG + "handleReplyIntent", "REPLY EXTRACTION FAILED, maybe not a replying intent");
//            Helper.toasted("Not a Reply Intent");
        }
    }
}