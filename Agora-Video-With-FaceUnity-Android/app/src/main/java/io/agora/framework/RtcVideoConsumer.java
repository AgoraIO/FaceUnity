package io.agora.framework;

import android.opengl.GLES20;
import android.util.Log;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.consumers.ICaptureFrameConsumer;
import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.AgoraVideoFrame;

/**
 * The renderer acts as the consumer of the video source
 * from current video channel, and also the video source
 * of rtc engine.
 */
public class RtcVideoConsumer implements ICaptureFrameConsumer, IVideoSource {
    private static final String TAG = RtcVideoConsumer.class.getSimpleName();

    private volatile IVideoFrameConsumer mRtcConsumer;
    private volatile boolean mValidInRtc;

    private final CameraVideoManager cameraVideoManager;

    public RtcVideoConsumer(CameraVideoManager cameraVideoManage) {
        this.cameraVideoManager = cameraVideoManage;
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (mValidInRtc) {
            int format = frame.format.getTexFormat() == GLES20.GL_TEXTURE_2D
                    ? AgoraVideoFrame.FORMAT_TEXTURE_2D
                    : AgoraVideoFrame.FORMAT_TEXTURE_OES;
            if (mRtcConsumer != null) {
                mRtcConsumer.consumeTextureFrame(frame.textureId, format,
                        frame.format.getWidth(), frame.format.getHeight(),
                        frame.rotation, frame.timestamp, frame.textureTransform);
            }
        }
    }

    @Override
    public boolean onInitialize(IVideoFrameConsumer consumer) {
        Log.i(TAG, "onInitialize");
        mRtcConsumer = consumer;
        cameraVideoManager.attachOffScreenConsumer(this);
        return true;
    }

    @Override
    public boolean onStart() {
        Log.i(TAG, "onStart");
        mValidInRtc = true;
        return true;
    }

    @Override
    public void onStop() {
        mValidInRtc = false;
        mRtcConsumer = null;
    }

    @Override
    public void onDispose() {
        Log.i(TAG , "onDispose");
        cameraVideoManager.detachOffScreenConsumer(this);
        mValidInRtc = false;
        mRtcConsumer = null;
    }

    @Override
    public int getBufferType() {
        return MediaIO.BufferType.TEXTURE.intValue();
    }
}
