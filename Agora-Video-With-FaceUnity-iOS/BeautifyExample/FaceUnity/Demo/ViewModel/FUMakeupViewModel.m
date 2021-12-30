//
//  FUMakeupViewModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import "FUMakeupViewModel.h"
#import "FUMakeupModel.h"

@interface FUMakeupViewModel ()

@property (nonatomic, strong) FUMakeup *makeup;

@property (nonatomic, copy) NSString *currentMakeup;


@end

@implementation FUMakeupViewModel

- (instancetype)initWithSelectedIndex:(NSInteger)selectedIndex needSlider:(BOOL)isNeedSlider {
    self = [super initWithSelectedIndex:selectedIndex needSlider:isNeedSlider];
    if (self) {
        self.model = [[FUMakeupModel alloc] init];
    }
    return self;
}

#pragma mark - Override methods
- (void)startRender {
    [super startRender];
    [FURenderKit shareRenderKit].makeup = self.makeup;
    [FURenderKit shareRenderKit].makeup.enable = YES;
}

- (void)stopRender {
    [super stopRender];
    [FURenderKit shareRenderKit].makeup.enable = NO;
    [FURenderKit shareRenderKit].makeup = nil;
    _makeup = nil;
    _currentMakeup = nil;
}

- (void)updateData:(FUSubModel *)subModel {
    if (!subModel) {
        NSLog(@"FaceUnity：美妆数据为空");
        return;
    }
    
    if ([subModel.imageName isEqualToString:@"remove"]) {
        // 选中取消美妆
        [self stopRender];
        return;
    }
    
    if ([subModel.imageName isEqualToString:self.currentMakeup]) {
        // 调节当前美妆程度
        self.makeup.intensity = subModel.currentValue;
        return;
    }
    NSString *path = [[NSBundle mainBundle] pathForResource:subModel.imageName ofType:@"bundle"];
    if (!path) {
        NSLog(@"FaceUnity：找不到美妆路径");
        return;
    }
    FUItem *makeupItem = [[FUItem alloc] initWithPath:path name:subModel.imageName];
    [self.makeup updateMakeupPackage:makeupItem needCleanSubItem:NO];
    self.makeup.intensity = subModel.currentValue;
    self.currentMakeup = subModel.imageName;
}

#pragma mark - Getters
- (FUMakeup *)makeup {
    if (!_makeup) {
        _makeup = [[FUMakeup alloc] initWithPath:[[NSBundle mainBundle] pathForResource:@"face_makeup" ofType:@"bundle"] name:@"face_makeup"];
        _makeup.isMakeupOn = YES;
    }
    return _makeup;
}


@end
