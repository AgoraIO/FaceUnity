//
//  FUStickerViewModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import "FUStickerViewModel.h"
#import "FUStickerModel.h"

@interface FUStickerViewModel ()

/// 当前的贴纸
@property (nonatomic, strong) FUSticker *currentSticker;

@end

@implementation FUStickerViewModel

- (instancetype)initWithSelectedIndex:(NSInteger)selectedIndex needSlider:(BOOL)isNeedSlider {
    self = [super initWithSelectedIndex:selectedIndex needSlider:isNeedSlider];
    if (self) {
        self.model = [[FUStickerModel alloc] init];
    }
    return self;
}

#pragma mark - Override methods
- (void)startRender {
    [super startRender];
}

- (void)stopRender {
    [super stopRender];
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    self.currentSticker = nil;
}

- (void)updateData:(FUSubModel *)model {
    if (!model) {
        NSLog(@"FaceUnity：贴纸数据为空");
        return;
    }
    if ([model.imageName isEqualToString:@"remove"]) {
        // 选中取消贴纸
        [self stopRender];
        return;
    }
    NSString *path = [[NSBundle mainBundle] pathForResource:model.imageName ofType:@"bundle"];
    if (!path) {
        NSLog(@"FaceUnity：找不到贴纸路径");
        return;
    }
    FUSticker *sticker = [[FUSticker alloc] initWithPath:path name:@"sticker"];
    if (self.currentSticker) {
        [[FURenderKit shareRenderKit].stickerContainer replaceSticker:self.currentSticker withSticker:sticker completion:nil];
    } else {
        [[FURenderKit shareRenderKit].stickerContainer addSticker:sticker completion:nil];
    }
    self.currentSticker = sticker;
}

@end
