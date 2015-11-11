package com.perchlive.broadcast.sdk.api;

import com.perchlive.broadcast.sdk.api.model.S3StreamStartResponse;
import com.perchlive.broadcast.sdk.api.model.S3StreamStopResponse;
import com.perchlive.broadcast.sdk.api.model.json.GsonHelper;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Client describing interaction with Django-Broadcast API
 * Created by dbro on 10/29/15.
 */
public class BroadcastApi {

    private interface Api {

        @POST("/stream/start/")
        S3StreamStartResponse startStream(@Query("name") String name);

        @POST("/stream/stop/")
        S3StreamStopResponse stopStream(@Query("id") String id);
    }

    public static Api getApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://django-broadcast.herokuapps.com")
                .addConverterFactory(GsonConverterFactory.create(GsonHelper.getGson()))
                .build();

        return retrofit.create(Api.class);
    }
}
