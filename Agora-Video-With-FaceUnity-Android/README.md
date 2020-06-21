# Agora Video With Faceunity

This doc helps you quickly get started with a real-time video call application with FaceUnity beautification/effects available. And you can:

* Join a video call using Agora video SDK;
* Change roles, switch between front/back cameras, change UI layout;
* Real-time Sticky/Effect/Filter/Beauty for video (provided by Faceunity).


## Prerequisites

* Android Studio 3.1 or above;
* Real Android device (e.g. Nexus 5X). Android Studio simulators may lack functionality;
* Agora video SDK;
* FaceUnity SDK (updated to v6.6).

## Quick Start
This section shows you how to configure and run the project.

### Create an Agora Account and Obtain an App ID
You must obtain an Agora app ID. When creating an Agora rtc engine, the engine needs the app id to identify your application.

1. If you don't have an Agora developer account, create a one at [agora.io](https://dashboard.agora.io/signin/).
2. Once you sign in, you will be redirected to the Dashboard;
3. Navigate **Project Manager** in the left Dashboard tree, you will see the **Create** button. For testing purpose, you can create an app id without a certificate or a token;
4. You will find an app id string, then copy this string to the project setting.

Locate the project file **app/src/main/res/values/strings.xml** and replace <#YOUR APP ID#> with the app id.

```xml
<string name="agora_app_id"><#YOUR APP ID#></string>
```
4. Contact sales@agora.io and get authpack.java for Faceunity SDK, then replace **faceunity/src/main/java/com/faceunity/authpack.java** with your authpack.java

### Prepare FaceUnity SDK
1. Download FaceUnity SDK from [Agora FaceUnity release SDK v6.6.0](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-SDK-Android.zip) or [FaceUnity official release SDK v6.6.0](https://github.com/Faceunity/FULiveDemoDroid/releases/download/v6.6/Faceunity-Android-v6.6.zip)
2. Unpack the zip file, copy jar file, assets, .so files to **faceunity** module. The folder structure is like:
```
faceunity
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

3. Download [FaceUnity Bundles](https://github.com/AgoraIO/FaceUnityLegacy/releases/download/6.6.0/FaceUnity-6.6.0-bundle-Android.zip) or copy from the official SDK zip file downloaded above.

4. Unpack the zip file, put bundle folder under app/src/main/effects


### Prepare agora SDK
1. Download agora SDK from the official download page: https://www.agora.io/en/download/.
2. Unpack the zip file, copy all .so libraries to their corresponding abi folders (app\src\main\jniLibs). The folder structure of jniLibs should be like this:

````
jniLibs
  |__arm64-v8a
     |__ libagora-rtc-sdk-jni.so

  |__ armeabi-v7a
     |__ libagora-rtc-sdk-jni.so

  |__x86
     |__ libagora-rtc-sdk-jni.so

  |__x86_64
       |__ libagora-rtc-sdk-jni.so

````
4. Copy agora-rtc-sdk.jar into "app/libs" folder.

## Contact Us

- For potential issues, take a look at our [FAQ](https://docs.agora.io/cn/faq) first
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated real use case
- Repositories managed by developer communities can be found at [Agora Community](https://github.com/AgoraIO-Community)
- You can find full API documentation at [Document Center](https://docs.agora.io/en/)
- If you encounter problems during integration, you can ask question in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io)
- You can file bugs about this sample at [issue](https://github.com/AgoraIO/FaceUnity/issues)

## License

The MIT License (MIT)
