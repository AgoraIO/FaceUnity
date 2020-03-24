# 快速上手 Agora RTC 与 FaceUnity
  
如今越来越多的用户开始对美颜/道具这一功能产生越来越大的需求，尤其是在泛娱乐场景下。而现如今市场上有许多第三方的美颜 SDK 可以供开发者选择使用，那么这些第三方的美颜 SDK 是否可以与 Agora RTC SDK 进行结合从而实现实时视频泛娱乐这一应用场景呢？答案当然是肯定的。  
本文的目的就是要帮助大家快速了解如何使用。默认情况下，Agora RTC SDK 提供端到端的整体方案，Agora RTC SDK 负责采集音视频，前处理，然后将数据发送到对端进行渲染，这种运行模式通常能满足大多数开发者的需求。但如果开发者希望对采集到的数据进行二次处理（比如美颜等），建议通过 `setVideoSource(IVideoSource videoSource)` 调用自定义视频数据源来实现。在这种情况下，整个过程的数据流如下图所示：

1. 从相机采集视频数据
2. 将采集到的数据传递给 FaceUnity SDK 进行二次处理，并进行渲染
3. 将处理过的数据传递给 Agora RTC SDK
4. Agora RTC SDK 将处理过的数据编码通过 SD-RTN 传输到对端，对端进行解码并渲染

![flow for Agora&FU](data_flow.png "flow for Agora&FU")

本文将以 [Android 平台代码](https://github.com/AgoraIO/FaceUnity/tree/master/Agora-Video-With-FaceUnity-Android) 为例子来具体讲解如何实现，其他平台实现参考 [FaceUnity](https://github.com/AgoraIO/FaceUnity/)。

## 1. 设置 Agora RTC SDK 视频源为自定义视频源

    // 一个通用的实现 IVideoSource 接口的类如下，本示例程序中未用到该类
    public class MyVideoSource implements IVideoSource {
        @Override
        public int getBufferType() {
            // 返回当前帧数据缓冲区的类型，每种类型数据在 Agora RTC SDK 内部会经过不同的处理，所以必须与帧数据的类型保持一致
            // 有三种类型 BufferType.BYTE_ARRAY/BufferType.TEXTURE/BufferType.BYTE_BUFFER
            return BufferType.BYTE_ARRAY;
        }

        @Override
        public boolean onInitialize(IVideoFrameConsumer consumer) {
            // IVideoFrameConsumer 是由 Agora RTC SDK 创建的，在 MyVideoSource 生命周期中注意保存它的引用，因为后续将通过它将数据传送给SDK
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
            // 释放对 Consumer 的引用
            mConsumer = null;
        }
    }

在本示例程序中，使用了 `TextureSource` 类，该类是 Agora RTC SDK 提供的适用于纹理类型(texture)视频源的预定义实现。当实例化了该类后，可调用 `setVideoSource` 接口来设置视频源，具体用法如下：

	mRtcEngine.setVideoSource(mTextureSource);

## 2. 采集数据
本示例程序中，使用到的自定义视频源为相机，视频数据采集在示例程序中完成，具体做法如下：

    private void openCamera(final int cameraType) {
        synchronized (mCameraLock) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            ......
            ...... // 省略部分代码
            mCameraOrientation = CameraUtils.getCameraOrientation(cameraId);
            CameraUtils.setCameraDisplayOrientation(mActivity, cameraId, mCamera); // 根据相机传感器方向和手机当前方向设置相机预览方向

            Camera.Parameters parameters = mCamera.getParameters(); 

            CameraUtils.setFocusModes(parameters);
            int[] size = CameraUtils.choosePreviewSize(parameters, mCameraWidth, mCameraHeight); // 选择最佳预览尺寸
            ......
            ...... // 省略部分代码

            mCamera.setParameters(parameters);
        }

        cameraStartPreview();
    }
    
    private void cameraStartPreview() {
        ......
        ...... // 省略部分代码  
        mCamera.setPreviewTexture(mSurfaceTexture = new SurfaceTexture(mCameraTextureId));
        mCamera.startPreview();
    }

其中 `openCamera` 方法主要是对相机做了一些参数配置，例如预览尺寸，显示方向，对焦模式等，而 `cameraStartPreview ` 方法则主要调用了 `setPreviewTexture` 方法来指定相机预览数据所需要输出到的 SurfaceTexture。另外本示例程序中还重载了 `onPreviewFrame` 回调接口，该接口主要用来接收相机返回的预览数据，其中入参 `byte[] data` 就是相机所捕捉到的预览数据，当得到该数据后本例会调用 `mGLSurfaceView.requesetRender()` 方法来请求绘制图像。

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mCameraNV21Byte = data;
        mCamera.addCallbackBuffer(data);
        mGLSurfaceView.requestRender();
    }

