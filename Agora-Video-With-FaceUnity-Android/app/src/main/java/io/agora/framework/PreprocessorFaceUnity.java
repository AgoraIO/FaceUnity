package io.agora.framework;

import android.content.Context;
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

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.processors.IPreprocessor;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.profile.CSVUtils;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    private FURenderer mFURenderer = FURenderer.getInstance();
    private boolean renderSwitch;
    private int skipFrame = 0;

    private Handler mGLHandler;

    public void setCSVUtils(CSVUtils cSVUtils) {
        this.mCSVUtils = cSVUtils;
    }

    private CSVUtils mCSVUtils;

    public PreprocessorFaceUnity(Context context) {
    }

    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (!renderSwitch) {
            return outFrame;
        }
        if (mGLHandler == null) {
            startGLThread();
        }
        if (skipFrame > 0) {
            skipFrame--;
            outFrame.textureId = 0;
            return outFrame;
        }
        mFURenderer.setInputOrientation(outFrame.rotation);

        if (FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID)//高性能设备
            cheekFaceNum();

        long start = System.nanoTime();
        int texId = mFURenderer.onDrawFrameDualInput(outFrame.image,
                outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight());

        long renderTime = System.nanoTime() - start;
        if (mCSVUtils != null) {
            mCSVUtils.writeCsv(null, renderTime);
        }

        // The texture is transformed to texture2D by beauty module.
        if (skipFrame <= 0) {
            outFrame.textureId = texId;
            outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        } else {
            outFrame.textureId = 0;
        }
        return outFrame;
    }


    @Override
    public void initPreprocessor() {
        // only call once when app launched
        Log.e(TAG, "initPreprocessor: ");
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        Log.e(TAG, "enablePreProcess: ");
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        // not called
        Log.d(TAG, "releasePreprocessor: ");
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
}
