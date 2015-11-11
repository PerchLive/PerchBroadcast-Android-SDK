package com.perchlive.broadcast.sdk.api.model;

import android.support.annotation.NonNull;

/**
 * Created by dbro on 10/29/15.
 */
public class S3Endpoint {

    @NonNull public final String awsAccessKeyId;
    @NonNull public final String awsSecretAccessKey;
    @NonNull public final String awsSessionToken;
    @NonNull public final String awsBucketName;
    @NonNull public final String awsBucketPath;

    public final int awsExpiration;

    public S3Endpoint(@NonNull String awsAccessKeyId,
                      @NonNull String awsSecretAccessKey,
                      @NonNull String awsSessionToken,
                      int awsExpiration,
                      @NonNull String awsBucketName,
                      @NonNull String awsBucketPath,
                      @NonNull String awsRegion) {

        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.awsSessionToken = awsSessionToken;
        this.awsExpiration = awsExpiration;
        this.awsBucketName = awsBucketName;
        this.awsBucketPath = awsBucketPath;
        this.awsRegion = awsRegion;
    }

    public final String awsRegion;
}
