package com.perchlive.broadcast.example;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dbro on 10/29/15.
 */
public class PermissionsHelper {

    public static boolean hasPermission(@NonNull Activity host,
                                        @NonNull String permission) {
        return ActivityCompat.checkSelfPermission(host, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermissions(@NonNull Activity host,
                                         @NonNull String[] permissions) {

        for (String permission : permissions) {
            if (!hasPermission(host, permission)) return false;
        }
        return true;
    }

    public static String[] shouldShowPermissionRationale(@NonNull Activity host,
                                                         @NonNull String[] permissions) {

        List<String> rationalePerms = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(host, permission)) {
                rationalePerms.add(permission);
            }
        }

        String[] rationalePermsArray = new String[rationalePerms.size()];
        rationalePerms.toArray(rationalePermsArray);
        return rationalePermsArray;
    }

    public static boolean allPermissionsGranted(@NonNull int[] grantResults) {

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
}
