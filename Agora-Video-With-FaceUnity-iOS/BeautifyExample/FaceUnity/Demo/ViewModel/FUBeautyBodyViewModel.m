//
//  FUBeautyBodyViewModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import "FUBeautyBodyViewModel.h"
#import "FUBeautyBodyModel.h"

@interface FUBeautyBodyViewModel ()

@property (nonatomic, strong) FUBodyBeauty *bodyBeauty;

@end

@implementation FUBeautyBodyViewModel

@synthesize rendering = _rendering;

- (instancetype)initWithSelectedIndex:(NSInteger)selectedIndex needSlider:(BOOL)isNeedSlider {
    self = [super initWithSelectedIndex:selectedIndex needSlider:isNeedSlider];
    if (self) {
        self.model = [[FUBeautyBodyModel alloc] init];
        _rendering = YES;
    }
    return self;
}

#pragma mark - Override methods

- (void)startRender {
    _rendering = YES;
    [FURenderKit shareRenderKit].bodyBeauty = self.bodyBeauty;
}

- (void)stopRender {
    _rendering = NO;
    [FURenderKit shareRenderKit].bodyBeauty = nil;
}

- (void)updateData:(FUSubModel *)subModel {
    if (!subModel) {
        NSLog(@"FaceUnity：美体数据为空");
        return;
    }
    switch (subModel.functionType) {
        case FUBeautyBodyItemSlim:
            self.bodyBeauty.bodySlimStrength = subModel.currentValue;
            break;
        case FUBeautyBodyItemLongLeg:
            self.bodyBeauty.legSlimStrength = subModel.currentValue;
            break;
        case FUBeautyBodyItemThinWaist:
            self.bodyBeauty.waistSlimStrength = subModel.currentValue;
            break;
        case FUBeautyBodyItemBeautyShoulder:
            self.bodyBeauty.shoulderSlimStrength = subModel.currentValue;
            break;
        case FUBeautyBodyItemBeautyButtock:
            self.bodyBeauty.hipSlimStrength = subModel.currentValue;
            break;
        case FUBeautyBodyItemSmallHead:
            self.bodyBeauty.headSlim = subModel.currentValue;
            break;
        case FUBeautyBodyItemThinLeg:
            self.bodyBeauty.legSlim = subModel.currentValue;
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

- (FUBodyBeauty *)bodyBeauty {
    if (!_bodyBeauty) {
        NSString *filePath = [[NSBundle mainBundle] pathForResource:@"body_slim" ofType:@"bundle"];
        _bodyBeauty = [[FUBodyBeauty alloc] initWithPath:filePath name:@"body_slim"];
    }
    return _bodyBeauty;
}

@end
