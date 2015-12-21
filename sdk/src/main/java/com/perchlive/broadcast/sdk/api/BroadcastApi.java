package com.perchlive.broadcast.sdk.api;

import com.perchlive.broadcast.sdk.api.model.S3StreamStartResponse;
import com.perchlive.broadcast.sdk.api.model.S3StreamStopResponse;

import retrofit.Call;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * API Defintion
 */
interface BroadcastApi {

    @POST("/stream/start/")
    Call<S3StreamStartResponse> startStream(@Query("name") String name);

    @POST("/stream/stop/")
    Call<S3StreamStopResponse> stopStream(@Query("id") String id);
}