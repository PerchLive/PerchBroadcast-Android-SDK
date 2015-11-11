package com.perchlive.broadcast.sdk.api.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

/**
 * Broadcast Stream
 * Created by dbro on 10/29/15.
 */
public class Stream implements Comparable<Stream>, Serializable {

    @NonNull public final String id;
    @NonNull public final String name;
    @NonNull public final Date   startDate;

    public Stream(@NonNull String id,
                  @NonNull String name,
                  @NonNull Date startDate) {

        this.id = id;
        this.name = name;
        this.startDate = startDate;
    }

    @Override
    public int compareTo(@NonNull Stream another) {
        return another.startDate.compareTo(startDate);
    }
}
