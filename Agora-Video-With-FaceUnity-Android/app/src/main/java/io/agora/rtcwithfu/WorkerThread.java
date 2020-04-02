package io.agora.rtcwithfu;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.IVideoSink;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class WorkerThread extends Thread {
    private final static String TAG = "WorkerThread";

    private final Context mContext;

    private static final int ACTION_WORKER_THREAD_QUIT = 0X1010; // quit this thread

    private static final int ACTION_WORKER_JOIN_CHANNEL = 0X2010;

    private static final int ACTION_WORKER_LEAVE_CHANNEL = 0X2011;

    private static final int ACTION_WORKER_CONFIG_ENGINE = 0X2012;

    private static final class WorkerThreadHandler extends Handler {

        private WorkerThread mWorkerThread;

        WorkerThreadHandler(WorkerThread thread) {
            this.mWorkerThread = thread;
        }

        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mWorkerThread == null) {
                Log.w(TAG, ("handler is already released! " + msg.what));
                return;
            }

            switch (msg.what) {
                case ACTION_WORKER_THREAD_QUIT:
                    mWorkerThread.exit();
                    break;
                case ACTION_WORKER_JOIN_CHANNEL:
                    String[] data = (String[]) msg.obj;
                    mWorkerThread.joinChannel(data[0], msg.arg1);
                    break;
                case ACTION_WORKER_LEAVE_CHANNEL:
                    String channel = (String) msg.obj;
                    mWorkerThread.leaveChannel(channel);
                    break;
                case ACTION_WORKER_CONFIG_ENGINE:
                    Object[] configData = (Object[]) msg.obj;
                    mWorkerThread.configEngine((int) configData[0], (VideoEncoderConfiguration) configData[1]);
                    break;
            }
        }
    }

    private WorkerThreadHandler mWorkerHandler;

    private boolean mReady;

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "wait for " + WorkerThread.class.getSimpleName());
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "start to run");
        Looper.prepare();
        mWorkerHandler = new WorkerThreadHandler(this);
        ensureRtcEngineReadyLock();
        mReady = true;
        Looper.loop();
    }

    private RtcEngine mRtcEngine;

    public final void joinChannel(final String channel, int uid) {
        if (Thread.currentThread() != this) {
            Log.w(TAG, ("joinChannel() - getWorker thread asynchronously " + channel + " " + uid));
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            envelop.obj = new String[]{channel};
            envelop.arg1 = uid;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        ensureRtcEngineReadyLock();
        mRtcEngine.joinChannel(null, channel, "AgoraWithBeauty", uid);
        mEngineConfig.mChannel = channel;

        Log.d(TAG, ("joinChannel " + channel + " " + uid));
    }

    public final void leaveChannel(String channel) {
        if (Thread.currentThread() != this) {
            Log.w(TAG, ("leaveChannel() - getWorker thread asynchronously " + channel));
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            envelop.obj = channel;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }

        int clientRole = mEngineConfig.mClientRole;
        mEngineConfig.reset();
        Log.d(TAG, ("leaveChannel " + channel + " " + clientRole));
    }

    private EngineConfig mEngineConfig;

    public final EngineConfig getEngineConfig() {
        return mEngineConfig;
    }

    private final MyRtcEngineEventHandler mEngineEventHandler;

    public final void configEngine(int cRole, VideoEncoderConfiguration vcConfiguration) {
        if (Thread.currentThread() != this) {
            Log.d(TAG, "configEngine() - getWorker thread asynchronously " + cRole + " " + vcConfiguration);
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_CONFIG_ENGINE;
            envelop.obj = new Object[]{cRole, vcConfiguration};
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        ensureRtcEngineReadyLock();
        mEngineConfig.mClientRole = cRole;

//      mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false); // Earlier than 2.3.0
        mRtcEngine.setVideoEncoderConfiguration(vcConfiguration);
        mRtcEngine.setClientRole(cRole);

        Log.d(TAG, "configEngine " + cRole + " " + vcConfiguration);
    }

    public static String getDeviceID(Context context) {
        // XXX according to the API docs, this value may change after factory reset
        // use Android id as device id
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void setVideoSource(IVideoSource source) {
        mRtcEngine.setVideoSource(source);
    }

    public void setLocalVideoRenderer(IVideoSink renderer){
        mRtcEngine.setLocalVideoRenderer(renderer);
    }

    private RtcEngine ensureRtcEngineReadyLock() {
        if (mRtcEngine == null) {
            String appId = mContext.getString(R.string.agora_app_id);
            if (TextUtils.isEmpty(appId)) {
                throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
            }

            try {
                mRtcEngine = RtcEngine.create(mContext, appId, mEngineEventHandler.mRtcEventHandler);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }

            // set channel profile mode as CHANNEL_PROFILE_LIVE_BROADCASTING or CHANNEL_PROFILE_COMMUNICATION
            mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
//            mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION);
            mRtcEngine.enableVideo();
            mRtcEngine.setLogFile(Environment.getExternalStorageDirectory()
                    + File.separator + mContext.getPackageName() + "/log/agora-rtc.log");
            mRtcEngine.enableDualStreamMode(true);
        }

        return mRtcEngine;
    }

    public MyRtcEngineEventHandler eventHandler() {
        return mEngineEventHandler;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    /**
     * call this method to exit
     * should ONLY call this method when this thread is running
     */
    public final void exit() {
        if (Thread.currentThread() != this) {
            Log.w(TAG, "exit() - exit app thread asynchronously");
            mWorkerHandler.sendEmptyMessage(ACTION_WORKER_THREAD_QUIT);
            return;
        }

        mReady = false;

        // TODO should remove all pending(read) messages
        Log.d(TAG, "exit() > start");

        // exit thread looper
        Looper.myLooper().quit();
        mWorkerHandler.release();

        Log.d(TAG, "exit() > end");
    }

    public WorkerThread(Context context) {
        this.mContext = context;
        this.mEngineConfig = new EngineConfig();
        this.mEngineConfig.mUid = Constants.UID;
        this.mEngineEventHandler = new MyRtcEngineEventHandler(mContext, this.mEngineConfig);
    }
}
