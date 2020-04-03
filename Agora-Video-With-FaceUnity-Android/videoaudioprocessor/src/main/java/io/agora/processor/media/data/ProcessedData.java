package io.agora.processor.media.data;

import java.nio.ByteBuffer;

/**
 * Created by yong on 2019/9/22.
 */

public class ProcessedData {
    public int mLength;
    public io.agora.processor.media.data.MediaFrameFormat.FrameType frameType;
    public ByteBuffer mBuffer;
    public long mTimeStamp;


    //data for encoder
    public ProcessedData(ByteBuffer buffer, int length, long timeStamp, io.agora.processor.media.data.MediaFrameFormat.FrameType frameType) {
        this.mBuffer = buffer;
        this.mLength = length;
        this.mTimeStamp = timeStamp;
        this.frameType = frameType;
    }
}
