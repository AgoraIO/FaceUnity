package com.faceunity.nama.ui.enums;

import com.faceunity.nama.R;
import com.faceunity.nama.entity.Makeup;

import java.util.ArrayList;

/**
 * 美妆列表
 *
 * @author Richie on 2020.06.20
 */
public enum MakeupEnum {
    /**
     * 减龄，暖冬，红枫，Rose，少女
     */
    MAKEUP_NONE(R.drawable.makeup_none_normal, "卸妆", "", false),
    MAKEUP_NAICHA(R.drawable.demo_combination_tea_with_milk, "奶茶", "makeup/naicha.bundle", false),
    MAKEUP_DOUSHA(R.drawable.demo_combination_red_bean_paste, "豆沙", "makeup/dousha.bundle", false),
    MAKEUP_CHAOA(R.drawable.demo_combination_super_a, "超A", "makeup/chaoa.bundle", false);

    private int iconId;
    private String name;
    private String filePath;
    private boolean isNeedFlipPoints;

    MakeupEnum(int iconId, String name, String filePath, boolean isNeedFlipPoints) {
        this.iconId = iconId;
        this.name = name;
        this.filePath = filePath;
        this.isNeedFlipPoints = isNeedFlipPoints;
    }

    public Makeup create() {
        return new Makeup(iconId, name, filePath, isNeedFlipPoints);
    }

    public static ArrayList<Makeup> getMakeupEntities() {
        MakeupEnum[] values = MakeupEnum.values();
        ArrayList<Makeup> makeupEntities = new ArrayList<>(values.length);
        for (MakeupEnum value : values) {
            makeupEntities.add(value.create());
        }
        return makeupEntities;
    }

}
