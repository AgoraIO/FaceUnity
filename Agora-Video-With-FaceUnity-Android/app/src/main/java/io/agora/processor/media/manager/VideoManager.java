package io.agora.processor.media.manager;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;


import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.common.constant.Constant;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.base.BaseRender;
import io.agora.processor.media.base.BaseVideoCapture;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.VideoCapturedFrame;
import io.agora.processor.media.internal.IRenderListener;
import io.agora.processor.video.capture.VideoCaptureFactory;
import io.agora.processor.video.renderer.RenderInGlSurfaceView;
import io.agora.processor.video.renderer.RenderInView;


public class VideoManager {
    private static volatile VideoManager mInstance;
    private Context mContext;

    private int mFacing = Constant.CAMERA_FACING_INVALID;

    private BaseVideoCapture mVideoCapture;
    private BaseRender mVideoRender;
    private VideoCaptureConfigInfo mVideoCaptureConfigInfo = null;
    private boolean mNeedsPreview;
    private View renderView = null;

    private VideoManager(Context context) {
        mContext = context;
    }

    public static VideoManager createInstance(Context context) {
        if (mInstance == null) {
            synchronized (VideoManager.class) {
                if (mInstance == null) {
                    mInstance = new VideoManager(context);
                }
            }
        }
        return mInstance;
    }

    public boolean allocate(VideoCaptureConfigInfo videoCaptureConfigInfo) {
        if (videoCaptureConfigInfo.getCameraFace() == Constant.CAMERA_FACING_FRONT
                || videoCaptureConfigInfo.getCameraFace() == Constant.CAMERA_FACING_BACK) {
            mFacing = videoCaptureConfigInfo.getCameraFace();
            mVideoCaptureConfigInfo = videoCaptureConfigInfo;
            if (mVideoCapture == null) {
                mVideoCapture = VideoCaptureFactory.createVideoCapture(mContext, mVideoCaptureConfigInfo);
            }

            return mVideoCapture.allocate();
        } else {
            mFacing = Constant.CAMERA_FACING_INVALID;
            LogUtil.e("invalid camera id provided");
            return false;
        }
    }

    public void deallocate() {
        renderView = null;
        if (mVideoCapture != null) {
            mFacing = Constant.CAMERA_FACING_INVALID;
            mVideoCapture.deallocate();
            mVideoCapture = null;
        }

        if (mVideoRender != null) {
            mVideoRender.destroy();
            mVideoRender = null;
        }
    }

    public void startCapture() {
        if (mVideoCapture != null) {
            mVideoCapture.startCaptureMaybeAsync(mNeedsPreview);
        } else {
            LogUtil.w("camera not allocated or already deallocated");
        }
    }

    public void stopCapture() {
        if (mVideoCapture != null) {
            mVideoCapture.stopCaptureAndBlockUntilStopped();
        } else {
            LogUtil.w("camera not allocated or already deallocated");
        }

    }

    public boolean setRenderView(SurfaceView view) {
        if (view == null || (renderView!=null&&renderView != view)) {
            mNeedsPreview = false;
            LogUtil.w("the render view error");
            return false;
        }
        renderView = view;
        if (mVideoRender == null) {
            mVideoRender = new RenderInView(null, mVideoCaptureConfigInfo);
        }
        if (!mVideoRender.setRenderView(renderView)) {
            mNeedsPreview = false;
            LogUtil.w("set view error");
            return false;
        }
        mNeedsPreview = true;
        mVideoCapture.getSrcConnector().connect(mVideoRender);
        mVideoRender.getTexConnector().connect(mVideoCapture);
        return true;
    }

