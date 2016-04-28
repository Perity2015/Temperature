package com.huiwu.temperaturecontrol.bean;

import android.util.Log;

/**
 * Created by HuiWu on 2016/4/18.
 */
public class TLog {
    public static boolean debug = true;

    public static void d(String tag, String value) {
        if (debug)
            Log.d(tag, value);
    }
}
