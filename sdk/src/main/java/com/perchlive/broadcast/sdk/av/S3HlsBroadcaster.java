package com.perchlive.broadcast.sdk.av;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.perchlive.broadcast.sdk.FileUtils;
import com.perchlive.broadcast.sdk.api.BroadcastApiClient;
import com.perchlive.broadcast.sdk.api.model.S3Endpoint;
import com.perchlive.broadcast.sdk.api.model.S3StreamStartResponse;
import com.perchlive.broadcast.sdk.api.model.S3StreamStopResponse;
import com.perchlive.broadcast.sdk.api.s3.S3QueuedUploader;
import com.perchlive.broadcast.sdk.view.GLCameraView;

import java.io.File;
import java.io.IOException;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

import static com.perchlive.broadcast.sdk.AndroidUtil.isKitKat;

public class S3HlsBroadcaster extends AVRecorder implements HlsFileObserver.HlsFileObserverCallback, S3QueuedUploader.UploadListener {

    private static final String VOD_FILENAME = "vod.m3u8";
    private static final int    MIN_BITRATE  = 3 * 100 * 1000;        // 300 kbps

    private Context               mContext;
    private BroadcastListener     mListener;
    private S3QueuedUploader      mS3Uploader;
    private BroadcastApiClient    mApiClient;
    private S3StreamStartResponse mS3StartStreamResponse;
    private HlsFileObserver       mFileObserver;
    private File                  mManifestSnapshotDir;               // Directory where manifest snapshots are stored
    private File                  mVodManifest;                       // VOD HLS Manifest containing complete history
    private int                   mVideoBitrate;
    private int                   mNumSegmentsWritten;
    private int                   mLastRealizedBandwidthBytesPerSecond;  // Bandwidth snapshot for adapting bitrate
    private boolean               mSentBroadcastLiveEvent;

    public S3HlsBroadcaster(@NonNull Context context,
                            @NonNull SessionConfig sessionConfig,
                            @NonNull String apiEndpointUrl) throws IOException {

        super(sessionConfig);

        mContext = context;
        mS3Uploader = new S3QueuedUploader(context);
        mS3Uploader.setUploadListener(this);
        mApiClient = new BroadcastApiClient(apiEndpointUrl);

        mManifestSnapshotDir = new File(sessionConfig.getOutputPath().substring(0,
                sessionConfig.getOutputPath().lastIndexOf("/") + 1), "m3u8");
        mManifestSnapshotDir.mkdir();
        mVodManifest = new File(mManifestSnapshotDir, VOD_FILENAME);
        writeEventManifestHeader(sessionConfig.getHlsSegmentDuration());

        String watchDir = sessionConfig.getOutputDirectory().getAbsolutePath();
        mFileObserver = new HlsFileObserver(watchDir, this);
        mFileObserver.startWatching();
    }

    private void writeEventManifestHeader(int targetDuration) {
        FileUtils.writeStringToFileAsync(
                String.format(
                        "#EXTM3U\n" +
                                "#EXT-X-PLAYLIST-TYPE:VOD\n" +
                                "#EXT-X-VERSION:3\n" +
                                "#EXT-X-MEDIA-SEQUENCE:0\n" +
                                "#EXT-X-TARGETDURATION:%d\n",
                        targetDuration + 1),
                mVodManifest,
                false,
                null
        );
    }

    /**
     * Start broadcasting.
     * <p>
     * Must be called after {@link #setPreviewDisplay(GLCameraView)}
     */
    @Override
    public void startRecording() {
        super.startRecording();

        // TODO : Allow setting stream name
        mApiClient.startStream(mConfig.getUUID().toString(),
                new Callback<S3StreamStartResponse>() {
                    @Override
                    public void onResponse(Response<S3StreamStartResponse> response, Retrofit retrofit) {
                        if (!response.isSuccess()) handleFailedStreamStart();

                        handleStreamStartResponse(response.body());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Timber.e(t, "Failed to send start stream request");
                        handleFailedStreamStart();
                    }
                });

        mCamEncoder.requestThumbnailOnDeltaFrameWithScaling(10, 1);

    }

    /**
     * Stop broadcasting and release resources.
     * After this call this Broadcaster can no longer be used.
     */
    @Override
    public void stopRecording() {
        super.stopRecording();
        if (mS3StartStreamResponse != null) {
            Timber.d("Stopping Stream");
            mApiClient.stopStream(mS3StartStreamResponse.stream.id, new Callback<S3StreamStopResponse>() {
                @Override
                public void onResponse(Response<S3StreamStopResponse> response, Retrofit retrofit) {
                    Timber.d("Got stop stream response");
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.e(t, "Failed to send stop stream request");
                }
            });
        }
    }

