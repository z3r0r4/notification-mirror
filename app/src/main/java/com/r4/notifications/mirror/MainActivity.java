package com.r4.notifications.mirror;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MirrorNotification notification = new MirrorNotification("123","TESTNOTIFIACTION","This is a Test Text","REPLYACTION");

        Button btnMsgTest = (Button) findViewById(R.id.btnMsgTest);
        btnMsgTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notification.post();
                Log.d(TAG, "onClick: msgTest");
            }
        });
        Button btnReply = (Button) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notification.reply("AUTOREPLY");//                (new MirrorNotification("123")).reply("TEST");
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
    }


    //TEST-FUNCTIONS
}