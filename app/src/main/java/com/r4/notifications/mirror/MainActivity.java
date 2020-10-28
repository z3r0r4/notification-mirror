package com.r4.notifications.mirror;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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

//        NotificationMirror notificationMirror = new NotificationMirror();
//        Intent bindIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
//        if(!bindService(bindIntent, notificationMirror.mConnection, Context.BIND_AUTO_CREATE))throw new NullPointerException();



        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(new NotificationChannel("TestChannel", "Test", NotificationManager.IMPORTANCE_HIGH));
        final MirrorNotification notification = new MirrorNotification("123456", "TestNotification", "Testing", "ReplyAction", this);

        Button btnMsgTest = (Button) findViewById(R.id.btnMsgTest);
        btnMsgTest.setOnClickListener(v -> {//TODO dont use lambda pass a actual callback function https://medium.com/@CodyEngel/4-ways-to-implement-onclicklistener-on-android-9b956cbd2928
            notification.post(notificationManager, getApplicationContext());
            Log.d(TAG, "onClick: msgTest");
        });



        Switch swListenerStatus = (Switch) findViewById(R.id.swListenerPermission);
        swListenerStatus.setClickable(false);
        swListenerStatus.setChecked(checkListenerService());

        swListenerStatus.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        });


//        if(!notificationMirror.mBound) throw new NullPointerException();
        handleReplyIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationReceiver receiver = NotificationReceiver.get();
        Button btnTestBinding = (Button) findViewById(R.id.btnTestBinding);
        btnTestBinding.setOnClickListener(v -> {
//            if(notificationMirror.mBound){
            receiver.checkBinding();
//            Log.e(TAG, "BOOOOOOOOOOOOOOOOOOOOOOUUUUUUUUUUUUNDDDDDDDDDDDDDDDDDDDDD", new Exception());
//            } else {
//                Log.e(TAG, "onCreate: NOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
//            }
        });
        Button btnReply = (Button) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(v -> {
            receiver.getLast().reply("AUTOREPLY", getApplicationContext());//TODO GET FROM BINDER INSTEAD //binder.activenotifications.last.reply("AUTOREPLY", getApplicationContext());
            Log.d(TAG, "onClick: Reply");
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
        try {//TODO dont react to every intent (use broadcasts?)
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

//    NotificationReceiver mReceiver;
//    boolean mBound = false;
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Bind to LocalService
//        Intent intent = new Intent(this, NotificationReceiver.class);
//        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//    }
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unbindService(connection);
//        mBound = false;
//    }
//    /** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            NotificationReceiver.LocalBinder binder = (NotificationReceiver.LocalBinder) service;
//            mReceiver = binder.getService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };
}