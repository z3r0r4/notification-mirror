package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * @since 2021-07-19
 * the Main Activity of the app
 *      displays the main activity and handles its buttons
 *          shows and gets config of the app
 *      sets the config of the app
 *      handles the replying intents of test notifications
 */
public class MainActivity extends AppCompatActivity {
    public static final String FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID = "45654654464";
    public static final String FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_NAME = "Foreground Service Persistent Notification";
    private final static String TAG = "MAIN";
    private final static String TAG = "nm.MainActivity";

    public static NotificationMirror notificationMirror;

    private NotificationManagerCompat notificationManager;
    private Notification testNotification;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;
    UserSettingsManager userSettingsManager;
    private Context applicationContext;  //instance of the application context
    public static Context sContext; //suck it Context

    //TODO test on other version -> move minsdk to 24
    //TODO test on real devices
    //TODO change accent color of switches to bluuue



//    /**
//     * Test only
//     * creates a Test NotificationChannel
//     *
//     * @return NotificationManager with channel for test purposes
//     */
//    @Deprecated
//    public static NotificationManagerCompat createTestNotificationChannel() {
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.sContext);
//        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
//        return notificationManager;
//    }
//    NOW IN HELPER CLASS

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
        applicationContext = getApplicationContext();//K

        shPref = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();
        userSettingsManager = UserSettingsManager.getInstance(applicationContext);//K

        //get an instance of the notification mirror
        notificationMirror = NotificationMirror.getInstance(applicationContext);
        //create the notification channel for the test notifications
        notificationMirror.createNotificationChannel(Helper.TESTCHANNEL_NAME, Helper.TESTCHANNEL_DESCRIPTION, Helper.TESTCHANNEL_ID, applicationContext);
        //create a basic test notification that can be posted
        testNotification = Helper.createTestNotification(this);

        //load the notification filter that classifies which notifications to mirror
        NotificationFilter.loadFilter(applicationContext);

        /**declare buttons and views*/
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
        SwitchCompat swListenerStatus = findViewById(R.id.swSetListenerPermission);
        SwitchCompat swMirrorState = findViewById(R.id.swMirrorState);
        SwitchCompat swRunReplyReceiverService = findViewById(R.id.swRunReplyReceiverService);
        TextView tvMirrorIP = findViewById(R.id.tvMirrorIP);
        TextView tvMirrorPORT = findViewById(R.id.tvMirrorPORT);
        TextView tvReceiverIP = findViewById(R.id.tvReplyIP);
        TextView tvReceiverPORT = findViewById(R.id.tvReplyPORT);
        EditText etIP = findViewById(R.id.etIP);
        EditText etPORT = findViewById(R.id.etPort);
        Button btnSaveConnection = findViewById(R.id.btnSave);
//        Button btnTestNotificationReceiverAccess = findViewById(R.id.btnTestBinding);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnNetTest = findViewById(R.id.btnNetTest);

        /**add on click to post test notification*/
        MirrorNotification notification = new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", sContext);
        notificationManager = createTestNotificationChannel();
        btnMsgTest.setOnClickListener(v -> notification.post(notificationManager, getApplicationContext()));
        //post the test notification when the "send test notification" button is clicked
        btnMsgTest.setOnClickListener(v -> notificationMirror.showTestNotification(testNotification));

        /**check and show if listener is connected*/
        swListenerStatus.setClickable(false);
        showListenerServiceStatus();

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener(v -> openListenerSettings());

        /**check if notifications should be mirrored*/
        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));
//        swMirrorState.setChecked(userSettingsManager.getMirrorState());//K

        /**add onclick to set mirroring state on toggle*/
        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> setMirrorState(isChecked));
//        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> userSettingsManager.setMirrorState(isChecked)); //K

        /**handle replyintents from testnotification*/
        handleReplyIntent();

        /**show Mirror Socket Address in Textviews*/
        showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);

        /**save IP:PORT from edittexts*/
        btnSaveConnection.setOnClickListener(v -> {
            setSocketAddress(etIP, etPORT);
            showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);
        });
        //save IP:PORT from edittexts
        btnSaveConnection.setOnClickListener(v -> {
            userSettingsManager.setSocketAddress(applicationContext, etIP, etPORT);
            showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);
            notificationMirror.updateHostCredentials(this);
        });

        /**show Receiver Socket Address in Textviews*/
        showReceiverSocketAddress(tvReceiverIP, tvReceiverPORT);

        if (!getListenerServiceStatus()) return; //Problematic Y?
        /**add onclick to post Test Notification *///wrong -> test reciever access
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
        showReceiverServiceStatus();//maybe not //K
        ensureReceiverServiceState();
    }

    /**
     * tries to start or stop the receiver depending on the sharedpreferences
     */
    private void ensureReceiverServiceState() {
        if (getReplyReceiverServiceStatus()) startReplyListenerService();
//        if (userSettingsManager.getReplyReceiverServiceStatus()) { //K
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
     * @param tvIP TextView where the ip will be displayed in
     * @param tvPORT TextView where the port will be displayed in
     */
    private void showMirrorSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(shPref.getString("HOST_IP", sContext.getResources().getString(R.string.DefaultMirrorIP)));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", sContext.getResources().getInteger(R.integer.DefaultMirrorPORT))));
//        tvIP.setText(userSettingsManager.getMirrorIP(applicationContext));
//        tvPORT.setText(String.valueOf(userSettingsManager.getMirrorPort(applicationContext)));
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
            Log.d("Test By extracting Last Notification", DeviceNotificationReceiver.getactiveNotifications().get(DeviceNotificationReceiver.lastKey).toString());
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
//old            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).reply("AUTOREPLY", getApplicationContext());
                MirrorNotification notification = DeviceNotificationReceiver.getLastNotification();
                notificationMirror.replyToNotification(notification, "AUTOREPLY", applicationContext);
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
//            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).dismiss();
            notificationMirror.dismissNotification(DeviceNotificationReceiver.getLastNotification());
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
//            Mirror mirror = new Mirror();
//            mirror.execute(NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey));
            notificationMirror.mirrorFromDevice(DeviceNotificationReceiver.getLastNotification());
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
            SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(DeviceNotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
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
//        swReceiverStatus.setChecked(getReplyReceiverServiceStatus());
        swReceiverStatus.setChecked(userSettingsManager.getReplyReceiverServiceStatus());
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
//        Intent i = new Intent(this, ReplyListenerService.class);
        Intent replyListenerIntent = new Intent(this, NetworkNotificationReceiver.class);
        this.startService(replyListenerIntent);
    }

    /**
     * stops the the foreground service that listens for replies to the messages
     */
    private void stopReplyListenerService() {
//        Intent i = new Intent(this, ReplyListenerService.class);
        Intent replyListenerIntent = new Intent(this, NetworkNotificationReceiver.class);
        this.stopService(replyListenerIntent);
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

            TextView replyTV = findViewById(R.id.tvRepliedText);
            replyTV.setText(inputString);

            Notification repliedNotification =  //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, "TestChannel")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();
            //notificationManager.notify(9003, repliedNotification); //doesnt work anymore =>TODO
        } catch (NullPointerException e) {
            Log.e(TAG + "handleReplyIntent", "REPLY EXTRACTION FAILED, maybe not a replying intent");
//            Helper.toasted("Not a Reply Intent");
        }
    }
}