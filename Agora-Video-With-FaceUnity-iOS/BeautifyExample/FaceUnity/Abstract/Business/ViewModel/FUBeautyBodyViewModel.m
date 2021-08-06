//
//  FUBeautyBodyViewModel.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyBodyViewModel.h"
#import "FUBaseModel.h"
#import <FURenderKit/FURenderKit.h>
#import "FUBodyBeautyDefine.h"
@interface FUBeautyBodyViewModel ()
@property (nonatomic, strong) FUBodyBeauty *bodyBeauty;
@end

@implementation FUBeautyBodyViewModel
- (instancetype)init {
    self = [super init];
    if (self) {
        NSString *filePath = [[NSBundle mainBundle] pathForResource:@"body_slim" ofType:@"bundle"];
        self.bodyBeauty = [[FUBodyBeauty alloc] initWithPath:filePath name:@"body_slim"];
        self.bodyBeauty.debug = 0;
        self.bodyBeauty.enable = NO;
        self.type = FUDataTypebody;
    }
    return self;
}

- (void)consumerWithData:(id)model viewModelBlock:(ViewModelBlock _Nullable)ViewModelBlock {
    FUBaseModel *m = nil;
    if ([model isKindOfClass:[FUBaseModel class]]) {
        m = (FUBaseModel *)model;
    } else {
        NSLog(@"%@数据源model 不正确",self);
        if (ViewModelBlock) {
            ViewModelBlock(nil);
        }
        return;
    }

    switch (m.indexPath.row) {
        case BODYBEAUTYTYPE_bodySlimStrength:
            self.bodyBeauty.bodySlimStrength = [m.mValue floatValue];
            break;
        case BODYBEAUTYTYPE_legSlimStrength:
            self.bodyBeauty.legSlimStrength = [m.mValue floatValue];
            break;
        case BODYBEAUTYTYPE_waistSlimStrength:
            self.bodyBeauty.waistSlimStrength = [m.mValue floatValue];
            break;
        case BODYBEAUTYTYPE_shoulderSlimStrength:
            self.bodyBeauty.shoulderSlimStrength = [m.mValue floatValue];
            break;
        case BODYBEAUTYTYPE_hipSlimStrength:
            self.bodyBeauty.hipSlimStrength = [m.mValue floatValue];
            break;
        case BODYBEAUTYTYPE_headSlim:
            self.bodyBeauty.headSlim = [m.mValue floatValue];
            break;
        case BODYBEAUTYTYPE_legSlim:
            self.bodyBeauty.legSlim = [m.mValue floatValue];
            break;
        default:
            break;
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


- (void)addToRenderLoop {
    [FURenderKit shareRenderKit].bodyBeauty = self.bodyBeauty;
    [self startRender];
}

- (void)removeFromRenderLoop {
    [self stopRender];
    [FURenderKit shareRenderKit].bodyBeauty = nil;
}

//开始生效
- (void)startRender {
    [FURenderKit shareRenderKit].bodyBeauty.enable = YES;
}

//不生效
- (void)stopRender {
    [FURenderKit shareRenderKit].bodyBeauty.enable = NO;
}

- (void)resetMaxFacesNumber {
    [FUAIKit shareKit].maxTrackFaces = 1;
}

- (void)cacheData {
    [self.provider cacheData];
}
@end
