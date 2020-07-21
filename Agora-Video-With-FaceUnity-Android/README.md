# Agora Video With Faceunity

This doc helps you quickly get started with an [Agora](www.agora.io) real-time video call with [FaceUnity](http://www.faceunity.com) effects available.

Refer to [Faceunity API reference](http://www.faceunity.com/docs_develop) for the use of FaceUnity SDK.

Refer to [Agora API reference](https://docs.agora.io/en/Interactive%20Broadcast/API%20Reference/java/index.html) for how to implement Agora interacting video calls.

Since the use of third-party video processing librarie needs custom capture and rendering, please refer to [Custom Video Source and Renderer](https://docs.agora.io/en/Interactive%20Broadcast/custom_video_android?platform=Android)


## Quick Start
This section shows you how to configure and run the project.

### Obtain an Agora App ID and FaceUnity licence
1. You must obtain an Agora app ID. When creating an Agora rtc engine, the engine needs the app id to identify your application.
Locate the project file **app/src/main/res/values/strings.xml** and replace <#YOUR APP ID#> with the app id.

```xml
<string name="agora_app_id"><#YOUR APP ID#></string>
```
2. Contact sales@agora.io and get a licence file `authpack.java`, then copy this file to project folder `faceunity/src/main/java/com/faceunity`. Note this licence determines which FaceUnity functions/effects you are allowed to use. 

### Configure Agora SDK

Now the demo automatically imports Agora Video SDK from JCenter. By default you do not need to download Agora Video SDK anywhere else.

### Download FaceUnity SDK

1. Currently this demo supports FaceUnity SDK v6.6, you can download FaceUnity SDK from [Official Release v6.6](https://github.com/Faceunity/FULiveDemoDroid/releases/download/v6.6/Faceunity-Android-v6.6.zip).
2. Unpack the zip file, copy files to `faceunity` module like:
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

FaceUnity SDK v6.6 has much more files than the previous version, remember to copy them all (do not forget sub directories).

### Download FaceUnity Demo Resources

FaceUnity resource effects/bundles are not part of FaceUnity SDK. Usually you need to make your own resources to the application's needs. 

But in order to run this project, you can download the official demo from [here](https://github.com/Faceunity/FULiveDemoDroid)

Once you have the demo source code, unpack the zip file, and then copy all files in folder `app/src/main/assets` to the corresponding folder of this project.

Note only the resource types that your licence allows take effect. 

## Capture and Rendering

This project uses a custom camera capture & rendering library `app/libs/video-capturer.aar`. If you want to know more about source code and how to use, please go to [Here](https://github.com/AgoraIO/Agora-Extensions/tree/master/VideoCapture/Android).

## Contact Us

- If you have questions, take a look at [FAQ](https://docs.agora.io/cn/faq) first
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated, real-world use cases
- More projects maintained by community can be found at [Agora Community](https://github.com/AgoraIO-Community)
- You can find full API documentation at [Document Center](https://docs.agora.io/en/)
- You can ask questions or see others' solutions in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io)
- If you find a bug of this project, please post an issue here [issue](https://github.com/AgoraIO/FaceUnity/issues)

## License

The MIT License (MIT)
