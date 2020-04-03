package io.agora.processor.media.data;

import io.agora.processor.common.constant.ConstantMediaConfig;
import io.agora.processor.common.utils.LogUtil;

import static io.agora.processor.common.constant.Constant.FIX_VIDEO_FPS;
import static io.agora.processor.common.constant.ConstantMediaConfig.VIDEO_KEY_IFRAME_RATE;

/**
 * Created by yong on 2019/9/22.
 */

public class VideoEncoderConfigInfo {
    /**
     * encoder fps which change gop size
     */
    private int videoGop;

    /**
     * used to calculate BitRate
     */
    private float videoBpp;

    /**
     * video bitrate ,calculate internal
     */
    private int videoEncodeBitrate;

    /**
     * video mime type,when use rtmp muxer ,only can use avc
     */
    public String videoMimeType;

    public VideoEncoderConfigInfo() {


        videoMimeType = ConstantMediaConfig.VIDEO_MIME_TYPE;
        //encoder
        videoGop = ConstantMediaConfig.VIDEO_GOP;
        videoBpp = ConstantMediaConfig.VIDEO_BPP;
        videoEncodeBitrate = 0;
    }

    public void calcBitRate(int videoWidth, int videoHeight, int fps) {
        //
        if (fps > FIX_VIDEO_FPS) {
            fps = FIX_VIDEO_FPS;
        }
        videoEncodeBitrate = (int) (this.videoBpp * fps * videoWidth * videoHeight);
        LogUtil.i(String.format("video bitrate=%5.2f[Mbps] %5.2f", videoEncodeBitrate / 1024f / 1024f,this.videoBpp));
    }

    public int getVideoEncodeBitrate() {
        return videoEncodeBitrate;
    }

    public void setVideoEncodeBitrate(int videoEncodeBitrate) {
        this.videoEncodeBitrate = videoEncodeBitrate;
    }

    public int getVideoGop() {
        return videoGop;
    }

    public void setVideoGop(int videoGop) {
        this.videoGop = videoGop;
    }

    public float getVideoBpp() {
        return videoBpp;
    }

    public void setVideoBpp(float videoBpp) {
        this.videoBpp = videoBpp;
    }

    public String getVideoMimeType() {
        return videoMimeType;
    }

    public void setVideoMimeType(String videoMimeType) {
        this.videoMimeType = videoMimeType;
    }

    public void calcNV21BitRate(int videoWidth, int videoHeight) {
        videoEncodeBitrate = (int) (this.videoBpp * this.videoGop * videoWidth * videoHeight);
        LogUtil.i(String.format("bitrate=%5.2f[Mbps]", videoEncodeBitrate / 1024f / 1024f));
    }
}
