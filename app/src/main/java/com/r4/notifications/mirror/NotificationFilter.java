package com.r4.notifications.mirror;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NotificationFilter {
    private static final String TAG = "nm.NotificationFilter";

    private static final List<String> filteredTags = new ArrayList<String>();
    private static final List<String> filteredPackageNames = new ArrayList<String>();
    private static final List<Integer> filteredFlags  = new ArrayList<Integer>();

    public static void loadFilter(Context context) {
        String jsonformattedstring = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("filter.json")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                jsonformattedstring = jsonformattedstring.concat(mLine);
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        Log.d(TAG, jsonformattedstring);

        try {
            JSONObject json = new JSONObject(jsonformattedstring);

            //add the blacklisted package names
            JSONArray packageNames = json.getJSONArray("package-names");
            for(int i = 0; i < packageNames.length(); i++) {
                String blacklistedPackageName = packageNames.getString(i);
                if(blacklistedPackageName != null && !blacklistedPackageName.isEmpty()) {
                    filteredPackageNames.add(blacklistedPackageName);
                }
            }

            //add the blacklisted tags
            JSONArray tags = json.getJSONArray("tags");
            for(int i = 0; i < tags.length(); i++) {
                String blacklistedTag = tags.getString(i);
                if(blacklistedTag != null && !blacklistedTag.isEmpty()) {
                    filteredTags.add(blacklistedTag);
                }
            }

            //add the necessary Flags
            JSONArray flags = json.getJSONArray("flags");
            for(int i = 0; i < flags.length(); i++) {
                int necessaryFlag = flags.getInt(i);
                filteredFlags.add(necessaryFlag);
            }

            Log.d(TAG, packageNames.toString());
            Log.d(TAG, tags.toString());
            Log.d(TAG, flags.toString());

        } catch(JSONException e) {
            Log.e(TAG, "could not parse notification filter!");
        }
    }

    /**
     * checks if the notification is one that is sensible to store
     * excludes charging state updates, low battery warnings and mobile data warnings
     *
     * @param sbn notification to be checked
     * @return if the notification may pass the filter
     */
    public static boolean isBlacklisted(StatusBarNotification sbn) {
        if(!containsNecessaryFlag(sbn.getNotification().flags)) {
            return true;
        }

        if(containsBlacklistedTag(sbn.getTag())) {
            return true;
        }

        if(containsBlacklistedPackageName(sbn.getPackageName())) {
            return true;
        }

        return false;
    }

    private static boolean containsNecessaryFlag(int flags) {
        for(int flag : filteredFlags) {
            if((flags & flag) == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsBlacklistedTag(String tag) {
        if(tag == null) return false;

        for(String blacklistedTag : filteredTags) {
            if(tag.contains(blacklistedTag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsBlacklistedPackageName(String packageName) {
        if(packageName == null) return false;

        for(String blacklistedPackageName : filteredPackageNames) {
            if(packageName.contains(blacklistedPackageName)) {
                return true;
            }
        }
        return false;
    }
}
