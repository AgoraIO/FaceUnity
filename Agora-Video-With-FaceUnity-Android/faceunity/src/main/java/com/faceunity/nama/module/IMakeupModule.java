package com.faceunity.nama.module;

import com.faceunity.nama.entity.Makeup;

/**
 * 美妆模块接口
 *
 * @author Richie on 2020.07.07
 */
public interface IMakeupModule {
    /**
     * 选择美妆
     *
     * @param makeup
     */
    void selectMakeup(Makeup makeup);

    /**
     * 调节美妆强度
     *
     * @param intensity 范围 [0-1]
     */
    void setMakeupIntensity(float intensity);

    /**
     * 美妆点位镜像
     *
     * @param isMakeupFlipPoints 0 为关闭，1 为开启
     */
    void setIsMakeupFlipPoints(int isMakeupFlipPoints);
}
