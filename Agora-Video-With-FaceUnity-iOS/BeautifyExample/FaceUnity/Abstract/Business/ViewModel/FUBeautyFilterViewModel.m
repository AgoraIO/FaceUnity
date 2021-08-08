//
//  FUBeautyFilterConsumer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyFilterViewModel.h"
#import "FUBeautyDefine.h"
#import "FUBaseModel.h"

#import <FURenderKit/FURenderKit.h>

@interface FUBeautyFilterViewModel ()
@property (nonatomic, strong) FUBeauty *beauty;
@end

@implementation FUBeautyFilterViewModel
- (instancetype)init {
    self = [super init];
    if (self) {
        if ([FURenderKit shareRenderKit].beauty) {
            self.beauty = [FURenderKit shareRenderKit].beauty;
        } else {
            NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification.bundle" ofType:nil];
            self.beauty = [[FUBeauty alloc] initWithPath:path name:@"FUBeauty"];
            self.beauty.enable = NO;
            /* 默认精细磨皮 */
            self.beauty.heavyBlur = 0;
            self.beauty.blurType = 2;
            /* 默认自定义脸型 */
            self.beauty.faceShape = 4;
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
    
    self.beauty.filterName = m.imageName;//滤镜是需要传字符串，
    self.beauty.filterLevel = [m.mValue doubleValue];
    
    if (ViewModelBlock) {
        ViewModelBlock(nil);
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
