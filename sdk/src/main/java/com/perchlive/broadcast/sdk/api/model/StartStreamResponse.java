package com.perchlive.broadcast.sdk.api.model;

import android.support.annotation.NonNull;

/**
 * Created by dbro on 10/29/15.
 */
public abstract class StartStreamResponse {

    @NonNull public final Stream stream;

    public StartStreamResponse(@NonNull Stream stream) {
        this.stream = stream;
    }
}
