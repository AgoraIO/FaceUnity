package io.agora.rtcwithfu;

import android.app.Application;
import android.content.Context;

import com.faceunity.FURenderer;

import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.framework.PreprocessorFaceUnity;

public class MyApplication extends Application {
    private WorkerThread mWorkerThread;
    private CameraVideoManager mVideoManager;

    @Override
    public void onCreate() {
        super.onCreate();
        initVideoCaptureAsync();
    }

    private void initVideoCaptureAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context application = getApplicationContext();
                FURenderer.initFURenderer(application);
                mVideoManager = new CameraVideoManager(application,
                        new PreprocessorFaceUnity(application));
            }
        }).start();
    }

    public CameraVideoManager videoManager() {
        return mVideoManager;
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();
            mWorkerThread.waitForReady();
        }
    }

    public synchronized void deInitWorkerThread() {
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;
    }
}
