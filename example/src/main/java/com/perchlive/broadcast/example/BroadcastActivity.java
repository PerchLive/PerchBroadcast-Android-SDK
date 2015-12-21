package com.perchlive.broadcast.example;

import android.support.v4.app.ActivityCompat;

import com.perchlive.broadcast.R;
import com.perchlive.broadcast.sdk.fragment.BroadcastFragment;

public class BroadcastActivity extends CameraActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected void showContentFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new BroadcastFragment())
                .commit();
    }
}