    public boolean setRenderView(GLSurfaceView view) {
        if (view == null || (renderView!=null&&renderView != view)) {
            mNeedsPreview = false;
            LogUtil.w("the render view error");
            return false;
        }
        renderView = view;
        if (mVideoRender == null) {
            mVideoRender = new RenderInGlSurfaceView(mVideoCaptureConfigInfo);
        }
        if (!mVideoRender.setRenderView(renderView)) {
            mNeedsPreview = false;
            LogUtil.w("set view error");
            return false;
        }
        mNeedsPreview = true;
        mVideoCapture.getSrcConnector().connect(mVideoRender);
        mVideoRender.getTexConnector().connect(mVideoCapture);
        return true;
    }

    public boolean setRenderView(TextureView view) {
        if (view == null || (renderView!=null&&renderView != view)) {
            mNeedsPreview = false;
            LogUtil.w("the render view error");
            return false;
        }
        renderView = view;
        if (mVideoRender == null) {
            mVideoRender = new RenderInView(null, mVideoCaptureConfigInfo);
        }
        if (!mVideoRender.setRenderView(renderView)) {
            mNeedsPreview = false;
            LogUtil.w("set view error");
            return false;
        }
        mNeedsPreview = true;
        mVideoCapture.getSrcConnector().connect(mVideoRender);
        mVideoRender.getTexConnector().connect(mVideoCapture);
        return true;
    }

    public void runInRenderThread(Runnable event) {
        if (mVideoRender != null) {
            LogUtil.i("runInRenderThread");
            mVideoRender.runInRenderThread(event);
        }
    }

    public void switchCamera() {
        switch (mFacing) {
            case Constant.CAMERA_FACING_INVALID:
                LogUtil.e("camera not allocated or already deallocated");
                break;
            case Constant.CAMERA_FACING_BACK:
                stopCapture();
                mVideoCapture.deallocate(false);
                mFacing = Constant.CAMERA_FACING_FRONT;
                mVideoCaptureConfigInfo.setCameraFace(mFacing);
                allocate(mVideoCaptureConfigInfo);
                startCapture();
                break;
            case Constant.CAMERA_FACING_FRONT:
                stopCapture();
                mVideoCapture.deallocate(false);
                mFacing = Constant.CAMERA_FACING_BACK;
                mVideoCaptureConfigInfo.setCameraFace(mFacing);
                allocate(mVideoCaptureConfigInfo);
                startCapture();
                break;
            default:
                LogUtil.e("no facing matched");
        }

    }

    public void connectEffectHandler(SinkConnector<CapturedFrame> connector) {
        if (connector != null) {
            if (mNeedsPreview) {
                mVideoRender.getBeautyConnector().connect(connector);
            } else {
                mVideoCapture.getSrcConnector().connect(connector);
            }
        } else {
            LogUtil.w("effectHandler is null");
        }
    }

    public void setMirrorMode(boolean mirror) {
        if (mVideoCapture != null) {
            if (mFacing == Constant.CAMERA_FACING_FRONT) {
                mVideoCapture.setMirrorMode(mirror);
            } else {
                LogUtil.w("mirror mode only applies to front camera");
            }
        } else {
            LogUtil.w("camera not allocated or already deallocated");
        }

    }

    public void setRenderListner(IRenderListener listner) {
        if (mVideoRender != null) {
            mVideoRender.setRenderListener(listner);
        }
    }

    public BaseVideoCapture getVideoCapture() {
        return mVideoCapture;
    }

    public BaseRender getVideoRender() {
        return mVideoRender;
    }

    public boolean isEglContextReady() {
        if (mVideoRender == null) {
            return false;
        }
        return mVideoRender.isEglContextReady();
    }

    public void attachConnectorToRender(SinkConnector videoTransmitter) {
        if (mVideoRender != null) {
            mVideoRender.getRrenderedConnector().connect(videoTransmitter);
        } else {
            LogUtil.w("attachConnectorToRender error for mVideoRender is null");
        }
    }

    public void attachConnectorToCapture(SinkConnector videoTransmitter) {
        if (mVideoCapture != null) {
            mVideoCapture.getTransmitConnector().connect(videoTransmitter);
        } else {
            LogUtil.w("attachConnectorToCapture error for mVideoRender is null");
        }
    }
}
