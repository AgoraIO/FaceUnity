# Agora Video With Faceunity

这个开源示例项目演示了如果快速集成 [Agora](www.agora.io) 视频 SDK 和 [Faceunity](http://www.faceunity.com) 美颜 SDK，实现一对一视频聊天。

Faceunity 美颜功能实现请参考 [Faceunity 官方文档](http://www.faceunity.com/docs_develop)

Agora 功能实现请参考 [Agora 官方文档](https://docs.agora.io/cn/Interactive%20Broadcast/API%20Reference/java/index.html)

由于在使用美颜的时候需要使用第三方采集，请特别参考[自定义视频采集和渲染 API](https://docs.agora.io/cn/Interactive%20Broadcast/custom_video_android?platform=Android)

## 快速开始

### 设置 Agora SDK

#### 获取开发者证书 

1. 在 [Agora.io 注册](https://dashboard.agora.io/cn/signup/) 注册账号，并创建自己的测试项目，获取到 AppID。将 AppID 填写进 strings.xml

```
<string name="agora_app_id"><#YOUR APP ID#></string>
```
现在 demo 将自动从 JCenter 上下载 Agora 视频 SDK，默认情况下您不需要从别的地方再下载。

### 添加FaceUnity SDK
#### 1.build.gradle配置
##### 1.1 allprojects配置
```
allprojects {
    repositories {
        ...
        maven { url 'http://maven.faceunity.com/repository/maven-public/' }
        ...
  }
}
```
##### 1.2 dependencies导入依赖
```
dependencies {
...
implementation 'com.faceunity:core:7.4.1.0' // 实现代码
implementation 'com.faceunity:model:7.4.1.0' // 道具以及AI bundle
...
}
```

#### 2.其他接入方式-底层库依赖

```
dependencies {
...
implementation 'com.faceunity:nama:7.4.1.0' //底层库-标准版
implementation 'com.faceunity:nama-lite:7.4.1.0' //底层库-lite版
...
}
```

如需指定应用的 so 架构，请修改 app 模块 build.gradle：
```
android {
    // ...
    defaultConfig {
        // ...
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
}

```
如需剔除不必要的 assets 文件，请修改 app 模块 build.gradle：
```
android {
    // ...
    applicationVariants.all { variant ->
        variant.mergeAssetsProvider.configure {
            doLast {
                delete(fileTree(dir: outputDir, includes: ['model/ai_face_processor_lite.bundle',
                                                           'model/ai_hand_processor.bundle',
                                                           'graphics/controller.bundle',
                                                           'graphics/fuzzytoonfilter.bundle',
                                                           'graphics/fxaa.bundle',
                                                           'graphics/tongue.bundle']))
            }
        }
    }
}
```

#### 3.证书激活
1. 请联系 sales@agora.io 获取证书文件替换本项目中的 **app/src/main/java/io/agora/rtcwithfu/authpack.java**。

### 使用SDK
#### 1.初始化
在 FURenderer 类 的  setup 静态方法是对 FaceUnity SDK 一些全局数据初始化的封装，可以在 Application 中调用，也可以在工作线程调用，仅需初始化一次即可。
当前demo在 MyApplication 类中
#### 2.创建
在 FaceUnityDataFactory 类 的  bindCurrentRenderer 方法是对 FaceUnity SDK 每次使用前数据初始化的封装。
在 FUChatActivity 类中 设置 onResume方法，在该方法中执行bindCurrentRenderer。
```java
    @Override
    protected void onResume() {
        super.onResume();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        mFaceUnityDataFactory.bindCurrentRenderer();
        preprocessor.setRenderEnable(true);
        mVideoManager.startCapture();
    }
```
#### 3.图像处理
在 FURenderer 类 的  onDrawFrame 方法是对 FaceUnity SDK 图像处理方法的封装，该方法有许多重载方法适用于不同的数据类型需求。
在 PreprocessorFaceUnity 类中注册 IPreprocessor监听，在 onPreProcessFrame 方法中执行。（代码如上）
onDrawFrameSingleInput 是单输入，输入图像buffer数组或者纹理Id，输出纹理Id
onDrawFrameDualInput 双输入，输入图像buffer数组与纹理Id，输出纹理Id。性能上，双输入优于单输入
在onDrawFrameSingleInput 与onDrawFrameDualInput 方法内，在执行底层方法之前，都会执行prepareDrawFrame()方法(执行各个特效模块的任务，将美颜参数传给底层)。
```java
 @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (!renderSwitch) {
            return outFrame;
        }
        if (mGLHandler == null) {
            startGLThread();
        }
        if (skipFrame > 0) {
            skipFrame--;
            outFrame.textureId = 0;
            return outFrame;
        }
        mFURenderer.setInputOrientation(outFrame.rotation);
        int texId = mFURenderer.onDrawFrameDualInput(outFrame.image,
                outFrame.textureId, outFrame.format.getWidth(),
                outFrame.format.getHeight());

        // The texture is transformed to texture2D by beauty module.
        if (skipFrame <= 0) {
            outFrame.textureId = texId;
            outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        } else {
            outFrame.textureId = 0;
        }
        return outFrame;
    }
```
#### 4.销毁
在 FURenderer 类 的  release 方法是对 FaceUnity SDK 数据销毁的封装。
在 PreprocessorFaceUnity 类中注册releaseFURender方法用于摧毁Furender
```java
    public void releaseFURender() {
        renderSwitch = false;
        mGLHandler.removeCallbacksAndMessages(0);
        mGLHandler.post(() -> FURenderer.getInstance().release());
        mGLHandler = null;
    }
```
#### 5.切换相机
调用CameraVideoManager的switchCamera方法进行切换

#### 6.旋转手机
调用 FURenderer 类 的  setDeviceOrientation 方法，用于重新为 SDK 设置参数。
在FUChatActivity中

```java
    protected void initStreamingManager(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mSensorManager) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
		//具体代码见  AVStreamingActivity
    }

```
### 接口介绍

- IFURenderer 是核心接口，提供了创建、销毁、渲染等接口。
- FaceUnityDataFactory 控制四个功能模块，用于功能模块的切换，初始化
- FaceBeautyDataFactory 是美颜业务工厂，用于调整美颜参数。
- PropDataFactory 是道具业务工厂，用于加载贴纸效果。
- MakeupDataFactory 是美妆业务工厂，用于加载美妆效果。
- BodyBeautyDataFactory 是美体业务工厂，用于调整美体参数。

## 相机采集和渲染

项目用到了采集渲染库 `app/libs/video-capturer.aar`， 如果想对这个库的源码和用法有更进一步的了解，请移步[这里](https://github.com/AgoraIO/Agora-Extensions/tree/master/VideoCapture/Android)

## 联系我们

- 如果你遇到了困难，可以先参阅[常见问题](https://docs.agora.io/cn/faq)
- 如果你想了解更多官方示例，可以参考[官方SDK示例](https://github.com/AgoraIO)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考[官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网开发者社区维护的项目，可以查看[社区](https://github.com/AgoraIO-Community)
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/AgoraIO/FaceUnity/issues)

## 代码许可

The MIT License (MIT)

