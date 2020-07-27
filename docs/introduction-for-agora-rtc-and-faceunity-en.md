# Get started quickly with Agora RTC and FaceUnity
  
Nowadays, more and more users begin to have an increasing demand for beauty/props, especially in pan-entertainment scenarios. Nowadays, there are many third-party beauty SDKs on the market that can be used by developers. Can these third-party beauty SDKs be combined with Agora RTC SDK to realize the application scenario of real-time video pan-entertainment? The answer is of course yes.
The purpose of this article is to help you quickly understand how to use it. By default, Agora RTC SDK provides an end-to-end overall solution. Agora RTC SDK is responsible for collecting audio and video, pre-processing, and then sending the data to the peer for rendering. This mode of operation usually meets the needs of most developers. However, if the developer wants to perform secondary processing on the collected data (such as beauty, etc.),
it is recommended to call a custom video data source through `setVideoSource(IVideoSource videoSource)`. In this case, the data flow of the whole process is shown in the following figure:

1. Collect video data from the camera
2. Pass the collected data to the FaceUnity SDK for secondary processing and rendering
3. Pass the processed data to Agora RTC SDK
4. The Agora RTC SDK transmits the processed data encoding to the peer through SD-RTN, and the peer decodes and renders

![flow for Agora&FU](data_flow.png)

This article will take [Android platform code](https://github.com/AgoraIO/FaceUnity/tree/master/Agora-Video-With-FaceUnity-Android) as an example to explain in detail how to implement it. For other platforms, please refer to [FaceUnity](https://github.com/AgoraIO/FaceUnity/).

## 1. Set the Agora RTC SDK video source as a custom video source

    // A general class that implements the IVideoSource interface is as follows, which is not used in this sample program
    public class MyVideoSource implements IVideoSource {
        @Override
        public int getBufferType() {
            // Returns the type of the current frame data buffer. Each type of data is processed differently in the Agora RTC SDK, so it must be consistent with the type of frame data.
            // There are three types: BufferType.BYTE_ARRAY/BufferType.TEXTURE/BufferType.BYTE_BUFFER
            return BufferType.BYTE_ARRAY;
        }

        @Override
        public boolean onInitialize(IVideoFrameConsumer consumer) {
            // IVideoFrameConsumer was created by the Agora RTC SDK. Please save its reference during the life cycle of MyVideoSource, because it will transfer data to the SDK later
            mConsumer = consumer;
        }

        @Override
        public boolean onStart() {
            mHasStarted = true;
        }

        @Override
        public void onStop() {
            mHasStarted = false;
        }

        @Override
        public void onDispose() {
            // Release the reference to Consumer
            mConsumer = null;
        }
    }

In this sample program, the `TextureSource` class is used, which is a predefined implementation of the texture type (texture) video source provided by the Agora RTC SDK. After instantiating this class, you can call the `setVideoSource` interface to set the video source. The specific usage is as follows:

	mRtcEngine.setVideoSource(mTextureSource);

## 2. Data collection
In this sample program, the custom video source used is the camera, and the video data collection is completed in the sample program. The specific methods are as follows：

    private void openCamera(final int cameraType) {
        synchronized (mCameraLock) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            ......
            ...... // Omit part of the code
            mCameraOrientation = CameraUtils.getCameraOrientation(cameraId);
            CameraUtils.setCameraDisplayOrientation(mActivity, cameraId, mCamera); // 根据相机传感器方向和手机当前方向设置相机预览方向

            Camera.Parameters parameters = mCamera.getParameters(); 

            CameraUtils.setFocusModes(parameters);
            int[] size = CameraUtils.choosePreviewSize(parameters, mCameraWidth, mCameraHeight); // 选择最佳预览尺寸
            ......
            ...... // Omit part of the code

            mCamera.setParameters(parameters);
        }

        cameraStartPreview();
    }
    
    private void cameraStartPreview() {
        ......
        ...... // Omit part of the code  
        mCamera.setPreviewTexture(mSurfaceTexture = new SurfaceTexture(mCameraTextureId));
        mCamera.startPreview();
    }

Among them, the `openCamera` method mainly configures some parameters of the camera, such as preview size, display direction, focus mode, etc., while the `cameraStartPreview` method mainly calls the `setPreviewTexture` method to specify the SurfaceTexture to which the camera preview data needs to be output . In addition, this sample program also overloads the `onPreviewFrame` callback interface, which is mainly used to receive the preview data returned by the camera. The input parameter `byte[] data` is the preview data captured by the camera. When the data is obtained This example will call the `mGLSurfaceView.requesetRender()` method to request an image to be drawn.

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mCameraNV21Byte = data;
        mCamera.addCallbackBuffer(data);
        mGLSurfaceView.requestRender();
    }

