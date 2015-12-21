package com.perchlive.broadcast.sdk.api;

import com.perchlive.broadcast.sdk.api.model.S3Endpoint;
import com.perchlive.broadcast.sdk.api.model.S3StreamStartResponse;
import com.perchlive.broadcast.sdk.api.model.S3StreamStopResponse;
import com.perchlive.broadcast.sdk.api.model.Stream;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.http.Query;

/**
 * Created by dbro on 12/21/15.
 */
public class MockBroadcastApi implements BroadcastApi {

    @Override
    public Call<S3StreamStartResponse> startStream(@Query("name") String name) {
        Stream mockStream = new Stream("0", name, new Date());
        // TODO : Pull in mock AWS credentials, perhaps via gradle?
        S3Endpoint mockS3Endpoint = new S3Endpoint(
                "aws_key_id",
                "aws_secret_key",
                "aws_session_token",
                3600,
                "mock_bucket",
                "mock",
                "us-west-1");
        HashMap<String, S3Endpoint> mockEndpoints = new HashMap<>();
        mockEndpoints.put(S3StreamStartResponse.ENDPOINT_KEY_S3, mockS3Endpoint);
        final S3StreamStartResponse mockStartResponse = new S3StreamStartResponse(mockStream, mockEndpoints);

        return new Call<S3StreamStartResponse>() {
            @Override
            public Response<S3StreamStartResponse> execute() throws IOException {
                return Response.success(mockStartResponse);
            }

            @Override
            public void enqueue(Callback<S3StreamStartResponse> callback) {
                callback.onResponse(Response.success(mockStartResponse), null);
            }

            @Override
            public void cancel() {
                // do nothing
            }

            @Override
            public Call<S3StreamStartResponse> clone() {
                return this;
            }
        };
    }

    @Override
    public Call<S3StreamStopResponse> stopStream(@Query("id") String id) {
        final S3StreamStopResponse mockStopResponse = new S3StreamStopResponse(
                id,
                "mock_name",
                new Date(System.currentTimeMillis() - 5 * 60 * 1000),
                new Date());

        return new Call<S3StreamStopResponse>() {
            @Override
            public Response<S3StreamStopResponse> execute() throws IOException {
                return Response.success(mockStopResponse);
            }

            @Override
            public void enqueue(Callback<S3StreamStopResponse> callback) {
                callback.onResponse(Response.success(mockStopResponse), null);
            }

            @Override
            public void cancel() {
                // do nothing
            }

            @Override
            public Call<S3StreamStopResponse> clone() {
                return this;
            }
        };
    }
}
