package com.perchlive.broadcast.example;

import android.app.Application;

import com.perchlive.broadcast.BuildConfig;

import timber.log.Timber;

/**
 * Created by dbro on 12/18/15.
 */
public class BroadcastExampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // If we abandon Timber logging in this app, enable below line
        // to enable Timber logging in sdk
        //Logging.forceLogging();
    }
}
