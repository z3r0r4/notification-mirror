package com.r4.notifications.mirror;

import android.content.Context;
import android.widget.Toast;

/**
 * @since 20210719
 * "what is my purpose"
 *  you make Toast
 * "oh..."
 */

class Helper {
    protected static void toasted(String text){
        Toast.makeText(MainActivity.sContext, text, Toast.LENGTH_LONG).show();
    }
}
