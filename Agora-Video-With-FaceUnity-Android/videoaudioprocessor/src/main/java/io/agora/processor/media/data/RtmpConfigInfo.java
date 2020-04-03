package io.agora.processor.media.data;

/**
 * Created by yong on 2019/8/30.
 */

public class RtmpConfigInfo {
    private String rtmpUrl = null;

    /**
     * could not be null if isNeedLocalWrite is true;
     */
    private String localFilePath = null;

    /**
     * set true if you need write local file while send rtmp stream
     */
    private boolean isNeedLocalWrite = false;


    /**
     * recommend to set the same size as encoded video width
     */
    private int videoWidth = 0;

    /**
     * recommend to set the same size as encoded video height
     */
    private int videoHeight = 0;

    /**
     * set true if you need add adts head in encoded aac data,
     * recommend to set true
     */

    private boolean enableAutoVideoBitrate = true;
    private boolean enableWriteAudio = true;
    private boolean enableWriteVideo = true;
    private boolean enableAdtsHeadInAudio = true;

    public String getRtmpUrl() {
        return rtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public boolean isNeedLocalWrite() {
        return isNeedLocalWrite;
    }

    public void setNeedLocalWrite(boolean needLocalWrite) {
        isNeedLocalWrite = needLocalWrite;
    }

    public boolean isEnableAdtsHeadInAudio() {
        return enableAdtsHeadInAudio;
    }

    //TODO open this api later
    public void setEnableAdtsHeadInAudio(boolean enableAdtsHeadInAudio) {
        this.enableAdtsHeadInAudio = enableAdtsHeadInAudio;
    }

    public boolean isEnableWriteAudio() {
        return enableWriteAudio;
    }

    public void setEnableWriteAudio(boolean enableWriteAudio) {
        this.enableWriteAudio = enableWriteAudio;
    }

    public boolean isEnableWriteVideo() {
        return enableWriteVideo;
    }

    public void setEnableWriteVideo(boolean enableWriteVideo) {
        this.enableWriteVideo = enableWriteVideo;
    }

    public boolean isEnableAutoVideoBitrate() {
        return enableAutoVideoBitrate;
    }

    public void setEnableAutoVideoBitrate(boolean enableAutoVideoBitrate) {
        this.enableAutoVideoBitrate = enableAutoVideoBitrate;
    }
}
