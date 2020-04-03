package io.agora.processor.media.internal;

import android.media.MediaCodecInfo;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.view.Surface;

import io.agora.processor.media.gles.ProgramTexture2d;
import io.agora.processor.media.gles.ProgramTextureOES;
import io.agora.processor.media.gles.core.EglCore;
import io.agora.processor.media.gles.core.Program;
import io.agora.processor.media.gles.core.WindowSurface;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.ProcessedData;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.VideoCapturedFrame;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.common.utils.ToolUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static io.agora.processor.common.constant.Constant.CAMERA_FACING_FRONT;
import static io.agora.processor.common.constant.Constant.LOCAL_RAW_VIDEO_FILE_PATH;
import static io.agora.processor.common.constant.Constant.LOCAL_YUV_VIDEO_FILE_PATH;

/**
 * Created by yong on 2019/8/31.
 */

/**
 * this class is used to divide encoder and capture module,
 * need to set to render thread after video encoder prepare
 * it worked just like a render when use with mediacodec and texture input,
 * raw data use connector ,texture direct to mediacodec input surface
 * <p>
 * usage:
 * construct the instance -->connect to the capture module-->connected by the encoder module
 * -->initEncoderContext(texture input only,make sure video codec has start and used in thread with egl conetx)-->setTextureId/frameAvailable-->
 * updateSharedContext(when needed)-->stopEncoderDataPrepare
 */
public class VideoPreProcessor implements Runnable {
    private static final int MSG_RELEASE_EGL_CONTEXT = 1;
    private static final int MSG_FRAME_AVAILABLE = 2;
    private static final int MSG_SET_TEXTURE_ID = 3;
    private static final int MSG_UPDATE_SHARED_CONTEXT = 4;
    private static final int MSG_QUIT = 5;
    private static final int MSG_INIT_EGL_CONTEXT = 6;

    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private Program programTexture;
    private int mTextureId;
    private int mFrameNum;
    private VideoCaptureConfigInfo videoCaptureConfigInfo;
    private boolean mMVPInit;
    // ----- accessed by multiple threads -----
    private volatile EncoderHandler mHandler;
    private Surface mInputSurface;
    private Object mReadyFence = new Object();      // guards ready/running
    private boolean mReady;
    private boolean mRunning;
    private EGLContext eglContext = null;
    private boolean isEglCoreInited = false;
    private int codecFormatType = 0;

    //count video/audio


    private boolean enableWriteVideoToFile = false;
    private String localVideoRawFilePath = null;
    private String localVideoYUVFilePath = null;

    public VideoPreProcessor(VideoCaptureConfigInfo videoCaptureConfigInfo) {
        this.videoCaptureConfigInfo = videoCaptureConfigInfo;
        this.startEncoderDataPrepare();
    }


