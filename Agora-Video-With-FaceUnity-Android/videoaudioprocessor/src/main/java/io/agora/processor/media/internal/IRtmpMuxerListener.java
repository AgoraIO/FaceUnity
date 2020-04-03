package io.agora.processor.media.internal;

/**
 * Created by yong on 2019/9/24.
 */

public interface IRtmpMuxerListener {

    void onConnectingError(int code);

    void onSendVideoError(int code);

    void onSendAudioError(int code);

    void onConnectRtmpSuccess();

    void onRtmpDisconnect();

    void onNeedDecreaseBitrate();

    void onNeedIncreaseBitrate();
}
