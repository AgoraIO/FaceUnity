package io.agora.rtcwithfu;

import android.app.Application;

import com.faceunity.FURenderer;

public class MyApplication extends Application {
    private WorkerThread mWorkerThread;

    @Override
    public void onCreate() {
        super.onCreate();
        FURenderer.initFURenderer(this);
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
