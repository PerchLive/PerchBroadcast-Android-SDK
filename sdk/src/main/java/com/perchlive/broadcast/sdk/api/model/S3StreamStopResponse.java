package pro.dbro.perchbroadcast.sdk.api.model;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by dbro on 10/29/15.
 */
public class S3StreamStopResponse {

    @NonNull public final String id;
    @NonNull public final String name;
    @NonNull public final Date   startDate;
    @NonNull public final Date   stopDate;


    public S3StreamStopResponse(@NonNull String id,
                                @NonNull String name,
                                @NonNull Date startDate,
                                @NonNull Date stopDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.stopDate = stopDate;
    }
}
