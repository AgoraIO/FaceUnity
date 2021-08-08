//
//  FUDemoBar.m
//  FUAPIDemoBar
//
//  Created by L on 2018/6/26.
//  Copyright © 2018年 L. All rights reserved.
//

#import "FUAPIDemoBar.h"
#import "FUFilterView.h"
#import "FUSlider.h"
#import "FUBeautyView.h"

#import "FUSquareButton.h"

#import "FUBaseViewModel.h"

#import "FUMakeupViewModel.h"
#import "FUStickerViewModel.h"
#import "FUBeautyBodyViewModel.h"
#import "FUBeautyViewModel.h"
#import "FUBeautyDefine.h"

@interface FUAPIDemoBar ()<FUFilterViewDelegate, FUBeautyViewDelegate>


@property (weak, nonatomic) IBOutlet UIButton *skinBtn;
@property (weak, nonatomic) IBOutlet UIButton *shapeBtn;
@property (weak, nonatomic) IBOutlet UIButton *beautyFilterBtn;
@property (weak, nonatomic) IBOutlet UIButton *stickerBtn;
@property (weak, nonatomic) IBOutlet UIButton *makeupBtn;
@property (weak, nonatomic) IBOutlet UIButton *bodyBtn;
@property (weak, nonatomic) IBOutlet UIView *recoverLine;
@property (weak, nonatomic) IBOutlet FUSquareButton *recoverButton;

// 上半部分
@property (weak, nonatomic) IBOutlet UIView *topView;
// 滤镜页
@property (weak, nonatomic) IBOutlet FUFilterView *stickerView;
// 美颜滤镜页
@property (weak, nonatomic) IBOutlet FUFilterView *beautyFilterView;
@property (weak, nonatomic) IBOutlet FUFilterView *makeupView;

@property (weak, nonatomic) IBOutlet FUSlider *beautySlider;
@property (weak, nonatomic) IBOutlet FUBeautyView *bodyView;
// 美型页
@property (weak, nonatomic) IBOutlet FUBeautyView *shapeView;
// 美肤页
@property (weak, nonatomic) IBOutlet FUBeautyView *skinView;

/* 当前选中参数 */
@property (strong, nonatomic) FUBaseModel *seletedParam;

//当前显示的view
@property (strong, nonatomic) FUBaseCollectionView *selectedView;

/* 滤镜参数 */
@property (nonatomic, strong) FUBeautyViewModel *beautyViewModel;

@property (nonatomic, strong) FUStickerViewModel *stickerViewModel;

@property (nonatomic, strong) FUMakeupViewModel *makeupViewModel;

@property (nonatomic, strong) FUBeautyBodyViewModel *beautyBodyViewModel;

@end

@implementation FUAPIDemoBar
-(instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {self.backgroundColor = [UIColor clearColor];
        
        self.backgroundColor = [UIColor clearColor];
        NSBundle *bundle = [NSBundle bundleForClass:[FUAPIDemoBar class]];
        self = (FUAPIDemoBar *)[bundle loadNibNamed:@"FUAPIDemoBar" owner:self options:nil].firstObject;
        self.frame = frame;
    }
    return self ;
}


