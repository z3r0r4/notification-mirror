package com.r4.notifications.mirror;

class NotificationMirror {
    public static void mirror(MirrorNotification notification) {
//    send NotificationData over windows to network
    }

    public static void mirrorCancel(MirrorNotification notification) {
//    send Update to cancel over network
    }

    /*
    Get DATA out of the service
     */
    private MirrorNotification getNotification(String id) {
        return null; //use Binder to access Data in ListenerService
    }

    private void onReceive(NetworkPackage networkPackage) {//maybe not here
        MirrorNotification notification = getNotification(networkPackage.getID());
        if (networkPackage.isReply())
            notification.reply(networkPackage.getMessage());
        else if (networkPackage.isAction())
            notification.act(networkPackage.getActionName());
    }
}
