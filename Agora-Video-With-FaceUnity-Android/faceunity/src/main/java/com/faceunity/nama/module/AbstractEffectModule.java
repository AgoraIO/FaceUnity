package com.faceunity.nama.module;

import com.faceunity.nama.utils.LogUtils;
import com.faceunity.wrapper.faceunity;

/**
 * 特效模块基类
 *
 * @author Richie on 2020.07.07
 */
public abstract class AbstractEffectModule implements IEffectModule {
    private static final String TAG = "AbstractEffectModule";
    protected int mItemHandle;
    protected int mRotationMode;
    protected RenderEventQueue mRenderEventQueue;

    @Override
    public void setRotationMode(final int rotationMode) {
        mRotationMode = rotationMode;
        if (mRenderEventQueue != null) {
            mRenderEventQueue.add(new Runnable() {
                @Override
                public void run() {
                    faceunity.fuSetDefaultRotationMode(rotationMode);
                    LogUtils.debug(TAG, "fuSetDefaultRotationMode : %d", rotationMode);
                }
            });
        }
    }

    @Override
    public void executeEvent() {
        if (mRenderEventQueue != null) {
            mRenderEventQueue.executeAndClear();
        }
    }

    @Override
    public void destroy() {
        if (mItemHandle > 0) {
            faceunity.fuDestroyItem(mItemHandle);
            LogUtils.debug(TAG, "destroy item %d", mItemHandle);
            mItemHandle = 0;
        }
    }

}
