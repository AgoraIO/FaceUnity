//
//  FUBeautyShapeConsumer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyShapeViewModel.h"
#import "FUBaseModel.h"
#import "FUBeautyDefine.h"
#import <FURenderKit/FURenderKit.h>

@interface FUBeautyShapeViewModel ()
@property (nonatomic, strong) FUBeauty *beauty;
@end

@implementation FUBeautyShapeViewModel
- (instancetype)init {
    self = [super init];
    if (self) {
        if ([FURenderKit shareRenderKit].beauty) {
            self.beauty = [FURenderKit shareRenderKit].beauty;
        } else {
            NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification.bundle" ofType:nil];
            self.beauty = [[FUBeauty alloc] initWithPath:path name:@"FUBeauty"];
            /* 默认精细磨皮 */
            self.beauty.heavyBlur = 0;
            self.beauty.blurType = 2;
            /* 默认自定义脸型 */
            self.beauty.faceShape = 4;
            self.beauty.enable = NO;
//            [FURenderKit shareRenderKit].beauty = self.beauty;
        }
        self.type = FUDataTypeBeauty;
    }
    return self;
}

- (void)consumerWithData:(id)model viewModelBlock:(ViewModelBlock _Nullable)ViewModelBlock {
    FUBaseModel *m = nil;
    if ([model isKindOfClass:[FUBaseModel class]]) {
        m = (FUBaseModel *)model;
    } else {
        NSLog(@"%@数据源model 不正确",self);
        return;
    }
    
    switch (m.indexPath.row) {
        case FUBeautifyShapeCheekThinning: {
            self.beauty.cheekThinning = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeCheekV: {
            self.beauty.cheekV = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeCheekNarrow: {
            self.beauty.cheekNarrow = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeCheekSmall: {
            self.beauty.cheekSmall = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityCheekbones: {
            self.beauty.intensityCheekbones = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityLowerJaw: {
            self.beauty.intensityLowerJaw = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeEyeEnlarging: {
            self.beauty.eyeEnlarging = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeEyeCircle: {
            self.beauty.intensityEyeCircle = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityChin: {
            self.beauty.intensityChin = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityForehead: {
            self.beauty.intensityForehead = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityNose: {
            self.beauty.intensityNose = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityMouth: {
            self.beauty.intensityMouth = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityCanthus: {
            self.beauty.intensityCanthus = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityEyeSpace: {
            self.beauty.intensityEyeSpace = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityEyeRotate: {
            self.beauty.intensityEyeRotate = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityLongNose: {
            self.beauty.intensityLongNose = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensityPhiltrum: {
            self.beauty.intensityPhiltrum = [m.mValue floatValue];
        }
            break;
        case FUBeautifyShapeIntensitySmile: {
            self.beauty.intensitySmile = [m.mValue floatValue];
        }
            break;
        default:
            break;
    }
    
    if (ViewModelBlock) {
        ViewModelBlock(nil);
    }
}


#pragma mark - 协议方法
- (BOOL)isDefaultValue {
    NSArray *arr = self.provider.dataSource;
    for (FUBaseModel *model in arr){
        if (fabs([model.mValue floatValue] - [model.defaultValue floatValue]) > 0.01 ) {
            return NO;
        }
    }
    return YES;
}

- (void)resetDefaultValue {
    NSArray *arr = self.provider.dataSource;
    for (FUBaseModel *model in arr) {
        model.mValue = model.defaultValue;
        [self consumerWithData:model viewModelBlock:nil];
    }
}

- (BOOL)isNeedSlider {
    return YES;
}

//加到FURenderKit 渲染loop
- (void)addToRenderLoop {
    [FURenderKit shareRenderKit].beauty = self.beauty;
    [self startRender];
}

//移除
- (void)removeFromRenderLoop {
    [self stopRender];
    [FURenderKit shareRenderKit].beauty = nil;
}

- (void)startRender {
    [FURenderKit shareRenderKit].beauty.enable = YES;
}

- (void)stopRender {
    [FURenderKit shareRenderKit].beauty.enable = NO;
}

- (void)resetMaxFacesNumber {
    [FUAIKit shareKit].maxTrackFaces = 4;
}


- (void)cacheData {
    [self.provider cacheData];
}
@end
