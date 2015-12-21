package com.perchlive.broadcast.sdk.api.model;

import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Created by dbro on 10/29/15.
 */
public class S3StreamStartResponse extends StartStreamResponse {

    public static final String ENDPOINT_KEY_S3 = "S3";

    @NonNull private final Map<String, S3Endpoint> endpoint;

    public S3StreamStartResponse(@NonNull Stream stream,
                                 @NonNull Map<String, S3Endpoint> endpoint) {
        super(stream);
        this.endpoint = endpoint;
    }

    public S3Endpoint getEndpoint() {
        return endpoint.get(ENDPOINT_KEY_S3);
    }
}