-(void)awakeFromNib {
    [super awakeFromNib];
    [self setupDate];
    
    [self reloadShapView:_beautyViewModel];
    [self reloadSkinView:_beautyViewModel];
    [self reloadFilterView:_beautyViewModel];
    
    _makeupView.dataList = _makeupViewModel.provider.dataSource;
    _makeupView.viewModel = _makeupViewModel;
    
    [_makeupView setDefaultFilter:_makeupViewModel.provider.dataSource[0]];
    [_makeupView reloadData];
    
    _stickerView.dataList = _stickerViewModel.provider.dataSource;
    _stickerView.viewModel = _stickerViewModel;
    [_makeupView setDefaultFilter:_stickerViewModel.provider.dataSource[0]];
    [_stickerView reloadData];
    
    _bodyView.dataList = _beautyBodyViewModel.provider.dataSource;
    _bodyView.viewModel = _beautyBodyViewModel;
    _bodyView.selectedIndex = 1;
    [_makeupView setDefaultFilter:_beautyBodyViewModel.provider.dataSource[0]];
    [_bodyView reloadData];
    
    self.stickerView.mDelegate = self ;
    self.makeupView.mDelegate = self;
    self.beautyFilterView.mDelegate = self ;
    
    self.bodyView.mDelegate = self;
    self.shapeView.mDelegate = self ;
    self.skinView.mDelegate = self;
    
    [self.skinBtn setTitle:NSLocalizedString(@"美肤", nil) forState:UIControlStateNormal];
    [self.shapeBtn setTitle:NSLocalizedString(@"美型", nil) forState:UIControlStateNormal];
    [self.beautyFilterBtn setTitle:NSLocalizedString(@"滤镜", nil) forState:UIControlStateNormal];
    
    self.skinBtn.tag = 101;
    self.shapeBtn.tag = 102;
    self.beautyFilterBtn.tag = 103 ;
    self.stickerBtn.tag = 104;
    self.makeupBtn.tag = 105;
    self.bodyBtn.tag = 106;
}

-(void)setupDate{
    //创建美颜 ，内部默认添加到FURenderKit渲染循环
    _beautyViewModel = [FUBeautyViewModel instanceViewModel];

    _stickerViewModel = [FUStickerViewModel instanceViewModel];
    _stickerViewModel.provider = [FUStickerNodeModelProvider instanceProducer];
    
    _makeupViewModel = [FUMakeupViewModel instanceViewModel];
    _makeupViewModel.provider = [FUMakeupNodeModelProvider instanceProducer];
    
    _beautyBodyViewModel = [FUBeautyBodyViewModel instanceViewModel];
    _beautyBodyViewModel.provider = [FUBeautyBodyNodeModelProvider instanceProducer];
}


- (void)setMDelegate:(id<FUAPIDemoBarDelegate>)mDelegate {
    _mDelegate = mDelegate;
    //默认选中美肤
    if ([self.mDelegate respondsToSelector:@selector(bottomDidChangeViewModel:)]) {
        [self.mDelegate bottomDidChangeViewModel:_beautyViewModel];
    }
    
    //默认隐藏底部菜单栏
    if ([self.mDelegate respondsToSelector:@selector(showTopView:)]) {
        [self.mDelegate showTopView:NO];
    }
    
    //设置默认的滤镜
    if (self.beautyFilterView.dataList.count >= 1) {
        FUBaseModel *defaultModel = self.beautyFilterView.dataList[1];
        [self.beautyFilterView setDefaultFilter:defaultModel];
        [self.beautyViewModel consumerWithData:defaultModel viewModelBlock:nil];
    }
}

-(void)layoutSubviews{
    [super layoutSubviews];
}

-(void)updateUI:(UIButton *)sender{
    [self.recoverButton setTitle:@"恢复" forState:UIControlStateNormal];
    self.skinBtn.selected = NO;
    self.shapeBtn.selected = NO;
    self.beautyFilterBtn.selected = NO;
    
    self.stickerBtn.selected = NO;
    self.makeupBtn.selected = NO;
    self.bodyBtn.selected = NO;
    
    
    self.skinView.hidden = YES;
    self.shapeView.hidden = YES ;
    self.beautyFilterView.hidden = YES;
    
    self.makeupView.hidden = YES;
    self.stickerView.hidden = YES;
    self.bodyView.hidden = YES;
    
    sender.selected = YES;
    
    if (sender == self.skinBtn) {
        self.skinView.hidden = NO;
        self.selectedView = self.skinView;
        [(FUBeautyViewModel *)self.selectedView.viewModel setBeautySubType:FUBeautyDefineSkin];
    }
    if (sender == self.stickerBtn) {
        self.stickerView.hidden = NO;
        self.selectedView = self.stickerView;
    }
    if (sender == self.makeupBtn) {
        self.makeupView.hidden = NO;
        self.selectedView = self.makeupView;
    }
    if (sender == self.beautyFilterBtn) {
        self.beautyFilterView.hidden = NO;
        self.selectedView = self.beautyFilterView;
        [(FUBeautyViewModel *)self.selectedView.viewModel setBeautySubType:FUBeautyDefineFilter];
    }
    if (sender == self.shapeBtn) {
        self.shapeView.hidden = NO;
        self.selectedView = self.shapeView;
        [(FUBeautyViewModel *)self.selectedView.viewModel setBeautySubType:FUBeautyDefineShape];
    }
    if (sender == self.bodyBtn) {
        self.bodyView.hidden = NO;
        self.selectedView = self.bodyView;
    }
}


