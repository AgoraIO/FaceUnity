# Agora Video With Faceunity

*其他语言版本： [简体中文](README.zh.md)*

This open source demo project demonstrates how to implement 1to1 video chat with  [Agora] (www.agora.io) video SDK and the [Faceunity] (http://www.faceunity.com) beauty SDK.

This project adopts the video beauty pre-processing function provided by Faceunity, Uses the audio collection, encoding, transmission, decoding and rendering functions provided by Agora's, and uses the video collection function provided by Agora Module.

Faceunity beauty function please refer to. [Faceunity Document](http://www.faceunity.com/docs_develop_en/#/)

Agora function implementation please refer to [Agora Document](https://docs.agora.io/en/Interactive%20Broadcast/API%20Reference/oc/docs/headers/Agora-Objective-C-API-Overview.html)

Due to the need to use third-party capture when using beauty function, please refer to [Customized Media Source API](https://docs.agora.io/en/Interactive%20Broadcast/raw_data_video_android?platform=Android)  or [Configuring the External Data API](https://docs.agora.io/en/Interactive%20Broadcast/raw_data_video_android?platform=Android)

## 1.Quick Start

This section shows you how to prepare, build, and run the sample application.

### 1.1 Obtain an App ID

To build and run the sample application, get an App ID:

1. Create a developer account at [agora.io](https://dashboard.agora.io/signin/). Once you finish the signup process, you will be redirected to the Dashboard.
2. Navigate in the Dashboard tree on the left to **Projects** > **Project List**.
3. Save the **App ID** from the Dashboard for later use.
4. Generate a temp **Access Token** (valid for 24 hours) from dashboard page with given channel name, save for later use.

5. Open `Agora iOS Tutorial Objective-C.xcodeproj` and edit the `AppID.m` file. Update `<#Your App Id#>` with your app ID, and assign the token variable with the temp Access Token generated from dashboard.

    ```
    NSString *const appID = @"<#Your App ID#>";
    // assign token to nil if you have not enabled app certificate
    NSString *const token = @"<#Temp Token#>";
    ```

### 1.2 Replace FaceUnity license file `authpack.h`
Please contact [FaceUnity](http://www.faceunity.com) to get license file and replace the `authpack.h` file located in BeautifyExample/FaceUnity

### 1.3 Integrate the Agora Video SDK

1. Update CocoaPods by running:

```
pod install
```

2. Connect your iPhone or iPad device and run the project. Ensure a valid provisioning profile is applied or your project will not run.



## 2.How to use the Agora Module capturer function.

## 2.1 Features
- [x] 	Capturer
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


### 2.3 Usage example 

#### 2.3.1 Objective-C

##### How to use Capturer

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

##### Custom Filter

Create a class that implements the `VideoFilterDelegate` protocol `FUManager` , Implement the `processFrame:` method to handle the videoframe .

```objc

#pragma mark - VideoFilterDelegate
/// process your video frame here
- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame {
    if(self.enabled) {
        CVPixelBufferRef buffer = [self renderItemsToPixelBuffer:frame];
        return buffer;
    }
    return frame;
}

```

##### FaceUnity load

decoupling module, we user Abstract API and category to do this，now , you can setupFaceUnity in viewDidLoad.

```objc

#import "UIViewController+FaceUnityUIExtension.h"

/** load Faceu */
[self setupFaceUnity];

```

All Business in Abstract file folder. you do not care for detail, only need add for every module(like makeup)  a provider and a viewModel.  for example:

```objective-c
#import "FUAPIDemoBar.h"
...   
//create viewModel
_makeupViewModel = [FUMakeupViewModel instanceViewModel];
//create provider for dataSource
_makeupViewModel.provider = [FUMakeupNodeModelProvider instanceProducer];

//bind to you view
_makeupView.dataList = _makeupViewModel.provider.dataSource;
_makeupView.viewModel = _makeupViewModel;

```


#### release

check `viewDidLoad dealloc:` 

## FAQ

- Please do not use the raw data interface provided by Agora to integrate beauty features
- Videosource internal is a strong reference, you must set nil when not in use, otherwise it will cause a circular reference
- If you encounter a big head problem, please contact technical support

## Developer Environment Requirements
* XCode 8.0 +
* Real devices (iPhone or iPad)
* iOS simulator is NOT supported

## Connect Us

- You can find full API document at [Document Center](https://docs.agora.io/en/)
- You can file bugs about this demo at [issue](https://github.com/AgoraIO/Agora-iOS-Tutorial-Swift-1to1/issues)

## License

The MIT License (MIT).


