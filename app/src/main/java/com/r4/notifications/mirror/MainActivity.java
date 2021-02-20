package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * @since 2021-02-17
 */
public class MainActivity extends AppCompatActivity {
    public static final String FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID = "45654654464";
    public static final String FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_NAME = "Foreground Service Persistent Notification";
    private final static String TAG = "MAIN";
    public static Context sContext; //suck it Context
    private NotificationManagerCompat notificationManager;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;

    //TODO test on other version -> move minsdk to 24
    //TODO test on real devices

    //TODO extract string and int resources
    //TODO improve logging
    //TODO add modifier for timeout

    //TODO show last replies from pc
    //TODO add test for mirror and receiver
    //TODO package mirror and receiver
    //TODO add conditional mirroring and receiving based on network name or other things
    //TODO add encryption?
    //TODO remove tests in release

    /**
     * Test only
     * creates a Test NotificationChannel
     *
     * @return NotificationManager with channel for test purposes
     */
    @Deprecated
    public static NotificationManagerCompat createTestNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.sContext);
        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
        return notificationManager;
    }

    /**
     * called on App first start to display/ inflate ui
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContext = getApplicationContext();
        shPref = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();

        /**declare buttons and views*/
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
        Switch swListenerStatus = findViewById(R.id.swSetListenerPermission);
        Switch swMirrorState = findViewById(R.id.swMirrorState);
        TextView tvMirrorIP = findViewById(R.id.tvMirrorIP);
        TextView tvMirrorPORT = findViewById(R.id.tvMirrorPORT);
        TextView tvReceiverIP = findViewById(R.id.tvReplyIP);
        TextView tvReceiverPORT = findViewById(R.id.tvReplyPORT);
        EditText etIP = findViewById(R.id.etIP);
        EditText etPORT = findViewById(R.id.etPort);
        Button btnSaveConnection = findViewById(R.id.btnSave);
        Button btnTestNotificationReceiverAccess = findViewById(R.id.btnTestBinding);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnNetTest = findViewById(R.id.btnNetTest);
        Switch swRunReplyReceiverService = findViewById(R.id.swRunReplyReceiverService);

        /**add on click to post test notification*/
        MirrorNotification notification = new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", sContext);
        notificationManager = createTestNotificationChannel();
        btnMsgTest.setOnClickListener(v -> notification.post(notificationManager, getApplicationContext()));

        /**check and show if listener is connected*/
        swListenerStatus.setClickable(false);
        showListenerServiceStatus();

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener(v -> openListenerSettings());

        /**check if notifications should be mirrored*/
        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));

        /**add onclick to set mirroring state on toggle*/
        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> setMirrorState(isChecked));

        /**handle replyintents from testnotification*/
        handleReplyIntent();

        /**show Mirror Socket Address in Textviews*/
        showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);

        /**save IP:PORT from edittexts*/
        btnSaveConnection.setOnClickListener(v -> {
            setSocketAddress(etIP, etPORT);
            showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);
        });

        /**show Receiver Socket Address in Textviews*/
        showReceiverSocketAddress(tvReceiverIP, tvReceiverPORT);


        if (!getListenerServiceStatus()) return;
        /**add onclick to post Test Notification */
        btnTestNotificationReceiverAccess.setOnClickListener(v -> getLastNotification());

        /**add onclick to reply to last notification */
        btnReply.setOnClickListener(v -> replyToLastNotification());

        /**add onclick to dismiss last notifiaction */
        btnDismiss.setOnClickListener(v -> dismissLastNotification());

        /**add onclick to mirror last notification*/
        btnNetTest.setOnClickListener(v -> mirrorLastNotification());

        createForegroundServiceNotificationChannel();

        /**make sure the reply receiver service is started and its shown*/
        showReceiverServiceStatus();
        ensureReceiverServiceState();

        /**add onclick to start listener service*/
        swRunReplyReceiverService.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) startReplyListenerService();
            else stopReplyListenerService();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**check and show if listener is connected*/
        showListenerServiceStatus();
        showReceiverServiceStatus();
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
        showReceiverServiceStatus();
        handleReplyIntent();
    }

    /**
     * Parse the editTexts as Socket address and store them or defaults in the sharedpreferences
     *
     * @param etIP
     * @param etPORT
     */
    private void setSocketAddress(EditText etIP, EditText etPORT) {
        String IP = MainActivity.sContext.getResources().getString(R.string.DefaultMirrorIP);
        IP = etIP.getText().toString().equals("") ? IP : etIP.getText().toString();

        int PORT = MainActivity.sContext.getResources().getInteger(R.integer.DefaultMirrorPORT);
        try {
            PORT = Integer.parseInt(etPORT.getText().toString()) != 0 ? Integer.parseInt(etPORT.getText().toString()) : PORT;
        } catch (NumberFormatException e) {
            Helper.toasted("Input an Integer");
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
        tvIP.setText(shPref.getString("HOST_IP", sContext.getResources().getString(R.string.DefaultMirrorIP)));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", sContext.getResources().getInteger(R.integer.DefaultMirrorPORT))));
    }

    /**
     * Show the Socket Address the Receiver listens to in the Textviews
     *
     * @param tvIP
     * @param tvPORT
     */
    private void showReceiverSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(MainActivity.sContext.getResources().getString(R.string.DefaultReceiverIP));//shPref.getString("HOST_IP",
        tvPORT.setText(String.valueOf(getResources().getInteger(R.integer.DefaultReceiverPORT)));//String.valueOf(shPref.getInt("HOST_PORT",
    }

    /**
     * Get the last notification the listener stored
     * requires a working listener
     */
    private void getLastNotification() {
        try {
            Log.d("Test By extracting Last Notification", NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).toString());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReceiverAccess", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
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
            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).reply("AUTOREPLY", getApplicationContext());
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
            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).dismiss();
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickDismiss", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }

    /**
     * sends the last notification over the network
     */
    private void mirrorLastNotification() {
        try {
            Mirror mirror = new Mirror();
            mirror.execute(NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey));
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickNetTest", "no Noficications yet, or Listener broke");
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
        Switch swListenerStatus = findViewById(R.id.swSetListenerPermission);
        swListenerStatus.setChecked(getListenerServiceStatus());
    }

    /**
     * checks if the listener is connected
     *
     * @return state of the listener connection
     */
    private boolean getListenerServiceStatus() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ListenerStatus", false);
    }

    /**
     * sets the state of the receiver preference as switch state
     */
    private void showReceiverServiceStatus() {
        Switch swReceiverStatus = findViewById(R.id.swRunReplyReceiverService);
        swReceiverStatus.setChecked(getReplyReceiverServiceStatus());
    }

    /**
     * gets the current state of the receiver preference
     *
     * @return boo if the receiver should be running
     */
    private boolean getReplyReceiverServiceStatus() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ReceiverStatus", false);
    }

    /**
     * creates a notification channel for the Foreground Receiver Service
     * could be merged with the createTestNotificationChannel in Mirrornotifcation class
     */
    private void createForegroundServiceNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.sContext);
        notificationManager.createNotificationChannel(new NotificationChannel(FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID, FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
    }

    /**
     * starts the the foreground service that listens for replies to the messages
     */
    private void startReplyListenerService() {
//        Helper.toasted("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Intent i = new Intent(this, ReplyListenerService.class);
        this.startService(i);
    }

    /**
     * stops the the foreground service that listens for replies to the messages
     */
    private void stopReplyListenerService() {
        Intent i = new Intent(this, ReplyListenerService.class);
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