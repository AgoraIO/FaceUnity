package io.agora.processor.video.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.base.BaseRender;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.gles.ProgramTexture2d;
import io.agora.processor.media.gles.ProgramTextureOES;
import io.agora.processor.media.gles.VaryTools;
import io.agora.processor.media.gles.core.EglCore;
import io.agora.processor.media.gles.core.GlUtil;
import io.agora.processor.media.gles.core.WindowSurface;
import io.agora.processor.media.data.VideoCapturedFrame;

/**
 * this render support textureView and surfaceview
 */
public class RenderWithSurfaceView extends BaseRender implements SurfaceHolder.Callback, TextureView.SurfaceTextureListener {
    public final static String TAG = RenderWithSurfaceView.class.getSimpleName();

    private Surface renderSurface;
    private SurfaceView renderSurfaceView;
    private TextureView renderTextureView;
    private int mCameraTextureId;
    private float[] mMTX = new float[16];
    private float[] mMVP = new float[16];
    private float[] mSendMVP = new float[16];

    private boolean mLastMirror;
    private boolean mMVPInit;

    private int mViewWidth;
    private int mViewHeight;

    private volatile boolean mNeedsDraw = false;
    private volatile boolean isFrameUpdated = true;
    private volatile boolean mRequestDestroy = false;
    private ProgramTexture2d mFullFrameRectTexture2D;
    private ProgramTextureOES mTextureOES;
    private EGLContext eglShareContext = null;
    private VideoCaptureConfigInfo videoCaptureConfigInfo;
    private RenderThread mRenderThread;
    private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();

    //Thread use to render picture
    private class RenderThread extends Thread implements
            SurfaceTexture.OnFrameAvailableListener {
        private volatile RenderHandler mRenderHandler;

        private Object mStartLock = new Object();
        private boolean mReady = false;
        private boolean isSurfaceCreated = false;
        private boolean isSurfaceBackGroud = false;
        private EglCore mEglCore;
        private WindowSurface mWindowSurface = null;
        private boolean configOrientation = false;

        @Override
        public void run() {
            Looper.prepare();

            mRenderHandler = new RenderHandler(this);
            LogUtil.i("init EglCore");
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();
            }
            mEglCore = new EglCore(eglShareContext, 0);
            LogUtil.i("init RenderThread");
            Looper.loop();
            LogUtil.i("render looper quit");
            releaseGl();
            isSurfaceCreated = false;
            synchronized (mStartLock) {
                mReady = false;
            }
        }

        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        private void shutdown() {
            LogUtil.d("shutdown");
            Looper.myLooper().quit();
        }

        public RenderHandler getHandler() {
            return mRenderHandler;
        }

        private void surfaceAvailable(Surface surface) {
            LogUtil.d("surfaceAvailable " + surface);
            mWindowSurface = new WindowSurface(mEglCore, surface, false);
            mWindowSurface.makeCurrent();
            if (!isSurfaceCreated) {
                mFullFrameRectTexture2D = new ProgramTexture2d();
                mTextureOES = new ProgramTextureOES();
                mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                LogUtil.d("mTexConnector  onDataAvailable " + mCameraTextureId);
                mTexConnector.onDataAvailable(new Integer(mCameraTextureId));
            }
            if (mRenderListener != null) {
                mRenderListener.onEGLContextReady();
            }
            isSurfaceCreated = true;

        }

        private void surfaceTexureAvailable(int width, int height, SurfaceTexture surfaceTexture) {

            mWindowSurface = new WindowSurface(mEglCore, surfaceTexture);
            mWindowSurface.makeCurrent();
            if (!isSurfaceCreated) {
                mFullFrameRectTexture2D = new ProgramTexture2d();
                mTextureOES = new ProgramTextureOES();
                mCameraTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                mTexConnector.onDataAvailable(new Integer(mCameraTextureId));
            }
            surfaceChanged(width, height);
            isSurfaceCreated = true;
            isSurfaceBackGroud = false;
        }

        private void surfaceChanged(int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            mViewWidth = width;
            mViewHeight = height;
            if (mNeedsDraw) {
                mMVP = GlUtil.changeMVPMatrix(GlUtil.IDENTITY_MATRIX, mViewWidth, mViewHeight,
                        mVideoCaptureFrame.videoHeight, mVideoCaptureFrame.videoWidth);
            }
            mMVPInit = false;
            mFPSUtil.resetLimit();
            isSurfaceBackGroud = false;
        }

