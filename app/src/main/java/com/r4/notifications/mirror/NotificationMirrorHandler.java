package com.r4.notifications.mirror;

import android.util.Log;

/**somewhere else plz*/

class NotificationMirrorHandler {
    private static final String TAG = "NotificationMirrorHandler";

    /**
     * sends the last notification over the network
     */
    private void mirrorLastNotification() {
        try {
//            Mirror mirror = new Mirror();
//            mirror.execute(NotificationReceiver.getactiveNotifications().get(NotificationReceiver.lastKey));
            NotificationMirror.getSingleInstance(MainActivity.sContext).mirrorFromDevice(DeviceNotificationReceiver.getLastNotification());
        } catch (NullPointerException e) {
            Log.e(TAG + "OnClickNetTest", "no Noficications yet, or Listener broke");
            Helper.toasted("No Notifications yet, check Listener connection");
        }
    }
}
