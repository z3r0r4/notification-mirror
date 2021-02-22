package com.r4.notifications.mirror;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton instance for managing shared resources
 */
public class SharedResourceManager {
    private static final String TAG = "nm.SharedResourceManager";
    //singleton instance
    private static SharedResourceManager instance;

    //member variables
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private SharedResourceManager(Context context, String name, int mode) {
        this.sharedPreferences = context.getSharedPreferences(name, mode);
        this.editor = sharedPreferences.edit();
    }

    public static void initializeInstance(Context context, String name, int mode) {
        instance = new SharedResourceManager(context, name, mode);
    }

    public static SharedResourceManager getInstance() {
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return this.sharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return this.editor;
    }

}
