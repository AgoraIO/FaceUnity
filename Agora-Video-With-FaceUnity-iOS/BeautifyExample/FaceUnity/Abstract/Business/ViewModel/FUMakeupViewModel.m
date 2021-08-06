//
//  FUMakeupConsumer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUMakeupViewModel.h"
#import "FUBaseModel.h"
#import <FURenderKit/FURenderKit.h>

#import "FUManager.h"

@interface FUMakeupViewModel ()
@property (nonatomic, strong) FUMakeup *makeup;
@property (nonatomic, strong) NSString *oldPackageName;
@end

@implementation FUMakeupViewModel
- (instancetype)init {
    self = [super init];
    if (self) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"face_makeup" ofType:@"bundle"];
        self.makeup = [[FUMakeup alloc] initWithPath:path name:@"face_makeup"];
        self.makeup.isMakeupOn = YES;
        self.makeup.enable = NO;
        self.type = FUDataTypeMakeup;
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
    
    if ([self.oldPackageName isEqualToString:m.imageName]) {
        self.makeup.intensity = [m.mValue doubleValue];
        
        if (ViewModelBlock) {
            ViewModelBlock(nil);
        }
        return ;
    }
    
    //替换bundle
    NSString *path = [[NSBundle mainBundle] pathForResource:m.imageName ofType:@"bundle"];
    if (path) {
        FUItem *item = [[FUItem alloc] initWithPath:path name:m.imageName];
        [self.makeup updateMakeupPackage:item needCleanSubItem:NO];
        self.oldPackageName = m.imageName;
    } else {
        [self.makeup updateMakeupPackage:nil needCleanSubItem:NO];
        self.oldPackageName = nil;
    }
    
    //设置整体妆容值
    self.makeup.intensity = [m.mValue doubleValue];
    
    if (ViewModelBlock) {
        ViewModelBlock(nil);
    }
}

- (BOOL)isNeedSlider {
    return YES;
}

//加到FURenderKit 渲染loop
- (void)addToRenderLoop {
    [FURenderKit shareRenderKit].makeup = self.makeup;
    [self startRender];
}

//移除
- (void)removeFromRenderLoop {
    [self stopRender];
    [FURenderKit shareRenderKit].makeup.enable = NO;
}


- (void)startRender {
    [FURenderKit shareRenderKit].makeup.enable = YES;
}


- (void)stopRender {
    [FURenderKit shareRenderKit].makeup = nil;
}

- (void)resetMaxFacesNumber {
    [FUAIKit shareKit].maxTrackFaces = 4;
}


@end
