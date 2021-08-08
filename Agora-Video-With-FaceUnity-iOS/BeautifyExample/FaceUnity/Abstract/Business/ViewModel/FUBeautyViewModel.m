//
//  FUBeautyViewModel.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/27.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyViewModel.h"
#import "FUBeautyDefine.h"
#import "FUBaseModel.h"

#import <FURenderKit/FURenderKit.h>

@interface FUBeautyViewModel ()
@property (nonatomic, strong) FUBeauty *beauty;
/* 滤镜参数 */
@property (nonatomic, strong) FUBeautyFilterViewModel *filtersViewModel;

/* 美肤参数 */
@property (nonatomic, strong) FUBeautySkinViewModel *skinViewModel;

/* 美型参数 */
@property (nonatomic, strong) FUBeautyShapeViewModel *shapeViewModel;

@property (nonatomic, strong) FUBaseViewModel *selectedViewModel;
@end

@implementation FUBeautyViewModel
- (instancetype)init {
    self = [super init];
    if (self) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification.bundle" ofType:nil];
        self.beauty = [[FUBeauty alloc] initWithPath:path name:@"FUBeauty"];
        self.beauty.enable = NO;
        /* 默认精细磨皮 */
        self.beauty.heavyBlur = 0;
        self.beauty.blurType = 2;
        /* 默认自定义脸型 */
        self.beauty.faceShape = 4;
        
        self.provider = [FUBeautyNodeModelProvider instanceProducer];
        
        //美颜是默认添加的，注意顺序问题。
        [self addToRenderLoop];
        
        //创建viewModel
        _filtersViewModel = [FUBeautyFilterViewModel instanceViewModel];
        //创建 provider 数据源提供者
        _filtersViewModel.provider = [(FUBeautyNodeModelProvider *)self.provider filterProvider];

        _shapeViewModel = [FUBeautyShapeViewModel instanceViewModel];
        _shapeViewModel.provider = [(FUBeautyNodeModelProvider *)self.provider shapeProvider];

        _skinViewModel = [FUBeautySkinViewModel instanceViewModel];
        _skinViewModel.provider = [(FUBeautyNodeModelProvider *)self.provider skinProvider];
        
        _selectedViewModel = _skinViewModel;
        [self setDefaultParams];
        
        //默认美肤
        self.beautySubType = FUBeautyDefineSkin;
    }
    return self;
}

- (void)setBeautySubType:(FUBeautySubDefine)beautySubType {
    _beautySubType = beautySubType;
    switch (beautySubType) {
        case FUBeautyDefineSkin:
            self.provider.dataSource = _skinViewModel.provider.dataSource;
            _selectedViewModel = _skinViewModel;
            break;
        case FUBeautyDefineShape:
            self.provider.dataSource = _shapeViewModel.provider.dataSource;
            _selectedViewModel = _shapeViewModel;
            break;
        case FUBeautyDefineFilter:
            self.provider.dataSource = _filtersViewModel.provider.dataSource;
            _selectedViewModel = _filtersViewModel;
            break;
        default:
            break;
    }
}

- (void)consumerWithData:(id)model viewModelBlock:(ViewModelBlock _Nullable)ViewModelBlock {
    FUBaseModel *m = nil;
    if ([model isKindOfClass:[FUBaseModel class]]) {
        m = (FUBaseModel *)model;
    } else {
        NSLog(@"%@数据源model 不正确",self);
        return;
    }
    
    switch (m.indexPath.section) {
        case FUBeautyDefineSkin:
            [self.skinViewModel consumerWithData:model viewModelBlock:ViewModelBlock];
            break;
        case FUBeautyDefineFilter:
            [self.filtersViewModel consumerWithData:model viewModelBlock:ViewModelBlock];
            break;
        case FUBeautyDefineShape:
            [self.shapeViewModel consumerWithData:model viewModelBlock:ViewModelBlock];
            break;
        default:
            break;
    }
}


- (BOOL)isDefaultValue {
    if ([self.selectedViewModel respondsToSelector:@selector(isDefaultValue)]) {
        return [self.selectedViewModel isDefaultValue];
    }
    return NO;
}

- (void)setDefaultParams {
    for (FUBaseModel *model in self.shapeViewModel.provider.dataSource) {
        [self.shapeViewModel consumerWithData:model viewModelBlock:nil];
    }
    
    for (FUBaseModel *model in self.skinViewModel.provider.dataSource) {
        [self.skinViewModel consumerWithData:model viewModelBlock:nil];
    }
    
    for (FUBaseModel *model in self.filtersViewModel.provider.dataSource) {
        [self.filtersViewModel consumerWithData:model viewModelBlock:nil];
    }
}

- (BOOL)isNeedSlider {
    return [self.selectedViewModel isNeedSlider];
}

- (void)resetDefaultValue {
    switch (self.beautySubType) {
        case FUBeautyDefineSkin:
            [self.skinViewModel resetDefaultValue];
            break;
        case FUBeautyDefineShape:
            [self.shapeViewModel resetDefaultValue];
            break;
        default:
            break;
    }
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
    [self.filtersViewModel.provider cacheData];
    [self.shapeViewModel.provider cacheData];
    [self.skinViewModel.provider cacheData];
}
@end
