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
2. 请联系 sales@agora.io 获取证书文件替换本项目中的 **app/src/main/java/io/agora/rtcwithfu/authpack.java**。

### 设置 Agora SDK

现在 demo 将自动从 JCenter 上下载 Agora 视频 SDK，默认情况下您不需要从别的地方再下载。

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


