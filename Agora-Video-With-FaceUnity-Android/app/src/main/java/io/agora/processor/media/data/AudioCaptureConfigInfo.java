package io.agora.processor.media.data;

import io.agora.processor.common.constant.ConstantMediaConfig;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.CHANNEL_IN_STEREO;

/**
 * Created by yong on 2019/9/22.
 */

public class AudioCaptureConfigInfo {

    private int audioSampleRate;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private int audioChannelFormat;    // 声道数 CHANNEL_IN_MONO 1;  CHANNEL_IN_STEREO 2
    private int audioPcmBit;
    //capture info
    private int audioChannelCount;    // 声道数 CHANNEL_IN_MONO 1;  CHANNEL_IN_STEREO 2


    public AudioCaptureConfigInfo() {
        audioSampleRate = ConstantMediaConfig.AUDIO_SAMPLE_RATE;
        audioChannelFormat = ConstantMediaConfig.AUDIO_CHANNEL_FORMAT;
        audioChannelCount = countAudioChannelCount();
        audioPcmBit = ConstantMediaConfig.AUDIO_PCM_BIT;

    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getAudioChannelCount() {
        return audioChannelCount;
    }

    public void setAudioChannelFormat(int audioChannelFormat) {
        this.audioChannelFormat = audioChannelFormat;
        audioChannelCount = countAudioChannelCount();
    }

    public int getAudioPcmBit() {
        return audioPcmBit;
    }

    //TODO rtmp推流器暂不支持bit采样精度的选择
//    public void setAudioPcmBit(int audioPcmBit) {
//        this.audioPcmBit = audioPcmBit;
//    }

    public int getAudioChannelFormat() {
        return audioChannelFormat;
    }

    private int countAudioChannelCount() {
        switch (audioChannelFormat) {
            case CHANNEL_IN_MONO:
                return 1;
            case CHANNEL_IN_STEREO:
                return 2;
            default:
                return 1;
        }
    }

    @Override
    public String toString() {
        return "AudioCaptureConfigInfo{" +
                "audioSampleRate=" + audioSampleRate +
                ", audioChannelFormat=" + audioChannelFormat +
                ", audioPcmBit=" + audioPcmBit +
                ", audioChannelCount=" + audioChannelCount +
                '}';
    }
}
