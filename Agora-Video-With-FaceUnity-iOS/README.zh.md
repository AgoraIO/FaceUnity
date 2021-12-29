# Agora Video With Faceunity

*Read this in other languages: [English](README.md)*

这个开源示例项目演示了如何快速集成 [Agora](www.agora.io) 视频 SDK 和 [Faceunity](http://www.faceunity.com) 美颜 SDK，实现一对一视频聊天。

本项目采用了 Faceunity 提供的视频美颜前处理功能，使用了 Agora 提供的声音采集，编码，传输，解码和渲染功能，使用了 Agora Module 提供的视频采集功能。

Faceunity 美颜功能实现请参考 [Faceunity 官方文档](http://www.faceunity.com/docs_develop/#/markdown/integrate/introduction)

Agora 功能实现请参考 [Agora 官方文档](https://docs.agora.io/cn/Interactive%20Broadcast/API%20Reference/oc/docs/headers/Agora-Objective-C-API-Overview.html)

## 1.运行示例程序

这个段落主要讲解了如何编译和运行实例程序。

### 1.1 创建Agora账号并获取AppId

在编译和启动实例程序前，您需要首先获取一个可用的App Id:

1. 在[agora.io](https://dashboard.agora.io/signin/)创建一个开发者账号
2. 前往后台页面，点击左部导航栏的 **项目 > 项目列表** 菜单
3. 复制后台的 **App Id** 并备注，稍后启动应用时会用到它
4. 在项目页面生成临时 **Access Token** (24小时内有效)并备注，注意生成的Token只能适用于对应的频道名。

5. 将 AppID 和 Token 填写进 AppID.m

    ```
    + (NSString *)AppId {
        return <#Your App Id#>;
    }
    ```

### 1.2 替换相芯美颜证书 `authpack.h`
请联系 [Faceunity](http://www.faceunity.com) 获取证书文件替换本项目 `BeautifyExample/FaceUnity` 文件夹中的 ”authpack.h“ 文件。

### 1.3 集成 Agora 视频 SDK

1. 执行以下命令更新CocoaPods依赖

```
pod install
```
  
2. 最后使用 XCode 打开 BeautifyExample.xcworkspace，连接iPhone／iPad 测试设备，设置有效的开发者签名后即可运行。



## 2.Agora摄像头采集模块 AMGCapturer
### 2.1 支持的功能
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



### 2.3 代码示例 

#### 2.3.1 Objective-C
##### 如何使用采集器

```objc
// init process manager
self.processingManager = [[VideoProcessingManager alloc] init];
    
// init capturer, it will push pixelbuffer to rtc channel
AGMCapturerVideoConfig *videoConfig = [AGMCapturerVideoConfig defaultConfig];
videoConfig.sessionPreset = AVCaptureSessionPreset1280x720;
videoConfig.fps = 30;
self.capturerManager = [[CapturerManager alloc] initWithVideoConfig:videoConfig delegate:self.processingManager];
    
// add FaceUnity filter and add to process manager
self.videoFilter = [FUManager shareManager];
self.videoFilter.enabled = YES;
[self.processingManager addVideoFilter:self.videoFilter];
[self.capturerManager startCapture];

```

#### 本地视图设置

* Agora 自采集自渲染

```objc

    [self.localView layoutIfNeeded];
    self.glVideoView = [[AGMEAGLVideoView alloc] initWithFrame:self.localView.frame];
//    [self.glVideoView setRenderMode:(AGMRenderMode_Fit)];
    [self.localView addSubview:self.glVideoView];
    [self.capturerManager setVideoView:self.glVideoView];
    // set custom capturer as video source
    [self.rtcEngineKit setVideoSource:self.capturerManager];
    
```

##### 自定义滤镜模块

创建一个实现 `VideoFilterDelegate` 协议的类 `FUManager`，在 `processFrame:` 代理方法里面处理视频帧数据。

```objc

#pragma mark - VideoFilterDelegate
/// process your video frame here
- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame {
    [[FUTestRecorder shareRecorder] processFrameWithLog];
    
    FURenderInput *input = [[FURenderInput alloc] init];
    input.pixelBuffer = frame;
    //默认图片内部的人脸始终是朝上，旋转屏幕也无需修改该属性。
    input.renderConfig.imageOrientation = FUImageOrientationUP;
    //开启重力感应，内部会自动计算正确方向，设置fuSetDefaultRotationMode，无须外面设置
    input.renderConfig.gravityEnable = YES;
    //如果来源相机捕获的图片一定要设置，否则将会导致内部检测异常
    input.renderConfig.isFromFrontCamera = YES;
    //该属性是指系统相机是否做了镜像: 一般情况前置摄像头出来的帧都是设置过镜像，所以默认需要设置下。如果相机属性未设置镜像，改属性不用设置。
    input.renderConfig.isFromMirroredCamera = YES;
    FURenderOutput *output = [self renderWithInput:input];
    if ([self.delegate respondsToSelector:@selector(checkAI)]) {
        [self.delegate checkAI];
    }
    return output.pixelBuffer;
}

```

##### FaceUnity美颜模块加载

在 `ViewController.m` 中导入头文件

```objc

#import "FUDemoManager.h"

```
在 `viewDidLoad` 中初始化 FaceUnity的界面和 SDK，FaceUnity界面工具和SDK都放在FUDemoManager中初始化了，也可以自行调用FUAPIDemoBar和FUManager初始化

```objc
    // FaceUnity UI
    CGFloat safeAreaBottom = 0;
    if (@available(iOS 11.0, *)) {
        safeAreaBottom = [UIApplication sharedApplication].delegate.window.safeAreaInsets.bottom;
    }
    [FUDemoManager setupFaceUnityDemoInController:self originY:CGRectGetHeight(self.view.frame) - FUBottomBarHeight - safeAreaBottom];
```

底部栏切换功能：使用不同的ViewModel控制

```C
-(void)bottomDidChangeViewModel:(FUBaseViewModel *)viewModel {
    if (viewModel.type == FUDataTypeBeauty || viewModel.type == FUDataTypebody) {
        self.renderSwitch.hidden = NO;
    } else {
        self.renderSwitch.hidden = YES;
    }

    [[FUManager shareManager].viewModelManager addToRenderLoop:viewModel];
    
    // 设置人脸数
    [[FUManager shareManager].viewModelManager resetMaxFacesNumber:viewModel.type];
}

```

更新美颜参数

```C
- (IBAction)filterSliderValueChange:(FUSlider *)sender {
    _seletedParam.mValue = @(sender.value * _seletedParam.ratio);
    /**
     * 这里使用抽象接口，有具体子类决定去哪个业务员模块处理数据
     */
    [self.selectedView.viewModel consumerWithData:_seletedParam viewModelBlock:nil];
}


#### 资源释放和销毁

参见 `delloc:` 方法


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


