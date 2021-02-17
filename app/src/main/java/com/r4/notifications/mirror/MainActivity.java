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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * @since 2021-02-17
 * */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MAIN";
    public static Context sContext;
    private NotificationManagerCompat notificationManager;
    private SharedPreferences shPref;
    private SharedPreferences.Editor editor;
    //TODO clean code
    //TODO reduce duplication
    //TODO improve readability
    //TODO replace default IP with 127.0.0.1
    //TODO create application data graph
    //TODO show last replies from pc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sContext = getApplicationContext();
        shPref = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        editor = shPref.edit();

        /**add on click to post test notification*/
        final MirrorNotification notification = getNotification();
        Button btnMsgTest = findViewById(R.id.btnMsgTest);
        btnMsgTest.setOnClickListener((v) -> notification.post(notificationManager, getApplicationContext()));

        /**check and show if listener is connected*/
        Switch swListenerStatus = findViewById(R.id.swListenerPermission);
        swListenerStatus.setClickable(false);
        swListenerStatus.setChecked(checkListenerService());

        /**add onclick to open notification listener settings*/
        swListenerStatus.setOnClickListener((v) -> openListenerSettings(v));

        /**check if notifications should be mirrored*/
        Switch swMirrorState = findViewById(R.id.swMirrorState);
        swMirrorState.setChecked(shPref.getBoolean("MirrorState", false));
        /**add onclick to set mirroring state on toggle*/
        swMirrorState.setOnCheckedChangeListener((v, isChecked) -> setMirrorState(isChecked));

        /**handle replyintents from testnotification*/
        handleReplyIntent();


        TextView tvIP = findViewById(R.id.tvIP);
        TextView tvPORT = findViewById(R.id.tvPORT);
        EditText etIP = findViewById(R.id.etIP);
        EditText etPORT = findViewById(R.id.etPort);

        /**show Socket Address in Textviews*/
        tvIP.setText(shPref.getString("HOST_IP", "192.168.178.84"));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", 9001)));

        /**save IP:PORT from edittexts*/
        Button btnSaveConnection = findViewById(R.id.btnSave);
        btnSaveConnection.setOnClickListener(v -> setSocketAddress(tvIP, tvPORT, etIP, etPORT));

        if (!checkListenerService()) return;
        /**add onclick to post Test Notification */
        Button btnTestNotificationReceiverAccess = findViewById(R.id.btnTestBinding);
        btnTestNotificationReceiverAccess.setOnClickListener((v) -> getLastNotification(v));

        /**add onclick to reply to last notification */
        Button btnReply = findViewById(R.id.btnReply);
        btnReply.setOnClickListener((v) -> replyToLastNotification(v));

        /**add onclick to mirror last notification*/
        Button btnNetTest = findViewById(R.id.btnNetTest);
        btnNetTest.setOnClickListener((v) -> mirrorLastNotification(v));
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**check and show if listener is connected*/
        checkshowListenerService();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /**check and show if listener is connected*/
        checkshowListenerService();

        handleReplyIntent();
    }

    /**
     * needs notificationManager
     * @return a generic Notification with action and reply action which can be used to Test
     */
    private MirrorNotification getNotification() {
        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
        return new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", this);
    }

    /**
     * Parse the editTexts as Socket address and store them or defaults in the sharedpreferences
     * Show the Socket Address in the Textviews
     * @param tvIP
     * @param tvPORT
     * @param etIP
     * @param etPORT
     */
    private void setSocketAddress(TextView tvIP, TextView tvPORT, EditText etIP, EditText etPORT) {
        String IP = etIP.getText().toString().equals("") ? "192.168.178.84" : etIP.getText().toString();

        int PORT = 9001;
        try {
            PORT = Integer.parseInt(etPORT.getText().toString()) != 0 ? Integer.parseInt(etPORT.getText().toString()) : 9001;
        } catch (NumberFormatException e) {
            Helper.toasted("Input an Integer");
        }

        editor.putString("HOST_IP", IP);
        editor.apply();
        Log.d(TAG, "changed HOST_IP to:" + IP);

        editor.putInt("HOST_PORT", PORT);
        editor.apply();
        Log.d(TAG, "changed HOST_PORT to:" + PORT);

        tvIP.setText(shPref.getString("HOST_IP", "192.168.178.84"));
        tvPORT.setText(String.valueOf(shPref.getInt("HOST_PORT", 9001)));
    }

    /**
     * Get the last notification the listener stored
     * requires a working listener
     * @param v
     */
    private void getLastNotification(View v) {
        try {
            Log.d("Test By extracting Last Notification", NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).toString());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReceiverAccess", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }

    /**
     * store and log if the notifications should be mirrored
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
     * @param v
     */
    private void replyToLastNotification(View v) {
        Log.d(TAG, "onClick: Reply");
        try {
            NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey).reply("AUTOREPLY", getApplicationContext());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickReply", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }

    /**
     * sends the last notification over the network
     * @param v
     */
    private void mirrorLastNotification(View v) {
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
     * @param v
     */
    private void openListenerSettings(View v) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
    }

    /**
     * shows if the listener is connected
     */
    private void checkshowListenerService() {
        Switch swListenerStatus = findViewById(R.id.swListenerPermission);
        swListenerStatus.setChecked(checkListenerService());
    }

    /**
     * checks if the listener is connected
     * @return state of the listener connection
     */

    private boolean checkListenerService() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(NotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ListenerStatus", false);
    }

    /* TEST ONLY */

    /**
     * handles an incoming intent and replies to the notification if its an intent from an reply to the notification
     */
    @Deprecated
    private void handleReplyIntent() {
        Intent intent = this.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        Log.d(TAG, "handleReplyIntent: Trying to get Replied Input");
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