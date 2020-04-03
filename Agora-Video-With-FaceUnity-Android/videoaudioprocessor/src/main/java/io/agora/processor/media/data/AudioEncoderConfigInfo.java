package io.agora.processor.media.data;


import io.agora.processor.common.constant.ConstantMediaConfig;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.CHANNEL_IN_STEREO;

/**
 * Created by yong on 2019/8/31.
 */

public class AudioEncoderConfigInfo {
    /**
     * audio mine type , only support aac
     */
    private String audioMimeType;

    public AudioEncoderConfigInfo() {
        audioMimeType = ConstantMediaConfig.AUDIO_MIME_TYPE;
    }


    public String getAudioMimeType() {
        return audioMimeType;
    }

    public void setAudioMimeType(String audioMimeType) {
        this.audioMimeType = audioMimeType;
    }

}
