package com.r4.notifications.mirror;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Set;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;

/**
 * @since 20210719
 * "what is my purpose"
 *  you make Toast
 * "oh..."
 *
 * and help create Test Notifications
 * in a previously created Test Channel (WHERE?!)
 */



class Helper {
    public static final String TESTCHANNEL_ID = "TestChannelID01";
    public static final String TESTCHANNEL_NAME = "TestChannel01";
    public static final String TESTCHANNEL_DESCRIPTION = "TestChannel";

    private static Context context;

    public static void setContext(Context context1) {
        context = context1;
    }

    /**
     * helps in creating and showing a toast
     *
     * @param text
     */
    protected static void toasted(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Returns a test notification that can be used to test if all functionalities of the
     * app are working properly:
     * catching
     * replies
     * dismiss
     * actions
     *
     * @param context the application context
     * @return test notification
     */
    protected static Notification createTestNotification(Context context) {
        androidx.core.app.RemoteInput remoteInput = new androidx.core.app.RemoteInput.Builder("reply")
                .setLabel("Enter Text Boss")
                .build();
        Intent replyIntent = new Intent(context, MainActivity.class);
        PendingIntent replyPendingIntent = PendingIntent.getActivity(
                    context, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.ic_dialog_info, "Reply", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        return new NotificationCompat.Builder(context, TESTCHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(replyAction)
                .build();
    }


    /**
     * turn actions into compat actions
     * @param action
     * @return
     */
    protected static NotificationCompat.Action compat(Notification.Action action) {
        IconCompat icon = IconCompat.createFromIcon(MainActivity.sContext, action.getIcon());
        CharSequence title = action.title;
        PendingIntent pIntent = action.actionIntent;
        Bundle extras = action.getExtras();
        android.app.RemoteInput[] remoteInputs = action.getRemoteInputs();
        android.app.RemoteInput[] dataOnlyRemoteInputs = new android.app.RemoteInput[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            dataOnlyRemoteInputs = action.getDataOnlyRemoteInputs();
        RemoteInput[] remoteInputsX = new RemoteInput[remoteInputs.length];
        RemoteInput[] dataOnlyRemoteInputsX = new RemoteInput[dataOnlyRemoteInputs.length];
        for(int i = 0; i< remoteInputs.length; i++)
            remoteInputsX[i] = compat(remoteInputs[i]); //boring ugly code
        for(int i = 0; i< dataOnlyRemoteInputs.length; i++)
            dataOnlyRemoteInputsX[i] = compat(dataOnlyRemoteInputs[i]); //boring ugly code
        boolean allowGeneratedReplies = action.getAllowGeneratedReplies();
        int semanticAction = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
            semanticAction = action.getSemanticAction();
        boolean showsUserInterface = true;
        boolean isContextual = true;
        NotificationCompat.Action.Builder Builder = new NotificationCompat.Action.Builder(icon, title, pIntent)
                .addExtras(extras)
                .setAllowGeneratedReplies(allowGeneratedReplies)
                .setSemanticAction(semanticAction)
                .setShowsUserInterface(showsUserInterface)
                .setContextual(isContextual);
        for (RemoteInput remoteInput: remoteInputsX)
            Builder.addRemoteInput(remoteInput);
        for (RemoteInput remoteInput: dataOnlyRemoteInputsX)
            Builder.addRemoteInput(remoteInput);
        return Builder.build();
    }

    /**
     * turn remoteinputs into compat remoteInputs
     * @param remoteInput
     * @return
     */
    private static RemoteInput compat(android.app.RemoteInput remoteInput) {
        String resultKey = remoteInput.getResultKey();
        CharSequence label = remoteInput.getLabel();
        CharSequence[] choices = remoteInput.getChoices();
        boolean allowFreeFormTextInput = remoteInput.getAllowFreeFormInput();
        int editChoicesBeforeSending = remoteInput.getEditChoicesBeforeSending();
        Bundle extras = remoteInput.getExtras();
        Set<String> allowedDataTypes = remoteInput.getAllowedDataTypes();
        RemoteInput.Builder compatRemoteInputBuilder = new RemoteInput.Builder(resultKey)
                .setLabel(label)
                .setChoices(choices)
                .setAllowFreeFormInput(allowFreeFormTextInput)
                .setEditChoicesBeforeSending(editChoicesBeforeSending)
                .addExtras(extras);
        allowedDataTypes.forEach((s) -> compatRemoteInputBuilder.setAllowDataType(s,true));
        return compatRemoteInputBuilder.build();
    }
}