如此一来，相机的预览数据就保存在了 mCameraNV21Byte 数组和 mSurfaceTexture 中。

## 3. 初始化 FaceUnity SDK
在使用 FaceUnity 提供的 SDK 之前，必须进行初始化工作，具体做法如下：

    public static void initFURenderer(Context context) {
        try {
            Log.e(TAG, "fu sdk version " + faceunity.fuGetVersion());

            /**
             * fuSetup faceunity 初始化
             * 其中 v3.bundle：人脸识别数据文件，缺少该文件会导致系统初始化失败；
             *      authpack：用于鉴权证书内存数组。若没有，请咨询 support@faceunity.com
             * 首先调用完成后再调用其他FU API
             */
            InputStream v3 = context.getAssets().open(BUNDLE_v3);
            byte[] v3Data = new byte[v3.available()];
            v3.read(v3Data);
            v3.close();
            faceunity.fuSetup(v3Data, null, authpack.A());

            /**
             * 加载优化表情跟踪功能所需要加载的动画数据文件 anim_model.bundle；
             * 启用该功能可以使表情系数及 avatar 驱动表情更加自然，减少异常表情、模型缺陷的出现。该功能对性能的影响较小。
             * 启用该功能时，通过 fuLoadAnimModel 加载动画模型数据，加载成功即可启动。该功能会影响通过 fuGetFaceInfo 获取的 expression 表情系数，以及通过表情驱动的 avatar 模型。
             * 适用于使用 Animoji 和 avatar 功能的用户，如果不是，可不加载
             */
            InputStream animModel = context.getAssets().open(BUNDLE_anim_model);
            byte[] animModelData = new byte[animModel.available()];
            animModel.read(animModelData);
            animModel.close();
            faceunity.fuLoadAnimModel(animModelData);

            /**
             * 加载高精度模式的三维张量数据文件 ardata_ex.bundle。
             * 适用于换脸功能，如果没用该功能可不加载；如果使用了换脸功能，必须加载，否则会报错
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

## 4. 对采集到的原始数据进行美颜处理
在第 2 步中，我们已经得到了相机的原始数据，那么下面我们就要调用相应的美颜 API 来对该数据进行二次处理，具体做法如下：

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            mSurfaceTexture.updateTexImage(); // 强制刷新生成新的纹理图片
            mSurfaceTexture.getTransformMatrix(mtx);
        } catch (Exception e) {
            return;
        }

        if (mCameraNV21Byte == null) {
            mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp);
            return;
        }
        mFuTextureId = mOnCameraRendererStatusListener.onDrawFrame(mCameraNV21Byte, mCameraTextureId, mCameraWidth, mCameraHeight, mtx, mSurfaceTexture.getTimestamp());
        // 用于屏蔽切换调用 SDK 处理数据方法导致的绿屏（切换SDK处理数据方法是用于展示，实际使用中无需切换，故无需调用做这个判断，直接使用 else 分支绘制即可）
        if (mFuTextureId <= 0) {
            mTextureOES.drawFrame(mCameraTextureId, mtx, mvp);
        } else {
            mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp); // 做显示绘制到界面上
        }

        mFPSUtil.limit();
        mGLSurfaceView.requestRender();

        isDraw = true;
    }

此处的 `onDrawFrame` 方法调用由第 2 步中 `mGLSurfaceView.requesetRender()` 调用触发，其中的 `mCameraNV21Byte` 与 `mCameraTextureId` 就是我们得到的相机原始数据，在 `onDrawFrame` 中我们进行了 `mOnCameraRendererStatusListener.onDrawFrame` 的回调，而该回调接口的实现如下：

    @Override
    public int onDrawFrame(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight, float[] mtx, long timeStamp) {
        int fuTextureId;
        byte[] backImage = new byte[cameraNV21Byte.length];
        fuTextureId = mFURenderer.onDrawFrame(cameraNV21Byte, cameraTextureId,
                cameraWidth, cameraHeight, backImage, cameraWidth, cameraHeight); // FU 美颜操作
        if (mVideoFrameConsumerReady) {
            mIVideoFrameConsumer.consumeByteArrayFrame(backImage,
                    MediaIO.PixelFormat.NV21.intValue(), cameraWidth,
                    cameraHeight, mCameraOrientation, System.currentTimeMillis()); // 数据传递给 Agora RTC SDK
        }
        return fuTextureId;
    }

可以看到，该回调接口又调用了 `mFURenderer.onDrawFrame` 方法，而该方法中主要调用了如下 FaceUnity 的 API 来对原始数据做美颜处理：

	int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray, readBackW, readBackH, readBackImg);

其中 `img` 和 `tex` 是我们传入的原始数据，`mItemsArray` 则是需要用到的美颜效果数组，当该方法返回时，得到的数据便是经过美颜处理的数据，该数据会写回到我们传入的 `img` 数组中，而返回的 `fuTex` 则是经过美颜处理的新的纹理标识。而相应的美颜效果可以通过如下方法进行调节(均在 faceunity 当中)：

    // filter_level 滤镜强度 范围 0~1 SDK 默认为 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "filter_level", mFilterLevel);
    // filter_name 滤镜
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "filter_name", mFilterName.filterName());

    // skin_detect 精准美肤 0:关闭 1:开启 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "skin_detect", mSkinDetect);
    // heavy_blur 美肤类型 0:清晰美肤 1:朦胧美肤 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "heavy_blur", mHeavyBlur);
    // blur_level 磨皮 范围 0~6 SDK 默认为 6
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_level", 6 * mBlurLevel);
    // blur_blend_ratio 磨皮结果和原图融合率 范围 0~1 SDK 默认为 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_blend_ratio", 1);

    // color_level 美白 范围 0~1 SDK 默认为 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "color_level", mColorLevel);
    // red_level 红润 范围 0~1 SDK 默认为 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "red_level", mRedLevel);
    // eye_bright 亮眼 范围 0~1 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_bright", mEyeBright);
    // tooth_whiten 美牙 范围 0~1 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "tooth_whiten", mToothWhiten);
    // face_shape_level 美型程度 范围 0~1 SDK 默认为 1
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape_level", mFaceShapeLevel);
    // face_shape 脸型 0：女神 1：网红 2：自然 3：默认 4：自定义（新版美型） SDK 默认为 3
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape", mFaceShape);
    // eye_enlarging 大眼 范围 0~1 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_enlarging", mEyeEnlarging);
    // cheek_thinning 瘦脸 范围 0~1 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "cheek_thinning", mCheekThinning);
    // intensity_chin 下巴 范围 0~1 SDK 默认为 0.5 大于   0.5 变大，小于 0.5 变小
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_chin", mIntensityChin);
    // intensity_forehead 额头 范围 0~1 SDK 默认为 0.5   大于 0.5 变大，小于 0.5 变小
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_forehead", mIntensityForehead);
    // intensity_nose 鼻子 范围 0~1 SDK 默认为 0
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_nose", mIntensityNose);
    // intensity_mouth 嘴型 范围 0~1 SDK 默认为 0.5   大于 0.5 变大，小于 0.5 变小
    faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_mouth", mIntensityMouth);

## 5. 本地对经过美颜处理的数据进行渲染显示
如果本地需要对美颜效果进行预览，则可以对进行过美颜处理的数据进行自渲染，具体做法如下：

    mFullFrameRectTexture2D.drawFrame(mFuTextureId, mtx, mvp);

其中 `mFuTextureId` 便是第 4 步中经过美颜处理返回的新的纹理标识，我们通过调用 `mFullFrameRectTexture2D.drawFrame` 方法在本地 `GLSurfaceView.Renderer` 中的 `onDrawFrame` 方法中进行绘制。

## 6. 将经过美颜处理的数据发送给对端
当拿到已经经过美颜处理的数据后，下一步要做的就是通过调用 Agora RTC SDK 提供的接口将该数据传送给对端，具体做法如下：

    mIVideoFrameConsumer.consumeByteArrayFrame(backImage,
                    MediaIO.PixelFormat.NV21.intValue(), cameraWidth,
                    cameraHeight, mCameraOrientation,
                    System.currentTimeMillis());
    
其中 `mIVideoFrameConsume` 就是我们在第 1 步中保存的 `IVideoFrameConsumer` 对象，通过调用该对象的 `consumeByteArrayFrame` 方法，我们就可以将经过美颜处理的数据发送给 Agora RTC SDK，然后通过 SD-RTN 传到对端，其中的入参 `backImage` 便是我们在第 4 步中得到的经过美颜处理的数据，`MediaIO.PixelFormat.NV21.intValue()` 为该视频数据使用的格式， `cameraWidth` 与 `cameraHeight` 为视频图像的宽与高，`mCameraOrientation` 为视频图像需要旋转的角度，`System.currentTimeMillis()` 为当前单调递增时间，Agora RTC SDK 以此来判断每一帧数据的先后顺序。

## 7. 对端对收到的经过美颜处理的数据进行渲染显示
当对端收到发送过来的经过美颜处理的数据时，我们可以对其进行渲染显示(这是默认的渲染方式，当然也可以类似于自定义的视频源去实现自定义渲染，这里就不展开)，具体做法如下：

    private void setupRemoteView(int uid) {
        SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
        surfaceV.setZOrderOnTop(true);
        surfaceV.setZOrderMediaOverlay(true);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_FIT, uid));
    }

其中 `uid` 为发送端的用户标识。

## 8. 更多参考
- [https://docs.agora.io/cn/](https://docs.agora.io/cn/)
- [http://www.faceunity.com/#/developindex](http://www.faceunity.com/#/developindex)
