package com.neal.adpannel.util;

import android.util.Log;

/**
 * 可控日志
 * Created by lichao on 17/5/2.
 */

public class Logs {

    public static void e(String tag, String message) {
        if (Cons.DEBUG)
            Log.e(tag, message);
    }

    public static void i(String tag, String message) {
        if (Cons.DEBUG)
            Log.i(tag, message);
    }

    public static void w(String tag, String message) {
        if (Cons.DEBUG)
            Log.w(tag, message);
    }

    public static void d(String tag, String message) {
        if (Cons.DEBUG)
            Log.d(tag, message);
    }

}
