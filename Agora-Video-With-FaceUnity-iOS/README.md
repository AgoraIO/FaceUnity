# Agora Video With Faceunity

*其他语言版本： [简体中文](README.zh.md)*

This open source demo project demonstrates how to implement 1to1 video chat with  [Agora] (www.agora.io) video SDK and the [Faceunity] (http://www.faceunity.com) beauty SDK.

With this sample app you can:

Agora 

- Join / leave channel
- Implement 1to1 video chat 
- Mute / unmute audio

Faceunity

- face tracking, beauty, Animoji, props stickers, AR mask, face transfer , face recognition, music filter, background segmentation
- Switch capture format
- Switch camera


This project adopts the video beauty pre-processing function provided by Faceunity, Uses the audio collection, encoding, transmission, decoding and rendering functions provided by Agora's, and uses the video collection function provided by Agora Module.

Faceunity beauty function please refer to. [Faceunity Document](http://www.faceunity.com/docs_develop_en/#/)

Agora function implementation please refer to [Agora Document](https://docs.agora.io/en/Interactive%20Broadcast/API%20Reference/oc/docs/headers/Agora-Objective-C-API-Overview.html)

Due to the need to use third-party capture when using beauty function, please refer to [Customized Media Source API](https://docs.agora.io/en/Interactive%20Broadcast/raw_data_video_android?platform=Android)  or [Configuring the External Data API](https://docs.agora.io/en/Interactive%20Broadcast/raw_data_video_android?platform=Android)

## Prepare FaceUnity SDK/Resources
1. Download [FaceUnity SDK](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-SDK-iOS.zip)
2. Download [FaceUnity items](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-items-iOS.zip)
3. Place two zip files under AgoraWithFaceunity/Faceunity, and unzip them

## How to use the Agora Module capturer function.

## Features
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



  
## Installation

#### Manually

1. If you are using the capturer module，Go to SDK Downloads, download the AgoraModule_Base and AgoraModule_Capturer module SDK. 
2. Copy the AGMBase.framework、AGMCapturer.framework file in the libs folder to the project folder.
3. In Xcode, go to the TARGETS > Project Name > Build Phases > Link Binary with Libraries menu, and click + to add the following frameworks and libraries. To add the AGMBase.framework、AGMCapturer.framework  file, remember to click Add Other... after clicking +.
4. Link with required frameworks:
     * UIKit.framework
     * Foundation.framework
     * AVFoundation.framework
     * VideoToolbox.framework
     * AudioToolbox.framework
     * libz.framework
     * libstdc++.framework

#### SDK Downloads
[AgoraModule_Base_iOS-1.2.2](https://download.agora.io/components/release/AgoraModule_Base_iOS-1.2.2.zip)
[AgoraModule_Capturer_iOS-1.2.2](https://download.agora.io/components/release/AgoraModule_Capturer_iOS-1.2.2.zip)
                               
                           
#### Add project permissions
Add the following permissions in the info.plist file for device access according to your needs:

| Key      |    Type | Value  |
| :-------- | --------:| :--: |
| Privacy - Microphone Usage Description	  | String |  To access the microphone, such as for a video call.|
| Privacy - Camera Usage Description	     |   String |  To access the camera, such as for a video call.|
        

## Usage example 

#### Objective-C

##### How to use Capturer

```objc
AGMCapturerVideoConfig *videoConfig = [AGMCapturerVideoConfig defaultConfig];
videoConfig.videoSize = CGSizeMake(720, 1280);
videoConfig.sessionPreset = AGMCaptureSessionPreset720x1280;
self.cameraCapturer = [[AGMCameraCapturer alloc] initWithConfig:videoConfig];
[self.cameraCapturer start];
```

##### Adapter Filter

 ```objc
 self.videoAdapterFilter = [[AGMVideoAdapterFilter alloc] init];
 self.videoAdapterFilter.ignoreAspectRatio = YES;
 self.videoAdapterFilter.isMirror = YES;
 #define DEGREES_TO_RADIANS(x) (x * M_PI/180.0)
 CGAffineTransform rotation = CGAffineTransformMakeRotation( DEGREES_TO_RADIANS(90));
 self.videoAdapterFilter.affineTransform = rotation;
 ```

##### Associate the modules

```objc

[self.cameraCapturer addVideoSink:self.videoAdapterFilter];
[self.videoAdapterFilter addVideoSink:senceTimeFilter];

```

##### Custom Filter

Create a class that inherits from AGMVideoSource and implements the AGMVideoSink protocol, Implement the onFrame: method to handle the videoframe .

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

## Running the App
First, create a developer account at [Agora.io](https://dashboard.agora.io/signin/), and obtain an App ID. Update "KeyCenter.m" with your App ID. 

```
+ (NSString *)AppId {
     return @"Your App ID";
}
```
Next, download the **Agora Video SDK** from [Agora.io SDK](https://www.agora.io/en/download/). Unzip the downloaded SDK package and copy the "libs/AgoraRtcEngineKit.framework" to the "AgoraWithFaceunity" folder.

Contact [Faceunity](http://www.faceunity.com)  to get the certificate file replace the "authpack.h" file in the "/AgoraWithFaceunity/Faceunity" folder of this project.

Finally, Open AgoraWithFaceunity.xcodeproj, connect your iPhone／iPad device, setup your development signing and run.

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


