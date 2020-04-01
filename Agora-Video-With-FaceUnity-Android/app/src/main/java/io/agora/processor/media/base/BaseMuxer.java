package io.agora.processor.media.base;

import java.io.IOException;

import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.media.data.EncodedFrame;

/**
 * Created by yong on 2019/8/30.
 */

public abstract class BaseMuxer implements SinkConnector<EncodedFrame> {
    protected boolean enableAudio;
    protected boolean enableVideo;

    protected BaseMuxer(boolean enableAudio, boolean enableVideo) {
        this.enableAudio = enableAudio;
        this.enableVideo = enableVideo;
    }

    /**
     *
     * @param data
     * @return 0  data process ok ,not 0 data process error
     */
    @Override
    public abstract void onDataAvailable(EncodedFrame data);

    /**
     * check muxer is stared
     *
     * @return
     */
    public abstract boolean isMuxerStarted();

    /**
     * allocate a muxer
     *
     * @return
     */
    public abstract void allocate() throws IOException;
    /**
     * start a muxer
     *
     * @return
     */
    public abstract int start();

    /**
     * stop a muxer and release all muxer reasouce
     */
    public abstract void stop();

    /**
     * allocate a muxer
     *
     * @return
     */
    public abstract int deallocate();

    /**
     * write an encoded data through muxer
     *
     * @param encodedFrame
     * @throws Exception
     */
    protected abstract void writeEncodedData(EncodedFrame encodedFrame) throws Exception;
}
