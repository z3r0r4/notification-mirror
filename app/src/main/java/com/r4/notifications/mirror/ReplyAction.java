package com.r4.notifications.mirror;

import android.app.PendingIntent;
import android.app.RemoteInput;

import java.util.ArrayList;
import java.util.UUID;

class ReplyAction {
    final String id = UUID.randomUUID().toString();
    PendingIntent pendingIntent;
    final ArrayList<RemoteInput> remoteInputs = new ArrayList<>();
    String packageName;
    String tag;
}
