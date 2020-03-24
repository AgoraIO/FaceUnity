// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.kit.media.capture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.agora.kit.media.connector.SinkConnector;
import io.agora.kit.media.connector.SrcConnector;
import io.agora.kit.media.gles.core.EglCore;
import io.agora.kit.media.gles.core.WindowSurface;

/**
 * Video Capture Device base class, defines a set of methods that native code
 * needs to use to configure, start capture, and to be reached by callbacks and
 * provides some necessary data type(s) with accessors.
 **/
public abstract class VideoCapture implements SinkConnector<Integer> {
    /**
     * Common class for storing a framerate range. Values should be multiplied by 1000.
     */
    protected static class FramerateRange {
        public int min;
        public int max;

        public FramerateRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    private static final String TAG = VideoCapture.class.getSimpleName();
    // The angle (0, 90, 180, 270) that the image needs to be rotated to show in
    // the display's native orientation.
    protected int mCameraNativeOrientation;
    // In some occasions we need to invert the device rotation readings, see the
    // individual implementations.
    protected boolean mInvertDeviceOrientationReadings;

    protected VideoCaptureFormat mCaptureFormat;
    protected Context mContext;

    protected SrcConnector<VideoCaptureFrame> mSrcConnector;
    protected SrcConnector<VideoCaptureFrame> mTransmitConnector;

    protected SurfaceTexture mSurfaceTexture;
    protected byte[] mImage;
    protected int mTexId = -1;

    private EglCore mEglCore;
    private WindowSurface mWindowSurface;
    private EGLContext mEGLContext;

    protected boolean mNeedsPreview;
    protected int mPreviewWidth;
    protected int mPreviewHeight;

    protected boolean mMirror;
    protected int mCameraId;
    protected String mCamera2Id;
    protected int mFacing;

    VideoCapture(Context context) {
        mContext = context;
        mSrcConnector = new SrcConnector<>();
        mTransmitConnector = new SrcConnector<>();
    }

    // Allocate necessary resources for capture.
    public abstract boolean allocate(
            int width, int height, int frameRate, int facing);

    public abstract void startCaptureMaybeAsync(boolean needsPreview);

    // Blocks until it is guaranteed that no more frames are sent.
    public abstract void stopCaptureAndBlockUntilStopped();

    public void deallocate(boolean disconnect) {
        if (disconnect) {
            mSrcConnector.disconnect();
            mTransmitConnector.disconnect();
        }
    }

    public void deallocate() {
        deallocate(true);
    }

    public void setMirrorMode(boolean mirror) {
        mMirror = mirror;
    }

    public int getCameraNativeOrientation() {
        return mCameraNativeOrientation;
    }

    protected final int getDisplayRotation() {
        int result;
        if (mInvertDeviceOrientationReadings) {
            result = (mCameraNativeOrientation + getDeviceRotation()) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (mCameraNativeOrientation - getDeviceRotation() + 360) % 360;
        }
        return result;
    }

    protected final int getDeviceRotation() {
        final int orientation;
        WindowManager wm = (WindowManager) mContext.getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
            case Surface.ROTATION_0:
            default:
                orientation = 0;
                break;
        }
        return orientation;
    }

    /**
     * Finds the framerate range matching |targetFramerate|. Tries to find a range with as low of a
     * minimum value as possible to allow the camera adjust based on the lighting conditions.
     * Assumes that all framerate values are multiplied by 1000.
     *
     * This code is mostly copied from WebRTC:
     * CameraEnumerationAndroid.getClosestSupportedFramerateRange
     * in webrtc/api/android/java/src/org/webrtc/CameraEnumerationAndroid.java
     */
    protected static FramerateRange getClosestFramerateRange(
            final List<FramerateRange> framerateRanges, final int targetFramerate) {
        return Collections.min(framerateRanges, new Comparator<FramerateRange>() {
            // Threshold and penalty weights if the upper bound is further away than
            // |MAX_FPS_DIFF_THRESHOLD| from requested.
            private static final int MAX_FPS_DIFF_THRESHOLD = 5000;
            private static final int MAX_FPS_LOW_DIFF_WEIGHT = 1;
            private static final int MAX_FPS_HIGH_DIFF_WEIGHT = 3;

            // Threshold and penalty weights if the lower bound is bigger than |MIN_FPS_THRESHOLD|.
            private static final int MIN_FPS_THRESHOLD = 8000;
            private static final int MIN_FPS_LOW_VALUE_WEIGHT = 1;
            private static final int MIN_FPS_HIGH_VALUE_WEIGHT = 4;

            // Use one weight for small |value| less than |threshold|, and another weight above.
            private int progressivePenalty(
                    int value, int threshold, int lowWeight, int highWeight) {
                return (value < threshold)
                        ? value * lowWeight
                        : threshold * lowWeight + (value - threshold) * highWeight;
            }

            int diff(FramerateRange range) {
                final int minFpsError = progressivePenalty(range.min, MIN_FPS_THRESHOLD,
                        MIN_FPS_LOW_VALUE_WEIGHT, MIN_FPS_HIGH_VALUE_WEIGHT);
                final int maxFpsError = progressivePenalty(Math.abs(targetFramerate - range.max),
                        MAX_FPS_DIFF_THRESHOLD, MAX_FPS_LOW_DIFF_WEIGHT, MAX_FPS_HIGH_DIFF_WEIGHT);
                return minFpsError + maxFpsError;
            }

            @Override
            public int compare(FramerateRange range1, FramerateRange range2) {
                return diff(range1) - diff(range2);
            }
        });
    }

    protected abstract int getNumberOfCameras();

    protected abstract void startPreview();

    public SrcConnector getSrcConnector() {
        return mSrcConnector;
    }
    public SrcConnector getTransmitConnector() {return mTransmitConnector;}

    protected void onFrameAvailable() {
        long timeMillis = System.currentTimeMillis();

        float[] texMatrix = new float[16];

        VideoCaptureFrame frame = new VideoCaptureFrame(mCaptureFormat, mSurfaceTexture,
                mTexId, mImage, texMatrix, timeMillis, mCameraNativeOrientation, mMirror);

        int effectId = mSrcConnector.onDataAvailable(frame);

        if (!mNeedsPreview) {
            frame.mTextureId = effectId;
            mTransmitConnector.onDataAvailable(frame);
        }
    }

    protected void prepareGlSurface(SurfaceTexture st, int width, int height) {
        mEglCore = new EglCore(mEGLContext, 0);

        if (st != null) {
            mWindowSurface = new WindowSurface(mEglCore, st);
        } else {
            mWindowSurface = new WindowSurface(mEglCore, width, height);
        }

        mWindowSurface.makeCurrent();
        GLES20.glViewport(0, 0, mWindowSurface.getWidth(), mWindowSurface.getHeight());
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mEGLContext = EGL14.eglGetCurrentContext();
    }

    @Override
    public int onDataAvailable(Integer data) {
        mTexId = data;
        startPreview();
        return 0;
    }
}
