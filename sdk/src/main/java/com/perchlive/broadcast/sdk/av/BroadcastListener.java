package com.perchlive.broadcast.sdk.av;

import android.support.annotation.NonNull;

import com.perchlive.broadcast.sdk.api.model.Stream;

/**
 * Provides callbacks for the major lifecycle benchmarks of a Broadcast.
 */
public interface BroadcastListener {
    /**
     * The broadcast has started, and is currently buffering.
     */
    void onBroadcastStart();

    /**
     * The broadcast is fully buffered and available. This is a good time to share the broadcast.
     *
     * @param stream the {@link Stream} representing this broadcast.
     */
    void onBroadcastLive(@NonNull Stream stream,
                         @NonNull String destinationUrl);

    /**
     * The broadcast has ended.
     */
    void onBroadcastStop();

    /**
     * An error occurred.
     */
    void onBroadcastError(@NonNull Throwable error);
}
