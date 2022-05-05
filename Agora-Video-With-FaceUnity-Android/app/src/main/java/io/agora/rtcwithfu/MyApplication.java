package io.agora.rtcwithfu;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.nama.FURenderer;

import io.agora.framework.PreprocessorFaceUnity;
import io.agora.rtc2.RtcEngine;


public class MyApplication extends Application {
    private RtcEngine mRtcEngine;
    private RtcEngineEventHandlerProxy mRtcEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        initRtcEngine();
        initVideoCaptureAsync();
    }

    private void initRtcEngine() {
        String appId = getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
        }

        mRtcEventHandler = new RtcEngineEventHandlerProxy();
        try {
            mRtcEngine = RtcEngine.create(this, appId, mRtcEventHandler);
            mRtcEngine.registerVideoFrameObserver(PreprocessorFaceUnity.getInstance());
            mRtcEngine.enableVideo();
            mRtcEngine.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void initVideoCaptureAsync() {
        new Thread(() -> {
            Context application = getApplicationContext();
            FURenderer.getInstance().setup(application);
        }).start();
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public void addRtcHandler(RtcEngineEventHandler handler) {
        mRtcEventHandler.addEventHandler(handler);
    }

    public void removeRtcHandler(RtcEngineEventHandler handler) {
        mRtcEventHandler.removeEventHandler(handler);
    }

    public RtcEngineEventHandlerProxy getRtcEventHandler() {
        return mRtcEventHandler;
    }

}
