package io.agora.processor.media.manager;

import android.content.Context;
import android.opengl.EGL14;

import java.io.IOException;

import io.agora.processor.audio.encoder.MediaCodecAudioEncoder;
import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.common.file.AndroidMPEG4Writer;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.base.BaseEncoder;
import io.agora.processor.media.base.BaseMuxer;
import io.agora.processor.media.data.AudioCaptureConfigInfo;
import io.agora.processor.media.data.AudioEncoderConfigInfo;
import io.agora.processor.media.data.EncodedFrame;
import io.agora.processor.media.data.RtmpConfigInfo;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.VideoEncoderConfigInfo;
import io.agora.processor.media.internal.IRenderListener;
import io.agora.processor.video.encoder.MediaCodecVideoEncoder;


/**
 * Created by yong on 2019/9/28.
 */

public class AVRecordingManager {
    private static volatile AVRecordingManager mInstance;
    private BaseMuxer mMuxer;
    private BaseEncoder mAudioEncoder, mVideoEncoder;


    private VideoCaptureConfigInfo videoCaptureConfigInfo;
    private VideoEncoderConfigInfo videoEncoderConfigInfo;
    private AudioEncoderConfigInfo audioEncoderConfigInfo;
    private AudioCaptureConfigInfo audioCaptureConfigInfo;
    private AVRecordingManager avRecordingManager;
    private VideoManager mVideoManager;
    private AudioManager mAudioManager;
    private boolean writeToMPEG4File;
    private boolean isRtmpStreamingKitStarted = false;

    private AVRecordingManager(VideoManager videoManager, AudioManager audioManager) {
        this.mVideoManager = videoManager;
        this.mAudioManager = audioManager;
    }


    public static AVRecordingManager createInstance(Context context,
                                                    VideoManager videoManager,
                                                    AudioManager audioManager) {
        if (mInstance == null) {
            synchronized (AVRecordingManager.class) {
                if (mInstance == null) {
                    mInstance = new AVRecordingManager(videoManager, audioManager);
                }
            }
        }
        return mInstance;
    }


    public void enableWriteVideoRawData(String rawfilePath, String yuvfilePath) {
        if (this.mVideoEncoder != null && this.mVideoEncoder instanceof MediaCodecVideoEncoder) {
            ((MediaCodecVideoEncoder) this.mVideoEncoder).enableWriteVideoRawData(rawfilePath, yuvfilePath);
        }
    }

    public boolean allocate(VideoCaptureConfigInfo videoCaptureConfigInfo,
                            VideoEncoderConfigInfo videoEncoderConfigInfo,
                            AudioCaptureConfigInfo audioCaptureConfigInfo,
                            AudioEncoderConfigInfo audioEncoderConfigInfo, String localFilePath) {
        this.videoCaptureConfigInfo = videoCaptureConfigInfo;
        this.videoEncoderConfigInfo = videoEncoderConfigInfo;
        this.audioCaptureConfigInfo = audioCaptureConfigInfo;
        this.audioEncoderConfigInfo = audioEncoderConfigInfo;
        this.writeToMPEG4File = true;
        if (writeToMPEG4File) {
            try {
                this.mMuxer = new AndroidMPEG4Writer(localFilePath);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        try {
            this.mMuxer.allocate();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        LogUtil.i("allocate:muxer inited");
        if (this.mVideoManager != null) {
            this.mVideoManager.setRenderListner(renderListener);
            this.mVideoEncoder = new MediaCodecVideoEncoder(videoCaptureConfigInfo, videoEncoderConfigInfo);
            this.mVideoEncoder.allocate();
            this.mVideoEncoder.getEncoderedDataConnector().connect(mMuxer);
            LogUtil.i("allocate:video encoder inited");
        }

        if (this.mAudioManager != null) {
            this.mAudioEncoder = new MediaCodecAudioEncoder(audioCaptureConfigInfo, audioEncoderConfigInfo);
            this.mAudioEncoder.allocate();
            this.mAudioEncoder.getEncoderedDataConnector().connect(mMuxer);
            LogUtil.i("allocate:audio encoder inited");
        }
        LogUtil.i("allocate:mVideoEncoder=" + mVideoEncoder);
        return true;
    }

    private boolean initEodecEGLContext() {
        //initEncoderContext must used after encoder started
        if (videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            if (this.mVideoManager != null) {
                if (!this.mVideoManager.isEglContextReady()) {
                    return false;
                }
                this.mVideoManager.runInRenderThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.i("start:initEncoderContext=" + EGL14.eglGetCurrentContext());
                        ((MediaCodecVideoEncoder) mVideoEncoder).initEncoderContext(EGL14.eglGetCurrentContext());
                    }
                });

            }
        }
        return true;
    }

