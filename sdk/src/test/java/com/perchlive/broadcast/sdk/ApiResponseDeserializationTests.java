package com.perchlive.broadcast;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.Date;

import com.perchlive.broadcast.sdk.api.model.S3Endpoint;
import com.perchlive.broadcast.sdk.api.model.S3StreamStartResponse;
import com.perchlive.broadcast.sdk.api.model.S3StreamStopResponse;
import com.perchlive.broadcast.sdk.api.model.json.GsonHelper;
import com.perchlive.broadcast.sdk.api.model.typeadapter.StreamDateTypeAdapter;

import static org.junit.Assert.*;

/**
 * Test deserialization of Django-Broadcast API JSON responses.
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ApiResponseDeserializationTests {

    // Stream attributes
    private static final String STREAM_ID   = "2930rdfi";
    private static final String STREAM_NAME = "skant4whiug";
    private static final String START_DATE  = "2015-10-22 15:27:40";
    private static final String STOP_DATE   = "2015-10-22 16:27:40";

    // Endpoint attributes
    private static final String ENDPOINT_AWS_ACCESS_KEY_ID     = "pwudfgreth";
    private static final String ENDPOINT_AWS_SECRET_ACCESS_KEY = "mnbvcxzssdfgbhbn";
    private static final String ENDPOINT_AWS_SESSION_TOKEN     = "sadlkfj3w98fjoei";
    private static final int    ENDPOINT_AWS_EXPIRATION        = 96000;
    private static final String ENDPOINT_AWS_BUCKET_NAME       = "mr_bucket";
    private static final String ENDPOINT_AWS_BUCKET_PATH       = "a/path/too/far";
    private static final String ENDPOINT_AWS_REGION            = "us-west-1";

    private static final String STREAM_START_RESPONSE =
            "{" +
                    "    \"stream\" : {" +
                    "        \"id\" : \"" + STREAM_ID + "\"," +
                    "        \"name\" : \"" + STREAM_NAME + "\"," +
                    "        \"start_date\" : \"" + START_DATE + "\"" +
                    "    }," +
                    "    \"endpoint\": {" +
                    "        \"S3\": {" +
                    "            \"aws_access_key_id\": \"" + ENDPOINT_AWS_ACCESS_KEY_ID + "\"," +
                    "            \"aws_secret_access_key\": \"" + ENDPOINT_AWS_SECRET_ACCESS_KEY + "\"," +
                    "            \"aws_session_token\": \"" + ENDPOINT_AWS_SESSION_TOKEN + "\"," +
                    "            \"aws_expiration\": " + ENDPOINT_AWS_EXPIRATION + "," +
                    "            \"aws_bucket_name\": \"" + ENDPOINT_AWS_BUCKET_NAME + "\"," +
                    "            \"aws_bucket_path\": \"" + ENDPOINT_AWS_BUCKET_PATH + "\"," +
                    "            \"aws_region\": \"" + ENDPOINT_AWS_REGION + "\"" +
                    "        }" +
                    "    }" +
                    "}";

    private static final String STREAM_STOP_RESPONSE =
            "{" +
                    "   \"id\" : \"" + STREAM_ID + "\"," +
                    "   \"name\" : \"" + STREAM_NAME + "\"," +
                    "   \"start_date\" : \"" + START_DATE + "\"," +
                    "   \"stop_date\" : \"" + STOP_DATE + "\"" +
                    "}";

    @Test
    public void parseS3StreamStartResponse() throws Exception {

        Gson gson = GsonHelper.getGson();

        S3StreamStartResponse response = gson.fromJson(STREAM_START_RESPONSE,
                S3StreamStartResponse.class);

        // Assert Stream attributes
        assertEquals(response.stream.id, STREAM_ID);
        assertEquals(response.stream.name, STREAM_NAME);

        Date startDate = StreamDateTypeAdapter.iso8601Format.parse(START_DATE);
        assertEquals(response.stream.startDate, startDate);

        // Assert Endpoint attributes
        S3Endpoint deserializedS3Endpoint = response.endpoint.get("S3");
        assertEquals(deserializedS3Endpoint.awsAccessKeyId, ENDPOINT_AWS_ACCESS_KEY_ID);
        assertEquals(deserializedS3Endpoint.awsSecretAccessKey, ENDPOINT_AWS_SECRET_ACCESS_KEY);
        assertEquals(deserializedS3Endpoint.awsSessionToken, ENDPOINT_AWS_SESSION_TOKEN);
        assertEquals(deserializedS3Endpoint.awsExpiration, ENDPOINT_AWS_EXPIRATION);
        assertEquals(deserializedS3Endpoint.awsBucketName, ENDPOINT_AWS_BUCKET_NAME);
        assertEquals(deserializedS3Endpoint.awsBucketPath, ENDPOINT_AWS_BUCKET_PATH);
        assertEquals(deserializedS3Endpoint.awsRegion, ENDPOINT_AWS_REGION);
    }

    @Test
    public void parseS3StreamStopResponse() throws Exception {

        Gson gson = GsonHelper.getGson();

        S3StreamStopResponse response = gson.fromJson(STREAM_STOP_RESPONSE,
                S3StreamStopResponse.class);

        // Assert Stream attributes
        assertEquals(response.id, STREAM_ID);
        assertEquals(response.name, STREAM_NAME);

        Date startDate = StreamDateTypeAdapter.iso8601Format.parse(START_DATE);
        assertEquals(response.startDate, startDate);
        Date stopDate = StreamDateTypeAdapter.iso8601Format.parse(STOP_DATE);
        assertEquals(response.stopDate, stopDate);
    }
}
