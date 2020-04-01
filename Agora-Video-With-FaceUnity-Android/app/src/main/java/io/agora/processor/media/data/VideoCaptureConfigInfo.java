package io.agora.processor.media.data;

import io.agora.processor.common.constant.ConstantMediaConfig;


/**
 * Created by yong on 2019/8/31.
 */

public class VideoCaptureConfigInfo {
    public enum CaptureFormat {
        TEXTURE_OES, TEXTURE_2D, NV21
    }

    public enum CaptureType {
        TEXTURE, BYTE_ARRAY
    }


    //capture
    private int cameraFace;
    private CaptureType videoCaptureType;
    private CaptureFormat videoCaptureFormat;
    private int videoCaptureWidth;
    private int videoCaptureHeight;
    private int videoCaptureFps;
    private boolean isHorizontal ;
    public VideoCaptureConfigInfo() {

        //capture
        cameraFace = ConstantMediaConfig.CAMERA_FACE;
        videoCaptureType = ConstantMediaConfig.VIDEO_CAPTURE_TYPE;
        videoCaptureFormat = ConstantMediaConfig.VIDEO_CAPTURE_FORMAT;
        videoCaptureWidth = ConstantMediaConfig.VIDEO_CAPTURE_WIDHT;
        videoCaptureHeight = ConstantMediaConfig.VIDEO_CAPTURE_HEIGHT;
        videoCaptureFps = ConstantMediaConfig.VIDEO_CAPTURE_FPS;
        isHorizontal  = ConstantMediaConfig.DEFAULT_ORIENTATION;
    }

    public CaptureType getVideoCaptureType() {
        return videoCaptureType;
    }

    public void setVideoCaptureType(CaptureType videoCaptureType) {
        this.videoCaptureType = videoCaptureType;
    }

    public CaptureFormat getVideoCaptureFormat() {
        return videoCaptureFormat;
    }

    public void setVideoCaptureFormat(CaptureFormat videoCaptureFormat) {
        this.videoCaptureFormat = videoCaptureFormat;
    }

    public int getVideoCaptureWidth() {
        return videoCaptureWidth;
    }

    public void setVideoCaptureWidth(int videoCaptureWidth) {
        this.videoCaptureWidth = videoCaptureWidth;
    }

    public int getVideoCaptureHeight() {
        return videoCaptureHeight;
    }

    public void setVideoCaptureHeight(int videoCaptureHeight) {
        this.videoCaptureHeight = videoCaptureHeight;
    }

    public int getVideoCaptureFps() {
        return videoCaptureFps;
    }

    public void setVideoCaptureFps(int videoCaptureFps) {
        this.videoCaptureFps = videoCaptureFps;
    }

    public int getCameraFace() {
        return cameraFace;
    }

    public void setCameraFace(int cameraFace) {
        this.cameraFace = cameraFace;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public void setHorizontal(boolean enable) {
        isHorizontal  = enable;
    }
}
