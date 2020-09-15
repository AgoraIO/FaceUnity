package com.faceunity.nama.entity;

import com.faceunity.nama.R;

import java.util.ArrayList;

/**
 * 美颜滤镜列表
 *
 * @author Richie on 2019.12.20
 */
public enum FilterEnum {
    /**
     * 滤镜
     */
    origin("origin", R.drawable.demo_icon_cancel, "原图"),
    nature1("ziran1", R.drawable.demo_icon_natural_1, "自然 1"),
    zhiganhui1("zhiganhui1", R.drawable.demo_icon_texture_gray1, "质感灰 1"),
    bailiang1("bailiang1", R.drawable.demo_icon_bailiang1, "白亮 1"),
    fennen1("fennen1", R.drawable.demo_icon_fennen1, "粉嫩 1"),
    lengsediao1("lengsediao1", R.drawable.demo_icon_lengsediao1, "冷色调 1");

    private String name;
    private int iconId;
    private String description;

    FilterEnum(String name, int iconId, String description) {
        this.name = name;
        this.iconId = iconId;
        this.description = description;
    }

    public Filter create() {
        return new Filter(name, iconId, description);
    }

    public static ArrayList<Filter> getFilters() {
        FilterEnum[] filterEnums = FilterEnum.values();
        ArrayList<Filter> filters = new ArrayList<>(filterEnums.length);
        for (FilterEnum f : filterEnums) {
            filters.add(f.create());
        }
        return filters;
    }
}
