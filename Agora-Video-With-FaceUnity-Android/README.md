# Agora Video With Faceunity

This tutorial enables you to quickly get started in your development efforts to create an Android app with real-time video calls, voice calls, and interactive broadcasting. With this sample app you can:

* Join and leave a channel.
* Choose between the front or rear camera.
* Real time Sticky/Effect/Filter for video(provided by Faceunity SDK)


## Prerequisites

* Android Studio 3.1 or above.
* Android device (e.g. Nexus 5X). A real device is recommended because some simulators have missing functionality or lack the performance necessary to run the sample.

## Quick Start
This section shows you how to prepare, build, and run the sample application.

### Create an Account and Obtain an App ID
In order to build and run the sample application you must obtain an App ID:

1. Create a developer account at [agora.io](https://dashboard.agora.io/signin/). Once you finish the signup process, you will be redirected to the Dashboard.
2. Navigate in the Dashboard tree on the left to **Projects** > **Project List**.
3. Locate the file **app/src/main/res/values/strings.xml** and replace <#YOUR APP ID#> with the App ID in the dashboard.

```xml
<string name="agora_app_id"><#YOUR APP ID#></string>
```
4. Contact sales@agora.io and get authpack.java for Faceunity SDK, then replace **faceunity/src/main/java/com/faceunity/authpack.java** with your authpack.java

### Prepare FaceUnity SDK
1. Download [FaceUnity SDK 6.6.0](https://github.com/AgoraIO/FaceUnity/releases/download/6.6.0/FaceUnity-6.6.0-SDK-Android.zip)
2. Unpack the zip file, put libs, assets, jniLibs folder following below rules
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
3. Download [FaceUnity Bundles](https://github.com/AgoraIO/FaceUnity/releases/download/6.6.0/FaceUnity-6.6.0-bundle-Android.zip)
4. Unpack the zip file, put bundle folder under app/src/main/effects


### Prepare agora SDK
1. Download agora SDK from the official download page: https://www.agora.io/en/download/.
2. Unpack the zip file, copy all .so libraries to their corresponding abi folders (app\src\main\jniLibs). The folder structure of jniLibs should be like this:

````
jniLibs
  |__arm64-v8a
     |__ libagora-crypto.so
     |__ libagora-rtc-sdk-jni.so

  |__ armeabi-v7a
     |__ libagora-crypto.so
     |__ libagora-rtc-sdk-jni.so

  |__x86
     |__ libagora-crypto.so
     |__ libagora-rtc-sdk-jni.so

````
3. If you need to implement raw data interfaces, please copy the header files under "include" folder in the zip file into the CPP source folder.
4. Besides, copy agora-rtc-sdk.jar into "app/libs" folder.

## Contract Us

- For potential issues, take a look at our [FAQ](https://docs.agora.io/cn/faq) first
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated real use case
- Repositories managed by developer communities can be found at [Agora Community](https://github.com/AgoraIO-Community)
- You can find full API documentation at [Document Center](https://docs.agora.io/en/)
- If you encounter problems during integration, you can ask question in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io)
- You can file bugs about this sample at [issue](https://github.com/AgoraIO/FaceUnity/issues)

## License

The MIT License (MIT)