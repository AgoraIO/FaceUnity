// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.processor.video.capture;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.Image;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import io.agora.processor.common.constant.Constant;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.base.BaseVideoCapture;
import io.agora.processor.media.gles.core.GlUtil;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.VideoCapturedFrame;

import static io.agora.processor.common.constant.Constant.FIX_VIDEO_FPS;


/**
 * Video Capture Device extension of BaseVideoCapture to provide common functionality
 * for capture using android.hardware.Camera API (deprecated in API 21). For Normal
 * Android devices, it provides functionality for receiving copies of preview
 * frames via Java-allocated buffers.
 **/
@SuppressWarnings("deprecation")
public class VideoCaptureCamera
        extends BaseVideoCapture implements android.hardware.Camera.PreviewCallback {
    private static final String TAG = VideoCaptureCamera.class.getSimpleName();
    private static final int NUM_CAPTURE_BUFFERS = 3;

    private int mExpectedFrameSize;

    private android.hardware.Camera mCamera;
    // Lock to mutually exclude execution of OnPreviewFrame() and {start/stop}Capture().
    private ReentrantLock mPreviewBufferLock = new ReentrantLock();
    // True when native code has started capture.
    private boolean mIsRunning;
    private VideoCaptureConfigInfo videoCaptureConfigInfo;

    private android.hardware.Camera.CameraInfo getCameraInfo(int id) {
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        try {
            android.hardware.Camera.getCameraInfo(id, cameraInfo);
        } catch (RuntimeException ex) {
            LogUtil.e("getCameraInfo: Camera.getCameraInfo: " + ex);
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
            LogUtil.e("getCameraParameters: android.hardware.Camera.getParameters: " + ex);
            if (camera != null) camera.release();
            return null;
        }
        return parameters;
    }

    private class CaptureErrorCallback implements android.hardware.Camera.ErrorCallback {
        @Override
        public void onError(int error, android.hardware.Camera camera) {
            LogUtil.e("Camera capture error: " + error);
        }
    }

    protected int getNumberOfCameras() {
        return android.hardware.Camera.getNumberOfCameras();
    }

    VideoCaptureCamera(Context context, VideoCaptureConfigInfo videoCaptureConfigInfo) {
        super(context);
        this.videoCaptureConfigInfo = videoCaptureConfigInfo;
    }

    @Override
    public boolean allocate() {
        LogUtil.d("allocate: requested width: " + videoCaptureConfigInfo.getVideoCaptureWidth()
                + " height: " + videoCaptureConfigInfo.getVideoCaptureHeight() +
                " face: " + videoCaptureConfigInfo.getCameraFace() +
                " fps: " + videoCaptureConfigInfo.getVideoCaptureFps());

        mFacing = videoCaptureConfigInfo.getCameraFace();
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
            LogUtil.e("allocate: Camera.open: " + ex);
            return false;
        }
        return true;
    }

    private boolean updateCameraParam() {

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
        LogUtil.d("allocate: Rotation dev=" + getDeviceRotation() + " cam=" + mCameraNativeOrientation
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
            LogUtil.e("allocate: no fps range found");
            return false;
        }
        final ArrayList<FramerateRange> framerateRanges =
                new ArrayList<FramerateRange>(listFpsRange.size());
        for (int[] range : listFpsRange) {
            framerateRanges.add(new FramerateRange(range[0], range[1]));
        }
        // API fps ranges are scaled up x1000 to avoid floating point.
        int frameRateScaled = FIX_VIDEO_FPS * 1000;
        final FramerateRange chosenFramerateRange =
                getClosestFramerateRange(framerateRanges, frameRateScaled);
        final int[] chosenFpsRange = new int[]{chosenFramerateRange.min, chosenFramerateRange.max};
        LogUtil.i("allocate: fps set to [" + chosenFpsRange[0] + "-" + chosenFpsRange[1] + "]");

        // Calculate size.
        List<Camera.Size> listCameraSize = parameters.getSupportedPreviewSizes();
        int minDiff = Integer.MAX_VALUE;
        int matchedWidth = videoCaptureConfigInfo.getVideoCaptureWidth();
        int matchedHeight = videoCaptureConfigInfo.getVideoCaptureHeight();
        for (android.hardware.Camera.Size size : listCameraSize) {
            int diff = Math.abs(size.width - videoCaptureConfigInfo.getVideoCaptureWidth())
                    + Math.abs(size.height - videoCaptureConfigInfo.getVideoCaptureHeight());
            if (diff < minDiff && (size.width % 32 == 0)) {
                minDiff = diff;
                matchedWidth = size.width;
                matchedHeight = size.height;
            }
        }
        if (minDiff == Integer.MAX_VALUE) {
            LogUtil.e("Couldn't find resolution close to (" + videoCaptureConfigInfo.getVideoCaptureWidth()
                    + "x" + videoCaptureConfigInfo.getVideoCaptureHeight() + ")");
            return false;
        }
        LogUtil.i("allocate: matched (" + matchedWidth + " x " + matchedHeight + ")");

        mPreviewWidth = matchedWidth;
        mPreviewHeight = matchedHeight;
        videoCaptureConfigInfo.setVideoCaptureWidth(mPreviewWidth);
        videoCaptureConfigInfo.setVideoCaptureHeight(mPreviewHeight);
        parameters.setPreviewSize(matchedWidth, matchedHeight);
        parameters.setPreviewFpsRange(chosenFpsRange[0], chosenFpsRange[1]);
        parameters.setPreviewFormat(ImageFormat.NV21);
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException ex) {
            LogUtil.e("setParameters: " + ex);
            return false;
        }

        mCamera.setErrorCallback(new CaptureErrorCallback());

        mExpectedFrameSize = mPreviewWidth * mPreviewHeight
                * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
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

        if (!updateCameraParam()) {
            LogUtil.e("updateCameraParam error");
            return;
        }

        LogUtil.d("start preview " + mTexId);
        mSurfaceTexture = new SurfaceTexture(mTexId);
        mSurfaceTexture.setOnFrameAvailableListener(null);

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException ex) {
            LogUtil.e("setPreviewTexture: " + ex);
            return;
        }

        if (mCamera == null) {
            LogUtil.e("startCaptureAsync: mCamera is null");
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
            LogUtil.e("startCaptureAsync: Camera.startPreview: " + ex);
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
        LogUtil.d("startCaptureMaybeAsync " + mTexId);
        mNeedsPreview = needsPreview;
        if (!needsPreview) {
            prepareGlSurface(null, mPreviewWidth, mPreviewHeight);
            mTexId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        }

        if (mTexId != -1) startPreview();
    }

    @Override
    public void stopCaptureAndBlockUntilStopped() {
        LogUtil.d("stopCaptureAndBlockUntilStopped");

        if (mCamera == null) {
            LogUtil.e("stopCaptureAndBlockUntilStopped: mCamera is null");
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
            LogUtil.e("setPreviewTexture: " + ex);
        }
        setPreviewCallback(null);
    }

    @Override
    public void deallocate(boolean disconnect) {
        LogUtil.d("deallocate " + disconnect);

        if (mCamera == null) return;

        stopCaptureAndBlockUntilStopped();
        try {
            mCamera.setPreviewTexture(null);
        } catch (IOException ex) {
            LogUtil.e("setPreviewTexture: " + ex);
        }

        if (mTexId != -1) {
            int[] textures = new int[]{mTexId};
            GLES20.glDeleteTextures(1, textures, 0);
        }
        mCamera.release();
        mCamera = null;
        super.deallocate(disconnect);
    }

    private void setPreviewCallback(android.hardware.Camera.PreviewCallback cb) {
        try {
            mCamera.setPreviewCallbackWithBuffer(cb);
        } catch (RuntimeException ex) {
            LogUtil.e("setPreviewCallbackWithBuffer: " + ex);
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
                onFrameAvailable(videoCaptureConfigInfo.getVideoCaptureFps());
            } else {
                LogUtil.e("the frame size is not as expected " + data.length + " mExpectedFrameSize" + mExpectedFrameSize);
            }
        } finally {
            mPreviewBufferLock.unlock();
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }
        }
    }

}
