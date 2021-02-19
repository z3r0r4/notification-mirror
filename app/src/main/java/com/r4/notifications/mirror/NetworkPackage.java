package com.r4.notifications.mirror;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

//TODO maybe merge with mirrornotification
class NetworkPackage {

    private static final String TAG = "NetworkPackage";

    private String key;
    private int id;
    private String message;
    private String actionName;

    private boolean isAction;
    private boolean isReply;
    private boolean isDismiss;


    /**
     * extract and store data from json formated string
     * id, key: see mirrornotification constructor (needed)
     * message: message which should be replied to notification
     * actionname: name of the action of the notification that should be fullfilled
     * isaction: boo if there is a action to be taken
     * isreply: boo if there is a reply to be send
     * isdismiss: boo if the notification should be dismissed
     *
     * @param jsonformatedstring
     */
    public NetworkPackage(String jsonformatedstring) {
        try {
            JSONObject json = new JSONObject(jsonformatedstring);
            this.id = Integer.parseInt(getString(json, "id"));
            this.key = getString(json, "key");
            this.message = getString(json, "message");
            this.actionName = getString(json, "actioname");
            this.isAction = getBoolean(json, "isaction");
            this.isReply = getBoolean(json, "isreply");
            this.isDismiss = getBoolean(json, "isdismiss");
        } catch (JSONException e) {
            Log.e(TAG, "could not extract");
        }
        if (this.key.equals("")) {
            Log.e(TAG, "key cant be empty, needed for notification extraction");
            throw new NullPointerException();
        }
        if (this.id == 0) {//maybe not that necessary
            Log.e(TAG, "id cant be 0, needed for notification extraction");
            throw new NullPointerException();
        }
    }

    /**
     * logs the data extracted from the notification
     */
    public void log() {
        Log.d(TAG, "Networkpackage:\n" +
                "\nid     :" + this.id +
                "\nkey    :" + this.key +
                "\nmessage:" + this.message +
                "\nisAct  :" + this.isAction +
                "\nisReply:" + this.isReply +
                "\nisDis  :" + this.isDismiss);
    }

    /**
     * gets strings out of json object and catches missing keys safely
     *
     * @param json    jsonobject that contains the data
     * @param jsonKey key that should be lookedup
     * @return key entry, or empty string "" if keys missing
     */
    public String getString(JSONObject json, String jsonKey) {
        try {
            return (String) json.get(jsonKey);
        } catch (JSONException j) {
            Log.e(TAG, "key entry missing: " + jsonKey);
            return "";
        }
    }

    /**
     * gets booleans out of json object and catches missing keys safely
     *
     * @param json    jsonobject that contains the data
     * @param jsonKey key that should be lookedup
     * @return key entry, or false if keys missing
     */
    public boolean getBoolean(JSONObject json, String jsonKey) {
        try {
            return Boolean.parseBoolean((String) json.get(jsonKey));
        } catch (JSONException j) {
            Log.e(TAG, "key entry missing: " + jsonKey);
            return false;
        }
    }

    /**
     * returns if pkg describes a action
     * checks if action is specified and usable
     *
     * @return if pkg is action
     */
    public boolean isAction() {
        if (actionName.equals("")) Log.e(TAG, "action without name: maleformed json");
        return this.isAction;
    }

    /**
     * returns if pkg is a reply
     * checks if reply message is specified and usable
     *
     * @return if pkg is reply
     */
    public boolean isReply() {
        if (message.equals("")) Log.e(TAG, "reply without message: maleformed json");
        return this.isReply;
    }

    public boolean isDismiss() {
        return this.isDismiss;
    }

    public String getKey() {
        return this.key;
    }

    public int getID() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getActionName() {
        return this.actionName;
    }
}