package com.perchlive.broadcast.example;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.perchlive.broadcast.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dbro on 10/29/15.
 */
public class PermissionsHelper {

    public final Activity hostActivity;
    public final int      permissionsRequestCode;
    public final String[] requiredPermissions;
    public final View     snackBarAnchorView;

    public PermissionsHelper(@NonNull Activity activity,
                             int permissionsRequestCode,
                             @NonNull String[] requiredPermissions,
                             @NonNull View snackBarAnchorView) {

        this.hostActivity = activity;
        this.permissionsRequestCode = permissionsRequestCode;
        this.requiredPermissions = requiredPermissions;
        this.snackBarAnchorView = snackBarAnchorView;
    }

    /**
     * @return true if all required permissions are granted. If result is false, await permission
     * grant on {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     */
    public boolean obtainPermissions() {
        if (PermissionsHelper.hasPermissions(hostActivity, requiredPermissions)) return true;

        final String[] permissionsRequiringRationale
                = PermissionsHelper.shouldShowPermissionRationale(hostActivity, requiredPermissions);

        if (permissionsRequiringRationale.length > 0) {
            // Repeat permission request, show some rationale before showing system permission propmt

            Snackbar.make(snackBarAnchorView, "Camera and Microphone permissions are required to record.",
                    Snackbar.LENGTH_INDEFINITE).setAction("Grant", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(hostActivity,
                            permissionsRequiringRationale,
                            permissionsRequestCode);
                }
            }).show();

        } else {
            // First time permissions request. Ask straight-up
            ActivityCompat.requestPermissions(hostActivity,
                    requiredPermissions,
                    permissionsRequestCode);
        }
        return false;
    }

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
