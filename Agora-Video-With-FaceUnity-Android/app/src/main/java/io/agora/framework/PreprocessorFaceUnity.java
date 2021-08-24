package io.agora.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.util.Log;

import com.faceunity.nama.FURenderer;
import com.faceunity.nama.utils.CameraUtils;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.framework.modules.processors.IPreprocessor;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.rtcwithfu.utils.TextureIdHelp;

public class PreprocessorFaceUnity implements IPreprocessor {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();

    private FURenderer mFURenderer;
    private Context mContext;
    private boolean mEnabled;
    public static volatile boolean needCapture = false;

    public PreprocessorFaceUnity(Context context) {
        mContext = context;
        mEnabled = true;
    }


    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (mFURenderer == null || !mEnabled) {
            return outFrame;
        }
        if(needCapture){
            long currentTime = System.currentTimeMillis();
            TextureIdHelp textureIdHelp = new TextureIdHelp();
            Bitmap pre = textureIdHelp.FrameToBitmap(outFrame,true);
            textureIdHelp.saveBitmap2Gallery(mContext, pre, currentTime, "PRE");

            textureIdHelp.release();
            // process this frame
            outFrame.textureId = mFURenderer.onDrawFrameDualInput(outFrame.image,
                    outFrame.textureId,  outFrame.format.getWidth(),
                    outFrame.format.getHeight());

            // The texture is transformed to texture2D by beauty module.
            outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
            pre.recycle();

            Bitmap after = textureIdHelp.FrameToBitmap(outFrame,false);
            textureIdHelp.saveBitmap2Gallery(mContext, after, currentTime, "AFTER");

            after.recycle();
            textureIdHelp.release();
            needCapture = false;

        }else {
            // process this frame
            outFrame.textureId = mFURenderer.onDrawFrameDualInput(outFrame.image,
                    outFrame.textureId, outFrame.format.getWidth(),
                    outFrame.format.getHeight());

            // The texture is transformed to texture2D by beauty module.
            outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        }
        return outFrame;
    }

    @Override
    public void initPreprocessor() {
        // only call once when app launched
        Log.e(TAG, "initPreprocessor: ");
        mFURenderer = new FURenderer.Builder(mContext)
                .setInputTextureType(FURenderer.INPUT_TEXTURE_EXTERNAL_OES)
                .setCameraFacing(FURenderer.CAMERA_FACING_FRONT)
                .setInputImageOrientation(CameraUtils.getCameraOrientation(FURenderer.CAMERA_FACING_FRONT))
                .build();
//        mFURenderer.onSurfaceCreated();
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        // not called
        Log.d(TAG, "releasePreprocessor: ");
        //这个可以不写，在FUChatActivity中添加了
//        if (mFURenderer != null) {
//            mFURenderer.onSurfaceDestroyed();
//        }
    }

    public FURenderer getFURenderer() {
        return mFURenderer;
    }
}
