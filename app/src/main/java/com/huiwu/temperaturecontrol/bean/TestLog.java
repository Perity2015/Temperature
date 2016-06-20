package com.huiwu.temperaturecontrol.bean;

import android.util.Log;

/**
 * Created by HuiWu on 2016/4/18.
 */
public class TestLog {
    public static boolean debug = true;

    public static void d(String tag, String value) {
        if (debug)
            Log.d(tag, value);
    }

    public static void i(String tag, String value) {
        if (debug)
            Log.i(tag, value);
    }


    public static void e(String tag, String value) {
        if (debug)
            Log.e(tag, value);
    }

    public static void w(String tag, String value) {
        if (debug)
            Log.w(tag, value);
    }
}
