package io.agora.kit.media.transmit;

import io.agora.rtc.mediaio.MediaIO;
import io.agora.kit.media.capture.VideoCaptureFrame;
import io.agora.kit.media.connector.SinkConnector;

public class VideoTransmitter implements SinkConnector<VideoCaptureFrame> {
    private VideoSource mSource;

    public VideoTransmitter(VideoSource source) {
        mSource = source;
    }

    public int onDataAvailable(VideoCaptureFrame data) {
        if (mSource.getConsumer() != null) {
            boolean needsFixWidthAndHeight = data.mCameraRotation == 90 || data.mCameraRotation == 270;
            mSource.getConsumer().consumeTextureFrame(data.mTextureId,
                    MediaIO.PixelFormat.TEXTURE_2D.intValue(),
                    needsFixWidthAndHeight ? data.mFormat.getHeight() : data.mFormat.getWidth(),
                    needsFixWidthAndHeight ? data.mFormat.getWidth() : data.mFormat.getHeight(),
                    data.mSurfaceRotation, data.mTimeStamp, data.mTexMatrix);
        }
        return 0;
    }
}
