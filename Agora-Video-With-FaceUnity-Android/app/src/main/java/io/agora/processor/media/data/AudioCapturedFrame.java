package io.agora.processor.media.data;

/**
 * Created by yong on 2019/9/22.
 */

public class AudioCapturedFrame extends CapturedFrame {

    //for audio raw data
    public AudioCapturedFrame(byte[] rawData, int length, MediaFrameFormat.FrameType frameType) {
        this.rawData = rawData;
        this.mLength = length;
        this.frameType = frameType;
    }
}