        private void releaseGl() {
            GlUtil.checkGlError("releaseGl start");
            if (mFullFrameRectTexture2D != null) {
                mFullFrameRectTexture2D.release();
                mFullFrameRectTexture2D = null;
            }

            if (mTextureOES != null) {
                mTextureOES.release();
                mTextureOES = null;
            }
            if (mWindowSurface != null) {
                mWindowSurface.release();
                mWindowSurface = null;
            }
            GlUtil.checkGlError("releaseGl done");

            mEglCore.makeNothingCurrent();
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            eglShareContext = null;
        }

        private void surfaceDestroyed() {
            LogUtil.d("RenderThread surfaceDestroyed");
            isSurfaceBackGroud = true;
            mMVPInit = false;
            //releaseGl();
            //isSurfaceCreated = false;
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mRenderHandler.sendFrameAvailable();
        }

        private void frameAvailable() {
            draw();
        }

        private void draw() {
            if (mNeedsDraw) {
                if (!isSurfaceBackGroud) {
                    mWindowSurface.makeCurrent();
                }
                VideoCapturedFrame frame = mVideoCaptureFrame;
                try {
                    frame.mSurfaceTexture.updateTexImage();

                    frame.mSurfaceTexture.getTransformMatrix(mMTX);
                    frame.mTexMatrix = mMTX;
                } catch (Exception e) {
                    LogUtil.e("updateTexImage failed, ignore " + Log.getStackTraceString(e));
                    return;
                }

                if (frame.rawData == null) {
                    //not render when raw data is null
                    return;
                }

                mBeautyConnector.onDataAvailable(frame);
                //LogUtil.i("RenderThread onDataAvailable "+frame.mEffectTextureId +" "+frame.mTextureId);
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
                if (isFrameUpdated) {
                    mRenderedConnector.onDataAvailable(frame);
                    isFrameUpdated = false;
                }
                mWindowSurface.swapBuffers();
                mFPSUtil.limit();
                LogUtil.v("swapBuffers ");
                if (mRequestDestroy) {
                    doDestroy();
                }
            }
        }