In this way, the camera preview data is saved in the mCameraNV21Byte array and mSurfaceTexture.

## 3. Initialize the FaceUnity SDK
Before using the SDK provided by FaceUnity, initialization must be done, as follows：

    public static void initFURenderer(Context context) {
        try {
            Log.e(TAG, "fu sdk version " + faceunity.fuGetVersion());

            /**
             * fuSetup faceunity initialization
             * Among them v3.bundle: face recognition data file, lack of this file will cause system initialization failure;
             *      authpack：Used for the memory array of authentication certificates. If not, please contact support@faceunity.com
             * Call other FU APIs after the call is completed.
             */
            InputStream v3 = context.getAssets().open(BUNDLE_v3);
            byte[] v3Data = new byte[v3.available()];
            v3.read(v3Data);
            v3.close();
            faceunity.fuSetup(v3Data, null, authpack.A());

            /**
             * Load the animation data file anim_model.bundle that needs to be loaded for the optimized expression tracking function; enable this function to
             * make expression coefficients and avatar-driven expressions more natural, reducing the appearance of abnormal expressions and model defects. This
             * function has less impact on performance. When this function is enabled, the animation model data is loaded through fuLoadAnimModel, and it can be
             * started after loading successfully. This function will affect the expression coefficient obtained by fuGetFaceInfo and the avatar model driven by
             * expression. Suitable for users who use Animoji and avatar functions, if not, don’t load it.
             */
            InputStream animModel = context.getAssets().open(BUNDLE_anim_model);
            byte[] animModelData = new byte[animModel.available()];
            animModel.read(animModelData);
            animModel.close();
            faceunity.fuLoadAnimModel(animModelData);

            /**
             * Load the 3D tensor data file ardata_ex.bundle in high precision mode. It is suitable for the face changing function, if the function is not used, it can
             * not be loaded; if the face changing function is used, it must be loaded, otherwise an error will be reported.
             */
            InputStream ar = context.getAssets().open(BUNDLE_ardata_ex);
            byte[] arDate = new byte[ar.available()];
            ar.read(arDate);
            ar.close();
            faceunity.fuLoadExtendedARData(arDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

## 4. Beautify the collected raw data
In the second step, we have obtained the raw data of the camera, then we will call the corresponding beauty API to perform secondary processing on the data. The specific methods are as follows:

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            mSurfaceTexture.updateTexImage(); // Force refresh to generate new texture image
            mSurfaceTexture.getTransformMatrix(mtx);
        } catch (Exception e) {
            return;
        }

        if (mCameraNV21Byte == null) {
            mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp);
            return;
        }
        mFuTextureId = mOnCameraRendererStatusListener.onDrawFrame(mCameraNV21Byte, mCameraTextureId, mCameraWidth, mCameraHeight, mtx, mSurfaceTexture.getTimestamp());
        // Used to shield the green screen caused by switching to the SDK data processing method (switching the SDK data processing method is for display, there is no need to switch in actual use, so there is no need to call to make this judgment, just use the else branch to draw directly)
        if (mFuTextureId <= 0) {
            mTextureOES.drawFrame(mCameraTextureId, mtx, mvp);
        } else {
            mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp); // Do display and draw on the interface
        }

        mFPSUtil.limit();
        mGLSurfaceView.requestRender();

        isDraw = true;
    }

The call of the `onDrawFrame` method here is triggered by the call of `mGLSurfaceView.requesetRender()` in step 2, where `mCameraNV21Byte` and `mCameraTextureId` are the raw camera data we got. In `onDrawFrame`, we performed `mOnCameraRendererStatusListener .onDrawFrame` callback, and the implementation of the callback interface is as follows:

    @Override
    public int onDrawFrame(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight, float[] mtx, long timeStamp) {
        int fuTextureId;
        byte[] backImage = new byte[cameraNV21Byte.length];
        fuTextureId = mFURenderer.onDrawFrame(cameraNV21Byte, cameraTextureId,
                cameraWidth, cameraHeight, backImage, cameraWidth, cameraHeight); // FU beauty operation
        if (mVideoFrameConsumerReady) {
            mIVideoFrameConsumer.consumeByteArrayFrame(backImage,
                    MediaIO.PixelFormat.NV21.intValue(), cameraWidth,
                    cameraHeight, mCameraOrientation, System.currentTimeMillis()); // Pass data to Agora RTC SDK
        }
        return fuTextureId;
    }

It can be seen that the callback interface calls the `mFURenderer.onDrawFrame` method, and the method mainly calls the following FaceUnity API to make the original data beautify:

	int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray, readBackW, readBackH, readBackImg);

