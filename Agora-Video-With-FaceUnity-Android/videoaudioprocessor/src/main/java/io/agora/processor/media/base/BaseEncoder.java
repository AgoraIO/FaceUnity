package io.agora.processor.media.base;

import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.ProcessedData;
import io.agora.processor.media.data.EncodedFrame;
import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.common.connector.SrcConnector;

import java.io.IOException;

/**
 * Created by yong on 2019/8/30.
 */

public abstract class BaseEncoder implements SinkConnector<CapturedFrame> {
    protected SrcConnector<EncodedFrame> mEncoderedDataConnector;

    public BaseEncoder() {
        mEncoderedDataConnector = new SrcConnector<>();
    }

    public SrcConnector<EncodedFrame> getEncoderedDataConnector() {
        return mEncoderedDataConnector;
    }

    /**
     *
     * @param data
     * @return 0  data process ok ,not 0 data process error
     */
    @Override
    public abstract void onDataAvailable(CapturedFrame data);

    /**
     * stop encoder
     */
    public abstract void allocate();

    /**
     * prepare encoder configuration
     *
     * @throws IOException
     */
    public abstract int start() throws IOException;

    /**
     * stop encoder
     */
    public abstract void stop();
    /**
     * stop encoder
     */
    public abstract int deallocate();


    /**
     * get an output buffer from encoder , used by encoder internal
     */
    protected abstract void drain();

    /**
     * notify encoder an new input buffer will come,used by encoder internal
     *
     * @return true success false false
     */
    protected abstract boolean frameAvailableSoon();


    /**
     * only for raw data temporary
     *
     * @param mediaData VideoEncoderConfigInfo for video encoder
     *                  AudioEncoderConfigInfo for audio encoder
     * @return 0 reloadEncoder success <0 reload encoder error
     * @throws IOException
     * @throws Exception
     */
    public abstract boolean reLoadEncoder(Object mediaData) throws IOException, Exception;

}
