package io.agora.kit.media.render;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.WindowManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.agora.kit.media.capture.VideoCaptureFrame;
import io.agora.kit.media.connector.SinkConnector;
import io.agora.kit.media.connector.SrcConnector;
import io.agora.kit.media.gles.ProgramTexture2d;
import io.agora.kit.media.gles.ProgramTextureOES;
import io.agora.kit.media.gles.core.GlUtil;
import io.agora.kit.media.util.FPSUtil;
import io.agora.kit.media.util.RotationUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoRender implements SinkConnector<VideoCaptureFrame> {
    public final static String TAG = VideoRender.class.getSimpleName();

    private GLSurfaceView mGLSurfaceView;
    private Context mContext;
    private VideoCaptureFrame mVideoCaptureFrame;
    private int mCameraTextureId;
    private int mEffectTextureId;
    private float[] mMTX = new float[16];
    private float[] mMVP = new float[16];

    private SrcConnector<Integer> mTexConnector;
    private SrcConnector<VideoCaptureFrame> mFrameConnector;
    private SrcConnector<VideoCaptureFrame> mTransmitConnector;

    private FPSUtil mFPSUtil;
    private boolean mLastMirror;
    private boolean mMVPInit;

    private int mViewWidth;
    private int mViewHeight;

    private volatile boolean mNeedsDraw = false;
    private volatile boolean mRequestDestroy = false;

    private ProgramTexture2d mFullFrameRectTexture2D;
    private ProgramTextureOES mTextureOES;

    // Display rotation is the angle in degrees from natural
    // orientation to the current surface orientation
    // counter-clockwise
    private int mDisplayRotation;

    // Clockwise angle in degrees by which the current
    // surface rotates to natural orientation.
    // The sum of display and counter-display rotations
    // should be 360
    private int mCounterDisplayRotation;

    private GLSurfaceView.Renderer mGLRenderer = new GLSurfaceView.Renderer() {
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mFullFrameRectTexture2D = new ProgramTexture2d();
            mTextureOES = new ProgramTextureOES();
            mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            mTexConnector.onDataAvailable(new Integer(mCameraTextureId));

            Log.e(TAG, "onSurfaceCreated gl " + gl + " " + config + " " + mGLSurfaceView + " " + mGLRenderer);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            mViewWidth = width;
            mViewHeight = height;
            mMVPInit = false;
            mDisplayRotation = getDisplayRotation();
            mCounterDisplayRotation = (360 - mDisplayRotation) % 360;
            mFPSUtil.resetLimit();

            Log.e(TAG, "onSurfaceChanged gl " + gl + " " + width + " " + height + " " + mGLSurfaceView + " " + mGLRenderer);
        }

        private int getDisplayRotation() {
            WindowManager manager = (WindowManager) mContext.
                    getSystemService(Context.WINDOW_SERVICE);
            return RotationUtil.getRotation(manager.getDefaultDisplay().getRotation());
        }

        public void onDrawFrame(GL10 gl) {
            if (mNeedsDraw) {
                VideoCaptureFrame frame = mVideoCaptureFrame;
                try {
                    frame.mSurfaceTexture.updateTexImage();

                    frame.mSurfaceTexture.getTransformMatrix(mMTX);
                    frame.mTexMatrix = mMTX;
                } catch (Exception e) {
                    Log.e(TAG, "updateTexImage failed, ignore " + Log.getStackTraceString(e));
                    return;
                }

                if (frame.mImage == null) {
                    mFullFrameRectTexture2D.drawFrame(mEffectTextureId, frame.mTexMatrix, mMVP);
                    Log.e(TAG, "return with texture id");
                    return;
                }

                mEffectTextureId = mFrameConnector.onDataAvailable(frame);

                if (!mMVPInit) {
                    // The texture transform matrix will help adjust the
                    // direction of texture to the direction of views,
                    // because of which, the clockwise rotation for the
                    // images to be upright is fortunately the display
                    // rotation.
                    mMVP = GlUtil.getVertexMatrix(mViewWidth, mViewHeight,
                            frame.mFormat.getWidth(), frame.mFormat.getHeight(),
                            shouldSwapWH(frame.mCameraRotation, mDisplayRotation),
                            rotateDegree(frame.mCameraRotation));
                    mMVPInit = true;
                }

                if (frame.mMirror != mLastMirror) {
                    mLastMirror = frame.mMirror;
                    flipFrontX();
                }

                if (mEffectTextureId <= 0) {
                    mTextureOES.drawFrame(frame.mTextureId, frame.mTexMatrix, mMVP);
                } else {
                    frame.mTextureId = mEffectTextureId;
                    mFullFrameRectTexture2D.drawFrame(frame.mTextureId, frame.mTexMatrix, mMVP);
                }

                // Tells the rtc engine how to adjust images rotation
                // that is caused by the surface rotation
                frame.mSurfaceRotation = mCounterDisplayRotation;
                mTransmitConnector.onDataAvailable(frame);

                mFPSUtil.limit();

                if (mRequestDestroy) {
                    doDestroy();
                }
            }
        }

        private boolean shouldSwapWH(int texRotation, int surfaceRotation) {
            if (texRotation == 0 || texRotation == 180) {
                // The texture itself does not need to rotate (because
                // the camera orientation has been fixed, for example),
                // or it should be rotated 180, in which case
                // the texture will not swap its width and height.
                return false;
            }

            // By default, the images from camera are horizontal,
            // which means if the surface is portrait, the images
            // must swap their width and height to be displayed
            // appropriately
            return surfaceRotation == 0 || surfaceRotation == 180;
        }

        private int rotateDegree(int texRotation) {
            if (texRotation == 90 || texRotation == 270) {
                return mDisplayRotation;
            } else {
                return 0;
            }
        }
    };

    private CountDownLatch mDestroyLatch;

    public VideoRender(Context context) {
        mContext = context;
        mDestroyLatch = new CountDownLatch(1);
        mFPSUtil = new FPSUtil();
        mTexConnector = new SrcConnector<>();
        mFrameConnector = new SrcConnector<>();
        mTransmitConnector = new SrcConnector<>();
    }

    public void setRenderView(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mGLSurfaceView.setRenderer(mGLRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public SrcConnector<Integer> getTexConnector() {
        return mTexConnector;
    }

    public SrcConnector<VideoCaptureFrame> getFrameConnector() {
        return mFrameConnector;
    }

    public SrcConnector<VideoCaptureFrame> getTransmitConnector() {
        return mTransmitConnector;
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

        mTexConnector.disconnect();
        mFrameConnector.disconnect();
        mTransmitConnector.disconnect();

        mDestroyLatch.countDown();

        Log.e(TAG, "doDestroy " + mDestroyLatch.getCount());
    }

    public int onDataAvailable(VideoCaptureFrame frame) {
        mVideoCaptureFrame = frame;

        if (mRequestDestroy && !mNeedsDraw) {
            return -1;
        }

        mNeedsDraw = true;

        mGLSurfaceView.requestRender();
        return 0;
    }
}

