package io.agora.framework;

import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.faceunity.FUConfig;
import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeautyBlurTypeEnum;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.utils.FuDeviceUtils;

import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.EglBase;
import io.agora.rtc2.video.IVideoFrameObserver;

public class PreprocessorFaceUnity implements IVideoFrameObserver {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    private FURenderer mFURenderer = FURenderer.getInstance();
    private boolean renderSwitch;
    private int skipFrame = 0;

    private Handler mGLHandler;
    private TextureBufferHelper textureBufferHelper;
    private boolean glPrepared = false;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private int mImageRotation = 0;
    private final android.graphics.Matrix localRenderMatrix = new android.graphics.Matrix();
    private static PreprocessorFaceUnity instance;

    public static PreprocessorFaceUnity getInstance() {
        if(instance == null){
            instance = new PreprocessorFaceUnity();
        }
        return instance;
    }

    private PreprocessorFaceUnity() {

    }

    private boolean prepareGl(EglBase.Context eglContext, final int width, final int height) {
        Log.d(TAG, "prepareGl");
        textureBufferHelper = TextureBufferHelper.create("RtcVideoConsumer", eglContext);
        if (textureBufferHelper == null) {
            return false;
        }
        textureBufferHelper.invoke(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                adjustViewPort(width, height);
                return null;
            }
        });
        Log.d(TAG, "prepareGl completed");
        return true;
    }

    /**
     * 根据显示区域大小调整一些参数信息
     *
     * @param width
     * @param height
     */
    private void adjustViewPort(int width, int height) {
        if (mImageWidth != width || mImageHeight != height) {
            mImageWidth = width;
            mImageHeight = height;
            GLES20.glViewport(0, 0, mImageWidth, mImageHeight);
        }
    }

    /* 创建线程  */
    private void startGLThread() {
        if (mGLHandler == null) {
            mGLHandler = new Handler(Looper.myLooper());
            mGLHandler.post(() -> {if (mSurfaceViewListener !=null ) mSurfaceViewListener.onSurfaceCreated();});
        }
    }

    public void doGLAction(Runnable runnable) {
        if (mGLHandler != null) {
            mGLHandler.post(runnable);
        }
    }

    public void setRenderEnable(boolean enabled) {
        renderSwitch = enabled;
    }


    public void skipFrame() {
        skipFrame = 5;
    }

    public void releaseFURender() {
        renderSwitch = false;
        mGLHandler.removeCallbacksAndMessages(0);
        mGLHandler.post(() -> {
            if (mSurfaceViewListener !=null ) mSurfaceViewListener.onSurfaceDestroyed();
        });
        mGLHandler = null;
        if (textureBufferHelper != null)
        {
            textureBufferHelper.dispose();
            textureBufferHelper = null;
        }
        glPrepared = false;
    }

    private SurfaceViewListener mSurfaceViewListener;

    public interface SurfaceViewListener{
        void onSurfaceCreated();
        void onSurfaceDestroyed();
    }

    public void setSurfaceListener(SurfaceViewListener surfaceViewListener) {
        this.mSurfaceViewListener = surfaceViewListener;
    }

    /**
     * 检查当前人脸数量
     */
    private void cheekFaceNum() {
        //根据有无人脸 + 设备性能 判断开启的磨皮类型
        float faceProcessorGetConfidenceScore = FUAIKit.getInstance().getFaceProcessorGetConfidenceScore(0);
        if (faceProcessorGetConfidenceScore >= 0.95) {
            //高端手机并且检测到人脸开启均匀磨皮，人脸点位质
            if (FURenderKit.getInstance() != null && FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.EquallySkin) {
                FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.EquallySkin);
                FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(true);
            }
        } else {
            if (FURenderKit.getInstance() != null && FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.FineSkin) {
                FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.FineSkin);
                FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(false);
            }
        }
    }

    @Override
    public boolean onCaptureVideoFrame(VideoFrame videoFrame) {

        if (videoFrame == null || videoFrame.getBuffer() == null) {
            return false;
        }

        final VideoFrame.Buffer buffer = videoFrame.getBuffer();
        if (!(buffer instanceof VideoFrame.TextureBuffer)) {
            return false;
        }

        if (glPrepared) {
            adjustViewPort(videoFrame.getRotatedWidth(), videoFrame.getRotatedHeight());
        }else{
            // setup egl context
            EglBase.Context eglContext =
                    ((VideoFrame.TextureBuffer) buffer).getEglBaseContext();
            glPrepared = prepareGl(eglContext, videoFrame.getRotatedWidth(), videoFrame.getRotatedHeight());
            if (!glPrepared) {
                // just by pass for now.
                Log.w(TAG, "Failed to prepare context");
                return false;
            }
        }
        mImageRotation = videoFrame.getRotation();
        VideoFrame.Buffer processedBuffer = textureBufferHelper.invoke(new Callable<VideoFrame.Buffer>() {
            @Override
            public VideoFrame.Buffer call() throws Exception {
                // Drop incoming frame if output texture buffer is still in use.
                if (textureBufferHelper.isTextureInUse()) {
                    return null;
                }

                if (!renderSwitch) {
                    return null;
                }
                if (mGLHandler == null) {
                    startGLThread();
                }
                if (skipFrame > 0) {
                    skipFrame--;
                    return null;
                }
//                mFURenderer.setInputOrientation(1);

                if (FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID)//高性能设备
                    cheekFaceNum();
                int texId = mFURenderer.onDrawFrameDualInput(null,
                        ((VideoFrame.TextureBuffer) buffer).getTextureId(), mImageHeight,
                        mImageWidth);

                // The texture is transformed to texture2D by beauty module.
                return textureBufferHelper.wrapTextureBuffer(buffer.getWidth(), buffer.getHeight(),
                        VideoFrame.TextureBuffer.Type.RGB, texId, localRenderMatrix);
            }
        });
        if (processedBuffer == null) {
            Log.w(TAG, "Drop, buffer in use");
            return false;
        } else {
            Log.d(TAG, "videoFrame done");
            videoFrame.replaceBuffer(processedBuffer, mImageRotation, videoFrame.getTimestampNs());
            return true;
        }
    }

    @Override
    public boolean onPreEncodeVideoFrame(VideoFrame videoFrame) {
        return false;
    }

    @Override
    public boolean onScreenCaptureVideoFrame(VideoFrame videoFrame) {
        return false;
    }

    @Override
    public boolean onPreEncodeScreenVideoFrame(VideoFrame videoFrame) {
        return false;
    }

    @Override
    public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int i) {
        return false;
    }

    @Override
    public boolean onRenderVideoFrame(String s, int i, VideoFrame videoFrame) {
        return false;
    }

    @Override
    public int getVideoFrameProcessMode() {
        return PROCESS_MODE_READ_WRITE;
    }

    @Override
    public int getVideoFormatPreference() {
        return IVideoFrameObserver.VIDEO_PIXEL_DEFAULT;
    }

    @Override
    public boolean getRotationApplied() {
        return false;
    }

    @Override
    public boolean getMirrorApplied() {
        return false;
    }

    @Override
    public int getObservedFramePosition() {
        return IVideoFrameObserver.POSITION_POST_CAPTURER;
    }
}
