package com.perchlive.broadcast.sdk.api.s3;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;

import timber.log.Timber;

/**
 * An S3 Uploader that allows queueing uploads before credentials are available.
 * Created by dbro on 12/16/15.
 */
public class S3QueuedUploader implements TransferListener {

    @NonNull
    private final Context context;
    private HashMap<Integer, ActiveUpload> activeUploadMap = new HashMap<>();
    private UploadListener listener;
    private ArrayDeque<QueuedUpload> mUploadQueue = new ArrayDeque<>();
    private AWSCredentials  credentials;
    private AmazonS3        s3;
    private TransferUtility transferUtility;
    private String          bucket;
    private String          bucketPath;

    public S3QueuedUploader(@NonNull Context context) {
        this.context = context;
    }

    public void setUploadListener(UploadListener listener) {
        this.listener = listener;
    }

    public synchronized void provideCredentials(@NonNull String bucket,
                                                @NonNull String bucketPath,
                                                @NonNull String awsKey,
                                                @NonNull String awsSecret) {

        if (hasCredentials()) {
            Timber.w("Credentials already provided");
            return;
        }

        this.bucket = bucket;
        this.bucketPath = bucketPath;
        this.credentials = new BasicAWSCredentials(awsKey, awsSecret);
        this.s3 = new AmazonS3Client(credentials);
        this.transferUtility = new TransferUtility(s3, context);

        executeQueuedUploads();
    }

    public synchronized void queueUpload(@NonNull File file,
                                         @NonNull String key,
                                         ObjectMetadata objectMetadata) {

        if (hasCredentials()) {
            uploadFile(key, file, objectMetadata);
        } else {
            mUploadQueue.add(new QueuedUpload(key, file, objectMetadata));
        }
    }

    public boolean hasCredentials() {
        return credentials != null;
    }

    /**
     * @param key      the S3 Key. Note that the value provided as 'bucketPath' in
     *                 {@link #provideCredentials(String, String, String, String)}
     *                 will be prepended to this value
     * @param contents the file contents to upload
     * @param metadata any additional metadata, like caching headers
     */
    private void uploadFile(@NonNull String key,
                            @NonNull File contents,
                            @NonNull ObjectMetadata metadata) {
        String absoluteKey = new File(bucketPath, key).getPath();
        TransferObserver observer = transferUtility.upload(
                bucket,
                absoluteKey,
                contents,
                metadata
        );
        // TODO: destination URL via S3 SDK?
        String destinationUrl = String.format("https://s3.amazonaws.com/%s/%s", bucket, absoluteKey);
        activeUploadMap.put(observer.getId(), new ActiveUpload(contents, destinationUrl));
        observer.setTransferListener(this);
    }

    private void executeQueuedUploads() {
        if (!hasCredentials()) return;

        for (QueuedUpload queuedUpload : mUploadQueue) {
            uploadFile(queuedUpload.key, queuedUpload.file, queuedUpload.metadata);
        }
    }

    // <editor-fold desc="TransferListener">

    @Override
    public void onStateChanged(int id, TransferState state) {
        Timber.d("Transfer %d state %s", id, state.name());

        if (listener == null) return;

        ActiveUpload upload = activeUploadMap.get(id);

        if (upload == null) {
            Timber.w("No ActiveUpload associated with transfer id %d. Cannot notify listener", id);
            return;
        }

        switch (state) {
            case IN_PROGRESS:
                Timber.d("Marking ActiveUpload start time");
                upload.markStartTime();
                break;

            case COMPLETED:
                int bytesPerSecond = (int) (upload.file.length() / (System.currentTimeMillis() - upload.startTimeMs));
                listener.onUploadComplete(upload.file, upload.destinationUrl, bytesPerSecond);
                activeUploadMap.remove(id);
                break;

            case CANCELED:
                listener.onUploadError(upload.file, upload.destinationUrl, new Exception("Transfer cancelled"));
                activeUploadMap.remove(id);
                break;

            case FAILED:
                listener.onUploadError(upload.file, upload.destinationUrl, new Exception("Transfer failed"));
                activeUploadMap.remove(id);
                break;
        }
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        Timber.d("Transfer %d progress %f", id, ((float) bytesCurrent) / bytesTotal);
    }

    @Override
    public void onError(int id, Exception ex) {
        Timber.e(ex, "Error %d", id);
    }

    // </editor-fold desc="TransferListener">

    private class QueuedUpload {
        @NonNull
        public final String key;

        @NonNull
        public final File file;

        @NonNull
        public final ObjectMetadata metadata;

        public QueuedUpload(@NonNull String key,
                            @NonNull File file,
                            ObjectMetadata metadata) {
            this.key = key;
            this.file = file;

            if (metadata == null) {
                this.metadata = new ObjectMetadata();
            } else {
                this.metadata = metadata;
            }
        }
    }

    /**
     * Used internally to map S3 Transfer Ids to the upload data required by
     * {@link com.perchlive.broadcast.sdk.api.s3.S3QueuedUploader.UploadListener}
     */
    private class ActiveUpload {

        @NonNull
        public final File file;

        @NonNull
        public final String destinationUrl;

        public long startTimeMs;

        public ActiveUpload(@NonNull File file,
                            @NonNull String destinationUrl) {
            this.file = file;
            this.destinationUrl = destinationUrl;
        }

        public void markStartTime() {
            if (startTimeMs != 0) {
                Timber.w("Ignoring duplicate request to set ActiveUpload start time.");
                return;
            }
            startTimeMs = System.currentTimeMillis();
        }
    }

    public interface UploadListener {
        void onUploadComplete(@NonNull File file, @NonNull String destinationUrl, int realizedBytesPerSecond);

        void onUploadError(@NonNull File file, @NonNull String destinationUrl, @NonNull Throwable error);
    }
}
