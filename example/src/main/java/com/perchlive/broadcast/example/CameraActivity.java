package com.perchlive.broadcast.example;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.perchlive.broadcast.R;
import com.perchlive.broadcast.sdk.activity.ImmersiveActivity;
import com.perchlive.broadcast.sdk.fragment.AVRecoderFragment;


public class CameraActivity extends ImmersiveActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CODE_CAMERA_PERMISSION = 0;

    private static final String[] REQUIRED_PERMISSIONS
            = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (PermissionsHelper.hasPermissions(this, REQUIRED_PERMISSIONS)) {

            showAVRecorderFragment();

        } else {

            final String[] permissionsRequiringRationale
                    = PermissionsHelper.shouldShowPermissionRationale(this, REQUIRED_PERMISSIONS);

            if (permissionsRequiringRationale.length > 0) {
                // Repeat permission request, show some rationale before showing system permission propmt

                Snackbar.make(findViewById(R.id.content), "Camera and Microphone permissions are required to record.",
                        Snackbar.LENGTH_INDEFINITE).setAction("Grant", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(CameraActivity.this,
                                permissionsRequiringRationale,
                                REQUEST_CODE_CAMERA_PERMISSION);
                    }
                }).show();

            } else {
                // First time permissions request. Ask straight-up
                ActivityCompat.requestPermissions(CameraActivity.this,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_CAMERA_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            // Request for camera permission.
            if (PermissionsHelper.allPermissionsGranted(grantResults)) {
                showAVRecorderFragment();
            } else {
                // Permission request was denied. Give user the cold shoulder
                // TODO : Behave yourself
                finish();
            }
        }
    }

    private void showAVRecorderFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new AVRecoderFragment())
                .commit();
    }
}
