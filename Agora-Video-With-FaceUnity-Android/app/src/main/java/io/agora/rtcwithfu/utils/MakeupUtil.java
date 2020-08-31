package io.agora.rtcwithfu.utils;

import com.faceunity.entity.MakeupItem;
import com.faceunity.param.MakeupParamHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeupUtil {
    private static final String TAG = "MakeupUtil";

    private static final String MAKEUP_RESOURCE_DIR = "makeup" + File.separator;
    private static final String MAKEUP_RESOURCE_JSON_DIR = MAKEUP_RESOURCE_DIR + "config_json" + File.separator;
    private static final String MAKEUP_RESOURCE_COMBINATION_BUNDLE_DIR = MAKEUP_RESOURCE_DIR + "combination_bundle" + File.separator;
    private static final String MAKEUP_RESOURCE_ITEM_BUNDLE_DIR = MAKEUP_RESOURCE_DIR + "item_bundle" + File.separator;

    public static MakeupItem noLipstickMakeupItem() {
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put(MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY_LIP, 0.0);
        return new MakeupItem(
                MakeupItem.FACE_MAKEUP_TYPE_LIPSTICK,
                0,
                MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY_LIP,
                null,
                paramMap);

    }

    public static MakeupItem defaultLipstickMakeupItem() {
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put(MakeupParamHelper.MakeupParam.LIP_TYPE, 0.0);
        paramMap.put(MakeupParamHelper.MakeupParam.IS_TWO_COLOR, 0.0);

        List<double[]> colorList = new ArrayList<>();
        double[] color = { 0.60, 0.16, 0.16, 1.0 };
        colorList.add(color);

        return new MakeupItem(MakeupItem.FACE_MAKEUP_TYPE_LIPSTICK,
                0,
                MakeupParamHelper.MakeupParam.MAKEUP_INTENSITY_LIP,
                MakeupParamHelper.MakeupParam.MAKEUP_LIP_COLOR,
                colorList,
                null, paramMap);
    }
}