- (IBAction)bottomBtnsSelected:(UIButton *)sender {
    if (sender.selected) {
        sender.selected = NO ;
        [self hiddenTopViewWithAnimation:YES];
        return ;
    }
    [self updateUI:sender];
    
    [self setRestBtnHidden:YES];
    
    if (self.shapeBtn.selected) {
        /* 修改当前UI */
        [self setRestBtnHidden:NO];
        [self sliderChangeEnd:nil];
        NSInteger selectedIndex = self.shapeView.selectedIndex;
        self.beautySlider.hidden = selectedIndex < 0 ;
        
        if (selectedIndex >= 0) {
            FUBaseModel *model = self.shapeView.dataList[selectedIndex];
            _seletedParam = model;
            self.beautySlider.value = [model.mValue floatValue] / model.ratio;
        }
    }
    
    if (self.skinBtn.selected) {
        [self setRestBtnHidden:NO];
        [self sliderChangeEnd:nil];
        NSInteger selectedIndex = self.skinView.selectedIndex;
        self.beautySlider.hidden = selectedIndex < 0 ;
        
        if (selectedIndex >= 0) {
            FUBaseModel *model = self.skinView.dataList[selectedIndex];
            _seletedParam = model;
            self.beautySlider.value = [model.mValue floatValue] / model.ratio;
        }
    }
    
    // slider 是否显示
    if (self.beautyFilterBtn.selected) {
        NSInteger selectedIndex = self.beautyFilterView.selectedIndex ;
        self.beautySlider.type = FUFilterSliderType01 ;
        self.beautySlider.hidden = selectedIndex <= 0;
        if (selectedIndex >= 0) {
            FUBaseModel *model = self.beautyFilterView.dataList[selectedIndex];
            _seletedParam = model;
            self.beautySlider.value = [model.mValue floatValue] / model.ratio;
        }
    }
    
    if (self.stickerBtn.selected) {
        NSInteger selectedIndex = self.stickerView.selectedIndex ;
        self.beautySlider.hidden = YES;
        if (selectedIndex >= 0) {
            FUBaseModel *model = self.stickerView.dataList[selectedIndex];
            _seletedParam = model;
        }
    }
    
    
    if (self.makeupBtn.selected) {
        NSInteger selectedIndex = self.makeupView.selectedIndex ;
        self.makeupView.type = FUFilterSliderType01 ;
        self.beautySlider.hidden = selectedIndex <= 0;
        if (selectedIndex >= 0) {
            FUBaseModel *model = self.makeupView.dataList[selectedIndex];
            _seletedParam = model;
            self.beautySlider.value = [model.mValue floatValue] / model.ratio;
        }
    }

    if (self.bodyBtn.selected) {
        [self setRestBtnHidden:NO];
        [self sliderChangeEnd:nil];
        NSInteger selectedIndex = self.bodyView.selectedIndex;
        self.beautySlider.hidden = selectedIndex < 0 ;
        
        if (selectedIndex >= 0) {
            FUBaseModel *model = self.bodyView.dataList[selectedIndex];
            _seletedParam = model;
            self.beautySlider.value = [model.mValue floatValue] / model.ratio;
        }
    }
    
    if ([self.mDelegate respondsToSelector:@selector(bottomDidChangeViewModel:)]) {
        [self.mDelegate bottomDidChangeViewModel:self.selectedView.viewModel];
    }
    
    [self showTopViewWithAnimation:self.topView.isHidden];
    [self setSliderTyep:_seletedParam];
}

