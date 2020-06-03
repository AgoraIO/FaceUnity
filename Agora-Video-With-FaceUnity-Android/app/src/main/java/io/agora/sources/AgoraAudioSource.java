package io.agora.sources;

import android.util.Log;

import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.media.data.AudioCapturedFrame;
import io.agora.rtc.RtcEngine;

/**
 * Created by yong on 2019/10/6.
 */

public class AgoraAudioSource implements SinkConnector<AudioCapturedFrame> {

    private static final String TAG = "AgoraAudioSource";

    private static final boolean DEBUG = false;

    private RtcEngine mRtcEngine;
    private boolean enablePushDataToAgora = false;

    public AgoraAudioSource(RtcEngine mRtcEngine) {
        this.mRtcEngine = mRtcEngine;
        this.enablePushDataToAgora = true;
    }

    @Override
    public void onDataAvailable(AudioCapturedFrame data) {
        if (!this.enablePushDataToAgora) {
            return;
        }
        if (this.mRtcEngine != null) {
            int audioResult = this.mRtcEngine.pushExternalAudioFrame(data.rawData, System.currentTimeMillis());
            if (DEBUG) {
                Log.d(TAG, "onDataAvailable: pushExternalAudioFrame = " + audioResult);
            }
        }
    }

    public void enablePushDataForAgora(boolean enabled) {
        this.enablePushDataToAgora = enabled;
    }
}
