package io.agora.processor.media.manager;

import android.content.Context;

import io.agora.processor.audio.capture.AudioCapture;
import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.base.BaseAudioCapture;
import io.agora.processor.media.data.AudioCaptureConfigInfo;
import io.agora.processor.media.data.AudioCapturedFrame;
import io.agora.processor.media.data.CapturedFrame;


/**
 * Created by yong on 2019/9/28.
 */

public class AudioManager {

    private static volatile AudioManager mInstance;
    private Context mContext;
    private BaseAudioCapture mAudioCapture;
    private AudioCaptureConfigInfo audioCaptureConfigInfo;

    private AudioManager(Context context) {
        mContext = context;
    }

    public static AudioManager createInstance(Context context) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(context);
                }
            }
        }
        return mInstance;
    }

    public void enableWritePCMToFile(String filePath) {
        if(this.mAudioCapture!=null){
            ((AudioCapture)this.mAudioCapture).enableWriteToFile(filePath);
        }
    }

    public boolean allocate(AudioCaptureConfigInfo audioCaptureConfigInfo) {
        if (this.mAudioCapture == null) {
            this.audioCaptureConfigInfo = audioCaptureConfigInfo;
            this.mAudioCapture = new AudioCapture(audioCaptureConfigInfo);
            this.mAudioCapture.allocate();
            return true;
        }
        return false;
    }

    public boolean start() {
        int startState = this.mAudioCapture.start();
        if (startState == 0) {
            return true;
        }
        return false;
    }

    public void stop() {
        if (mAudioCapture != null) {
            mAudioCapture.stop();
        }
        LogUtil.i("stop:mAudioCapture=" + mAudioCapture);
    }

    public boolean deallocate() {
        int deallocateAudioCaptureFlag = 0;
        if (mAudioCapture != null) {
            deallocateAudioCaptureFlag = mAudioCapture.deallocate();
            mAudioCapture = null;
        }
        LogUtil.i("stop:mAudioCapture" + mAudioCapture);
        if (deallocateAudioCaptureFlag == 0) {
            return true;
        }
        return false;

    }

    public void attachConnectorAudioCapture(SinkConnector audioCapturedData) {
        if (mAudioCapture != null) {
            this.mAudioCapture.getCaptureDataConnector().connect(audioCapturedData);
        } else {
            LogUtil.w("attachConnectorAudioCapture error for mAudioCapture is null");
        }
    }

    public BaseAudioCapture getAudioCapture() {
        return mAudioCapture;
    }

}
