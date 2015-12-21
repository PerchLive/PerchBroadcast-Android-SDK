package com.perchlive.broadcast.example;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.perchlive.broadcast.R;
import com.perchlive.broadcast.sdk.activity.ImmersiveActivity;
import com.perchlive.broadcast.sdk.fragment.AVRecorderFragment;

import timber.log.Timber;


public class CameraActivity extends ImmersiveActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private PermissionsHelper permissionsHelper;

    private static final int REQUEST_CODE_PERMISSION = 0;

    protected final String[] REQUIRED_PERMISSIONS
            = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        permissionsHelper = new PermissionsHelper(
                this,
                REQUEST_CODE_PERMISSION,
                REQUIRED_PERMISSIONS,
                findViewById(R.id.content));

        // If obtainPermissions returns false, await result in onRequestPermissionsResult
        if (permissionsHelper.obtainPermissions()) {
            showContentFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSION) {
            // Request for camera permission.
            if (PermissionsHelper.allPermissionsGranted(grantResults)) {
                showContentFragment();
            } else {
                // Permission request was denied. Give user the cold shoulder
                // TODO : Behave yourself
                finish();
            }
        }
    }

    protected void showContentFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new AVRecorderFragment())
                .commit();
    }
}