    public boolean start() {
        if (isRtmpStreamingKitStarted) {
            LogUtil.e("kit has started");
            return false;
        }
        int startVideoEncoderFlag = 0;
        int startAudioEncoderFlag = 0;
        int startMuxerFlag = 0;
        if (this.mVideoManager != null) {
            try {
                startVideoEncoderFlag = this.mVideoEncoder.start();
            } catch (IOException e) {
                LogUtil.e("start:mVideoEncoder error" + e.toString());
                return false;
            }
            this.mVideoManager.attachConnectorToRender(this.mVideoEncoder);
            LogUtil.i("start:mVideoEncoder=" + mVideoEncoder);
        } else {
            LogUtil.w("start without video manager");
        }

        if (!initEodecEGLContext()) {
            LogUtil.e("initEodecEGLContext error");
            return false;
        }

        if (this.mAudioManager != null) {
            try {
                startAudioEncoderFlag = this.mAudioEncoder.start();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            this.mAudioManager.attachConnectorAudioCapture(this.mAudioEncoder);
            LogUtil.i("start:mAudioEncoder=" + mAudioEncoder);
        } else {
            LogUtil.w("start without audio manager");
        }
        startMuxerFlag = this.mMuxer.start();
        if (startVideoEncoderFlag == 0 &&
                startAudioEncoderFlag == 0 &&
                startMuxerFlag == 0) {
            LogUtil.i("start:mMuxer=" + mMuxer);
            isRtmpStreamingKitStarted = true;
            return true;
        }
        return false;
    }

    public void reloadCodec(float videoBpp) {
        if (!this.writeToMPEG4File) {
            LogUtil.w("reloadCodec videoEncoder and reduce bitrate to format " + videoBpp);
            videoEncoderConfigInfo.setVideoBpp(videoBpp);
            try {
                if (mVideoEncoder != null) {
                    mVideoEncoder.reLoadEncoder(videoEncoderConfigInfo);
                    initEodecEGLContext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {


        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
        }
        LogUtil.i("stop:mVideoEncoder=" + mVideoEncoder);

        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
        }
        LogUtil.i("stop:mAudioEncoder=" + mAudioEncoder);
        if (mMuxer != null) {
            mMuxer.stop();
        }
        LogUtil.i("stop:mMuxer=" + mMuxer);
        isRtmpStreamingKitStarted = false;

    }

    public boolean deallocate() {
        int deallocateVideoEncoderFlag = 0;
        int deallocateAudioEncoderFlag = 0;
        int deallocateMuxerFlag = 0;
        if (mVideoEncoder != null) {
            deallocateVideoEncoderFlag = mVideoEncoder.deallocate();
            mVideoEncoder = null;
        }
        LogUtil.i("deallocate:mVideoEncoder=" + mVideoEncoder);

        if (mAudioEncoder != null) {
            deallocateAudioEncoderFlag = mAudioEncoder.deallocate();
            mAudioEncoder = null;
        }
        LogUtil.i("deallocate:mAudioEncoder=" + mAudioEncoder);

        if (mMuxer != null) {
            deallocateMuxerFlag = mMuxer.deallocate();
            mMuxer = null;
        }
        LogUtil.i("deallocate:mMuxer=" + mMuxer);
        if (deallocateVideoEncoderFlag == 0 &&
                deallocateAudioEncoderFlag == 0 &&
                deallocateMuxerFlag == 0) {
            return true;
        }
        return false;

    }

    public boolean isMuxerStarted() {
        if (this.writeToMPEG4File) {
            return true;
        }
        if (this.mMuxer == null) {
            return false;
        }
        return this.mMuxer.isMuxerStarted();
    }

    public BaseMuxer getMuxer() {
        return mMuxer;
    }

    public BaseEncoder getmAudioEncoder() {
        return mAudioEncoder;
    }

    public BaseEncoder getmVideoEncoder() {
        if (mVideoEncoder == null) {
            return null;
        }
        return mVideoEncoder;
    }

    public void attachConnectorVideoEncoder(SinkConnector<EncodedFrame> videoEncoderedData) {
        if (mVideoEncoder != null) {
            this.mVideoEncoder.getEncoderedDataConnector().connect(videoEncoderedData);
        } else {
            LogUtil.w("attachConnectorVideoEncoder error for mVideoEncoder is null");
        }
    }

    public void attachConnectorAudioEncoder(SinkConnector<EncodedFrame> audioEncoderedData) {
        if (mAudioEncoder != null) {
            this.mAudioEncoder.getEncoderedDataConnector().connect(audioEncoderedData);
        } else {
            LogUtil.w("attachConnectorAudioEncoder error for mAudioEncoder is null");
        }
    }


    private IRenderListener renderListener = new IRenderListener() {
        @Override
        public void onEGLContextReady() {
            LogUtil.i("onEGLContextReady ");
        }

        @Override
        public void onViewIsPortrait(boolean isPortrait) {
            if (isPortrait) {

            }
            LogUtil.i("onViewIsPortrait " + isPortrait);
        }
    };
}
