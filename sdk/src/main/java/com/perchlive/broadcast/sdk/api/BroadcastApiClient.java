package com.perchlive.broadcast.sdk.api;

import android.support.annotation.NonNull;

import com.perchlive.broadcast.sdk.BuildConfig;
import com.perchlive.broadcast.sdk.api.model.S3StreamStartResponse;
import com.perchlive.broadcast.sdk.api.model.S3StreamStopResponse;
import com.perchlive.broadcast.sdk.api.model.json.GsonHelper;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Client describing interaction with Django-Broadcast API
 * Created by dbro on 10/29/15.
 */
public class BroadcastApiClient {

    private BroadcastApi api;

    public BroadcastApiClient(@NonNull String rootUrl) {
        if (BuildConfig.MOCK) {
            api = new MockBroadcastApi();
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(rootUrl) // "https://django-broadcast.herokuapps.com"
                    .addConverterFactory(GsonConverterFactory.create(GsonHelper.getGson()))
                    .build();

            api = retrofit.create(BroadcastApi.class);
        }
    }

    public void startStream(@NonNull String name,
                            @NonNull Callback<S3StreamStartResponse> callback) {

        api.startStream(name).enqueue(callback);
    }

    public void stopStream(@NonNull String id,
                           @NonNull Callback<S3StreamStopResponse> callback) {

        api.stopStream(id).enqueue(callback);
    }
}
