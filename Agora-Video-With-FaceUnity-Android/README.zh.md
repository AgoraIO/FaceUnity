# Agora Video With Faceunity

这个开源示例项目演示了如果快速集成 [Agora](www.agora.io) 视频 SDK 和 [Faceunity](http://www.faceunity.com) 美颜 SDK，实现一对一视频聊天。

在这个示例项目中包含以下功能：

Agora 

- 加入通话和离开通话
- 实现一对一视频聊天
- 静音和解除静音

Faceunity

- 贴纸，滤镜，美颜滤镜，美肤，美型功能
- 切换采集模式
- 切换前置摄像头和后置摄像头

本项目采用了 Faceunity 提供的视频采集，美颜，本地渲染等视频前处理功能，使用了 Agora 提供的声音采集，编码，传输，解码和远端渲染功能。

Faceunity 美颜功能实现请参考 [Faceunity 官方文档](http://www.faceunity.com/docs_develop)

Agora 功能实现请参考 [Agora 官方文档](https://docs.agora.io/cn/Interactive%20Broadcast/API%20Reference/java/index.html)

由于在使用美颜的时候需要使用第三方采集，请特别参考[自定义视频采集和渲染 API](https://docs.agora.io/cn/Interactive%20Broadcast/custom_video_android?platform=Android)

## 运行示例程序
### 获取开发者账号与证书
1. 在 [Agora.io 注册](https://dashboard.agora.io/cn/signup/) 注册账号，并创建自己的测试项目，获取到 AppID。将 AppID 填写进 strings.xml

```
<string name="agora_app_id"><#YOUR APP ID#></string>
```
2. 请联系 sales@agora.io 获取证书文件替换本项目中的 **faceunity/src/main/java/com/faceunity/authpack.java**。

### 下载FaceUnity SDK与资源文件
1. 下载 [FaceUnity SDK 6.6.0](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-SDK-Android.zip)
2. 解压压缩包, 将 libs, assets, jniLibs 目录按下列规则置入项目
```
faceunity
  |__ build.gradle
  |__ proguard-rules.pro
  |__ libs
      |__ nama.jar
  |__ src
      |__ main
            |__ assets
                  |__ AI_model
                  |__ face_beautification.bundle
                  |__ fxaa.bundle
                  |__ v3.bundle
      |__ jniLibs
            |__ arm64-v8a
            |__ armeabi-v7a
            |__ x86
            |__ x86_64
```
3. 下载 [FaceUnity Bundle](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-bundle-Android.zip)
4. 解压压缩包, 将 bundle 目录放到app/src/main/effects 目录下


### 下载Agora SDK
1. 从官方网站下载Agora SDK: https://www.agora.io/en/download/.
2. 解压压缩包, 将 libs, jniLibs 目录按下列规则置入项目
```
app
  |__ build.gradle
  |__ proguard-rules.pro
  |__ libs
      |__ agora-rtc-sdk.jar
  |__ src
      |__ java
      |__ effects
      |__ res
      |__ AndroidManifest.xml
      |__ jniLibs
            |__ arm64-v8a
            |__ armeabi-v7a
            |__ x86
            |__ x86_64
```



## 运行环境
* Android Studio(3.1+)
* Android 真机设备(不支持模拟器)

## 联系我们

- 如果你遇到了困难，可以先参阅[常见问题](https://docs.agora.io/cn/faq)
- 如果你想了解更多官方示例，可以参考[官方SDK示例](https://github.com/AgoraIO)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考[官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网的一些社区开发者维护的项目，可以查看[社区](https://github.com/AgoraIO-Community)
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/AgoraIO/FaceUnity/issues)

## 代码许可

The MIT License (MIT)


