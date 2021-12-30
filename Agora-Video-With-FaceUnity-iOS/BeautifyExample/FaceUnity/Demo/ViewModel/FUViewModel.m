//
//  FUViewModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/11.
//

#import "FUViewModel.h"

@interface FUViewModel ()

/// 是否需要Slider
@property (nonatomic, assign) BOOL needSlider;

@end

@implementation FUViewModel

- (instancetype)initWithSelectedIndex:(NSInteger)selectedIndex needSlider:(BOOL)isNeedSlider {
    self = [super init];
    if (self) {
        self.selectedIndex = selectedIndex;
        self.needSlider = isNeedSlider;
    }
    return self;
}

- (instancetype)init {
    return [self initWithSelectedIndex:-1 needSlider:NO];
}

- (void)startRender {
    _rendering = YES;
}

- (void)stopRender {
    _rendering = NO;
}

- (void)updateData:(FUSubModel *)subModel {
}

- (void)recover {
    for (FUSubModel *subModel in self.model.moduleData) {
        subModel.currentValue = subModel.defaultValue;
        [self updateData:subModel];
    }
}

@end
