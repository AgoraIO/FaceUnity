package com.faceunity.fulivedemo.entity;

import com.faceunity.entity.Effect;

import java.util.ArrayList;

import io.agora.rtcwithfu.R;

/**
 * Created by tujh on 2018/1/30.
 */
public enum EffectEnum {
    /**
     * 关闭道具
     */
    EffectNone("none", R.drawable.ic_delete_all, "none", 1, Effect.EFFECT_TYPE_NONE, 0),
    /**
     * 道具贴纸
     */
    Effect_sdlu("sdlu", R.drawable.sdlu, "normal/sdlu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_daisypig("daisypig", R.drawable.daisypig, "normal/daisypig.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_fashi("fashi", R.drawable.fashi, "normal/fashi.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_chri1("chri1", R.drawable.chri1, "normal/chri1.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_xueqiu_lm_fu("xueqiu_lm_fu", R.drawable.xueqiu_lm_fu, "normal/xueqiu_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_wobushi("wobushi", R.drawable.wobushi, "normal/wobushi.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_gaoshiqing("gaoshiqing", R.drawable.gaoshiqing, "normal/gaoshiqing.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    /**
     * AR面具
     */
    Effect_bluebird("bluebird", R.drawable.bluebird, "ar/bluebird.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_lanhudie("lanhudie", R.drawable.lanhudie, "ar/lanhudie.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_fenhudie("fenhudie", R.drawable.fenhudie, "ar/fenhudie.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_tiger_huang("tiger_huang", R.drawable.tiger_huang, "ar/tiger_huang.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_tiger_bai("tiger_bai", R.drawable.tiger_bai, "ar/tiger_bai.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_afd("afd", R.drawable.afd, "ar/afd.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_baozi("baozi", R.drawable.baozi, "ar/baozi.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_tiger("tiger", R.drawable.tiger, "ar/tiger.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    Effect_xiongmao("xiongmao", R.drawable.xiongmao, "ar/xiongmao.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    /**
     * 表情识别
     */
    Effect_future_warrior("future_warrior", R.drawable.future_warrior, "expression/future_warrior.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.future_warrior),
    Effect_jet_mask("jet_mask", R.drawable.jet_mask, "expression/jet_mask.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.jet_mask),
    Effect_sdx2("sdx2", R.drawable.sdx2, "expression/sdx2.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.sdx2),
    Effect_luhantongkuan_ztt_fu("luhantongkuan_ztt_fu", R.drawable.luhantongkuan_ztt_fu, "expression/luhantongkuan_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.luhantongkuan_ztt_fu),
    Effect_qingqing_ztt_fu("qingqing_ztt_fu", R.drawable.qingqing_ztt_fu, "expression/qingqing_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.qingqing_ztt_fu),
    Effect_xiaobianzi_zh_fu("xiaobianzi_zh_fu", R.drawable.xiaobianzi_zh_fu, "expression/xiaobianzi_zh_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.xiaobianzi_zh_fu),
    Effect_xiaoxueshen_ztt_fu("xiaoxueshen_ztt_fu", R.drawable.xiaoxueshen_ztt_fu, "expression/xiaoxueshen_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_EXPRESSION, R.string.xiaoxueshen_ztt_fu),
    /**
     * 背景分割
     */
    Effect_hez_ztt_fu("hez_ztt_fu", R.drawable.hez_ztt_fu, "background/hez_ztt_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, R.string.hez_ztt_fu),
    Effect_gufeng_zh_fu("gufeng_zh_fu", R.drawable.gufeng_zh_fu, "background/gufeng_zh_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    Effect_xiandai_ztt_fu("xiandai_ztt_fu", R.drawable.xiandai_ztt_fu, "background/xiandai_ztt_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    Effect_sea_lm_fu("sea_lm_fu", R.drawable.sea_lm_fu, "background/sea_lm_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    Effect_ice_lm_fu("ice_lm_fu", R.drawable.ice_lm_fu, "background/ice_lm_fu.bundle", 1, Effect.EFFECT_TYPE_BACKGROUND, 0),
    /**
     * 手势识别
     */
    Effect_ctrl_rain("ctrl_rain", R.drawable.ctrl_rain, "gesture/ctrl_rain.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.push_hand),
    Effect_ctrl_snow("ctrl_snow", R.drawable.ctrl_snow, "gesture/ctrl_snow.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.push_hand),
    Effect_ctrl_flower("ctrl_flower", R.drawable.ctrl_flower, "gesture/ctrl_flower.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.push_hand),
    Effect_fu_lm_koreaheart("fu_lm_koreaheart", R.drawable.fu_lm_koreaheart, "gesture/ssd_thread_korheart.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.fu_lm_koreaheart),
    Effect_ssd_thread_six("ssd_thread_six", R.drawable.ssd_thread_six, "gesture/ssd_thread_six.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ssd_thread_six),
    Effect_ssd_thread_cute("ssd_thread_cute", R.drawable.ssd_thread_cute, "gesture/ssd_thread_cute.bundle", 4, Effect.EFFECT_TYPE_GESTURE, R.string.ssd_thread_cute),
    /**
     * Animoji
     */
    Effect_frog_Animoji("frog_Animoji", R.drawable.frog_st_animoji, "animoji/frog_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_huangya_Animoji("huangya_Animoji", R.drawable.huangya_animoji, "animoji/huangya_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_hetun_Animoji("hetun_Animoji", R.drawable.hetun_animoji, "animoji/hetun_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_douniuquan_Animoji("douniuquan_Animoji", R.drawable.douniuquan_animoji, "animoji/douniuquan_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_hashiqi_Animoji("hashiqi_Animoji", R.drawable.hashiqi_animoji, "animoji/hashiqi_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_baimao_Animoji("baimao_Animoji", R.drawable.baimao_animoji, "animoji/baimao_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    Effect_kuloutou_Animoji("kuloutou_Animoji", R.drawable.kuloutou_animoji, "animoji/kuloutou_Animoji.bundle", 4, Effect.EFFECT_TYPE_ANIMOJI, 0),
    /**
     * 人像驱动
     */
    Effect_picasso_e1("picasso_e1", R.drawable.picasso_e1, "portrait_drive/picasso_e1.bundle", 1, Effect.EFFECT_TYPE_PORTRAIT_DRIVE, 0),
    Effect_picasso_e2("picasso_e2", R.drawable.picasso_e2, "portrait_drive/picasso_e2.bundle", 1, Effect.EFFECT_TYPE_PORTRAIT_DRIVE, 0),
    Effect_picasso_e3("picasso_e3", R.drawable.picasso_e3, "portrait_drive/picasso_e3.bundle", 1, Effect.EFFECT_TYPE_PORTRAIT_DRIVE, 0),
    /**
     * 哈哈镜
     */
    Effect_facewarp2("facewarp2", R.drawable.facewarp2, "facewarp/facewarp2.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp3("facewarp3", R.drawable.facewarp3, "facewarp/facewarp3.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp4("facewarp4", R.drawable.facewarp4, "facewarp/facewarp4.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp5("facewarp5", R.drawable.facewarp5, "facewarp/facewarp5.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    Effect_facewarp6("facewarp6", R.drawable.facewarp6, "facewarp/facewarp6.bundle", 4, Effect.EFFECT_TYPE_FACE_WARP, 0),
    /**
     * 音乐滤镜
     */
    Effect_douyin_old("douyin_01", R.drawable.douyin_old, "musicfilter/douyin_01.bundle", 4, Effect.EFFECT_TYPE_MUSIC_FILTER, 0),
    Effect_douyin("douyin_02", R.drawable.douyin, "musicfilter/douyin_02.bundle", 4, Effect.EFFECT_TYPE_MUSIC_FILTER, 0);

    private String bundleName;
    private int resId;
    private String path;
    private int maxFace;
    private int effectType;
    private int description;

    EffectEnum(String name, int resId, String path, int maxFace, int effectType, int description) {
        this.bundleName = name;
        this.resId = resId;
        this.path = path;
        this.maxFace = maxFace;
        this.effectType = effectType;
        this.description = description;
    }

    public String bundleName() {
        return bundleName;
    }

    public int resId() {
        return resId;
    }

    public String path() {
        return path;
    }

    public int maxFace() {
        return maxFace;
    }

    public int effectType() {
        return effectType;
    }

    public int description() {
        return description;
    }

    public Effect effect() {
        return new Effect(bundleName, resId, path, maxFace, effectType, description);
    }

    public static ArrayList<Effect> getEffectsByEffectType(int effectType) {
        ArrayList<Effect> effects = new ArrayList<>(16);
        effects.add(EffectNone.effect());
        for (EffectEnum e : EffectEnum.values()) {
            if (e.effectType == effectType) {
                effects.add(e.effect());
            }
        }
        return effects;
    }
}