    public ProcessedData preProcessVideoData(CapturedFrame capturedFrame) {
        synchronized (mReadyFence) {
            if (!mReady || !mRunning || capturedFrame == null) {
                return null;
            }
        }
        VideoCapturedFrame videoCapturedFrame = (VideoCapturedFrame) capturedFrame;
        if (videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            if (mHandler != null) {
                if (isEglCoreInited) {
                    LogUtil.d("preProcessVideoData texture");
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE, videoCapturedFrame));
                    return new ProcessedData(null, 0, 0, videoCapturedFrame.frameType);
                } else {
                    return null;
                }
            }
            return null;
        } else if (videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.BYTE_ARRAY) {
            if (!mReady) {
                return null;
            }
            if (capturedFrame.rawData == null) {
                LogUtil.e("data push in encoder is null");
                return null;
            }
            byte[] finalRawData = null;
            byte[] i420RawData = new byte[videoCapturedFrame.rawData.length];
            //change to 1420;
            if (videoCapturedFrame.frameFace == CAMERA_FACING_FRONT) {
                if (videoCaptureConfigInfo.isHorizontal()) {
                    byte[] i420TempData = new byte[videoCapturedFrame.rawData.length];
                    RawDataProcess.formatToI420(videoCapturedFrame.rawData, videoCapturedFrame.rawData.length,
                            videoCapturedFrame.videoHeight, videoCapturedFrame.videoWidth, 0, 0, i420TempData);
                    RawDataProcess.I420Mirror(i420TempData, i420RawData, videoCapturedFrame.videoWidth, videoCapturedFrame.videoHeight);

                } else {
                    byte[] i420TempData = new byte[videoCapturedFrame.rawData.length];
                    RawDataProcess.formatToI420(videoCapturedFrame.rawData, videoCapturedFrame.rawData.length,
                            videoCapturedFrame.videoWidth, videoCapturedFrame.videoHeight, 0, 270, i420TempData);
                    RawDataProcess.I420Mirror(i420TempData, i420RawData, videoCapturedFrame.videoHeight, videoCapturedFrame.videoWidth);
                }
            } else {
                if (videoCaptureConfigInfo.isHorizontal()) {
                    RawDataProcess.formatToI420(videoCapturedFrame.rawData, videoCapturedFrame.rawData.length,
                            videoCapturedFrame.videoWidth, videoCapturedFrame.videoHeight, 0, 0, i420RawData);
                } else {
                    RawDataProcess.formatToI420(videoCapturedFrame.rawData, videoCapturedFrame.rawData.length,
                            videoCapturedFrame.videoWidth, videoCapturedFrame.videoHeight, 0, 90, i420RawData);
                }
            }

            if (codecFormatType == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                //I420 data
                finalRawData = i420RawData;
                if (enableWriteVideoToFile) {
                    try {
                        ToolUtil.saveDataToFile(this.localVideoRawFilePath, videoCapturedFrame.rawData);
                        ToolUtil.saveDataToFile(this.localVideoYUVFilePath, finalRawData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //NV21 data
                //swap height and width after rotation
                finalRawData = new byte[videoCapturedFrame.rawData.length];
                RawDataProcess.I420toNV12(i420RawData, finalRawData, videoCapturedFrame.videoHeight, videoCapturedFrame.videoWidth);
                if (enableWriteVideoToFile) {
                    try {
                        ToolUtil.saveDataToFile(this.localVideoRawFilePath, videoCapturedFrame.rawData);
                        ToolUtil.saveDataToFile(this.localVideoYUVFilePath, finalRawData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            final ByteBuffer buf = ByteBuffer.allocateDirect(finalRawData.length);
            buf.clear();
            buf.put(finalRawData);
            buf.flip();
            //currentVideoPTSUs = ToolUtil.getPTSUs(prevVideoOutputPTSUs);
            //LogUtil.i("VideoPreProcessor video length:" + finalRawData.length + " width:" + videoCapturedFrame.videoWidth + " height:" + videoCapturedFrame.videoHeight+" pts:"+currentVideoPTSUs);
            ProcessedData processedData = new ProcessedData(buf, finalRawData.length, System.nanoTime() / 1000, videoCapturedFrame.frameType);
            return processedData;
        }
        return null;
    }


    @Override
    public void run() {
        // Establish a Looper for this thread, and define a Handler for it.
        LogUtil.i("VideoPreProcessor  thread start " + this.eglContext);
        Looper.prepare();
        if (this.eglContext != null && !isEglCoreInited) {
            handleInitEncoderContext(this.eglContext);
        }
        synchronized (mReadyFence) {
            mHandler = new EncoderHandler(this);
            mReady = true;
            mReadyFence.notify();
        }
        LogUtil.i("VideoPreProcessor  thread started");
        Looper.loop();

        LogUtil.i("VideoPreProcessor thread exiting");
        synchronized (mReadyFence) {
            mReady = mRunning = false;
            mHandler = null;
            eglContext = null;
        }
        LogUtil.i("VideoPreProcessor thread exiting over:" + mReady);
    }

    public int setEncoderInputSurface(Surface encoderInputSurface) {
        LogUtil.d("startEncoderDataPrepare");
        this.mInputSurface = encoderInputSurface;
        return 0;
    }

    private int startEncoderDataPrepare() {
        LogUtil.d("startEncoderDataPrepare");
        if (this.videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            synchronized (mReadyFence) {
                if (mRunning) {
                    LogUtil.w("Encoder thread already running " + this.videoCaptureConfigInfo.getVideoCaptureType());
                    return -2;
                }
                mRunning = true;
                new Thread(this, "VideoPreProcessor").start();
                while (!mReady) {
                    try {
                        mReadyFence.wait();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
            }
        } else if (this.videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.BYTE_ARRAY) {
            if (mRunning) {
                LogUtil.w("Encoder thread already running " + this.videoCaptureConfigInfo.getVideoCaptureType());
                return -3;
            }
            mRunning = true;
            mReady = true;
        }
        return 0;
    }

    public void stopEncoderDataPrepare() {
        //TODO check whay handler dead
        LogUtil.i("stopEncoderDataPrepare");
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RELEASE_EGL_CONTEXT));
            mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
        } else {
            mReady = mRunning = false;
        }
        while (mReady) {
            try {
                //make sure thread stoped
                Thread.sleep(10);
            } catch (Exception e) {
                LogUtil.e(e.toString());
            }
        }
        // We don't know when these will actually finish (or even start).  We don't want to
        // delay the UI thread though, so we return immediately.
    }

    public boolean updateSharedContext(EGLContext sharedContext) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SHARED_CONTEXT, sharedContext));
            return true;
        }
        return false;
    }

    public boolean initEncoderContext(EGLContext eglContext) {
        LogUtil.d("initEncoderContext");
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_INIT_EGL_CONTEXT, eglContext));
            return true;
        } else {
            this.eglContext = eglContext;
        }
        return false;
    }


    private void handleInitEncoderContext(EGLContext eglContext) {
        if (this.mInputSurface == null) {
            LogUtil.e("inputSurface is null,preProcessor module not work success");
            return;
        }
        releaseAllEGLResource();
        mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);

        mInputWindowSurface = new WindowSurface(mEglCore, this.mInputSurface, true);
        mInputWindowSurface.makeCurrent();
        if (this.videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE && this.videoCaptureConfigInfo.getVideoCaptureFormat() == VideoCaptureConfigInfo.CaptureFormat.TEXTURE_OES) {
            LogUtil.i("ProgramTextureOES " + eglContext);
            programTexture = new ProgramTextureOES();
        } else if (this.videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE && this.videoCaptureConfigInfo.getVideoCaptureFormat() == VideoCaptureConfigInfo.CaptureFormat.TEXTURE_2D) {
            LogUtil.i("ProgramTextureOES " + eglContext);
            programTexture = new ProgramTexture2d();
        }
        isEglCoreInited = true;
    }

    private void handleUpdateSharedContext(EGLContext newSharedContext) {
        LogUtil.d("handleUpdatedSharedContext " + newSharedContext);

        // Release the EGLSurface and EGLContext.
        releaseAllEGLResource();

        // Create a new EGLContext and recreate the window surface.
        mEglCore = new EglCore(newSharedContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface.recreate(mEglCore);
        mInputWindowSurface.makeCurrent();

        // Create new programs and such for the new context.
        if (this.videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE && this.videoCaptureConfigInfo.getVideoCaptureFormat() == VideoCaptureConfigInfo.CaptureFormat.TEXTURE_OES) {
            programTexture = new ProgramTextureOES();
        } else if (this.videoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE && this.videoCaptureConfigInfo.getVideoCaptureFormat() == VideoCaptureConfigInfo.CaptureFormat.TEXTURE_2D) {
            programTexture = new ProgramTexture2d();
        }
    }

    private float[] mMTX = new float[16];
    private float[] mMVP = new float[16];

    private void handleFrameAvailable(VideoCapturedFrame videoCapturedFrame) {
        if (this.videoCaptureConfigInfo.getVideoCaptureFormat() == VideoCaptureConfigInfo.CaptureFormat.TEXTURE_OES) {
            mTextureId = videoCapturedFrame.mTextureId;
        } else {
            mTextureId = videoCapturedFrame.mEffectTextureId;
        }

        videoCapturedFrame.mSurfaceTexture.getTransformMatrix(mMTX);
        videoCapturedFrame.mTexMatrix = mMTX;
        LogUtil.d("handleFrameAvailable videoCapturedFrame" + videoCapturedFrame + " time:" + videoCapturedFrame.mSurfaceTexture.getTimestamp());
        programTexture.drawFrame(mTextureId, videoCapturedFrame.mTexMatrix, videoCapturedFrame.mMvpMatrix);
        mInputWindowSurface.swapBuffers();
    }

    private void handleRleaseEglContext() {
        LogUtil.i("handleRleaseEglContext");
        releaseAllEGLResource();
        isEglCoreInited = false;
    }

    private void releaseAllEGLResource(){
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (programTexture != null) {
            programTexture.release();
            programTexture = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;

        }
    }

    public void setCodecFormatType(int codecFormatType) {
        this.codecFormatType = codecFormatType;
    }

    public void enableWriteVideoRawData(String rawfilePath, String yuvfilePath, boolean enabled) {
        this.enableWriteVideoToFile = enabled;
        if (rawfilePath == null || rawfilePath.length() == 0) {
            this.localVideoRawFilePath = LOCAL_RAW_VIDEO_FILE_PATH;
        } else {
            this.localVideoRawFilePath = LOCAL_RAW_VIDEO_FILE_PATH;
        }
        if (yuvfilePath == null || yuvfilePath.length() == 0) {
            this.localVideoYUVFilePath = LOCAL_YUV_VIDEO_FILE_PATH;
        } else {
            this.localVideoYUVFilePath = LOCAL_YUV_VIDEO_FILE_PATH;
        }
    }

    /**
     * Handles encoder state change requests.  The handler is created on the encoder thread.
     */
    private static class EncoderHandler extends Handler {
        private WeakReference<VideoPreProcessor> mWeakEncoder;

        public EncoderHandler(VideoPreProcessor encoder) {
            mWeakEncoder = new WeakReference<VideoPreProcessor>(encoder);
        }

        @Override  // runs on encoder thread
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            VideoPreProcessor encoder = mWeakEncoder.get();
            if (encoder == null) {
                LogUtil.w("EncoderHandler.handleMessage: encoder is null");
                return;
            }

            switch (what) {
                case MSG_RELEASE_EGL_CONTEXT:
                    encoder.handleRleaseEglContext();
                    break;
                case MSG_INIT_EGL_CONTEXT:
                    encoder.handleInitEncoderContext((EGLContext) obj);
                    break;
                case MSG_FRAME_AVAILABLE:
//                    long timestamp = (((long) inputMessage.arg1) << 32) |
//                            (((long) inputMessage.arg2) & 0xffffffffL);
                    encoder.handleFrameAvailable((VideoCapturedFrame) obj);
                    break;
                case MSG_UPDATE_SHARED_CONTEXT:
                    encoder.handleUpdateSharedContext((EGLContext) inputMessage.obj);
                    break;
                case MSG_QUIT:
                    Looper.myLooper().quit();
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }


}
