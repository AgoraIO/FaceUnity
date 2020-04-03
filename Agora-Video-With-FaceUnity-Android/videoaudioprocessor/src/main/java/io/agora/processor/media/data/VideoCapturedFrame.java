package io.agora.processor.media.data;

import android.graphics.SurfaceTexture;
import android.opengl.Matrix;

import io.agora.processor.common.constant.Constant;

public class VideoCapturedFrame extends CapturedFrame {

    public static final int NO_TEXTURE = -1;
    public static final float[] DEFAULT_MATRIX = new float[16];
    public int mTextureId = NO_TEXTURE;
    public int mEffectTextureId = NO_TEXTURE;
    public float[] mTexMatrix;
    public float[] mMvpMatrix;
    public int mRotation;
    public long mTimeStamp;
    public int videoWidth;
    public int videoHeight;
    public SurfaceTexture mSurfaceTexture;
    public boolean mMirror;
    public int frameFace = Constant.CAMERA_FACING_FRONT;

    //for video raw data
    public VideoCapturedFrame(byte[] rawData, int videoWidth, int videoHeight,int frameFace, MediaFrameFormat.FrameType frameType) {
        this.frameFace = frameFace;
        this.rawData = rawData;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.frameType = frameType;
    }

    //for video texture
    public VideoCapturedFrame(SurfaceTexture texture, int textureId, float[] mvpMatrix,int frameFace, MediaFrameFormat.FrameType frameType) {
        this.frameFace = frameFace;
        mSurfaceTexture = texture;
        mMvpMatrix = mvpMatrix;
        mTextureId = textureId;
        this.frameType = frameType;
    }

    //for video texture
    public VideoCapturedFrame(SurfaceTexture texture, int textureId, byte[] data, float[] matrix,int frameFace, long ts, int rotation, boolean mirror, MediaFrameFormat.FrameType frameType) {
        this.frameFace = frameFace;
        mTextureId = textureId;
        rawData = data;
        mTimeStamp = ts;
        mRotation = rotation;
        mSurfaceTexture = texture;
        mMirror = mirror;
        this.frameType = frameType;
        if (matrix != null && matrix.length == 16) {
            mTexMatrix = matrix;
        } else {
            mTexMatrix = DEFAULT_MATRIX;
            Matrix.setIdentityM(mTexMatrix, 0);
        }
    }

}