- (IBAction)sliderChangeEnd:(id)sender {
    id <FUCharacteristicProtocol> viewModel = (id<FUCharacteristicProtocol>)self.selectedView.viewModel;
    if ([viewModel respondsToSelector:@selector(isDefaultValue)]) {
        if ([viewModel isDefaultValue]) {
            self.recoverButton.alpha = 0.7;
            self.recoverButton.userInteractionEnabled = NO;
        } else {
            self.recoverButton.alpha = 1;
            self.recoverButton.userInteractionEnabled = YES;
        }
    } else {
        self.recoverButton.alpha = 1;
        self.recoverButton.userInteractionEnabled = YES;
    }
    [self.selectedView reloadData];
}

- (IBAction)recoverAction:(id)sender {
    UIAlertController *alertCon = [UIAlertController alertControllerWithTitle:nil message:@"是否将所有参数恢复到默认值" preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *cancleAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
    }];
    [cancleAction setValue:[UIColor colorWithRed:44/255.0 green:46/255.0 blue:48/255.0 alpha:1.0] forKey:@"titleTextColor"];
    
    UIAlertAction *certainAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.recoverButton.alpha = 0.7;
        self.recoverButton.userInteractionEnabled = NO;
        //抽象接口， 设置默认值处理
        id <FUCharacteristicProtocol> viewModel = (id<FUCharacteristicProtocol>)self.selectedView.viewModel;
        FUBaseNodeModelProvider *provider = self.selectedView.viewModel.provider;
        if ([viewModel respondsToSelector:@selector(resetDefaultValue)]) {
            [viewModel resetDefaultValue];
        }
        [self.selectedView reloadData];
        if (self.selectedView.selectedIndex >= 0) {
            //处理数据逻辑
            FUBaseModel *model = provider.dataSource[self.selectedView.selectedIndex];
            self.beautySlider.value = [model.mValue floatValue] / model.ratio;
        }
    }];

    [certainAction setValue:[UIColor colorWithRed:31/255.0 green:178/255.0 blue:255/255.0 alpha:1.0] forKey:@"titleTextColor"];
    
    [alertCon addAction:cancleAction];
    [alertCon addAction:certainAction];
    
    [[self viewControllerFromView:self]  presentViewController:alertCon animated:YES completion:^{
    }];
}

/// 设置恢复按钮状态
/// @param hidden 隐藏/显示
- (void)setRestBtnHidden:(BOOL)hidden{
    self.recoverButton.hidden = hidden;
    self.recoverLine.hidden = hidden;
}

-(void)setSliderTyep:(FUBaseModel *)param{
    if (param.iSStyle101) {
        self.beautySlider.type = FUFilterSliderType101;
    }else{
        self.beautySlider.type = FUFilterSliderType01 ;
    }
}


// 开启上半部分
- (void)showTopViewWithAnimation:(BOOL)animation {
    
    if (animation) {
        self.topView.alpha = 0.0 ;
        self.topView.transform = CGAffineTransformMakeTranslation(0, self.topView.frame.size.height / 2.0) ;
        self.topView.hidden = NO ;
        [UIView animateWithDuration:0.35 animations:^{
            self.topView.transform = CGAffineTransformIdentity ;
            self.topView.alpha = 1.0 ;
        }];
        
        if (self.mDelegate && [self.mDelegate respondsToSelector:@selector(showTopView:)]) {
            [self.mDelegate showTopView:YES];
        }
    }else {
        self.topView.transform = CGAffineTransformIdentity ;
        self.topView.alpha = 1.0 ;
    }
}

