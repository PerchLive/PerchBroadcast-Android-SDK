package com.perchlive.broadcast.sdk.av;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import static com.perchlive.broadcast.sdk.AndroidUtil.isKitKat;

/**
 * Configuration information for a Recording session.
 * Includes meta data, video + audio encoding
 * and muxing parameters
 */
public class SessionConfig {

    private final VideoEncoderConfig mVideoConfig;
    private final AudioEncoderConfig mAudioConfig;
    private       File               mOutputDirectory;
    private final UUID               mUUID;
    private       Muxer              mMuxer;
    private       boolean            mConvertVerticalVideo;
    private       boolean            mIsAdaptiveBitrate;
    private       boolean            mAttachLocation;
    private       int                mHlsSegmentDuration;

    public SessionConfig(@NonNull File rootDirectory) {
        mVideoConfig = new VideoEncoderConfig(1280, 720, 2 * 1000 * 1000);
        mAudioConfig = new AudioEncoderConfig(1, 44100, 96 * 1000);

        mUUID = UUID.randomUUID();

        File rootDir    = rootDirectory;
        File outputDir  = new File(rootDir, mUUID.toString());
        File outputFile = new File(outputDir, String.format("kf_%d.m3u8", System.currentTimeMillis()));
        outputDir.mkdir();
        mMuxer = FFmpegMuxer.create(outputFile.getAbsolutePath(), Muxer.Format.MPEG4);
    }

