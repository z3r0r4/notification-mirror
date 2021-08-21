package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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

    public static MirrorNotificationHandler notificationHandler;

    public static NotificationMirror notificationMirror; //rename

//    private NotificationManagerCompat notificationManager;
    private Notification testNotification;

    private SharedPreferencesManager mSharedPreferencesManager;
    public static Context sContext; //suck it Context. maybe rather not static? hehe leaky memory
    private Context mContext;

    //TODO test on real devices
    //TODO change accent color of switches to bluuue

    //TODO check one feature by one

    //TODO replace PC side mirror references with "reflection" !!!
//    /**
//     * Test only
//     * creates a Test NotificationChannel
//     *
//     * @return NotificationManager with channel for test purposes
//     */
//    @Deprecated
//    public static NotificationManagerCompat createTestNotificationChannel() {
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.mContext);
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
        mContext = getApplicationContext();
        Helper.setContext(getApplicationContext());
        mSharedPreferencesManager = SharedPreferencesManager.getSingleInstance(mContext);

        /**declare buttons and views*/
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
//        Button btnTestNotificationReceiverAccess = findViewById(R.id.btnTestBinding);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        Button btnNetTest = findViewById(R.id.btnNetTest);
        Button btnSaveConnection = findViewById(R.id.btnSave);
        SwitchCompat swListenerStatus = findViewById(R.id.swSetListenerPermission);
        SwitchCompat swMirrorState = findViewById(R.id.swMirrorState);
        SwitchCompat swRunReplyReceiverService = findViewById(R.id.swRunReplyReceiverService);
        TextView tvMirrorIP = findViewById(R.id.tvMirrorIP);
        TextView tvMirrorPORT = findViewById(R.id.tvMirrorPORT);
        TextView tvReceiverIP = findViewById(R.id.tvReplyIP);
        TextView tvReceiverPORT = findViewById(R.id.tvReplyPORT);
        EditText etIP = findViewById(R.id.etIP);
        EditText etPORT = findViewById(R.id.etPort);

        //get an instance of the notification mirror
        notificationMirror = NotificationMirror.getSingleInstance(mContext);

        /** load the notification filter that classifies which notifications to mirror */
        NotificationFilter.loadFilter(mContext);

        /** create TestChannel and TestNotification */
        //get an instance of the notification handler
        notificationHandler = MirrorNotificationHandler.getSingleInstance(mContext);
        //create the notification channel for the test notifications
        notificationHandler.createNotificationChannel(Helper.TESTCHANNEL_NAME, Helper.TESTCHANNEL_DESCRIPTION, Helper.TESTCHANNEL_ID, mContext);
        //create a basic test notification that can be posted
        testNotification = Helper.createTestNotification(mContext);

        /** onclick to post test notification when the "send test notification" button is clicked */
        btnMsgTest.setOnClickListener(v -> notificationHandler.postTestNotification(testNotification));


        /**check and show if listener is connected*/
        swListenerStatus.setClickable(false);
        showListenerServiceStatus();

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener(v -> openListenerSettings());

        /**check if notifications should be mirrored*/
//        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));
        swMirrorState.setChecked(mSharedPreferencesManager.getMirrorState(false));//K

        /**add onclick to set mirroring state on toggle*/
//        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> setMirrorState(isChecked));
        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> mSharedPreferencesManager.setMirrorState(isChecked)); //K
//
        /**handle replyintents from testnotification*/
        handleReplyIntent();
//
        /**show Mirror Socket Address in Textviews*/
        showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);

        /**save IP:PORT from edittexts*/
        btnSaveConnection.setOnClickListener(v -> {
            mSharedPreferencesManager.setSocketAddress(mContext, etIP, etPORT);
            showMirrorSocketAddress(tvMirrorIP, tvMirrorPORT);
            notificationMirror.updateHostCredentials(mContext);
        });
//
//        /**show Receiver Socket Address in Textviews*/
        showReceiverSocketAddress(tvReceiverIP, tvReceiverPORT);

        /** check and abort when there is not connection to the listener */
//        if (!mSharedPreferencesManager.getListenerServiceStatus(mContext)) return;
        //Problematic: yes because buttons dont work if there is no listener connection -> TODO make reply actions and such safe to execute even if there is no listener connection

//        /** test reciever access */
//        btnTestNotificationReceiverAccess.setOnClickListener(v -> getLastNotification());
//
        /**add onclick to reply to last notification */
        btnReply.setOnClickListener(v -> replyToLastNotification());
