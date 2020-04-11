// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.kit.media.capture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import io.agora.kit.media.support.IntDef;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.agora.kit.media.constant.Constant;
import io.agora.kit.media.gles.core.GlUtil;

/**
 * This class implements Video Capture using Camera2 API, introduced in Android
 * API 21 (L Release). Capture takes place in the current Looper, while pixel
 * download takes place in another thread used by ImageReader. A number of
 * static methods are provided to retrieve information on current system cameras
 * and their capabilities, using android.hardware.camera2.CameraManager.
 **/
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VideoCaptureCamera2 extends VideoCapture {
    // Inner class to extend a CameraDevice state change listener.
    private class CameraStateListener extends CameraDevice.StateCallback {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";

            Log.e(TAG, "CameraDevice.StateCallback onOpened");
            mCameraDevice = cameraDevice;
            mWaitForDeviceClosedConditionVariable.close();
            changeCameraStateAndNotify(CameraState.CONFIGURING);
            createPreviewObjectsAndStartPreviewOrFail();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";
            Log.e(TAG, "cameraDevice was closed unexpectedly");

            cameraDevice.close();
            mCameraDevice = null;
            changeCameraStateAndNotify(CameraState.STOPPED);
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";
            Log.e(TAG, "cameraDevice encountered an error");

            cameraDevice.close();
            mCameraDevice = null;
            Log.e(TAG, "Camera device error " + Integer.toString(error));
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "cameraDevice closed");
            // If we called mCameraDevice.close() while mPreviewSession was running,
            // mPreviewSession will get closed, but the corresponding CameraPreviewSessionListener
            // will not receive a callback to onClosed(). Therefore we have to clean up
            // the reference to mPreviewSession here.
            if (mPreviewSession != null) {
                mPreviewSession = null;
            }

            mWaitForDeviceClosedConditionVariable.open();
        }
    };

    // Inner class to extend a Capture Session state change listener.
    private class CameraPreviewSessionListener extends CameraCaptureSession.StateCallback {
        private final CaptureRequest mPreviewRequest;
        CameraPreviewSessionListener(CaptureRequest previewRequest) {
            mPreviewRequest = previewRequest;
        }

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";

            Log.d(TAG, "CameraPreviewSessionListener.onConfigured");
            mPreviewSession = cameraCaptureSession;
            try {
                // This line triggers the preview. A |listener| is registered to receive the actual
                // capture result details. A CrImageReaderListener will be triggered every time a
                // downloaded image is ready. Since |handler| is null, we'll work on the current
                // Thread Looper.
                mPreviewSession.setRepeatingRequest(
                        mPreviewRequest, null, null);

            } catch (CameraAccessException | SecurityException | IllegalStateException
                    | IllegalArgumentException ex) {
                Log.e(TAG, "setRepeatingRequest: ", ex);
                return;
            }

            changeCameraStateAndNotify(CameraState.STARTED);

            // Frames will be arriving at CameraPreviewReaderListener.onImageAvailable();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";
            Log.e(TAG, "CameraPreviewSessionListener.onConfigureFailed");

            changeCameraStateAndNotify(CameraState.STOPPED);
            mPreviewSession = null;
            Log.e(TAG,"Camera session configuration error");
        }

        @Override
        public void onClosed(CameraCaptureSession cameraCaptureSession) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";
            Log.d(TAG, "CameraPreviewSessionListener.onClosed");

            // The preview session gets closed temporarily when a takePhoto
            // request is being processed. A new preview session will be
            // started after that.
            mPreviewSession = null;
        }
    };

    // Internal class implementing an ImageReader listener for Preview frames. Gets pinged when a
    // new frame is been captured and downloads it to memory-backed buffers.
    private class CameraPreviewReaderListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";

            try (Image image = reader.acquireLatestImage()) {
                if (image == null) {
                    return;
                }

                if (image.getFormat() != ImageFormat.YUV_420_888 || image.getPlanes().length != 3) {
                    Log.e(TAG,"Unexpected image format: " + image.getFormat()
                                    + " or #planes: " + image.getPlanes().length);
                    throw new IllegalStateException();
                }

                if (reader.getWidth() != image.getWidth()
                        || reader.getHeight() != image.getHeight()) {
                    Log.e(TAG,"ImageReader size (" + reader.getWidth() + "x" + reader.getHeight()
                                    + ") did not match Image size (" + image.getWidth() + "x"
                                    + image.getHeight() + ")");
                    throw new IllegalStateException();
                }

                mImage = YUV_420_888toNV21(image);
                onFrameAvailable();
            } catch (IllegalStateException ex) {
                Log.e(TAG, "acquireLatestImage():", ex);
            }
        }
    };

    private class StopCaptureTask implements Runnable {
        @Override
        public void run() {
            assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";

            if (mCameraDevice == null) return;

            // As per Android API documentation, this will automatically abort captures
            // pending for mPreviewSession, but it will not lead to callbacks such as
            // onClosed() to the corresponding CameraPreviewSessionListener.
            // Different from what the Android API documentation says, pending frames
            // may still get delivered after this call. Therefore, we have to wait for
            // CameraStateListener.onClosed() in order to have a guarantee that no more
            // frames are delivered.
            mCameraDevice.close();

            changeCameraStateAndNotify(CameraState.STOPPED);
        }
    }

    private static final String TAG = VideoCaptureCamera2.class.getSimpleName();
    @IntDef({CameraState.OPENING, CameraState.CONFIGURING, CameraState.STARTED,
            CameraState.STOPPED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface CameraState {
        int OPENING = 0;
        int CONFIGURING = 1;
        int STARTED = 2;
        int STOPPED = 3;
    }

    private final Object mCameraStateLock = new Object();

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest mPreviewRequest;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private ImageReader mImageReader;
    private static CameraManager mCameraManager;
    // We create a dedicated HandlerThread for operating the camera on. This
    // is needed, because the camera APIs requires a Looper for posting
    // asynchronous callbacks to. The native thread that calls the constructor
    // and public API cannot be used for this, because it does not have a
    // Looper.
    private Handler mCameraThreadHandler;
    private ConditionVariable mWaitForDeviceClosedConditionVariable = new ConditionVariable();

    private Range<Integer> mAeFpsRange;
    private @CameraState int mCameraState = CameraState.STOPPED;
    private Surface mSurface;


    // Service function to grab CameraCharacteristics and handle exceptions.
    private CameraCharacteristics getCameraCharacteristics(String id) {
        try {
            return mCameraManager.getCameraCharacteristics(id);
        } catch (CameraAccessException | IllegalArgumentException | AssertionError ex) {
            Log.e(TAG, "getCameraCharacteristics: ", ex);
        }
        return null;
    }

    private void createPreviewObjectsAndStartPreviewOrFail() {
        assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";

        if (createPreviewObjectsAndStartPreview()) return;

        changeCameraStateAndNotify(CameraState.STOPPED);
        Log.e(TAG, "Error starting or restarting preview");
    }

    private boolean createPreviewObjectsAndStartPreview() {
        assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";
        if (mCameraDevice == null) return false;

        // Create an ImageReader and plug a thread looper into it to have
        // readback take place on its own thread.
        mImageReader = ImageReader.newInstance(mCaptureFormat.getWidth(),
                mCaptureFormat.getHeight(), mCaptureFormat.getPixelFormat(), 2 /* maxImages */);
        final CameraPreviewReaderListener imageReaderListener = new CameraPreviewReaderListener();
        mImageReader.setOnImageAvailableListener(imageReaderListener, mCameraThreadHandler);


        try {
            // TEMPLATE_PREVIEW specifically means "high frame rate is given
            // priority over the highest-quality post-processing".
            mPreviewRequestBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException | IllegalArgumentException | SecurityException ex) {
            Log.e(TAG, "createCaptureRequest: ", ex);
            return false;
        }

        if (mPreviewRequestBuilder == null) {
            Log.e(TAG, "mPreviewRequestBuilder error");
            return false;
        }

        mSurfaceTexture = new SurfaceTexture(mTexId);
        mSurfaceTexture.setDefaultBufferSize(mPreviewWidth, mPreviewHeight);
        mSurface = new Surface(mSurfaceTexture);
        // Construct an ImageReader Surface and plug it into our CaptureRequest.Builder.
        mPreviewRequestBuilder.addTarget(mSurface);
        mPreviewRequestBuilder.addTarget(mImageReader.getSurface());

        configureCommonCaptureSettings(mPreviewRequestBuilder);

        List<Surface> surfaceList = new ArrayList<Surface>(2);
        surfaceList.add(mSurface);
        surfaceList.add(mImageReader.getSurface());

        mPreviewRequest = mPreviewRequestBuilder.build();

        try {
            mCameraDevice.createCaptureSession(
                    surfaceList, new CameraPreviewSessionListener(mPreviewRequest), null);
        } catch (CameraAccessException | IllegalArgumentException | SecurityException ex) {
            Log.e(TAG, "createCaptureSession: ", ex);
            return false;
        }
        // Wait for trigger on CameraPreviewSessionListener.onConfigured();
        return true;

    }

    private void configureCommonCaptureSettings(CaptureRequest.Builder requestBuilder) {
        assert mCameraThreadHandler.getLooper() == Looper.myLooper() : "called on wrong thread";

        // |mFocusMode| indicates if we're in auto/continuous, single-shot or manual mode.
        // AndroidMeteringMode.SINGLE_SHOT is dealt with independently since it needs to be
        // triggered by a capture.
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mAeFpsRange);
    }

    private void changeCameraStateAndNotify(@CameraState int state) {
        synchronized (mCameraStateLock) {
            mCameraState = state;
            mCameraStateLock.notifyAll();
        }
    }

    // Finds the closest Size to (|width|x|height|) in |sizes|, and returns it or null.
    // Ignores |width| or |height| if either is zero (== don't care).
    private static Size findClosestSizeInArray(Size[] sizes, int width, int height) {
        if (sizes == null) return null;
        Size closestSize = null;
        int minDiff = Integer.MAX_VALUE;
        for (Size size : sizes) {
            final int diff = ((width > 0) ? Math.abs(size.getWidth() - width) : 0)
                    + ((height > 0) ? Math.abs(size.getHeight() - height) : 0);
            if (diff < minDiff && size.getWidth() % 32 == 0) {
                minDiff = diff;
                closestSize = size;
            }
        }
        if (minDiff == Integer.MAX_VALUE) {
            Log.e(TAG, "Couldn't find resolution close to (" + width + "x" + height + ")");
            return null;
        }
        return closestSize;
    }

    protected int getNumberOfCameras() {
        try {
            return mCameraManager.getCameraIdList().length;
        } catch (CameraAccessException | SecurityException | AssertionError ex) {
            // SecurityException is undocumented but seen in the wild: https://crbug/605424.
            Log.e(TAG, "getNumberOfCameras: getCameraIdList(): ", ex);
            return 0;
        }
    }

    public VideoCaptureCamera2(Context context) {
        super(context);

        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        HandlerThread thread = new HandlerThread("VideoCaptureCamera2_CameraThread");
        thread.start();
        mCameraThreadHandler = new Handler(thread.getLooper());
    }

    @Override
    public void finalize() {
        mCameraThreadHandler.getLooper().quit();
    }

    @Override
    public boolean allocate(int width, int height, int frameRate, int facing) {
        Log.d(TAG, "allocate: requested width: " + width + " height: " + height + " fps: " + frameRate);

        mFacing = facing;
        synchronized (mCameraStateLock) {
            if (mCameraState == CameraState.OPENING || mCameraState == CameraState.CONFIGURING) {
                Log.e(TAG, "allocate() invoked while Camera is busy opening/configuring.");
                return false;
            }
        }

        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = getCameraCharacteristics(cameraId);

                Integer face = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (mFacing == Constant.CAMERA_FACING_FRONT && face == characteristics.LENS_FACING_FRONT) {
                    mCamera2Id = cameraId;
                    break;
                }

                if (mFacing == Constant.CAMERA_FACING_BACK && face == characteristics.LENS_FACING_BACK) {
                    mCamera2Id = cameraId;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        final CameraCharacteristics cameraCharacteristics = getCameraCharacteristics(mCamera2Id);
        final StreamConfigurationMap streamMap =
                cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        // Find closest supported size.
        final Size[] supportedSizes = streamMap.getOutputSizes(ImageFormat.YUV_420_888);
        //final Size[] supportedSizes = streamMap.getOutputSizes(SurfaceTexture.class);
        final Size closestSupportedSize = findClosestSizeInArray(supportedSizes, width, height);
        if (closestSupportedSize == null) {
            Log.e(TAG, "No supported resolutions.");
            return false;
        }
        Log.d(TAG, "allocate: matched (" + closestSupportedSize.getWidth() +  " x "
                + closestSupportedSize.getHeight() + ")");
        mPreviewWidth = closestSupportedSize.getWidth();
        mPreviewHeight = closestSupportedSize.getHeight();

        final List<Range<Integer>> fpsRanges = Arrays.asList(cameraCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES));
        if (fpsRanges.isEmpty()) {
            Log.e(TAG, "No supported framerate ranges.");
            return false;
        }
        final List<FramerateRange> framerateRanges =
                new ArrayList<FramerateRange>(fpsRanges.size());
        // On some legacy implementations FPS values are multiplied by 1000. Multiply by 1000
        // everywhere for consistency. Set fpsUnitFactor to 1 if fps ranges are already multiplied
        // by 1000.
        final int fpsUnitFactor = fpsRanges.get(0).getUpper() > 1000 ? 1 : 1000;
        for (Range<Integer> range : fpsRanges) {
            framerateRanges.add(new FramerateRange(
                    range.getLower() * fpsUnitFactor, range.getUpper() * fpsUnitFactor));
        }
        final FramerateRange aeFramerateRange =
                getClosestFramerateRange(framerateRanges, frameRate * 1000);
        mAeFpsRange = new Range<Integer>(
                aeFramerateRange.min / fpsUnitFactor, aeFramerateRange.max / fpsUnitFactor);
        Log.d(TAG, "allocate: fps set to [" + mAeFpsRange.getLower() + "-" + mAeFpsRange.getUpper() + "]");

        mPreviewWidth = closestSupportedSize.getWidth();
        mPreviewHeight = closestSupportedSize.getHeight();
        // |mCaptureFormat| is also used to configure the ImageReader.
        mCaptureFormat = new VideoCaptureFormat(closestSupportedSize.getWidth(),
                closestSupportedSize.getHeight(), aeFramerateRange.max / fpsUnitFactor, ImageFormat.YUV_420_888);
        mCameraNativeOrientation =
                cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mInvertDeviceOrientationReadings =
                cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                == CameraCharacteristics.LENS_FACING_FRONT;
        return true;
    }

    @Override
    public void startCaptureMaybeAsync(boolean needsPreview) {
        Log.d(TAG, "startCaptureMaybeAsync " + mTexId);
        mNeedsPreview = needsPreview;
        changeCameraStateAndNotify(CameraState.OPENING);

        if (!needsPreview) {
            prepareGlSurface(null, mPreviewWidth, mPreviewHeight);
            mTexId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

        }

        if (mTexId != -1) startPreview();
    }

    protected void startPreview() {
        Log.d(TAG, "startPreview");
        final CameraStateListener stateListener = new CameraStateListener();
        try {
            mCameraManager.openCamera(mCamera2Id, stateListener, mCameraThreadHandler);
        } catch (CameraAccessException | IllegalArgumentException | SecurityException ex) {
            Log.e(TAG, "allocate: manager.openCamera: ", ex);
        }
    }

    @Override
    public void stopCaptureAndBlockUntilStopped() {
        // With Camera2 API, the capture is started asynchronously, which will cause problem if
        // stopCapture comes too quickly. Without stopping the previous capture properly, the
        // next startCapture will fail. So wait camera to be STARTED.
        Log.d(TAG, "stopCaptureAndBlockUntilStopped");
        synchronized (mCameraStateLock) {
            while (mCameraState != CameraState.STARTED && mCameraState != CameraState.STOPPED) {
                try {
                    mCameraStateLock.wait();
                } catch (InterruptedException ex) {
                    Log.e(TAG, "CaptureStartedEvent: ", ex);
                }
            }
            if (mCameraState == CameraState.STOPPED) return;
        }

        mCameraThreadHandler.post(new StopCaptureTask());
        mWaitForDeviceClosedConditionVariable.block();
    }

    @Override
    public void deallocate(boolean disconnect) {
        Log.d(TAG, "deallocate " + disconnect);

        stopCaptureAndBlockUntilStopped();

        if (mTexId != -1) {
            int[] textures = new int[]{mTexId};
            GLES20.glDeleteTextures(1, textures, 0);
        }

        super.deallocate(disconnect);
    }

    private byte[] YUV_420_888toNV21(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            int yBufferPos = width - rowStride; // not an actual position
            for (; pos < ySize; pos += width) {
                yBufferPos += rowStride - width;
                yBuffer.position(yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());
        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            vBuffer.put(1, (byte) 0);
            if (uBuffer.get(0) == 0) {
                vBuffer.put(1, (byte) 255);
                if (uBuffer.get(0) == 255) {
                    vBuffer.put(1, savePixel);
                    vBuffer.get(nv21, ySize, uvSize);

                    return nv21; // shortcut
                }
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = col * pixelStride + row * rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }
}