        public void excuteEvent() {
            Runnable event = null;
            if (!mEventQueue.isEmpty()) {
                event = mEventQueue.remove(0);
            }
            if (event != null) {
                event.run();
                event = null;
            }
        }
    }

    private static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_SIZE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_REDRAW = 9;
        private static final int MSG_SURFACE_VIEW_CHANGED = 10;
        private static final int MSG_SURFACE_VIEW_DESTORY = 11;
        private static final int MSG_SURFACE_TEXTURE_AVAILABLE = 12;
        private static final int MSG_SURFACE_TEXTURE_DESTORY = 13;
        private static final int MSG_SURFACE_TEXTURE_CHANGED = 14;
        private static final int MSG_QUEUE_EVENT = 15;

        private WeakReference<RenderThread> mWeakRenderThread;

        public RenderHandler(RenderThread rt) {
            mWeakRenderThread = new WeakReference<RenderThread>(rt);
        }

        public void sendSurfaceAvailable(Surface surface) {
            LogUtil.i("sendSurfaceAvailable");
            sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE, surface));
        }

        public void sendSurfaceViewChanged(int width, int height) {
            LogUtil.i("sendSurfaceViewChanged");
            sendMessage(obtainMessage(MSG_SURFACE_VIEW_CHANGED, width, height));
        }

        public void sendSurfaceViewDestroyed(SurfaceHolder surfaceHolder) {
            LogUtil.i("sendSurfaceViewDestroyed");
            sendMessage(obtainMessage(MSG_SURFACE_VIEW_DESTORY,
                    surfaceHolder));
        }


        public void sendTextureViewAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            LogUtil.i("sendTextureViewAvailable");
            sendMessage(obtainMessage(MSG_SURFACE_TEXTURE_AVAILABLE, width, height, surfaceTexture));
        }

        public void sendTextureViewChanged(int width, int height) {
            LogUtil.i("sendTextureViewChanged");
            sendMessage(obtainMessage(MSG_SURFACE_TEXTURE_CHANGED,
                    width, height));
        }

        public void sendTexureViewDestroy(SurfaceTexture surfaceTexture) {
            LogUtil.i("sendTexureViewDestroy");
            sendMessage(obtainMessage(MSG_SURFACE_TEXTURE_DESTORY,
                    surfaceTexture));
        }

        public void sendQueueEvent() {
            LogUtil.i("sendQueueEvent");
            sendMessage(obtainMessage(MSG_QUEUE_EVENT));
        }

        public void sendShutdown() {
            LogUtil.i("sendShutdown");
            sendMessage(obtainMessage(MSG_SHUTDOWN));
        }

        public void sendFrameAvailable() {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE));
        }

        public void sendRedraw() {
            sendMessage(obtainMessage(MSG_REDRAW));
        }

        public boolean getEglContextState() {
            RenderThread renderThread = mWeakRenderThread.get();
            return renderThread.isSurfaceCreated;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_SURFACE_AVAILABLE:
                    renderThread.surfaceAvailable((Surface) msg.obj);
                    break;
                case MSG_SURFACE_TEXTURE_AVAILABLE:
                    renderThread.surfaceTexureAvailable(msg.arg1, msg.arg2, (SurfaceTexture) msg.obj);
                    break;
                case MSG_SURFACE_VIEW_CHANGED:
                case MSG_SURFACE_TEXTURE_CHANGED:
                case MSG_SURFACE_SIZE_CHANGED:
                    renderThread.surfaceChanged(msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_VIEW_DESTORY:
                case MSG_SURFACE_TEXTURE_DESTORY:
                case MSG_SURFACE_DESTROYED:
                    renderThread.surfaceDestroyed();
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                case MSG_FRAME_AVAILABLE:
                    renderThread.frameAvailable();
                    break;
                case MSG_REDRAW:
                    renderThread.draw();
                    break;
                case MSG_QUEUE_EVENT:
                    renderThread.excuteEvent();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }


    private void flipFrontX() {
        Matrix.scaleM(mMVP, 0, -1, 1, 1);
    }

    @Override
    public void destroy() {
        mRequestDestroy = true;
        try {
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendShutdown();
            mDestroyLatch.await(100, TimeUnit.MILLISECONDS);
            doDestroy();
        } catch (InterruptedException e) {
            doDestroy();
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean isEglContextReady() {
        if (mRenderThread == null) {
            return false;
        }
        RenderHandler rh = mRenderThread.getHandler();
        return rh.getEglContextState();
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
        LogUtil.i("doDestroy " + mDestroyLatch.getCount());
    }


    @Override
    public void onDataAvailable(CapturedFrame frame) {
        mVideoCaptureFrame = (VideoCapturedFrame) frame;
        if (mRequestDestroy && !mNeedsDraw) {
            return;
        }
        mNeedsDraw = true;
        isFrameUpdated = true;
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendFrameAvailable();
    }

    public RenderWithSurfaceView(EGLContext eglContext, VideoCaptureConfigInfo videoCaptureConfigInfo) {
        super();
        this.eglShareContext = eglContext;
        this.videoCaptureConfigInfo = videoCaptureConfigInfo;
        initRenderThread();
    }

    private void initRenderThread() {
        mRenderThread = new RenderThread();
        mRenderThread.setName("RenderWithSurfaceView");
        mRenderThread.start();
        mRenderThread.waitUntilReady();
    }

    private void setRenderSurfaceView(SurfaceView surfaceView) {
        renderSurfaceView = surfaceView;
        this.renderSurfaceView.getHolder().addCallback(this);
    }

    private void setRenderTextureView(TextureView textureView) {
        renderTextureView = textureView;
        this.renderTextureView.setSurfaceTextureListener(this);
    }

    public void setRenderSurface(Surface surface) {
        renderSurface = surface;
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendSurfaceAvailable(renderSurface);
    }

    @Override
    public boolean setRenderView(View view) {
        if (view instanceof SurfaceView) {
            setRenderSurfaceView((SurfaceView) view);
            LogUtil.i("setRenderSurfaceView");
            return true;
        } else if (view instanceof TextureView) {
            LogUtil.i("setRenderTextureView");
            setRenderTextureView((TextureView) view);
            return true;
        }
        return false;
    }

    public void runInRenderThread(Runnable r) {
        if (r == null) {
            throw new IllegalArgumentException("r must not be null");
        }
        mEventQueue.add(r);
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendQueueEvent();
    }


    //surfaceview deal with
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        RenderHandler rh = mRenderThread.getHandler();
        rh.removeCallbacksAndMessages(null);
        rh.sendSurfaceAvailable(surfaceHolder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendSurfaceViewChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendSurfaceViewDestroyed(surfaceHolder);
    }

    //textureView deal with
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        RenderHandler rh = mRenderThread.getHandler();
        rh.removeCallbacksAndMessages(null);
        rh.sendTextureViewAvailable(surfaceTexture, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendTextureViewChanged(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendTexureViewDestroy(surfaceTexture);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

}