Among them, `img` and `tex` are the raw data we passed in, and `mItemsArray` is the array of beauty effects that need to be used. When the method returns, the data obtained is the data after the beauty process. It will be written back to the `img` array we passed in, and the returned `fuTex` is the new texture identifier after beautification processing. The corresponding beauty effects can be adjusted by the following methods (all in faceunity):

    // filter_level filter intensity range 0~1 SDK default is 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "filter_level", mFilterLevel);
    // filter_name filter
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "filter_name", mFilterName.filterName());

    // skin_detect Precise skin beauty 0: Disable 1: Enable SDK The default is 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "skin_detect", mSkinDetect);
    // heavy_blur Beauty type 0: Clear skin 1: Hazy skin The SDK defaults to 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "heavy_blur", mHeavyBlur);
    // blur_level Microdermabrasion range 0~6 SDK default is 6
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_level", 6 * mBlurLevel);
    // blur_blend_ratio Fusion rate of microdermabrasion result and original image Range 0~1 SDK default is 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_blend_ratio", 1);

    // color_level Whitening range 0~1 SDK default is 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "color_level", mColorLevel);
    // red_level Ruddy Range 0~1 SDK default is 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "red_level", mRedLevel);
    // eye_bright Brightness Range 0~1 SDK default is 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_bright", mEyeBright);
    // tooth_whiten Dental beauty range 0~1 SDK default is 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "tooth_whiten", mToothWhiten);
    // face_shape_level Beauty degree Range 0~1 SDK default is 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape_level", mFaceShapeLevel);
    // face_shape Face shape 0: Goddess 1: Internet celebrity 2: Natural 3: Default 4: Custom (new version of American style) SDK default is 3
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape", mFaceShape);
    // eye_enlarging Big Eye Range 0~1 SDK default is 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_enlarging", mEyeEnlarging);
    // cheek_thinning Face-lifting range 0~1 SDK default is 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "cheek_thinning", mCheekThinning);
    // intensity_chin Chin Range 0~1 SDK defaults to 0.5, larger than 0.5, and smaller than 0.5
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_chin", mIntensityChin);
    // intensity_forehead Forehead Range 0~1 SDK defaults to 0.5. Greater than 0.5 becomes larger, less than 0.5 becomes smaller
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_forehead", mIntensityForehead);
    // intensity_nose Nose Range 0~1 SDK default is 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_nose", mIntensityNose);
    // intensity_mouth Mouth shape range 0~1 SDK defaults to 0.5, larger than 0.5, and smaller than 0.5
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_mouth", mIntensityMouth);

## 5. Locally render and display the processed data
If you need to preview the beauty effect locally, you can perform self-rendering on the data that has undergone beautification processing, as follows:

    mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp);

Among them, `mFuTextureId` is the new texture identifier returned by the beauty processing in step 4. We draw in the `onDrawFrame` method in the local `GLSurfaceView.Renderer` by calling the `mFullFrameRectTexture2D.drawFrame` method.

## 6. Send the processed data to the peer
After getting the data that has been processed for beauty, the next step is to transfer the data to the peer by calling the interface provided by the Agora RTC SDK. The specific methods are as follows:

    mIVideoFrameConsumer.consumeByteArrayFrame(backImage,
                    MediaIO.PixelFormat.NV21.intValue(), cameraWidth,
                    cameraHeight, mCameraOrientation,
                    System.currentTimeMillis());
    
Among them, `mIVideoFrameConsume` is the `IVideoFrameConsumer` object we saved in step 1. By calling the object's `consumeByteArrayFrame` method, we can send the beauty-processed data to the Agora RTC SDK, and then send it via SD-RTN To the opposite end, the input parameter `backImage` is the beautifying data we got in step 4, `MediaIO.PixelFormat.NV21.intValue()` is the format used by the video data, `cameraWidth` And `cameraHeight` is the width and height of the video image, `mCameraOrientation` is the angle that the video image needs to be rotated, and `System.currentTimeMillis()` is the current monotonically increasing time. Agora RTC SDK uses this to determine the sequence of each frame of data .

## 7. The peer renders and displays the received data after beauty processing
When the peer receives the sent beauty-processing data, we can render and display it (this is the default rendering method, of course, it can also be similar to a custom video source to achieve custom rendering. Expand), the specific method is as follows:

    private void setupRemoteView(int uid) {
        SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
        surfaceV.setZOrderOnTop(true);
        surfaceV.setZOrderMediaOverlay(true);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_FIT, uid));
    }

Where `uid` is the user ID of the sender.

## 8. More references
- [https://docs.agora.io/cn/](https://docs.agora.io/cn/)
- [http://www.faceunity.com/#/developindex](http://www.faceunity.com/#/developindex)
