package io.agora.rtcwithfu;

/**
 * FURenderer 与界面之间的交互接口
 */
public interface OnFUControlListener {
    /**
     * 美颜效果全局开关
     *
     * @param isOn
     */
    void setBeautificationOn(boolean isOn);


    /**
     * 选择道具贴纸
     *
     * @param effect
     */
    void onEffectSelected(Effect effect);


    /**
     * 设置美妆妆容参数
     *
     * @param isOn
     */
    void setMakeupItemParam(boolean isOn);

    /**
     * 设置美体参数
     *
     * @param isOn
     */
    void setBodySlim(boolean isOn);
}
