package com.faceunity.nama.module;

import android.content.Context;

/**
 * 特效模块
 *
 * @author Richie on 2020.07.07
 */
public interface IEffectModule {
    /**
     * 创建
     *
     * @param context
     * @param moduleCallback
     */
    void create(Context context, ModuleCallback moduleCallback);

    /**
     * 设置 rotationMode
     *
     * @param rotationMode
     */
    void setRotationMode(int rotationMode);

    /**
     * 执行任务
     */
    void executeEvent();

    /**
     * 销毁
     */
    void destroy();


    interface ModuleCallback {
        /**
         * bundle 创建完成
         *
         * @param itemHandle
         */
        void onCreateFinish(int itemHandle);
    }

}
