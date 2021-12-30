//
//  FUFilterViewModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import "FUFilterViewModel.h"
#import "FUFilterModel.h"

@interface FUFilterViewModel ()

@property (nonatomic, strong) FUBeauty *beauty;

@end


@implementation FUFilterViewModel

- (instancetype)initWithSelectedIndex:(NSInteger)selectedIndex needSlider:(BOOL)isNeedSlider {
    self = [super initWithSelectedIndex:selectedIndex needSlider:isNeedSlider];
    if (self) {
        self.model = [[FUFilterModel alloc] init];
        if ([FURenderKit shareRenderKit].beauty) {
            self.beauty = [FURenderKit shareRenderKit].beauty;
        } else {
            NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification" ofType:@"bundle"];
            self.beauty = [[FUBeauty alloc] initWithPath:path name:@"FUBeauty"];
            self.beauty.heavyBlur = 0;
            self.beauty.blurType = 3;
            self.beauty.faceShape = 4;
        }
        // 默认滤镜
        [self updateData:self.model.moduleData[1]];
    }
    return self;
}

#pragma mark - Override methods
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
        NSLog(@"FaceUnity：滤镜数据为空");
        return;
    }
    self.beauty.filterName = subModel.imageName;
    self.beauty.filterLevel = subModel.currentValue;
}

- (void)recover {
    self.selectedIndex = 1;
    self.beauty.filterName = self.model.moduleData[self.selectedIndex].imageName;
    self.beauty.filterLevel = self.model.moduleData[self.selectedIndex].currentValue;
}

@end
