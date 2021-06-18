package io.agora.framework;

import android.opengl.GLES20;
import android.util.Log;

import io.agora.base.NV21Buffer;
import io.agora.base.VideoFrame;
import io.agora.capture.framework.modules.channels.ChannelManager;
import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.consumers.IVideoConsumer;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.capture.video.camera.VideoModule;
import io.agora.rtc2.RtcEngine;

/**
 * The renderer acts as the consumer of the video source
 * from current video channel, and also the video source
 * of rtc engine.
 */
public class RtcVideoConsumer implements IVideoConsumer {
    private static final String TAG = RtcVideoConsumer.class.getSimpleName();

    private volatile boolean mValidInRtc;

    private volatile VideoModule mVideoModule;
    private int mChannelId;

    private RtcEngine mRtcEngine;

    public RtcVideoConsumer(RtcEngine mRtcEngine) {
        this(ChannelManager.ChannelID.CAMERA);
        this.mRtcEngine=mRtcEngine;
    }

    private RtcVideoConsumer(int channelId) {
        mVideoModule = VideoModule.instance();
        mChannelId = channelId;
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (mValidInRtc) {
            //TODO update
            if (frame.image != null) {
                VideoFrame.Buffer buffer = new NV21Buffer(frame.image, frame.format.getWidth(), frame.format.getHeight(), null);
                mRtcEngine.pushExternalVideoFrame(new VideoFrame(buffer, frame.rotation, System.nanoTime()));
            } else {
                Log.e(TAG, "onConsumeFrame: frame.image is empty");
            }

//            int format = frame.format.getTexFormat() == GLES20.GL_TEXTURE_2D
//                    ? AgoraVideoFrame.FORMAT_TEXTURE_2D
//                    : AgoraVideoFrame.FORMAT_TEXTURE_OES;
//            if (mRtcConsumer != null) {
//                mRtcConsumer.consumeTextureFrame(frame.textureId, format,
//                        frame.format.getWidth(), frame.format.getHeight(),
//                        frame.rotation, frame.timestamp, frame.textureTransform);
//            }
        }
    }

    @Override
    public void connectChannel(int channelId) {
        // Rtc transmission is an off-screen rendering procedure.
        VideoChannel channel = mVideoModule.connectConsumer(
                this, channelId, IVideoConsumer.TYPE_OFF_SCREEN);
    }

    @Override
    public void disconnectChannel(int channelId) {
        mVideoModule.disconnectConsumer(this, channelId);
    }

    @Override
    public void setMirrorMode(int mode) {

    }

    @Override
    public Object getDrawingTarget() {
        return null;
    }

    @Override
    public int onMeasuredWidth() {
        return 0;
    }

    @Override
    public int onMeasuredHeight() {
        return 0;
    }

    @Override
    public void recycle() {

    }

    @Override
    public String getId() {
        return null;
    }

    public boolean onStart() {
        Log.i(TAG, "onStart");
        connectChannel(mChannelId);
        mValidInRtc = true;
        return true;
    }

    public void onStop() {
        mValidInRtc = false;
    }

    public void onDispose() {
        Log.i(TAG , "onDispose");
        mValidInRtc = false;
        disconnectChannel(mChannelId);
    }
}
