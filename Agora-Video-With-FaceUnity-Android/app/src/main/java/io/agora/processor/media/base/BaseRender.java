package io.agora.processor.media.base;

import android.content.Context;
import android.view.View;

import java.util.concurrent.CountDownLatch;

import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.VideoCapturedFrame;
import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.common.connector.SrcConnector;
import io.agora.processor.common.utils.FPSUtil;
import io.agora.processor.media.internal.IRenderListener;


/**
 * Created by yong on 2019/8/16.
 */

public abstract class BaseRender implements SinkConnector<CapturedFrame> {
    protected VideoCapturedFrame mVideoCaptureFrame;
    protected SrcConnector<Integer> mTexConnector;
    //push data to beauty connect
    protected SrcConnector<CapturedFrame> mBeautyConnector;
    protected IRenderListener mRenderListener;
    //push
    protected SrcConnector<CapturedFrame> mRenderedConnector;
    protected FPSUtil mFPSUtil;
    protected CountDownLatch mDestroyLatch;
    public Context context;

    protected BaseRender() {
        mVideoCaptureFrame = null;
        mDestroyLatch = new CountDownLatch(1);
        mFPSUtil = new FPSUtil();
        mTexConnector = new SrcConnector<>();
        mBeautyConnector = new SrcConnector<>();
        mRenderedConnector = new SrcConnector<>();
    }


    /**
     * set render view
     *
     * @param view a surfaceview or textureview
     * @return true: set success ,false: set error
     */
    public abstract boolean setRenderView(View view);

    /**
     * send an  executable function to render thread
     *
     * @param r an executable
     */
    public abstract void runInRenderThread(Runnable r);

    /**
     * pass render data to render thread
     *
     * @param frame
     * @return 0:no error, <0 send error
     */
    public abstract void onDataAvailable(CapturedFrame frame);

    //destroy all data

    /**
     * release render resource
     */
    public abstract void destroy();


    public abstract boolean isEglContextReady();

    public void setRenderListener(IRenderListener renderListener){
        this.mRenderListener = renderListener;
    }

    public SrcConnector<Integer> getTexConnector() {
        return mTexConnector;
    }

    public SrcConnector<CapturedFrame> getBeautyConnector() {
        return mBeautyConnector;
    }

    public SrcConnector<CapturedFrame> getRrenderedConnector() {
        return mRenderedConnector;
    }

}