// 关闭上半部分
-(void)hiddenTopViewWithAnimation:(BOOL)animation {
    
    if (self.topView.hidden) {
        return ;
    }
    if (animation) {
        self.topView.alpha = 1.0 ;
        self.topView.transform = CGAffineTransformIdentity ;
        self.topView.hidden = NO ;
        [UIView animateWithDuration:0.35 animations:^{
            self.topView.transform = CGAffineTransformMakeTranslation(0, self.topView.frame.size.height / 2.0) ;
            self.topView.alpha = 0.0 ;
        }completion:^(BOOL finished) {
            self.topView.hidden = YES ;
            self.topView.alpha = 1.0 ;
            self.topView.transform = CGAffineTransformIdentity ;
            
            self.skinBtn.selected = NO ;
            self.shapeBtn.selected = NO ;
            self.beautyFilterBtn.selected = NO ;
        }];
        
        if (self.mDelegate && [self.mDelegate respondsToSelector:@selector(showTopView:)]) {
            [self.mDelegate showTopView:NO];
        }
    }else {
        
        self.topView.hidden = YES ;
        self.topView.alpha = 1.0 ;
        self.topView.transform = CGAffineTransformIdentity ;
    }
}


- (UIViewController *)viewControllerFromView:(UIView *)view {
    for (UIView *next = [view superview]; next; next = next.superview) {
        UIResponder *nextResponder = [next nextResponder];
        if ([nextResponder isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)nextResponder;
        }
    }
    return nil;
}

#pragma mark ---- FUFilterViewDelegate
// 开启滤镜
-(void)filterViewDidSelectedFilter:(FUBaseModel *)param{
    _seletedParam = param;
    id <FUCharacteristicProtocol> viewModel = (id<FUCharacteristicProtocol>)self.selectedView.viewModel;
    if ([viewModel respondsToSelector:@selector(isNeedSlider)] && self.selectedView.selectedIndex > 0) {
        self.beautySlider.value = [param.mValue floatValue] / param.ratio;
        self.beautySlider.hidden = ![viewModel isNeedSlider];
    } else {
        self.beautySlider.hidden = YES;
    }
    [self setSliderTyep:_seletedParam];
    /**
     * 这里使用抽象接口，有具体子类决定去哪个业务员模块处理数据
     */
    [self.selectedView.viewModel consumerWithData:_seletedParam viewModelBlock:nil];
}

-(void)beautyCollectionView:(FUBeautyView *)beautyView didSelectedParam:(FUBaseModel *)param{
    _seletedParam = param;
    self.beautySlider.value = [param.mValue floatValue] / param.ratio;
    self.beautySlider.hidden = NO;
    
    [self setSliderTyep:_seletedParam];
}


// 滑条滑动
- (IBAction)filterSliderValueChange:(FUSlider *)sender {
    _seletedParam.mValue = @(sender.value * _seletedParam.ratio);
    /**
     * 这里使用抽象接口，有具体子类决定去哪个业务员模块处理数据
     */
    [self.selectedView.viewModel consumerWithData:_seletedParam viewModelBlock:nil];
}


-(void)reloadSkinView:(FUBeautyViewModel *)viewModel{
    _skinView.viewModel = viewModel;
    _skinView.dataList = [(FUBeautyNodeModelProvider *)viewModel.provider skinProvider].dataSource;
    _skinView.selectedIndex = 0;
    FUBaseModel *model = nil;
    if (model) {
        model = _skinView.dataList[0];
    }
    if (model) {
        _beautySlider.hidden = NO;
        _beautySlider.value = [model.mValue floatValue];
    }
    [_skinView reloadData];
}

-(void)reloadShapView:(FUBeautyViewModel *)viewModel {
    _shapeView.viewModel = viewModel;
    _shapeView.dataList = [(FUBeautyNodeModelProvider *)viewModel.provider shapeProvider].dataSource;
    _shapeView.selectedIndex = 1;
    [_shapeView reloadData];
}

-(void)reloadFilterView:(FUBeautyViewModel *)viewModel {
    _beautyFilterView.viewModel = viewModel;
    _beautyFilterView.dataList = [(FUBeautyNodeModelProvider *)viewModel.provider filterProvider].dataSource;
    _beautyFilterView.selectedIndex = 1;
    [_beautyFilterView reloadData];
}

-(void)setDefaultFilter:(FUBaseModel *)filter{
    [self.beautyFilterView setDefaultFilter:filter];
}

-(BOOL)isTopViewShow {
    return !self.topView.hidden ;
}



@end
