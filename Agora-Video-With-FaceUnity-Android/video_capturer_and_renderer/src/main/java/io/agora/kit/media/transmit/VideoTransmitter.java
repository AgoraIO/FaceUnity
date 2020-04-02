package io.agora.kit.media.transmit;

import io.agora.kit.media.capture.VideoCaptureFrame;
import io.agora.kit.media.connector.SinkConnector;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.MediaIO;

public class VideoTransmitter implements SinkConnector<VideoCaptureFrame> {
    private VideoSource mSource;

    public VideoTransmitter(VideoSource source) {
        mSource = source;
    }

    public int onDataAvailable(VideoCaptureFrame data) {
        if (mSource.getConsumer() != null) {
            sendTextureWith2D(mSource.getConsumer(), data);
//            sendByteArrayWithNV21(mSource.getConsumer(), data);
        }
        return 0;
    }

    /**
     * send Texture data with TEXTURE_2D PixelFormat
     *
     * @see VideoSource#getBufferType()
     * @see MediaIO.BufferType#TEXTURE
     */
    private void sendTextureWith2D(IVideoFrameConsumer consumer, VideoCaptureFrame data) {
        boolean needsFixWidthAndHeight = data.mCameraRotation == 90 || data.mCameraRotation == 270;
        consumer.consumeTextureFrame(data.mTextureId,
                MediaIO.PixelFormat.TEXTURE_2D.intValue(),
                needsFixWidthAndHeight ? data.mFormat.getHeight() : data.mFormat.getWidth(),
                needsFixWidthAndHeight ? data.mFormat.getWidth() : data.mFormat.getHeight(),
                data.mSurfaceRotation, data.mTimeStamp, data.mTexMatrix);
    }

    /**
     * send ByteArray data with NV21 PixelFormat
     *
     * @see io.agora.rtc.RtcEngine#setChannelProfile(int)
     * @see io.agora.rtc.Constants#CHANNEL_PROFILE_COMMUNICATION
     * this profile not support TEXTURE, you can use this method to send frame
     * @see VideoSource#getBufferType()
     * @see MediaIO.BufferType#BYTE_ARRAY
     */
    private void sendByteArrayWithNV21(IVideoFrameConsumer consumer, VideoCaptureFrame data) {
        consumer.consumeByteArrayFrame(data.mImage,
                MediaIO.PixelFormat.NV21.intValue(),
                data.mFormat.getWidth(),
                data.mFormat.getHeight(),
                getYUVRotation(data.mCameraRotation, data.mSurfaceRotation), data.mTimeStamp);
    }

    private int getYUVRotation(int cameraRotation, int surfaceRotation) {
        int result;
        // check camera is front or back, maybe not support for some device
        if (cameraRotation == 270) {
            result = (cameraRotation - surfaceRotation + 360) % 360;
        } else { // back-facing
            result = (cameraRotation + surfaceRotation) % 360;
        }
        return result;
    }
}
