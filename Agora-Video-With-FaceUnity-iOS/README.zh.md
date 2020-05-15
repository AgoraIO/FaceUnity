# Agora Video With Faceunity

*Read this in other languages: [English](README.md)*

这个开源示例项目演示了如何快速集成 [Agora](www.agora.io) 视频 SDK 和 [Faceunity](http://www.faceunity.com) 美颜 SDK，实现一对一视频聊天。

在这个示例项目中包含以下功能：

Agora 

- 加入通话和离开通话
- 实现一对一视频聊天
- 静音和解除静音

Faceunity

- 美颜，贴纸，AR面具，换脸，表情识别，背景分割，手势识别等功能
- 切换采集模式
- 切换前置摄像头和后置摄像头

本项目采用了 Faceunity 提供的视频美颜前处理功能，使用了 Agora 提供的声音采集，编码，传输，解码和渲染功能，使用了 Agora Module 提供的视频采集功能。

Faceunity 美颜功能实现请参考 [Faceunity 官方文档](http://www.faceunity.com/docs_develop/#/markdown/integrate/introduction)

Agora 功能实现请参考 [Agora 官方文档](https://docs.agora.io/cn/Interactive%20Broadcast/API%20Reference/oc/docs/headers/Agora-Objective-C-API-Overview.html)

由于在使用美颜的时候需要使用第三方采集，请特别参考[自定义设备API](https://docs.agora.io/cn/Interactive%20Broadcast/raw_data_video_android?platform=Android)  或者 [自采集API](https://docs.agora.io/cn/Interactive%20Broadcast/raw_data_video_android?platform=Android)

## 下载FaceUnity SDK/资源文件
1. 下载 [FaceUnity SDK](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-SDK-iOS.zip)
2. 下载 [FaceUnity items](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-items-iOS.zip)
3. 将下载得到的两个zip包放到 AgoraWithFaceunity/Faceunity 下后解压即可

## 如何使用 Agora 模块化 SDK的采集功能

## 支持的功能
- [x]     Capturer
    - [x] Camera Capturer
        - [x] Support for front and rear camera switching
        - [x] Support for dynamic resolution switching
        - [x] Support I420, NV12, BGRA pixel format output
        - [x] Support Exposure, ISO
        - [ ] Support ZoomScale
        - [ ] Support Torch
        - [ ] Support watermark
    - [x] Audio Capturer
        - [x] Support single and double channel
        - [x] Support Mute
    - [x]  Video Adapter Filter (For processing the video frame direction required by different modules)
        - [x] Support VideoOutputOrientationModeAdaptative for RTC function
        - [x] Support ...FixedLandscape and ...FixedLandscape for CDN live streaming
- [x] Renderer
    - [x] gles
        - [x] Support glContext Shared
        - [x] Support mirror
        - [x] Support fit、hidden zoom mode


  
## 如何使用

#### 导入方式

1. 如果使用采集模块，需要下载 AgoraModule_Base 和 AgoraModule_Capturer 这两个 SDK. 
2. 把 AGMBase.framework、AGMCapturer.framework 这两个库拖入工程里面.
3. 依赖的系统库:
     * UIKit.framework
     * Foundation.framework
     * AVFoundation.framework
     * VideoToolbox.framework
     * AudioToolbox.framework
     * libz.framework
     * libstdc++.framework

#### SDK 下载
[AgoraModule_Base_iOS-1.2.2](https://download.agora.io/components/release/AgoraModule_Base_iOS-1.2.2.zip)
[AgoraModule_Capturer_iOS-1.2.2](https://download.agora.io/components/release/AgoraModule_Capturer_iOS-1.2.2.zip)
                               
                           
#### 添加权限
Add the following permissions in the info.plist file for device access according to your needs:

| Key      |    Type | Value  |
| :-------- | --------:| :--: |
| Privacy - Microphone Usage Description	  | String |  To access the microphone, such as for a video call.|
| Privacy - Camera Usage Description	     |   String |  To access the camera, such as for a video call.|
        

## 代码示例 

#### Objective-C

##### 如何使用采集器

```objc
AGMCapturerVideoConfig *videoConfig = [AGMCapturerVideoConfig defaultConfig];
videoConfig.videoSize = CGSizeMake(720, 1280);
videoConfig.sessionPreset = AGMCaptureSessionPreset720x1280;
self.cameraCapturer = [[AGMCameraCapturer alloc] initWithConfig:videoConfig];
[self.cameraCapturer start];
```

##### 方向适配器

```objc
self.videoAdapterFilter = [[AGMVideoAdapterFilter alloc] init];
self.videoAdapterFilter.ignoreAspectRatio = YES;
self.videoAdapterFilter.isMirror = YES;
#define DEGREES_TO_RADIANS(x) (x * M_PI/180.0)
CGAffineTransform rotation = CGAffineTransformMakeRotation( DEGREES_TO_RADIANS(90));
self.videoAdapterFilter.affineTransform = rotation;
```


##### 模块之间连接

```objc

[self.cameraCapturer addVideoSink:self.videoAdapterFilter];
[self.videoAdapterFilter addVideoSink:senceTimeFilter];

```

##### 自定义滤镜模块

创建一个继承 AGMVideoSource 并实现了 AGMVideoSink 协议的类，在 onFrame: 代理方法里面处理视频帧数据。

```objc

#import <AGMBase/AGMBase.h>

interface AGMSenceTimeFilter : AGMVideoSource <AGMVideoSink>

@end

#import "AGMSenceTimeFilter.h"

@implementation AGMSenceTimeFilter

- (void)onTextureFrame:(AGMImageFramebuffer *)textureFrame frameTime:(CMTime)time {
{
#pragma mark Write the filter processing.
    
    
#pragma mark When you're done, pass it to the next sink.
    if (self.allSinks.count) {
        for (id<AGMVideoSink> sink in self.allSinks) {
            [sink onTextureFrame:textureFrame frameTime:time];
        }
    }
}

@end

```

## 运行示例程序
首先在 [Agora.io 注册](https://dashboard.agora.io/cn/signup/) 注册账号，并创建自己的测试项目，获取到 AppID。将 AppID 填写进 KeyCenter.m

```
+ (NSString *)AppId {
     return @"Your App ID";
}
```
然后在 [Agora.io SDK](https://www.agora.io/cn/download/) 下载 视频通话 + 直播 SDK，解压后将其中的 libs/AgoraRtcEngineKit.framework 复制到本项目的 “AgoraWithFaceunity” 文件夹下。

请联系 [Faceunity](http://www.faceunity.com) 获取证书文件替换本项目/AgoraWithFaceunity/Faceunity 文件夹中的 ”authpack.h“ 文件。

最后使用 XCode 打开 AgoraWithFaceunity.xcodeproj，连接 iPhone／iPad 测试设备，设置有效的开发者签名后即可运行。

## 运行环境
* XCode 8.0 +
* iOS 真机设备
* 不支持模拟器

## FAQ
- 请尽量不要使用声网提供的裸数据接口集成美颜功能
- videosource内部是强引用，不用时必须置nil，不然会造成循环引用
- 如果遇到大头问题请联系技术支持

## 联系我们

- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 如果在集成中遇到问题，你可以到 [开发者社区](https://dev.agora.io/cn/) 提问
- 如果有售前咨询问题，可以拨打 400 632 6626，或加入官方Q群 12742516 提问
- 如果需要售后技术支持，你可以在 [Agora Dashboard](https://dashboard.agora.io) 提交工单
- 如果发现了示例代码的bug，欢迎提交 [issue](https://github.com/AgoraIO/Agora-Video-With-FaceUnity-iOS/issues)

## 代码许可

The MIT License (MIT).


