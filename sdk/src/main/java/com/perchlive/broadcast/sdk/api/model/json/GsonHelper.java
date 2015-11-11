package com.perchlive.broadcast.sdk.api.model.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import com.perchlive.broadcast.sdk.api.model.typeadapter.StreamDateTypeAdapter;

/**
 * Created by dbro on 10/29/15.
 */
public class GsonHelper {

    public static Gson getGson() {
        GsonBuilder gson = new GsonBuilder();
        gson.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gson.registerTypeAdapter(Date.class, new StreamDateTypeAdapter());
        return gson.create();
    }
}
