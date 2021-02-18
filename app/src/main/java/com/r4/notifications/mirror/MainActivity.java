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
    private final static String TAG = "MAIN";

    public static final String FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID = "45654654464";
    public static final String FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_NAME = "Foreground Service Persistent Notification";

    public static Context sContext; //suck it Context
    private NotificationManagerCompat notificationManager;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;
    //TODO replace default IP with 127.0.0.1
    //TODO create application data graph
    //TODO show last replies from pc

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContext = getApplicationContext();
        shPref = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();

        /**declare buttons and views*/
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
        Switch swListenerStatus = findViewById(R.id.swListenerPermission);
        Switch swMirrorState = findViewById(R.id.swMirrorState);
        TextView tvIP = findViewById(R.id.tvIP);
        TextView tvPORT = findViewById(R.id.tvPORT);
        EditText etIP = findViewById(R.id.etIP);
        EditText etPORT = findViewById(R.id.etPort);
        Button btnSaveConnection = findViewById(R.id.btnSave);
        Button btnTestNotificationReceiverAccess = findViewById(R.id.btnTestBinding);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnNetTest = findViewById(R.id.btnNetTest);
        Button btnListenerServiceStart = findViewById(R.id.btnReplyListenerServiceStart);
        Button btnListenerServiceStop = findViewById(R.id.btnReplyListenerServiceStop);

        /**add on click to post test notification*/
        MirrorNotification notification = new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", sContext);
        notificationManager = createTestNotificationChannel();
        btnMsgTest.setOnClickListener(v -> notification.post(notificationManager, getApplicationContext()));

        /**check and show if listener is connected*/
        swListenerStatus.setClickable(false);
        showListenerService();

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener(v -> openListenerSettings());

        /**check if notifications should be mirrored*/
        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));

        /**add onclick to set mirroring state on toggle*/
        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> setMirrorState(isChecked));

        /**handle replyintents from testnotification*/
        handleReplyIntent();

        /**show Socket Address in Textviews*/
        tvIP.setText(shPref.getString("HOST_IP", "192.168.178.84"));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", 9001)));

        /**save IP:PORT from edittexts*/
        btnSaveConnection.setOnClickListener(v -> {
            setSocketAddress(etIP, etPORT);
            showSocketAddress(tvIP, tvPORT);
        });

        if (!getListenerServiceStatus()) return;
        /**add onclick to post Test Notification */
        btnTestNotificationReceiverAccess.setOnClickListener(v -> getLastNotification());

        /**add onclick to reply to last notification */
        btnReply.setOnClickListener(v -> replyToLastNotification());

        /**add onclick to dismiss last notifiaction */
        btnDismiss.setOnClickListener(v -> dismissLastNotification());

        /**add onclick to mirror last notification*/
        btnNetTest.setOnClickListener(v -> mirrorLastNotification());

        createServiceNotificationChannel();
        /**start the reply listener service in foreground*/
        btnListenerServiceStart.setOnClickListener(v -> startReplyListenerService());

        /**stop the replyListener service*/
        btnListenerServiceStop.setOnClickListener(v -> stopReplyListenerService());
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**check and show if listener is connected*/
        showListenerService();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /**check and show if listener is connected*/
        showListenerService();
        handleReplyIntent();
    }

    /**
     * Parse the editTexts as Socket address and store them or defaults in the sharedpreferences
     *
     * @param etIP
     * @param etPORT
     */
    private void setSocketAddress(EditText etIP, EditText etPORT) {
        String IP = etIP.getText().toString().equals("") ? "192.168.178.84" : etIP.getText().toString();

        int PORT = 9001;
        try {
            PORT = Integer.parseInt(etPORT.getText().toString()) != 0 ? Integer.parseInt(etPORT.getText().toString()) : 9001;
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
     * Show the Socket Address in the Textviews
     *
     * @param tvIP
     * @param tvPORT
     */
    private void showSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(shPref.getString("HOST_IP", "192.168.178.84"));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", 9001)));
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
    private void showListenerService() {
        Switch swListenerStatus = findViewById(R.id.swListenerPermission);
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

    private void createServiceNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.sContext);
        notificationManager.createNotificationChannel(new NotificationChannel(FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID, FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
    }

    private void startReplyListenerService() {
//        Helper.toasted("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Intent i = new Intent(this, ReplyListenerService.class);
        this.startService(i);
    }

    private void stopReplyListenerService(){
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

            Notification repliedNotification =   //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, "TestChannel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();
            notificationManager.notify(9001, repliedNotification);
        } catch (NullPointerException e) {
            Log.e(TAG + "handleReplyIntent", "REPLY EXTRACTION FAILED, maybe not a replying intent");
            Helper.toasted("Not a Reply Intent");
        }
    }
}