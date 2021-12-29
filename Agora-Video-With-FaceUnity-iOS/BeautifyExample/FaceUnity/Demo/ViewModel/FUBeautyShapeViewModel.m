//
//  FUBeautyShapeViewModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/11.
//

#import "FUBeautyShapeViewModel.h"
#import "FUBeautyShapeModel.h"

@interface FUBeautyShapeViewModel ()

@property (nonatomic, strong) FUBeauty *beauty;

@end

@implementation FUBeautyShapeViewModel

- (instancetype)initWithSelectedIndex:(NSInteger)selectedIndex needSlider:(BOOL)isNeedSlider {
    self = [super initWithSelectedIndex:selectedIndex needSlider:isNeedSlider];
    if (self) {
        self.model = [[FUBeautyShapeModel alloc] init];
        
        if ([FURenderKit shareRenderKit].beauty) {
            self.beauty = [FURenderKit shareRenderKit].beauty;
        } else {
            NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification" ofType:@"bundle"];
            self.beauty = [[FUBeauty alloc] initWithPath:path name:@"FUBeauty"];
            self.beauty.heavyBlur = 0;
            self.beauty.blurType = 3;
            self.beauty.faceShape = 4;
        }
        
        // 默认美颜
        for (FUSubModel *subModel in self.model.moduleData) {
            [self updateData:subModel];
        }
    }
    return self;
}

#pragma mark - Override
- (void)startRender {
    [super startRender];
    if (![FURenderKit shareRenderKit].beauty) {
        [FURenderKit shareRenderKit].beauty = self.beauty;
    }
    if (![FURenderKit shareRenderKit].beauty.enable) {
        [FURenderKit shareRenderKit].beauty.enable = YES;
    }
}

- (void)stopRender {
    [super stopRender];
    [FURenderKit shareRenderKit].beauty.enable = NO;
    [FURenderKit shareRenderKit].beauty = nil;
    
}

- (void)updateData:(FUSubModel *)subModel {
    if (!subModel) {
        NSLog(@"FaceUnity：美肤数据为空");
        return;
    }
    switch (subModel.functionType) {
        case FUBeautyShapeItemCheekThinning:
            self.beauty.cheekThinning = subModel.currentValue;
            break;
        case FUBeautyShapeItemCheekV:
            self.beauty.cheekV = subModel.currentValue;
            break;
        case FUBeautyShapeItemCheekNarrow:
            self.beauty.cheekNarrowV2 = subModel.currentValue;
            break;
        case FUBeautyShapeItemCheekShort:
            self.beauty.cheekShort = subModel.currentValue;
            break;
        case FUBeautyShapeItemCheekSmall:
            self.beauty.cheekSmallV2 = subModel.currentValue;
            break;
        case FUBeautyShapeItemCheekBones:
            self.beauty.intensityCheekbones = subModel.currentValue;
            break;
        case FUBeautyShapeItemLowerJaw:
            self.beauty.intensityLowerJaw = subModel.currentValue;
            break;
        case FUBeautyShapeItemEyeEnlarging:
            self.beauty.eyeEnlargingV2 = subModel.currentValue;
            break;
        case FUBeautyShapeItemEyeCircle:
            self.beauty.intensityEyeCircle = subModel.currentValue;
            break;
        case FUBeautyShapeItemChin:
            self.beauty.intensityChin = subModel.currentValue;
            break;
        case FUBeautyShapeItemForehead:
            self.beauty.intensityForeheadV2 = subModel.currentValue;
            break;
        case FUBeautyShapeItemNose:
            self.beauty.intensityNoseV2 = subModel.currentValue;
            break;
        case FUBeautyShapeItemMouth:
            self.beauty.intensityMouthV2 = subModel.currentValue;
            break;
        case FUBeautyShapeItemCanthus:
            self.beauty.intensityCanthus = subModel.currentValue;
            break;
        case FUBeautyShapeItemEyeSpace:
            self.beauty.intensityEyeSpace = subModel.currentValue;
            break;
        case FUBeautyShapeItemEyeRotate:
            self.beauty.intensityEyeRotate = subModel.currentValue;
            break;
        case FUBeautyShapeItemLongNose:
            self.beauty.intensityLongNose = subModel.currentValue;
            break;
        case FUBeautyShapeItemPhiltrum:
            self.beauty.intensityPhiltrum = subModel.currentValue;
            break;
        case FUBeautyShapeItemSmile:
            self.beauty.intensitySmile = subModel.currentValue;
            break;
        default:
            break;
    }
}

- (void)recover {
    for (FUSubModel *subModel in self.model.moduleData) {
        subModel.currentValue = subModel.defaultValue;
        [self updateData:subModel];
    }
}

- (BOOL)isDefaultValue {
    for (FUSubModel *subModel in self.model.moduleData) {
        int currentIntValue = subModel.isBidirection ? (int)(subModel.currentValue / subModel.ratio * 100 - 50) : (int)(subModel.currentValue / subModel.ratio * 100);
        int defaultIntValue = subModel.isBidirection ? (int)(subModel.defaultValue / subModel.ratio * 100 - 50) : (int)(subModel.defaultValue / subModel.ratio * 100);
        if (currentIntValue != defaultIntValue) {
            return NO;
        }
    }
    return YES;
}

@end
