// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.kit.media.capture;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import io.agora.kit.media.constant.Constant;
import io.agora.kit.media.gles.core.GlUtil;

/**
 * Video Capture Device extension of VideoCapture to provide common functionality
 * for capture using android.hardware.Camera API (deprecated in API 21). For Normal
 * Android devices, it provides functionality for receiving copies of preview
 * frames via Java-allocated buffers.
 **/
@SuppressWarnings("deprecation")
public class VideoCaptureCamera
        extends VideoCapture implements android.hardware.Camera.PreviewCallback {
    private static final String TAG = VideoCaptureCamera.class.getSimpleName();
    private static final int NUM_CAPTURE_BUFFERS = 3;

    private int mExpectedFrameSize;

    private android.hardware.Camera mCamera;
    // Lock to mutually exclude execution of OnPreviewFrame() and {start/stop}Capture().
    private ReentrantLock mPreviewBufferLock = new ReentrantLock();
    // True when native code has started capture.
    private boolean mIsRunning;

    private android.hardware.Camera.CameraInfo getCameraInfo(int id) {
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        try {
            android.hardware.Camera.getCameraInfo(id, cameraInfo);
        } catch (RuntimeException ex) {
            Log.e(TAG, "getCameraInfo: Camera.getCameraInfo: " + ex);
            return null;
        }
        return cameraInfo;
    }

    private static android.hardware.Camera.Parameters getCameraParameters(
            android.hardware.Camera camera) {
        android.hardware.Camera.Parameters parameters;
        try {
            parameters = camera.getParameters();
        } catch (RuntimeException ex) {
            Log.e(TAG, "getCameraParameters: android.hardware.Camera.getParameters: " + ex);
            if (camera != null) camera.release();
            return null;
        }
        return parameters;
    }

    private class CaptureErrorCallback implements android.hardware.Camera.ErrorCallback {
        @Override
        public void onError(int error, android.hardware.Camera camera) {
            Log.e(TAG, "Camera capture error: " + error);
        }
    }

    protected int getNumberOfCameras() {
        return android.hardware.Camera.getNumberOfCameras();
    }

    VideoCaptureCamera(Context context) {
        super(context);
    }

    @Override
    public boolean allocate(int width, int height, int frameRate, int facing) {
        Log.d(TAG, "allocate: requested width: " + width + " height: " + height + " fps: " + frameRate);

        mFacing = facing;
        Camera.CameraInfo info = new Camera.CameraInfo();
        int numCameras = getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (mFacing == Constant.CAMERA_FACING_FRONT && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = i;
                break;
            }

            if (mFacing == Constant.CAMERA_FACING_BACK && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i;
                break;
            }
        }

        try {
            mCamera = android.hardware.Camera.open(mCameraId);
        } catch (RuntimeException ex) {
            Log.e(TAG, "allocate: Camera.open: " + ex);
            return false;
        }

        android.hardware.Camera.CameraInfo cameraInfo = getCameraInfo(mCameraId);
        if (cameraInfo == null) {
            mCamera.release();
            mCamera = null;
            return false;
        }
        mCameraNativeOrientation = cameraInfo.orientation;
        // For Camera API, the readings of back-facing camera need to be inverted.
        mInvertDeviceOrientationReadings =
                (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        Log.d(TAG, "allocate: Rotation dev=" + getDeviceRotation() + " cam=" + mCameraNativeOrientation
                + " facing front? " + mInvertDeviceOrientationReadings);
        mCamera.setDisplayOrientation(getDisplayRotation());
        android.hardware.Camera.Parameters parameters = getCameraParameters(mCamera);
        if (parameters == null) {
            mCamera = null;
            return false;
        }

        // getSupportedPreviewFpsRange() returns a List with at least one
        // element, but when camera is in bad state, it can return null pointer.
        List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
        if (listFpsRange == null || listFpsRange.size() == 0) {
            Log.e(TAG, "allocate: no fps range found");
            return false;
        }
        final ArrayList<FramerateRange> framerateRanges =
                new ArrayList<FramerateRange>(listFpsRange.size());
        for (int[] range : listFpsRange) {
            framerateRanges.add(new FramerateRange(range[0], range[1]));
        }
        // API fps ranges are scaled up x1000 to avoid floating point.
        int frameRateScaled = frameRate * 1000;
        final FramerateRange chosenFramerateRange =
                getClosestFramerateRange(framerateRanges, frameRateScaled);
        final int[] chosenFpsRange = new int[] {chosenFramerateRange.min, chosenFramerateRange.max};
        Log.d(TAG, "allocate: fps set to [" + chosenFpsRange[0] + "-" + chosenFpsRange[1] + "]");

        // Calculate size.
        List<android.hardware.Camera.Size> listCameraSize = parameters.getSupportedPreviewSizes();
        int minDiff = Integer.MAX_VALUE;
        int matchedWidth = width;
        int matchedHeight = height;
        for (android.hardware.Camera.Size size : listCameraSize) {
            int diff = Math.abs(size.width - width) + Math.abs(size.height - height);
            if (diff < minDiff && (size.width % 32 == 0)) {
                minDiff = diff;
                matchedWidth = size.width;
                matchedHeight = size.height;
            }
        }
        if (minDiff == Integer.MAX_VALUE) {
            Log.e(TAG, "Couldn't find resolution close to (" + width + "x" + height + ")");
            return false;
        }
        Log.d(TAG, "allocate: matched (" + matchedWidth +  " x " + matchedHeight + ")");

        mPreviewWidth = matchedWidth;
        mPreviewHeight = matchedHeight;
        mCaptureFormat = new VideoCaptureFormat(matchedWidth, matchedHeight,
                chosenFpsRange[1] / 1000, ImageFormat.NV21);
        parameters.setPreviewSize(matchedWidth, matchedHeight);
        parameters.setPreviewFpsRange(chosenFpsRange[0], chosenFpsRange[1]);
        parameters.setPreviewFormat(mCaptureFormat.mPixelFormat);
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException ex) {
            Log.e(TAG, "setParameters: " + ex);
            return false;
        }

        mCamera.setErrorCallback(new CaptureErrorCallback());

        mExpectedFrameSize = mCaptureFormat.mWidth * mCaptureFormat.mHeight
                * ImageFormat.getBitsPerPixel(mCaptureFormat.mPixelFormat) / 8;
        for (int i = 0; i < NUM_CAPTURE_BUFFERS; i++) {
            byte[] buffer = new byte[mExpectedFrameSize];
            mCamera.addCallbackBuffer(buffer);
        }
        return true;
    }

    protected void startPreview() {
        if (mIsRunning) {
            return;
        }

        Log.d(TAG, "start preview");
        mSurfaceTexture = new SurfaceTexture(mTexId);
        mSurfaceTexture.setOnFrameAvailableListener(null);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException ex) {
            Log.e(TAG, "setPreviewTexture: " + ex);
            return;
        }

        if (mCamera == null) {
            Log.e(TAG, "startCaptureAsync: mCamera is null");
            return;
        }

        mPreviewBufferLock.lock();
        try {
            if (mIsRunning) {
                return;
            }
        } finally {
            mPreviewBufferLock.unlock();
        }

        setPreviewCallback(this);
        try {
            mCamera.startPreview();
        } catch (RuntimeException ex) {
            Log.e(TAG, "startCaptureAsync: Camera.startPreview: " + ex);
            return;
        }

        mPreviewBufferLock.lock();
        try {
            mIsRunning = true;
        } finally {
            mPreviewBufferLock.unlock();
        }
    }

    @Override
    public void startCaptureMaybeAsync(boolean needsPreview) {
        Log.d(TAG, "startCaptureMaybeAsync " + mTexId);
        mNeedsPreview = needsPreview;
        if (!needsPreview) {
            prepareGlSurface(null, mPreviewWidth, mPreviewHeight);
            mTexId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        }

        if (mTexId != -1) startPreview();
    }

    @Override
    public void stopCaptureAndBlockUntilStopped() {
        Log.d(TAG, "stopCaptureAndBlockUntilStopped");

        if (mCamera == null) {
            Log.e(TAG, "stopCaptureAndBlockUntilStopped: mCamera is null");
            return;
        }

        mPreviewBufferLock.lock();
        try {
            if (!mIsRunning) {
                return;
            }
            mIsRunning = false;
        } finally {
            mPreviewBufferLock.unlock();
        }

        try {
        mCamera.stopPreview();
        } catch (RuntimeException ex) {
            Log.e(TAG, "setPreviewTexture: " + ex);
        }
        setPreviewCallback(null);
    }

    @Override
    public void deallocate(boolean disconnect) {
        Log.d(TAG, "deallocate " + disconnect);

        if (mCamera == null) return;

        stopCaptureAndBlockUntilStopped();
        try {
            mCamera.setPreviewTexture(null);
        } catch (IOException ex) {
            Log.e(TAG, "setPreviewTexture: " + ex);
        }

        if (mTexId != -1) {
            int[] textures = new int[]{mTexId};
            GLES20.glDeleteTextures(1, textures, 0);
        }

        mCaptureFormat = null;
        mCamera.release();
        mCamera = null;

        super.deallocate(disconnect);
    }

    private void setPreviewCallback(android.hardware.Camera.PreviewCallback cb) {
        try {
            mCamera.setPreviewCallbackWithBuffer(cb);
        } catch (RuntimeException ex) {
            Log.e(TAG, "setPreviewCallbackWithBuffer: " + ex);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        mPreviewBufferLock.lock();
        try {
            if (!mIsRunning) {
                return;
            }
            if (data.length == mExpectedFrameSize) {
                mImage = data;
                onFrameAvailable();
            } else {
                Log.e(TAG, "the frame size is not as expected");
            }
        } finally {
            mPreviewBufferLock.unlock();
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
        }
    }

}
