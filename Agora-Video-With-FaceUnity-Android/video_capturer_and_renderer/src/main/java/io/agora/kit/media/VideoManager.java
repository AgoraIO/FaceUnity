package io.agora.kit.media;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;

import io.agora.kit.media.capture.VideoCapture;
import io.agora.kit.media.capture.VideoCaptureFactory;
import io.agora.kit.media.capture.VideoCaptureFrame;
import io.agora.kit.media.connector.SinkConnector;
import io.agora.kit.media.constant.Constant;
import io.agora.kit.media.render.VideoRender;
import io.agora.kit.media.transmit.VideoSource;
import io.agora.kit.media.transmit.VideoTransmitter;
import io.agora.rtc.RtcEngine;

public class VideoManager {
    private static final String TAG = VideoManager.class.getSimpleName();
    private static volatile VideoManager mInstance;
    private Context mContext;

    private int mFacing = Constant.CAMERA_FACING_INVALID;

    private VideoCapture mVideoCapture;
    private VideoRender mVideoRender;
    private VideoTransmitter mVideoTransmitter;
    private VideoSource mVideoSource;

    private int mWidth;
    private int mHeight;
    private int mFrameRate;
    private boolean mNeedsPreview;

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

    public boolean allocate(int width, int height, int frameRate, int facing) {
        if (facing == Constant.CAMERA_FACING_FRONT || facing == Constant.CAMERA_FACING_BACK) {
            mFacing = facing;
            mWidth = width;
            mHeight = height;
            mFrameRate = frameRate;
            if (mVideoCapture == null) {
                mVideoCapture = VideoCaptureFactory.createVideoCapture(mContext);
            }

            return mVideoCapture.allocate(width, height, frameRate, facing);
        } else {
            mFacing = Constant.CAMERA_FACING_INVALID;
            Log.e(TAG, "invalid camera id provided");
            return false;
        }
    }

    public void deallocate() {
        if (mVideoTransmitter != null) {
            detachToRTCEngine();
        }

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
            Log.w(TAG, "camera not allocated or already deallocated");
        }
    }

    public void stopCapture() {
        if (mVideoCapture != null) {
            mVideoCapture.stopCaptureAndBlockUntilStopped();
        } else {
            Log.w(TAG, "camera not allocated or already deallocated");
        }

    }

    public void setRenderView(GLSurfaceView view) {
        if (view != null) {
            mNeedsPreview = true;
            if (mVideoRender == null) {
                mVideoRender = new VideoRender(mContext);
            }
            mVideoRender.setRenderView(view);
            mVideoCapture.getSrcConnector().connect(mVideoRender);
            mVideoRender.getTexConnector().connect(mVideoCapture);
        } else {
            mNeedsPreview = false;
            Log.w(TAG, "the render view provided is null");
        }
    }

    public void switchCamera() {
        switch (mFacing) {
            case Constant.CAMERA_FACING_INVALID:
                Log.e(TAG, "camera not allocated or already deallocated");
                break;
            case Constant.CAMERA_FACING_BACK:
                stopCapture();
                mVideoCapture.deallocate(false);
                allocate(mWidth, mHeight, mFrameRate, Constant.CAMERA_FACING_FRONT);
                startCapture();
                break;
            case Constant.CAMERA_FACING_FRONT:
                stopCapture();
                mVideoCapture.deallocate(false);
                allocate(mWidth, mHeight, mFrameRate, Constant.CAMERA_FACING_BACK);
                startCapture();
                break;
            default:
                Log.e(TAG, "no facing matched");
        }
    }

    public void connectEffectHandler(SinkConnector<VideoCaptureFrame> connector) {
        if (connector != null) {
            if (mNeedsPreview) {
                mVideoRender.getFrameConnector().connect(connector);
            } else {
                mVideoCapture.getSrcConnector().connect(connector);
            }
        } else {
            Log.w(TAG, "effectHandler is null");
        }
    }

    public void setMirrorMode(boolean mirror) {
        if (mVideoCapture != null) {
            if (mFacing == Constant.CAMERA_FACING_FRONT) {
                mVideoCapture.setMirrorMode(mirror);
            } else {
                Log.w(TAG, "mirror mode only applies to front camera");
            }
        } else {
            Log.w(TAG, "camera not allocated or already deallocated");
        }

    }

    public void attachToRTCEngine(RtcEngine engine) {
        if (engine != null) {
            mVideoSource = new VideoSource();
            engine.setVideoSource(mVideoSource);
            mVideoTransmitter = new VideoTransmitter(mVideoSource);
            if (mNeedsPreview) {
                mVideoRender.getTransmitConnector().connect(mVideoTransmitter);
            } else {
                mVideoCapture.getTransmitConnector().connect(mVideoTransmitter);
            }
        } else {
            Log.w(TAG, "the engine provided is null");
        }
    }

    public void detachToRTCEngine() {
        if (mVideoTransmitter != null) {
            if (mNeedsPreview) {
                mVideoRender.getTransmitConnector().disconnect();
            } else {
                mVideoCapture.getTransmitConnector().disconnect();
            }
            mVideoTransmitter = null;
        } else {
            Log.w(TAG, "not attached to engine, no need to detach");
        }
    }

    public int getCameraFacing() {
        int facing = mFacing == Constant.CAMERA_FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_BACK
                : Camera.CameraInfo.CAMERA_FACING_FRONT;
        return facing;
    }

    public int getCameraOrientation() {
        return mVideoCapture.getCameraNativeOrientation();
    }
}