    /**
     * Set a Listener to be notified of basic Broadcast events relevant to
     * updating a broadcasting UI.
     * e.g: Broadcast begun, went live, stopped, or encountered an error.
     */
    public void setBroadcastListener(BroadcastListener listener) {
        mListener = listener;
    }

    /**
     * @return whether this Broadcaster is Live. Currently, this means that at least one
     * video segment was successfully uploaded and that {@link #stopRecording()} has not been called.
     */
    public boolean isLive() {
        return mSentBroadcastLiveEvent;
    }

    private void handleFailedStreamStart() {
        stopRecording();
        if (mListener != null) {
            mListener.onBroadcastError(new Exception("Failed to retrieve start stream response"));
        }
    }

    private void handleStreamStartResponse(@NonNull S3StreamStartResponse response) {
        mS3StartStreamResponse = response;
        if (mConfig.shouldAttachLocation()) {
            // TODO: Is Location within this SDK's scope?
        }
        // TODO : Allow setting S3 Region
        S3Endpoint s3Endpoint = response.getEndpoint();
        mS3Uploader.provideCredentials(
                s3Endpoint.awsBucketName,
                s3Endpoint.awsBucketPath,
                s3Endpoint.awsAccessKeyId,
                s3Endpoint.awsSecretAccessKey,
                s3Endpoint.awsSessionToken);
//        mReadyToBroadcast = true;
        if (mListener != null) {
            mListener.onBroadcastStart();
        }
    }

