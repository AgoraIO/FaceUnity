# Agora Video With Faceunity

这个开源示例项目演示了如果快速集成 [Agora](www.agora.io) 视频 SDK 和 [Faceunity](http://www.faceunity.com) 美颜 SDK，实现一对一视频聊天。

Faceunity 美颜功能实现请参考 [Faceunity 官方文档](http://www.faceunity.com/docs_develop)

Agora 功能实现请参考 [Agora 官方文档](https://docs.agora.io/cn/Interactive%20Broadcast/API%20Reference/java/index.html)

由于在使用美颜的时候需要使用第三方采集，请特别参考[自定义视频采集和渲染 API](https://docs.agora.io/cn/Interactive%20Broadcast/custom_video_android?platform=Android)

## 快速开始
### 获取开发者账号与 FaceUnity 证书
1. 在 [Agora.io 注册](https://dashboard.agora.io/cn/signup/) 注册账号，并创建自己的测试项目，获取到 AppID。将 AppID 填写进 strings.xml

```
<string name="agora_app_id"><#YOUR APP ID#></string>
```
2. 请联系 sales@agora.io 获取证书文件替换本项目中的 **faceunity/src/main/java/com/faceunity/authpack.java**。

### 设置 Agora SDK

现在 demo 将自动从 JCenter 上下载 Agora 视频 SDK，默认情况下您不需要从别的地方再下载。

### 下载FaceUnity SDK与资源文件
1. Demo 当前适配到 FaceUnity SDK v6.6，您可到官方链接下载 [FaceUnity SDK v6.6](https://github.com/Faceunity/FULiveDemoDroid/releases/download/v6.6/Faceunity-Android-v6.6.zip)
2. 解压压缩包, 将 libs, assets, jniLibs 目录按此方式拷贝到项目中
```
faceunity
  |__ libs
      |__ nama.jar

  |__ src
      |__ main
            |__ assets
                    |__ AI_model
                        |__  ai_bgseg.bundle
                             // other bundles related to AI
                             // ...
                             // ... 

                    |__ body_slim.bundle
                    |__ face_beautification.bundle
                    |__ face_makeup.bundle
                    |__ fxaa.bundle
                    |__ hair_gradient.bundle
                    |__ hair_normal.bundle
                    |__ v3.bundle
      |__ jniLibs
            |__ arm64-v8a
            |__ armeabi-v7a
            |__ x86
            |__ x86_64
```

FaceUnity SDK v6.6 相比之前的版本拥有更多的文件, 您需要将其都拷贝到对应的路径下（包括子路径）。

### 下载 FaceUnity Demo 资源

FaceUnity 的 effects/bundles 资源不是 SDK 的一部分，通常您需要为您的应用定制资源库。

为了运行此项目，您可以下载 [官方 demo](https://github.com/Faceunity/FULiveDemoDroid) 并使用其中的资源文件。

当您下载好官方 demo 的源码后，解压缩 zip 文件并将 `app/src/main/assets` 路径下的文件全部拷贝到工程的对应文件夹下。

注意，只有得到 licence 授权的资源库类型才能够生效。

## Capture and Rendering

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


