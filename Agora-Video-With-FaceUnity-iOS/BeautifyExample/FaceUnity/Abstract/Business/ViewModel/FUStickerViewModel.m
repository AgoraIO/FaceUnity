//
//  FUStickerConsumer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUStickerViewModel.h"
#import "FUBaseModel.h"
#import <FURenderKit/FURenderKit.h>

@interface FUStickerViewModel ()
@property (nonatomic, strong) FUSticker *curSticker;
@property (nonatomic, strong) FUSticker *oldSticker;
@end

@implementation FUStickerViewModel
- (instancetype)init {
    self = [super init];
    if (self) {
        self.type = FUDataTypeSticker;
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
    
    NSString *path = [[NSBundle mainBundle] pathForResource:m.imageName ofType:@"bundle"];
    if (path) {
        FUSticker *newItem = [[FUSticker alloc] initWithPath:path name:@"sticker"];
        [[FURenderKit shareRenderKit].stickerContainer replaceSticker:self.curSticker withSticker:newItem completion:^{
            if (ViewModelBlock) {
                ViewModelBlock(nil);
            }
        }];
        self.curSticker = newItem;
    } else {
        [self removeFromRenderLoop];
    }
}

- (BOOL)isNeedSlider {
    return NO;
}

//加到FURenderKit 渲染loop
- (void)addToRenderLoop {
    if (self.curSticker) {
        self.curSticker.enable = YES;
        [[FURenderKit shareRenderKit].stickerContainer addSticker:self.curSticker completion:^{

        }];
    }
}

//移除
- (void)removeFromRenderLoop {
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    self.curSticker = nil;
}

//开始生效
- (void)startRender {
    [self addToRenderLoop];
}

//不生效
- (void)stopRender {
    self.curSticker.enable = NO;
}

- (void)resetMaxFacesNumber {
    [FUAIKit shareKit].maxTrackFaces = 4;
}


@end