    private void appendLastManifestEntryToVodManifest(@NonNull File sourceManifest,
                                                      final boolean lastEntry) {
        // TODO : Inspect manifest to see if "end" marker line is present directly...
        // If so we tail the last three lines, else just the last two.
        String result = FileUtils.tail2(sourceManifest, lastEntry ? 3 : 2);
        FileUtils.writeStringToFileAsync(result, mVodManifest, true, new FileUtils.WriteCallback() {
            @Override
            public void onComplete(@NonNull File destination) {
                if (lastEntry) {
                    mS3Uploader.queueUpload(
                            mVodManifest,
                            VOD_FILENAME,
                            sManifestMetadata);
                    Timber.d("Queued VOD manifest " + mVodManifest.getAbsolutePath());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // -\_(*_*)_/-
            }
        });

    }

    /**
     * An S3 .m3u8 upload completed.
     * <p>
     * Called on a background thread
     */
    private void onManifestUploaded(@NonNull File manifestFile,
                                    @NonNull String destinationUrl) {

        // Delete uploaded file(s)
        Timber.d("Deleting " + manifestFile.getAbsolutePath());
        manifestFile.delete();
        if (destinationUrl.endsWith("vod.m3u8")) {
            Timber.d("Uploaded VOD Manifest. Deleting directory %s", mConfig.getOutputDirectory());
            mFileObserver.stopWatching();
            FileUtils.deleteDirectory(mConfig.getOutputDirectory());
        }


        if (mListener != null && !mSentBroadcastLiveEvent) {
            mSentBroadcastLiveEvent = true;
            mListener.onBroadcastLive(mS3StartStreamResponse.stream, destinationUrl);
        }
    }

    /**
     * An S3 .ts segment upload completed.
     * <p>
     * Use this opportunity to adjust bitrate based on the bandwidth
     * measured during this segment's transmission.
     * <p>
     * Called on a background thread
     */
    private void onVideoSegmentUploaded(@NonNull File videoSegmentFile,
                                        @NonNull String destinationUrl,
                                        int realizedBytesPerSecond) {
        // Delete uploaded file
        videoSegmentFile.delete();


        if (isKitKat() && mConfig.isAdaptiveBitrate() && isRecording()) {
            mLastRealizedBandwidthBytesPerSecond = realizedBytesPerSecond;
            // Adjust video encoder bitrate per bandwidth of just-completed upload
            long currentStreamBytesPerSecond = (((mVideoBitrate + mConfig.getAudioBitrate()) / 8));
            Timber.d("Competed segment at %f kBps. Encoder set to %f kBps",
                    (mLastRealizedBandwidthBytesPerSecond / 1000.0),
                    currentStreamBytesPerSecond / 1000.0);

            if (mLastRealizedBandwidthBytesPerSecond < currentStreamBytesPerSecond) {
                // The new bitrate is equal to the last upload bandwidth, never inferior to MIN_BITRATE, nor superior to the initial specified bitrate
                mVideoBitrate = Math.max(Math.min(mLastRealizedBandwidthBytesPerSecond * 8, mConfig.getVideoBitrate()), MIN_BITRATE);
                Timber.d("Adjusting video bitrate to %f kBps. Last measured bandwidth: %f kBps",
                        mVideoBitrate / (8 * 1000.0),
                        mLastRealizedBandwidthBytesPerSecond / 1000.0);
                adjustVideoBitrate(mVideoBitrate);
            }
        }
    }

    /**
     * A thumbnail upload completed.
     * <p>
     * Called on a background thread
     */
    private void onThumbnailUploaded(@NonNull File thumbnailFile,
                                     @NonNull String destinationUrl) {

        // Delete uploaded file
        thumbnailFile.delete();

        // TODO : Update stream with thumbnail
    }

    // <editor-fold desc="HlsFileObserverCallback">

    static ObjectMetadata sManifestMetadata = new ObjectMetadata();

    static {
        sManifestMetadata.setCacheControl("max-age=0");
    }

    @Override
    public void onManifestWritten(@NonNull String manifestPath) {

        File manifestFile = new File(manifestPath);
        final File manfiestCopy = new File(mManifestSnapshotDir, manifestFile.getName()
                .replace(".m3u8", "_" + mNumSegmentsWritten + ".m3u8"));

        FileUtils.copyAsync(manifestFile, manfiestCopy, new FileUtils.CopyCallback() {
            @Override
            public void onComplete(@NonNull File source, @NonNull File destination) {

                mS3Uploader.queueUpload(
                        destination,
                        "index.m3u8",   // Always upload live manifests as 'index.m3u8'
                        sManifestMetadata);
                appendLastManifestEntryToVodManifest(destination, !isRecording());
            }

            @Override
            public void onFailure(Throwable t) {
                // -\_(*_*)_/-
            }
        });
        mNumSegmentsWritten++;
    }

    @Override
    public void onVideoSegmentWritten(@NonNull String videoSegmentPath) {
        File segmentFile = new File(videoSegmentPath);
        mS3Uploader.queueUpload(
                segmentFile,
                segmentFile.getName(),
                null
        );

        // TODO : Do this on another thread
        if (isKitKat() && mConfig.isAdaptiveBitrate() && isRecording()) {
            // Adjust bitrate to match expected filesize
            long actualSegmentSizeBytes = segmentFile.length();
            long expectedSizeBytes = ((mConfig.getAudioBitrate() / 8) + (mVideoBitrate / 8)) * mConfig.getHlsSegmentDuration();
            float filesizeRatio = actualSegmentSizeBytes / (float) expectedSizeBytes;
            Timber.d("onVideoSegmentWritten. Segment size: " + (actualSegmentSizeBytes / 1000) + "kB. ratio: " + filesizeRatio);
            if (filesizeRatio < .7) {
                if (mLastRealizedBandwidthBytesPerSecond != 0) {
                    // Scale bitrate while not exceeding available bandwidth
                    float scaledBitrate = mVideoBitrate * (1 / filesizeRatio);
                    float bandwidthBitrate = mLastRealizedBandwidthBytesPerSecond * 8;
                    mVideoBitrate = (int) Math.min(scaledBitrate, bandwidthBitrate);
                } else {
                    // Scale bitrate to match expected fileSize
                    mVideoBitrate *= (1 / filesizeRatio);
                }
                Timber.d("Scaling video bitrate to " + mVideoBitrate + " bps");
                adjustVideoBitrate(mVideoBitrate);
            }
        }
    }

    @Override
    public void onThumbnailWritten(@NonNull String thumbnailPath) {
        File thumbnailFile = new File(thumbnailPath);
        mS3Uploader.queueUpload(
                thumbnailFile,
                thumbnailFile.getName(),
                null);
    }

    // </editor-fold desc="HlsFileObserverCallback">

    // <editor-fold desc="UploadListener">

    @Override
    public void onUploadComplete(@NonNull File file,
                                 @NonNull String destinationUrl,
                                 int realizedBytesPerSecond) {

        if (destinationUrl.endsWith(".m3u8")) {
            onManifestUploaded(file, destinationUrl);
        } else if (destinationUrl.endsWith(".ts")) {
            onVideoSegmentUploaded(file, destinationUrl, realizedBytesPerSecond);
        } else if (destinationUrl.endsWith(".jpg")) {
            onThumbnailUploaded(file, destinationUrl);
        }

    }

    @Override
    public void onUploadError(@NonNull File file,
                              @NonNull String destinationUrl,
                              @NonNull Throwable error) {

    }

    // </editor-fold desc="UploadListener">
}
