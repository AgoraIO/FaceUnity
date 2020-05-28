package io.agora.rtcwithfu.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.rtcwithfu.MyApplication;
import io.agora.rtcwithfu.EngineConfig;
import io.agora.rtcwithfu.MyRtcEngineEventHandler;
import io.agora.rtcwithfu.WorkerThread;
import io.agora.rtc.RtcEngine;

/**
 * Base activity enabling sub activities to communicate using
 * remote video calls.
 */
public abstract class RTCBaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).initWorkerThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deInitUIAndEvent();
    }

    protected abstract void initUIAndEvent();

    protected abstract void deInitUIAndEvent();

    protected MyApplication application() {
        return (MyApplication) getApplication();
    }

    protected final WorkerThread worker() {
        return application().getWorkerThread();
    }

    protected RtcEngine rtcEngine() {
        return worker().getRtcEngine();
    }

    protected final EngineConfig config() {
        return worker().getEngineConfig();
    }

    protected final MyRtcEngineEventHandler eventHandler() {
        return worker().eventHandler();
    }

    protected final CameraVideoManager videoManager() {
        return application().videoManager();
    }
}
