package io.agora.processor.media.data;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;


/**
 * Created by yong on 2019/8/30.
 */

public class EncodedFrame {

    private MediaFrameFormat.FrameType frameType;
    private ByteBuffer encodedByteBuffer;
    private MediaCodec.BufferInfo mBufferInfo;
    private VideoBufferWithMetaData videoBufferWithMetaData;
    private MediaFormat mediaFormat;

    public EncodedFrame(MediaFrameFormat.FrameType frameType, ByteBuffer encodedByteBuffer, MediaCodec.BufferInfo mBufferInfo, MediaFormat mediaFormat, VideoBufferWithMetaData videoBufferWithMetaData) {
        this.encodedByteBuffer = encodedByteBuffer;
        this.frameType = frameType;
        this.mBufferInfo = mBufferInfo;
        this.videoBufferWithMetaData = videoBufferWithMetaData;
        this.mediaFormat = mediaFormat;
    }

    public ByteBuffer getEncodedByteBuffer() {
        return encodedByteBuffer;
    }

    public void setEncodedByteBuffer(ByteBuffer encodedByteBuffer) {
        this.encodedByteBuffer = encodedByteBuffer;
    }

    public MediaCodec.BufferInfo getmBufferInfo() {
        return mBufferInfo;
    }

    public void setmBufferInfo(MediaCodec.BufferInfo mBufferInfo) {
        this.mBufferInfo = mBufferInfo;
    }

    public MediaFrameFormat.FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(MediaFrameFormat.FrameType frameType) {
        this.frameType = frameType;
    }

    public VideoBufferWithMetaData getVideoBufferWithMetaData() {
        return videoBufferWithMetaData;
    }

    public void setVideoBufferWithMetaData(VideoBufferWithMetaData videoBufferWithMetaData) {
        this.videoBufferWithMetaData = videoBufferWithMetaData;
    }

    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    @Override
    public String toString() {
        return "EncodedFrame{" +
                ", frameType=" + frameType +
                ", encodedByteBuffer=" + encodedByteBuffer +
                ", presentationTimeUs=" + mBufferInfo.presentationTimeUs +
                ", size=" + mBufferInfo.size +
                ", mediaFormat=" + mediaFormat +
                '}';
    }
}
