package io.agora.framework;

import android.util.Log;

import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.EglBase;
import io.agora.base.internal.video.EglBase14;
import io.agora.base.internal.video.RendererCommon;
import io.agora.capture.framework.modules.channels.ChannelManager;
import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.consumers.IVideoConsumer;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.capture.video.camera.VideoModule;
import io.agora.rtc2.RtcEngine;

/**
 * The renderer acts as the consumer of the video source from current video channel, and also the
 * video source of rtc engine.
 */
public class RtcVideoConsumer implements IVideoConsumer {

    private static final String TAG = RtcVideoConsumer.class.getSimpleName();

    private static volatile boolean mValidInRtc;

    private static volatile VideoModule mVideoModule;
    private int mChannelId;

    private RtcEngine mRtcEngine;

    public RtcVideoConsumer(RtcEngine mRtcEngine) {
        this(ChannelManager.ChannelID.CAMERA);
        this.mRtcEngine = mRtcEngine;
    }

    private RtcVideoConsumer(int channelId) {
        mVideoModule = VideoModule.instance();
        mChannelId = channelId;
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (mValidInRtc) {
            //TODO update
            if (glPrepared) {
                //TODO update
            } else {
                // setup egl context
                EglBase.Context eglContext = new EglBase14.Context(context.getEglContext());
                glPrepared = prepareGl(eglContext, frame.format.getWidth(), frame.format.getHeight());
                if (!glPrepared) {
                    // just by pass for now.
                    Log.w(TAG, "Failed to prepare context");
                    return;
                }
            }

            int rotation = frame.rotation;
            VideoFrame.Buffer processedBuffer = textureBufferHelper
                    .invoke(new Callable<VideoFrame.Buffer>() {
                        @Override
                        public VideoFrame.Buffer call() throws Exception {
                            int textureId = frame.textureId;
                            return textureBufferHelper.wrapTextureBuffer(
                                    frame.format.getWidth(),
                                    frame.format.getHeight(),
                                    VideoFrame.TextureBuffer.Type.RGB,
                                    textureId,
                                    RendererCommon.convertMatrixToAndroidGraphicsMatrix(frame.textureTransform));
                        }
                    });

            if (processedBuffer == null) {
                Log.w(TAG, "Drop, buffer in use");
            } else {
                //Log.d(TAG, "videoFrame done");
                VideoFrame mVideoFrame = new VideoFrame(processedBuffer, rotation, System.nanoTime());
                mRtcEngine.pushExternalVideoFrame(mVideoFrame);
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

    private volatile static boolean glPrepared;
    private volatile TextureBufferHelper textureBufferHelper;

    private boolean prepareGl(EglBase.Context eglContext, final int width, final int height) {
        Log.d(TAG, "prepareGl");
        textureBufferHelper = TextureBufferHelper.create("STProcess", eglContext);
        if (textureBufferHelper == null) {
//            LogUtils.e(TAG, "Failed to create texture buffer helper!");
            return false;
        }
        Log.d(TAG, "prepareGl completed");
        return true;
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
        Log.i(TAG, "onDispose");
        mValidInRtc = false;

        //TODO update
        if (textureBufferHelper != null) {
            textureBufferHelper.dispose();
            textureBufferHelper = null;
        }
        glPrepared = false;
        disconnectChannel(mChannelId);
    }
}