    public SessionConfig(@NonNull UUID uuid,
                         @NonNull Muxer muxer,
                         @NonNull VideoEncoderConfig videoConfig,
                         @NonNull AudioEncoderConfig audioConfig) {

        mVideoConfig = videoConfig;
        mAudioConfig = audioConfig;

        mMuxer = muxer;
        mUUID = uuid;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public Muxer getMuxer() {
        return mMuxer;
    }

    public void setOutputDirectory(File outputDirectory) {
        mOutputDirectory = outputDirectory;
    }

    public File getOutputDirectory() {
        return mOutputDirectory;
    }

    public String getOutputPath() {
        return mMuxer.getOutputPath();
    }

    public int getTotalBitrate() {
        return mVideoConfig.getBitRate() + mAudioConfig.getBitrate();
    }

    public int getVideoWidth() {
        return mVideoConfig.getWidth();
    }

    public int getVideoHeight() {
        return mVideoConfig.getHeight();
    }

    public int getVideoBitrate() {
        return mVideoConfig.getBitRate();
    }

    public int getNumAudioChannels() {
        return mAudioConfig.getNumChannels();
    }

    public int getAudioBitrate() {
        return mAudioConfig.getBitrate();
    }

    public int getAudioSamplerate() {
        return mAudioConfig.getSampleRate();
    }

    public boolean isAdaptiveBitrate() {
        return mIsAdaptiveBitrate;
    }

    public boolean isConvertingVerticalVideo() {
        return mConvertVerticalVideo;
    }

    public int getHlsSegmentDuration() {
        return mHlsSegmentDuration;
    }

    public void setUseAdaptiveBitrate(boolean useAdaptiveBit) {
        mIsAdaptiveBitrate = useAdaptiveBit;
    }

    public void setConvertVerticalVideo(boolean convertVerticalVideo) {
        mConvertVerticalVideo = convertVerticalVideo;
    }

    public boolean shouldAttachLocation() {
        return mAttachLocation;
    }

    public void setAttachLocation(boolean mAttachLocation) {
        this.mAttachLocation = mAttachLocation;
    }

    public void setHlsSegmentDuration(int hlsSegmentDuration) {
        mHlsSegmentDuration = hlsSegmentDuration;
    }

    public static class Builder {
        private int mWidth;
        private int mHeight;
        private int mVideoBitrate;

        private int mAudioSamplerate;
        private int mAudioBitrate;
        private int mNumAudioChannels;

        private Muxer mMuxer;

        private File    mOutputDirectory;
        private UUID    mUUID;
        private String  mTitle;
        private String  mDescription;
        private boolean mPrivate;
        private boolean mAttachLocation;
        private boolean mConvertVerticalVideo;
        private boolean mAdaptiveStreaming;
        private Map     mExtraInfo;

        private int mHlsSegmentDuration;

        /**
         * Configure a SessionConfig quickly with intelligent path interpretation.
         * Valid inputs are "/path/to/name.m3u8", "/path/to/name.mp4"
         * <p/>
         * For file-based outputs (.m3u8, .mp4) the file structure is managed
         * by a recording UUID.
         * <p/>
         * Given an absolute file-based outputLocation like:
         * <p/>
         * /sdcard/test.m3u8
         * <p/>
         * the output will be available in:
         * <p/>
         * /sdcard/<UUID>/test.m3u8
         * /sdcard/<UUID>/test0.ts
         * /sdcard/<UUID>/test1.ts
         * ...
         * <p/>
         * You can query the final outputLocation after building with
         * SessionConfig.getOutputPath()
         *
         * @param outputLocation desired output location. For file based recording,
         *                       recordings will be stored at <outputLocationParent>/<UUID>/<outputLocationFileName>
         */
        public Builder(String outputLocation) {
            setAVDefaults();
            setMetaDefaults();
            mUUID = UUID.randomUUID();

            if (outputLocation.contains(".m3u8")) {
                mMuxer = FFmpegMuxer.create(createRecordingPath(outputLocation), Muxer.Format.HLS);
            } else if (outputLocation.contains(".mp4")) {
                mMuxer = AndroidMuxer.create(createRecordingPath(outputLocation), Muxer.Format.MPEG4);
                //mMuxer = FFmpegMuxer.create(createRecordingPath(outputLocation), Muxer.Format.MPEG4);
            } else
                throw new RuntimeException("Unexpected muxer output. Expected a .mp4 or .m3u8. Got: " + outputLocation);

        }

        /**
         * Use this builder to manage file hierarchy manually
         * or to provide your own Muxer
         *
         * @param muxer
         */
        public Builder(@NonNull Muxer muxer) {
            setAVDefaults();
            setMetaDefaults();
            mMuxer = muxer;
            mOutputDirectory = new File(mMuxer.getOutputPath()).getParentFile();
            mUUID = UUID.randomUUID();
        }

        /**
         * Inserts a directory into the given path based on the
         * value of mUUID.
         *
         * @param outputPath a desired storage location like /path/filename.ext
         * @return a File pointing to /path/UUID/filename.ext
         */
        private String createRecordingPath(String outputPath) {
            File   desiredFile     = new File(outputPath);
            String desiredFilename = desiredFile.getName();
            File   outputDir       = new File(desiredFile.getParent(), mUUID.toString());
            mOutputDirectory = outputDir;
            outputDir.mkdirs();
            return new File(outputDir, desiredFilename).getAbsolutePath();
        }

        private void setAVDefaults() {
            mWidth = 1280;
            mHeight = 720;
            mVideoBitrate = 2 * 1000 * 1000;

            mAudioSamplerate = 44100;
            mAudioBitrate = 96 * 1000;
            mNumAudioChannels = 1;
        }

        private void setMetaDefaults() {
            mPrivate = false;
            mAttachLocation = false;
            mAdaptiveStreaming = isKitKat();
            mConvertVerticalVideo = false;
            mHlsSegmentDuration = 10;
        }

        public Builder withMuxer(@NonNull Muxer muxer) {
            mMuxer = muxer;
            return this;
        }

        public Builder withTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder withDescription(String description) {
            mDescription = description;
            return this;
        }

        public Builder withPrivateVisibility(boolean isPrivate) {
            mPrivate = isPrivate;
            return this;
        }

        public Builder withLocation(boolean attachLocation) {
            mAttachLocation = attachLocation;
            return this;
        }

        public Builder withAdaptiveStreaming(boolean adaptiveStreaming) {
            mAdaptiveStreaming = adaptiveStreaming;
            return this;
        }

        public Builder withVerticalVideoCorrection(boolean convertVerticalVideo) {
            mConvertVerticalVideo = convertVerticalVideo;
            return this;
        }

        public Builder withExtraInfo(Map extraInfo) {
            mExtraInfo = extraInfo;
            return this;
        }


        public Builder withVideoResolution(int width, int height) {
            mWidth = width;
            mHeight = height;
            return this;
        }

        public Builder withVideoBitrate(int bitrate) {
            mVideoBitrate = bitrate;
            return this;
        }

        public Builder withAudioSamplerate(int samplerate) {
            mAudioSamplerate = samplerate;
            return this;
        }

        public Builder withAudioBitrate(int bitrate) {
            mAudioBitrate = bitrate;
            return this;
        }

        public Builder withAudioChannels(int numChannels) {
            if (!(numChannels == 0 || numChannels == 1)) {
                throw new IllegalArgumentException("Illegal number of audio channels");
            }

            mNumAudioChannels = numChannels;
            return this;
        }

        public Builder withHlsSegmentDuration(int segmentDuration) {
            mHlsSegmentDuration = segmentDuration;
            return this;
        }

        public SessionConfig build() {
            SessionConfig session = new SessionConfig(mUUID, mMuxer,
                    new VideoEncoderConfig(mWidth, mHeight, mVideoBitrate),
                    new AudioEncoderConfig(mNumAudioChannels, mAudioSamplerate, mAudioBitrate));

            session.setUseAdaptiveBitrate(mAdaptiveStreaming);
            session.setConvertVerticalVideo(mConvertVerticalVideo);
            session.setAttachLocation(mAttachLocation);
            session.setHlsSegmentDuration(mHlsSegmentDuration);
            session.setOutputDirectory(mOutputDirectory);

            return session;
        }


    }
}
