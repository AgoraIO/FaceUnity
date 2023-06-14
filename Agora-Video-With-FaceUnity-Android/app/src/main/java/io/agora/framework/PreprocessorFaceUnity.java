package io.agora.framework;

import android.util.Size;

import com.faceunity.FUConfig;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeautyBlurTypeEnum;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.utils.FuDeviceUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.YuvHelper;
import io.agora.rtc2.gl.EglBaseProvider;
import io.agora.rtc2.video.IVideoFrameObserver;

public class PreprocessorFaceUnity implements IVideoFrameObserver {
    private final static String TAG = PreprocessorFaceUnity.class.getSimpleName();
    private final static android.graphics.Matrix IDENTITY_MATRIX = new android.graphics.Matrix();

    private final FURenderer mFURenderer = FURenderer.getInstance();
    private boolean enable = true;

    private TextureBufferHelper textureBufferHelper;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private int mImageRotation = 0;
    private ByteBuffer mImageBuffer;
    private byte[] mImageNV21;
    private volatile boolean resetGLEnv = false;

    private volatile static PreprocessorFaceUnity instance;

    public static PreprocessorFaceUnity getInstance() {
        if (instance == null) {
            instance = new PreprocessorFaceUnity();
        }
        return instance;
    }

    private PreprocessorFaceUnity() {
    }


    public void setRenderEnable(boolean enabled) {
        enable = enabled;
    }

    public void resetGLEnv() {
        resetGLEnv = true;
    }


    /**
     * 检查当前人脸数量
     */
    private void cheekFaceNum() {
        //根据有无人脸 + 设备性能 判断开启的磨皮类型
        float faceProcessorGetConfidenceScore = FUAIKit.getInstance().getFaceProcessorGetConfidenceScore(0);
        if (faceProcessorGetConfidenceScore >= 0.95) {
            //高端手机并且检测到人脸开启均匀磨皮，人脸点位质
            if (FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.EquallySkin) {
                FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.EquallySkin);
                FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(true);
            }
        } else {
            if (FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.FineSkin) {
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

        if (!enable) {
            return true;
        }

        final VideoFrame.Buffer buffer = videoFrame.getBuffer();

        if (textureBufferHelper == null) {
            textureBufferHelper = TextureBufferHelper.create(this.getClass().getSimpleName(), EglBaseProvider.instance().getRootEglBase().getEglBaseContext());
        } else if (resetGLEnv) {
            textureBufferHelper.dispose();
            resetGLEnv = false;
            return false;
        }

        if (mImageWidth != buffer.getWidth() || mImageHeight != buffer.getHeight()) {
            mImageWidth = buffer.getWidth();
            mImageHeight = buffer.getHeight();
            return false;
        }

        if (FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID) {
            //高性能设备
            textureBufferHelper.invoke((Callable<Void>) () -> {
                cheekFaceNum();
                return null;
            });
        }

        boolean skipFrame = false;
        if (mImageRotation != videoFrame.getRotation()) {
            mImageRotation = videoFrame.getRotation();
            skipFrame = true;
        }
        boolean isFront = mImageRotation == 270;
        //shouldMirror = isFront;

        int outTexId;
        android.graphics.Matrix outMatrix;
        if (buffer instanceof VideoFrame.TextureBuffer) {

            if (isFront) {
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
            } else {
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0);
            }

            VideoFrame.TextureBuffer texBuffer = (VideoFrame.TextureBuffer) buffer;
            Size originSize = VideoCaptureUtils.getCaptureOriginSize(texBuffer);
            outTexId = textureBufferHelper.invoke(() -> mFURenderer.onDrawFrameDualInput(
                    texBuffer.getTextureId(), originSize.getWidth(), originSize.getHeight()
            ));
            outMatrix = texBuffer.getTransformMatrix();
        } else {
            if (isFront) {
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0);
            } else {
                mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
            }

            VideoFrame.I420Buffer i420Buffer = buffer.toI420();
            int nv21Size = mImageHeight * mImageWidth * 3 / 2;
            if (mImageBuffer == null || mImageBuffer.capacity() != nv21Size) {
                if (mImageBuffer != null) {
                    mImageBuffer.clear();
                }
                mImageBuffer = ByteBuffer.allocateDirect(nv21Size);
                mImageNV21 = new byte[nv21Size];
            }

            YuvHelper.I420ToNV12(i420Buffer.getDataY(), i420Buffer.getStrideY(),
                    i420Buffer.getDataV(), i420Buffer.getStrideV(),
                    i420Buffer.getDataU(), i420Buffer.getStrideU(),
                    mImageBuffer, mImageWidth, mImageHeight);
            mImageBuffer.position(0);
            mImageBuffer.get(mImageNV21);
            i420Buffer.release();

            outTexId = textureBufferHelper.invoke(() -> mFURenderer.onDrawFrameInput(
                    mImageNV21, i420Buffer.getWidth(), i420Buffer.getHeight()
            ));
            outMatrix = IDENTITY_MATRIX;
        }

        if(skipFrame){
            return false;
        }

        VideoFrame.TextureBuffer textureBuffer = textureBufferHelper.wrapTextureBuffer(mImageWidth, mImageHeight,
                VideoFrame.TextureBuffer.Type.RGB,
                outTexId, outMatrix);
        videoFrame.replaceBuffer(textureBuffer, mImageRotation, videoFrame.getTimestampNs());
        return true;
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
