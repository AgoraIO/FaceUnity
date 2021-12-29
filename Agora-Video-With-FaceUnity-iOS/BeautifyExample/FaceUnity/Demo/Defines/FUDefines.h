//
//  FUDefines.h
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import <CoreGraphics/CoreGraphics.h>

#pragma mark - 宏

#define FULocalizedString(key) NSLocalizedStringFromTable(key, @"FaceUnity", nil)


#pragma mark - 枚举

/// 功能模块
typedef NS_ENUM(NSInteger, FUModuleType) {
    FUModuleTypeBeautySkin,             // 美肤
    FUModuleTypeBeautyShape,            // 美型
    FUModuleTypeFilter,                 // 滤镜
    FUModuleTypeSticker,                // 贴纸
    FUModuleTypeMakeup,                 // 美妆
    FUModuleTypeBeautyBody,             // 美体
    FUModuleTypeGame,                   // 游戏
    FUModuleTypePortraitSegmentation    // 人像分割
};

/// 美肤模块子功能
typedef NS_ENUM(NSUInteger, FUBeautySkinItem) {
    FUBeautySkinItemFineSmooth,         // 精细磨皮
    FUBeautySkinItemWhiten,             // 美白
    FUBeautySkinItemRuddy,              // 红润
    FUBeautySkinItemSharpen,            // 锐化
    FUBeautySkinItemEyeBrighten,        // 亮眼
    FUBeautySkinItemToothWhiten,        // 美牙
    FUBeautySkinItemCircles,            // 去黑眼圈
    FUBeautySkinItemWrinkles,           // 去法令纹
    FUBeautySkinItemMax
};

/// 美型模块子功能
typedef NS_ENUM(NSUInteger, FUBeautyShapeItem) {
    FUBeautyShapeItemCheekThinning,         // 瘦脸
    FUBeautyShapeItemCheekV,                // V脸
    FUBeautyShapeItemCheekNarrow,           // 窄脸
    FUBeautyShapeItemCheekShort,            // 短脸
    FUBeautyShapeItemCheekSmall,            // 小脸
    FUBeautyShapeItemCheekBones,            // 瘦颧骨
    FUBeautyShapeItemLowerJaw,              // 瘦下颌骨
    FUBeautyShapeItemEyeEnlarging,          // 大眼
    FUBeautyShapeItemEyeCircle,             // 圆眼
    FUBeautyShapeItemChin,                  // 下巴
    FUBeautyShapeItemForehead,              // 额头
    FUBeautyShapeItemNose,                  // 瘦鼻
    FUBeautyShapeItemMouth,                 // 嘴型
    FUBeautyShapeItemCanthus,               // 开眼角
    FUBeautyShapeItemEyeSpace,              // 眼距
    FUBeautyShapeItemEyeRotate,             // 眼睛角度
    FUBeautyShapeItemLongNose,              // 长鼻
    FUBeautyShapeItemPhiltrum,              // 缩人中
    FUBeautyShapeItemSmile,                 // 微笑嘴角
    FUBeautyShapeItemMax
};

/// 美体模块子功能
typedef NS_ENUM(NSUInteger, FUBeautyBodyItem) {
    FUBeautyBodyItemSlim,                   // 瘦身
    FUBeautyBodyItemLongLeg,                // 长腿
    FUBeautyBodyItemThinWaist,              // 细腰
    FUBeautyBodyItemBeautyShoulder,         // 美肩
    FUBeautyBodyItemBeautyButtock,          // 美臀
    FUBeautyBodyItemSmallHead,              // 小头
    FUBeautyBodyItemThinLeg,                // 瘦腿
    FUBeautyBodyItemMax
};


#pragma mark - 常量

static CGFloat const FUBottomBarHeight = 49.f;

static CGFloat const FUFunctionViewHeight = 118.f;

static CGFloat const FUFunctionSliderHeight = 30.f;


