//
//  FUBeautySkinConsumer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautySkinViewModel.h"
#import "FUBaseModel.h"
#import "FUBeautyDefine.h"
#import <FURenderKit/FURenderKit.h>

@interface FUBeautySkinViewModel ()
@property (nonatomic, strong) FUBeauty *beauty;
@end


@implementation FUBeautySkinViewModel
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
//            [self addToRenderLoop];
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
        case FUBeautifySkinBlurLevel: {
            self.beauty.blurLevel =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinColorLevel: {
            self.beauty.colorLevel =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinRedLevel: {
            self.beauty.redLevel =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinSharpen: {
            self.beauty.sharpen =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinEyeBright: {
            self.beauty.eyeBright =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinToothWhiten: {
            self.beauty.toothWhiten =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinRemovePouchStrength: {
            self.beauty.removePouchStrength =  [m.mValue floatValue];
        }
            break;
        case FUBeautifySkinRemoveNasolabialFoldsStrength: {
            self.beauty.removeNasolabialFoldsStrength =  [m.mValue floatValue];
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
