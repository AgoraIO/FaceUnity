package io.agora.kit.media.transmit;

import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;

public class VideoSource implements IVideoSource {

    private IVideoFrameConsumer mConsumer;

    @Override
    public boolean onInitialize(IVideoFrameConsumer observer) {
        mConsumer = observer;
        return true;
    }

    @Override
    public int getBufferType() {
        return MediaIO.BufferType.TEXTURE.intValue();
    }

    @Override
    public void onDispose() {
        mConsumer = null;
    }

    @Override
    public void onStop() {
    }

    @Override
    public boolean onStart() {
        return true;
    }

    public IVideoFrameConsumer getConsumer() {
        return mConsumer;
    }

}
