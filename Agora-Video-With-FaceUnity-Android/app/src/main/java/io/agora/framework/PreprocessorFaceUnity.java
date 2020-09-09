package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;

import com.faceunity.nama.FURenderer;
import com.faceunity.nama.utils.CameraUtils;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.processors.IPreprocessor;
import io.agora.capture.video.camera.VideoCaptureFrame;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    private FURenderer mFURenderer;
    private Context mContext;
    private boolean mEnabled;

    public PreprocessorFaceUnity(Context context) {
        mContext = context;
        mEnabled = true;
    }
    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (mFURenderer == null || !mEnabled) {
            return outFrame;
        }

        //双输入美妆会出问题
        outFrame.textureId = mFURenderer.onDrawFrameSingleInput(
                outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight());

        // The texture is transformed to texture2D by beauty module.
        outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        return outFrame;
    }

    @Override
    public void initPreprocessor() {
        //这个可以不写，在FUChatActivity中添加了
        if (mFURenderer != null) {
            mFURenderer.onSurfaceCreated();
        }
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        //这个可以不写，在FUChatActivity中添加了
        if (mFURenderer != null) {
            mFURenderer.onSurfaceDestroyed();
        }
    }

    public void setFURenderer(FURenderer FURenderer) {
        mFURenderer = FURenderer;
    }
}