//
//        /**add onclick to dismiss last notifiaction */
        btnDismiss.setOnClickListener(v -> MirrorNotificationHandler.getSingleInstance(mContext).dismissLastNotification());
//
//        /**add onclick to mirror last notification*/
//        btnNetTest.setOnClickListener(v -> mirrorLastNotification());
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createForegroundServiceNotificationChannel();
//        }
//
//
//        /**make sure the reply receiver service is started and its shown*/
//        showReceiverServiceStatus();
//        ensureReceiverServiceState();
//
//        /**add onclick to start listener service*/
//        swRunReplyReceiverService.setOnCheckedChangeListener((v, isChecked) -> {
//            if (isChecked) startReplyListenerService();
//            else stopReplyListenerService();
//        });
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
        if (mSharedPreferencesManager.getReplyReceiverServiceStatus()) startReplyListenerService();
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
     * Show the Socket Address the mirror tries to connect to in the Textviews
     *
     * @param tvIP TextView where the ip will be displayed in
     * @param tvPORT TextView where the port will be displayed in
     */
    private void showMirrorSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(mSharedPreferencesManager.getMirrorIP(mContext));
        tvPORT.setText(String.valueOf(mSharedPreferencesManager.getMirrorPort(mContext)));
    }

    /**
     * Show the Socket Address the Receiver listens to in the Textviews
     *
     * @param tvIP
     * @param tvPORT
     */
    private void showReceiverSocketAddress(TextView tvIP, TextView tvPORT) {
        tvIP.setText(mSharedPreferencesManager.getReceiverIP(mContext));
        tvPORT.setText(mSharedPreferencesManager.getReceiverPort(mContext));
    }



    /**
     * replies to the last notification the listener stored
     * requires working listener
     */
    private void replyToLastNotification() {
        try {
//old            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).reply("AUTOREPLY", getApplicationContext());
                MirrorNotification notification = DeviceNotificationReceiver.getLastNotification();
                notificationHandler.replyToNotification(notification, "AUTOREPLY", mContext);
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReply", "no Noficications yet, or Listener broke");
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
        swListenerStatus.setChecked(mSharedPreferencesManager.getListenerServiceStatus(mContext));
    }

    /**
     * sets the state of the receiver preference as switch state
     */
    private void showReceiverServiceStatus() {
        SwitchCompat swReceiverStatus = findViewById(R.id.swRunReplyReceiverService);
        swReceiverStatus.setChecked(mSharedPreferencesManager.getReplyReceiverServiceStatus());
    }

    /**
     * creates a notification channel for the Foreground Receiver Service
     * could be merged with the createTestNotificationChannel in Mirrornotifcation class
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createForegroundServiceNotificationChannel() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.createNotificationChannel(new NotificationChannel(FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_ID, FOREGROUND_SERVICE_NOTIFICATIONCHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
    }

    /**
     * starts the the foreground service that listens for replies to the messages
     */
    private void startReplyListenerService() {
        Intent replyListenerIntent = new Intent(mContext, NetworkNotificationReceiver.class);
        this.startService(replyListenerIntent);
    }

    /**
     * stops the the foreground service that listens for replies to the messages
     */
    private void stopReplyListenerService() {
        Intent replyListenerIntent = new Intent(mContext, NetworkNotificationReceiver.class);
        this.stopService(replyListenerIntent);
    }

    /**
     * Test only
     * handles an incoming intent and replies to the notification if its an intent from an reply to the notification.
     * TODONT: add the feature to replace the 'replied-to-notification' with a notification that says 'it was replied to' nah just tests
     */
    @Deprecated
    private void handleReplyIntent() {
        Intent intent = this.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        Log.d(TAG, "handleReplyIntent: Trying to get Reply Input");
        try {//TODONT dont react to every intent (use broadcasts?) doesnt matter as its just for testing
            String inputString = remoteInput.getCharSequence("reply").toString();

            TextView replyTV = findViewById(R.id.tvRepliedText);
            replyTV.setText(inputString);

            Notification repliedNotification =  //update Notifiaction to stop sending loading circle
                    new NotificationCompat.Builder(this, Helper.TESTCHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentText("Reply received")
                            .build();
            notificationHandler.updateTestNotification(repliedNotification); //doesnt work anymore =>TODO
        } catch (NullPointerException e) {
            Log.e(TAG + "handleReplyIntent", "REPLY EXTRACTION FAILED, maybe not a replying intent");
//            Helper.toasted("Not a Reply Intent");
        }
    }
}