package com.r4.notifications.mirror;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Manages the user settings that are stored in the shared preferences
 *
 */
public class UserSettingsManager {
    private static final String TAG = "nm.UserSettingsManager";
    final private SharedPreferences shPref;

    private static UserSettingsManager singleton_instance;

    /**
     * Constructor
     *
     * @param context the application context
     */
    private UserSettingsManager(Context context) {
        shPref = context.getSharedPreferences(DeviceNotificationReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
    }

    /**
     * Create and return an instance of the UserSettingsManager if not already instantiated
     *
     * @param context the application context
     * @return instance of the UserSettingsManager
     */
    public static UserSettingsManager getInstance(Context context) {
        if(singleton_instance == null) {
            singleton_instance = new UserSettingsManager(context);
        }

        return singleton_instance;
    }

    /**
     * Parse the editTexts as Socket address and store them or defaults in the shared preferences
     *
     * @param context the application context
     * @param etIP the text field that contains the mirror ip
     * @param etPort the text field that contains the mirror port
     */
    public void setSocketAddress(Context context, EditText etIP, EditText etPort) {
        final String mirrorIP = extractMirrorIpFromEditText(context, etIP);
        final int mirrorPort = extractMirrorPortFromEditText(context, etPort);

        //open an editor for the shared preferences
        SharedPreferences.Editor editor = shPref.edit();

        editor.putString("HOST_IP", mirrorIP);
        editor.putInt("HOST_PORT", mirrorPort);

        //apply the changes to the shared preferences
        editor.apply();

        Log.d(TAG, "changed HOST_IP to:" + mirrorIP);
        Log.d(TAG, "changed HOST_PORT to:" + mirrorPort);
    }

    /**
     * store and log if the notifications should be mirrored
     *
     * @param isChecked flag if the mirror state is checked or not
     */
    public void setMirrorState(boolean isChecked) {
        //open an editor for the shared preferences
        SharedPreferences.Editor editor = shPref.edit();
        editor.putBoolean("MirrorState", isChecked);
        //apply the changes to the shared preferences
        editor.apply();

        if (isChecked) {
            Log.d(TAG, "onCreate: Mirroring now");
        }
        else {
            Log.d(TAG, "onCreate: NOT Mirroring now");
        }
    }

    /**
     * Write the status of the DeviceNotificationReceiver to shared preferences
     *
     * @param status status
     */
    public void setDeviceNotificationReceiverStatus(Boolean status) {
        shPref.edit().putBoolean("ListenerStatus", status).apply();
    }

    /**
     * Write the status of the NetworkNotificationReceiver to shared preferences
     *
     * @param status status
     */
    public void setNetworkNotificationReceiverStatus(Boolean status) {
        shPref.edit().putBoolean("ReceiverStatus", status).apply();
    }

    /**
     * Validates the ip in the text field where the user can enter a custom ip for the mirror.
     * Return the content of the input field if the value is valid, otherwise returns the default.
     *
     * @param context the application context
     * @param etIP input field that contains the ip
     * @return the ip of the mirror as a string
     */
    private String extractMirrorIpFromEditText(Context context, EditText etIP) {
        if(etIP.getText().toString().equals("")) {
            //return the default mirror IP
            return context.getResources().getString(R.string.DefaultMirrorIP);
        } else {
            //return the ip that the user set
            return etIP.getText().toString();
        }
    }

    /**
     * Validates the port in the input field where the user can enter a custom port for the mirror
     * Returns the content for the input field  if the value is valid, otherwise returns the default.
     *
     * @param context the application context
     * @param etPort the input field that contains the port
     * @return the port of the mirror as an integer
     */
    private int extractMirrorPortFromEditText(Context context, EditText etPort) {
        if(etPort.getText().toString().isEmpty()) {
            //return the default mirror port
            return context.getResources().getInteger(R.integer.DefaultMirrorPORT);
        } else {
            //return the port that the user set.
            try {
                return Integer.parseInt(etPort.getText().toString());
            }
            catch(NumberFormatException e) {
                //return the default port when the conversion fails
                return context.getResources().getInteger(R.integer.DefaultMirrorPORT);
            }
        }
    }

    /**
     * Return the host ip from the shared preferences or the default mirror ip when empty
     *
     * @param context the application context
     * @return mirror ip
     */
    public final String getMirrorIP(Context context) {
        return shPref.getString("HOST_IP", context.getResources().getString(R.string.DefaultMirrorIP));
    }

    /**
     * Return the host port from the shared preferences of the default mirror port when empty
     *
     * @param context the application context
     * @return mirror port
     */
    public final int getMirrorPort(Context context) {
        return shPref.getInt("HOST_PORT", context.getResources().getInteger(R.integer.DefaultMirrorPORT));
    }

    /**
     * Return the mirror state from the shared preferences or 'false' when empty
     *
     * @return mirror state
     */
    public final Boolean getMirrorState() {
        return shPref.getBoolean("MirrorState", false);
    }

    /**
     * Return the receiver service status from the shared preferences or 'false' when empty
     *
     * @return receiver service status
     */
    public final boolean getReplyReceiverServiceStatus() {
        return shPref.getBoolean("ReceiverStatus", false);
    }
}
