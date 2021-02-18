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


    public NetworkPackage(String bufferedLine) {
        try {
            JSONObject json = new JSONObject(bufferedLine);
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
    }

    public void log() {
        Log.d(TAG, "Networkpackage:\n" +
                "\nid     :" + this.id +
                "\nkey    :" + this.key +
                "\nmessage:" + this.message +
                "\nisAct  :" + this.isAction +
                "\nisReply:" + this.isReply +
                "\nisDis  :" + this.isDismiss);
    }

    public String getString(JSONObject json, String jsonKey) {
        try {
            return (String) json.get(jsonKey);
        } catch (JSONException j) {
            Log.e(TAG, "key entry missing: " + jsonKey);
            return "";
        }
    }

    public boolean getBoolean(JSONObject json, String jsonKey) {
        try {
            return Boolean.parseBoolean((String)json.get(jsonKey));
        } catch (JSONException j) {
            Log.e(TAG, "key entry missing: " + jsonKey);
            return false;
        }
    }

    public boolean isAction() {
        return this.isAction;
    }

    public boolean isReply() {
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