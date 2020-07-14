# Agora-FaceUnity-Tutorial-Windows

*Read this in other languages: [English](README.md)*

 这个开源示例代码演示了如何快速的集成 Agora-FaceUnity-Tutorial-Windows. 这个demo包含以下功能模块

 - Agora直播功能
 - Faceunity 贴纸，滤镜，美颜

 本开源程序采用 **C++** 语言

Agora-FaceUnity-Tutorial-Windows 还支持 Android / IOS 平台，你可以查看对应其他平台的示例程序

- https://github.com/AgoraIO-Community/Agora-Video-With-FaceUnity-Android
- https://github.com/AgoraIO-Community/Agora-Video-With-FaceUnity-IOS

##准备Agora SDK
1. [下载 Agora SDK](https://download.agora.io/sdk/release/Agora_Native_SDK_for_Windows(x86)_v3_0_0_FULL.zip)

2. 加压文件，sdk拷贝到Agora-Video-With-FaceUnity-Windows下面

3. sdk版本在3.0.1以上，把libs放到Agora-Video-With-FaceUnity-Windows下面
##下载FaceUnity SDK/资源文件

1. 下载 [FaceUnity SDK](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnitySDK-Windows.zip)
2. 解压文件，改名为FaceUnitySDK，放到Agora-Video-With-FaceUnity-Windows下面。
3. 把FaceUnitySDK目录下的bin文件夹，放到Agora-Video-With-FaceUnity-Windows下面。
##下载其他依赖库
1. 下载[windows依赖库](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/windows-depencies.zip)
2. 解压文件，把windows-depencies下面的Thirdparty拷贝到Agora-Video-With-FaceUnity-Windows下面

## 开发环境
* VC++2013 或更高版本
* WIN7 或更高版本

## 运行示例程序
首先在 [Agora.io 注册](https://dashboard.agora.io/cn/signup/) 注册账号，并创建自己的测试项目，获取到 AppID。修改配置文件AgoraFaceUnity.ini


[LoginInfo]

    AppId=//Your AppID


用VS2013 打开 Agora-FaceUnity-Tutorial-Windows.sln，程序中默认填写采集参数是640x480 ，那么对应的 VideoSolutinIndex 必须为40，如果摄像头不支持640x480 ，需要修改采集宽高，同时VideoSolutionIdnex 也需要相应的修改.


## 联系我们

- 如果你遇到了困难，可以先参阅[常见问题](https://docs.agora.io/cn/faq)
- 如果你想了解更多官方示例，可以参考[官方SDK示例](https://github.com/AgoraIO)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考[官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网的一些社区开发者维护的项目，可以查看[社区](https://github.com/AgoraIO-Community)
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/AgoraIO/FaceUnity/issues)

## 代码许可

The MIT License (MIT).
