package com.perchlive.broadcast.sdk;

import android.os.Build;

/**
 * Created by dbro on 10/29/15.
 */
public class AndroidUtil {

    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
