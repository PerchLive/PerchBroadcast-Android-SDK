package pro.dbro.perchbroadcast.sdk.api.model;

import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Created by dbro on 10/29/15.
 */
public class S3StreamStartResponse extends StartStreamResponse {

    @NonNull public final Map<String, S3Endpoint> endpoint;

    public S3StreamStartResponse(@NonNull Stream stream,
                                 @NonNull Map<String, S3Endpoint> endpoint) {
        super(stream);
        this.endpoint = endpoint;
    }
}
