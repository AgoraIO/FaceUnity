package io.agora.sources;

import android.util.Log;

import com.faceunity.FURenderer;

import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.VideoCapturedFrame;

/**
 * Created by lixiaochen on 2020/4/3.
 */

public class EffectHandler implements SinkConnector<CapturedFrame> {

    private static final String TAG = "EffectHandler";

    private static final boolean DEBUG = false;

    private FURenderer mFURenderer;

    public EffectHandler(FURenderer mFURenderer) {
        this.mFURenderer = mFURenderer;
    }

    @Override
    public void onDataAvailable(CapturedFrame data) {
        VideoCapturedFrame videoCapturedFrame = (VideoCapturedFrame) data;
        int fuTextureId = mFURenderer.onDrawFrame(data.rawData, videoCapturedFrame.mTextureId,
                videoCapturedFrame.videoWidth, videoCapturedFrame.videoHeight);
        if (DEBUG) {
            Log.d(TAG, "FU onDataAvailable done " + fuTextureId);
        }
        videoCapturedFrame.mEffectTextureId = fuTextureId;
    }
}
