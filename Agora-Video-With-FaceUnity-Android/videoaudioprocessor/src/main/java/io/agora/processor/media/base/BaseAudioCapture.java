package io.agora.processor.media.base;

import io.agora.processor.common.connector.SrcConnector;
import io.agora.processor.media.data.CapturedFrame;

/**
 * Created by yong on 2019/9/1.
 */

public abstract class BaseAudioCapture {
    protected SrcConnector<CapturedFrame> mCaptureDataConnector;

    public BaseAudioCapture() {
        mCaptureDataConnector = new SrcConnector<>();
    }

    public SrcConnector getCaptureDataConnector() {
        return mCaptureDataConnector;
    }

    /**
     * prepare audio capture
     *
     * @return 0: success ,  <0 error
     */
    public abstract void allocate();

    /**
     * prepare audio capture
     *
     * @return 0: success ,  <0 error
     */
    public abstract int start();

    /**
     * prepare audio capture
     *
     * @return 0: success ,  <0 error
     */
    public abstract void stop();

    /**
     * prepare audio capture
     *
     * @return 0: success ,  <0 error
     */
    public abstract int deallocate();


}
