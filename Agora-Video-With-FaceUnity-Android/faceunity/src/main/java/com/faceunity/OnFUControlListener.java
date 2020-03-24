package com.faceunity;


import com.faceunity.entity.Effect;

/**
 * FURenderer与界面之间的交互接口
 */
public interface OnFUControlListener {

    /**
     * 音乐滤镜时间
     *
     * @param time
     */
    void onMusicFilterTime(long time);

    /**
     * 道具贴纸选择
     *
     * @param effectItemName 道具贴纸文件名
     */
    void onEffectSelected(Effect effectItemName);

    /**
     * 滤镜强度
     *
     * @param progress 滤镜强度
     */
    void onFilterLevelSelected(float progress);

    /**
     * 滤镜选择
     *
     * @param filterName 滤镜名称
     */
    void onFilterNameSelected(String filterName);

    /**
     * 精准磨皮
     *
     * @param isOpen 是否开启精准磨皮（0关闭 1开启）
     */
    void onSkinDetectSelected(float isOpen);

    /**
     * 美肤类型
     *
     * @param isOpen 0:清晰美肤 1:朦胧美肤
     */
    void onHeavyBlurSelected(float isOpen);

    /**
     * 磨皮选择
     *
     * @param level 磨皮level
     */
    void onBlurLevelSelected(float level);

    /**
     * 美白选择
     *
     * @param level 美白
     */
    void onColorLevelSelected(float level);

    /**
     * 红润
     */
    void onRedLevelSelected(float level);

    /**
     * 亮眼
     */
    void onEyeBrightSelected(float level);

    /**
     * 美牙
     */
    void onToothWhitenSelected(float level);

    /**
     * 大眼选择
     *
     * @param level 大眼
     */
    void onEyeEnlargeSelected(float level);

    /**
     * 瘦脸选择
     *
     * @param level 瘦脸
     */
    void onCheekThinningSelected(float level);

    /**
     * 下巴
     */
    void onIntensityChinSelected(float level);

    /**
     * 额头
     */
    void onIntensityForeheadSelected(float level);

    /**
     * 瘦鼻
     */
    void onIntensityNoseSelected(float level);


    /**
     * 嘴形
     */
    void onIntensityMouthSelected(float level);

    /**
     * 窄脸选择
     *
     * @param level
     */
    void onCheekNarrowSelected(float level);

    /**
     * 小脸选择
     *
     * @param level
     */
    void onCheekSmallSelected(float level);

    /**
     * V脸选择
     *
     * @param level
     */
    void onCheekVSelected(float level);
}
