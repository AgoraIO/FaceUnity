package io.agora.processor.video.renderer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.agora.processor.media.base.BaseRender;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.gles.ProgramTexture2d;
import io.agora.processor.media.gles.ProgramTextureOES;
import io.agora.processor.media.gles.VaryTools;
import io.agora.processor.media.gles.core.GlUtil;
import io.agora.processor.media.data.VideoCapturedFrame;


/**
 * this render support glsurfaceview
 */
public class RenderInGlSurfaceView extends BaseRender {
    public final static String TAG = RenderInGlSurfaceView.class.getSimpleName();

    private GLSurfaceView mGLSurfaceView;
    private int mCameraTextureId;
    private float[] mMTX = new float[16];
    private float[] mMVP = new float[16];
    private float[] mSendMVP = new float[16];
    private boolean configOrientation = false;
    private boolean mLastMirror;
    private boolean mMVPInit;

    private int mViewWidth;
    private int mViewHeight;

    private volatile boolean mNeedsDraw = false;
    private volatile boolean mRequestDestroy = false;

    private ProgramTexture2d mFullFrameRectTexture2D;
    private ProgramTextureOES mTextureOES;
    private boolean isSurfaceCreated = false;
    private VideoCaptureConfigInfo videoCaptureConfigInfo;
    private GLSurfaceView.Renderer mGLRenderer = new GLSurfaceView.Renderer() {
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mFullFrameRectTexture2D = new ProgramTexture2d();
            mTextureOES = new ProgramTextureOES();
            mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            mTexConnector.onDataAvailable(new Integer(mCameraTextureId));
            isSurfaceCreated = true;
            if (mRenderListener != null) {
                mRenderListener.onEGLContextReady();
            }
            Log.e(TAG, "onSurfaceCreated gl " + gl + " " + config + " " + mGLSurfaceView + " " + mGLRenderer);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            mViewWidth = width;
            mViewHeight = height;
            if (mNeedsDraw) {
                mMVP = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, mViewWidth, mViewHeight,
                        mVideoCaptureFrame.videoHeight, mVideoCaptureFrame.videoWidth);
            }
            mFPSUtil.resetLimit();
            isSurfaceCreated = true;
            Log.e(TAG, "onSurfaceChanged gl " + gl + " " + width + " " + height + " " + mGLSurfaceView + " " + mGLRenderer);
        }

        public void onDrawFrame(GL10 gl) {
            if (mNeedsDraw) {
                VideoCapturedFrame frame = mVideoCaptureFrame;
                try {
                    frame.mSurfaceTexture.updateTexImage();

                    frame.mSurfaceTexture.getTransformMatrix(mMTX);
                    frame.mTexMatrix = mMTX;
                } catch (Exception e) {
                    Log.e(TAG, "updateTexImage failed, ignore " + Log.getStackTraceString(e));
                    return;
                }

                if (frame.rawData == null) {
                    //not render when raw data is null
                    return;
                }

                mBeautyConnector.onDataAvailable(frame);

                if (!mMVPInit || configOrientation != videoCaptureConfigInfo.isHorizontal()) {
                    mMVP = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, mViewWidth, mViewHeight,
                            frame.videoHeight, frame.videoWidth);
                    if (videoCaptureConfigInfo.isHorizontal()) {
                        VaryTools tools = new VaryTools();
                        tools.pushMatrix();
                        tools.rotate(90, 0, 0, 1);
                        mSendMVP = GlUtil.changeMVPMatrix(tools.getFinalMatrix(), frame.videoWidth, frame.videoHeight,
                                frame.videoWidth, frame.videoHeight);
                    } else {
                        mSendMVP = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, frame.videoHeight, frame.videoWidth,
                                frame.videoHeight, frame.videoWidth);
                    }


//                    if(isScreenOriatationPortrait(context)){
//                        LogUtil.i("isScreenOriatationPortrait true");
//                        mMVP = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, mViewWidth, mViewHeight,
//                                frame.videoHeight, frame.videoWidth);
//                        mSendMVP = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, frame.videoHeight, frame.videoWidth,
//                                frame.videoHeight, frame.videoWidth);
//                    }else{
//                        LogUtil.i("isScreenOriatationPortrait true");
//                        VaryTools tools = new VaryTools();
//                        tools.pushMatrix();
//                        tools.rotate(270,0,0,1);
//                        //mMVP = tools.getFinalMatrix();
//                        mMVP = GlUtil.changeMVPMatrix(tools.getFinalMatrix(), mViewWidth,mViewHeight ,
//                                frame.videoWidth , frame.videoHeight);
//                        mSendMVP = GlUtil.changeMVPMatrix(tools.getFinalMatrix(),frame.videoWidth,frame.videoHeight,
//                                frame.videoWidth ,frame.videoHeight );
//                    }
//                    if (mRenderListener != null) {
//                        mRenderListener.onViewIsPortrait(true);
//                    }
                    mMVPInit = true;
                }
                configOrientation = videoCaptureConfigInfo.isHorizontal();

                if (frame.mMirror != mLastMirror) {
                    mLastMirror = frame.mMirror;
                    flipFrontX();
                }
                frame.mMvpMatrix = mMVP;
                if (frame.mEffectTextureId <= 0) {
                    mTextureOES.drawFrame(frame.mTextureId, frame.mTexMatrix, mMVP);
                } else {
                    mFullFrameRectTexture2D.drawFrame(frame.mEffectTextureId, frame.mTexMatrix, mMVP);
                }
                frame.mMvpMatrix = mSendMVP;
                mRenderedConnector.onDataAvailable(frame);

                mFPSUtil.limit();

                if (mRequestDestroy) {
                    doDestroy();
                }
            }
        }
    };

    @Override
    public boolean isEglContextReady() {
        return isSurfaceCreated;
    }

    public RenderInGlSurfaceView(VideoCaptureConfigInfo videoCaptureConfigInfo) {
        super();
        this.videoCaptureConfigInfo = videoCaptureConfigInfo;
    }

    public boolean setRenderView(View view) {
        if (view instanceof GLSurfaceView) {
            mGLSurfaceView = (GLSurfaceView) view;
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setPreserveEGLContextOnPause(true);
            mGLSurfaceView.setRenderer(mGLRenderer);
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            Log.i(TAG, "setRenderGLSurfaceView");
            return true;
        }
        return false;
    }

    @Override
    public void runInRenderThread(Runnable r) {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.queueEvent(r);
        }

    }

    private void flipFrontX() {
        Matrix.scaleM(mMVP, 0, -1, 1, 1);
    }

    public void destroy() {
        mRequestDestroy = true;

        try {
            mDestroyLatch.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            doDestroy();
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void doDestroy() {
        mNeedsDraw = false;

        if (mFullFrameRectTexture2D != null) {
            mFullFrameRectTexture2D.release();
            mFullFrameRectTexture2D = null;
        }

        if (mTextureOES != null) {
            mTextureOES.release();
            mTextureOES = null;
        }

        mTexConnector.clear();
        mBeautyConnector.clear();
        mRenderedConnector.clear();

        mDestroyLatch.countDown();

        Log.e(TAG, "doDestroy " + mDestroyLatch.getCount());
    }

    public void onDataAvailable(CapturedFrame frame) {
        mVideoCaptureFrame = (VideoCapturedFrame) frame;

        if (mRequestDestroy && !mNeedsDraw) {
            return;
        }

        mNeedsDraw = true;

        mGLSurfaceView.requestRender();
    }
}

