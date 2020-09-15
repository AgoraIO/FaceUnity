package com.faceunity.nama.param;

/**
 * 美体道具参数
 *
 * @author Richie on 2019.11.25
 */
public final class BodySlimParam {
    /**
     * 瘦身，0.0~1.0，0.0表示强度为 0，值越大，强度越大，瘦身效果越明显，默认为 0.0
     */
    public static final String BODY_SLIM_STRENGTH = "BodySlimStrength";
    /**
     * 长腿，0.0~1.0，0.0表示强度为 0，值越大，腿越长，默认为 0.0
     */
    public static final String LEG_SLIM_STRENGTH = "LegSlimStrength";
    /**
     * 瘦腰，0.0~1.0，0.0 表示强度为 0，值越大，腰越细，默认为 0.0
     */
    public static final String WAIST_SLIM_STRENGTH = "WaistSlimStrength";
    /**
     * 美肩，0.0~1.0，0.5 表示强度为 0，0.5 到 1.0，值越大，肩膀越宽；0.5到 0.0，值越小，肩膀越窄，0.5为默认值
     */
    public static final String SHOULDER_SLIM_STRENGTH = "ShoulderSlimStrength";
    /**
     * 美臀，0.0~1.0，0.0表示强度为 0，值越大，强度越大， 提臀效果越明显，默认为 0.0
     */
    public static final String HIP_SLIM_STRENGTH = "HipSlimStrength";
    /**
     * 小头，0.0~1.0  0.0表示强度为 0，值越大，小头效果越明显，默认为 0.0
     */
    public static final String HEAD_SLIM = "HeadSlim";
    /**
     * 瘦腿，0.0~1.0  0.0表示强度为 0，值越大，瘦腿效果越明显，默认为 0.0
     */
    public static final String LEG_SLIM = "LegSlim";
    /**
     * 清除所有美体效果，恢复为默认值
     */
    public static final String CLEAR_SLIM = "ClearSlim";
    /**
     * 设置相机方向 0, 1, 2, 3。和 rotationMode 一致
     */
    public static final String ORIENTATION = "Orientation";
    /**
     * 参数 0.0 或者 1.0,  0.0 为关闭点位绘制，1.0 为打开，默认关闭
     */
    public static final String DEBUG = "Debug";
}
