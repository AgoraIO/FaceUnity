package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.faceunity.nama.FURenderer;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.processors.IPreprocessor;
import io.agora.capture.video.camera.VideoCaptureFrame;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    private FURenderer mFURenderer = FURenderer.getInstance();
    private Context mContext;
    private boolean renderSwitch;
    private int skipFrame = 0;

    private Handler mGLHandler;

    public PreprocessorFaceUnity(Context context) {
        mContext = context;
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
        int texId = mFURenderer.onDrawFrameDualInput(outFrame.image,
                outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight());

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
        mGLHandler.post(() -> FURenderer.getInstance().release());
        mGLHandler = null;
    }


}
