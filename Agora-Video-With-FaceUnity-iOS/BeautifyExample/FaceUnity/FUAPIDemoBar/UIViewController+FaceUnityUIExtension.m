//
//  UIViewController+FaceUnityUIExtension.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/30.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "UIViewController+FaceUnityUIExtension.h"
#import "FUModuleDefine.h"
#import "FUBaseViewModel.h"
#import "FUModuleDefine.h"
#import <FURenderKit/FUAIKit.h>
#import <objc/runtime.h>

static NSString *switchKey;
static NSString *tipsLabelKey;
static NSString *fuApiDemoBarKey;

@interface UIViewController ()


@end

@implementation UIViewController (FaceUnityUIExtension)
/// faceunity
- (void)setupFaceUnity {

    [FUManager shareManager].flipx = YES;
    [FUManager shareManager].isRender = YES;
    
    [self.view addSubview:self.demoBar];
    [self.demoBar mas_makeConstraints:^(MASConstraintMaker *make) {
        if (@available(iOS 11.0, *)) {
            make.left.mas_equalTo(self.view.mas_safeAreaLayoutGuideLeft);
            make.right.mas_equalTo(self.view.mas_safeAreaLayoutGuideRight);
            make.bottom.mas_equalTo(self.view.mas_safeAreaLayoutGuideBottom);
        
        } else {
        
            make.left.right.bottom.mas_equalTo(0);
        }
        make.height.mas_equalTo(195);
        
    }];
    
    [self.view addSubview:self.renderSwitch];
    [self.renderSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.view.mas_leading).mas_offset(15);
        make.bottom.equalTo(self.demoBar.mas_top).mas_offset(-8);
        make.size.mas_offset(CGSizeMake(44, 44));
    }];
}

- (UISwitch *)createSwitch {
    UISwitch *btn = [[UISwitch alloc] init];
    [btn addTarget:self action:@selector(renderSwitchAction:) forControlEvents:UIControlEventValueChanged];
    btn.hidden = YES;
    [btn setOn:YES];
    return btn;
}

- (UILabel *)createTipsLabel {
    /* 未检测到人脸提示 */
    UILabel *label = [[UILabel alloc] init];
    label.textColor = [UIColor whiteColor];
    label.font = [UIFont systemFontOfSize:17];
    label.textAlignment = NSTextAlignmentCenter;
    label.text = NSLocalizedString(@"No_Face_Tracking", @"未检测到人脸");
    label.hidden = YES;
    /* 未检测到人脸提示 */
    [self.view addSubview:label];
    [label mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self.view);
        make.width.mas_equalTo(140);
        make.height.mas_equalTo(22);
    }];
    return label;
}

#pragma mark 用于检测是否有ai人脸和人形
- (void)checkAI {
    dispatch_async(dispatch_get_main_queue(), ^{
        FUBaseViewModel *viewModel = [FUManager shareManager].viewModelManager.selectedViewModel;
        if (viewModel) {
            self.noTrackLabel.text = viewModel.provider.tipsStr;
            if (viewModel.type != FUDataTypebody) {
                int facenums = [FUAIKit shareKit].trackedFacesCount;
                if (facenums > 0) {
                    self.noTrackLabel.hidden = YES;
                } else {
                    self.noTrackLabel.hidden = NO;
                }
            } else {
                int bodyNums = [FUAIKit aiHumanProcessorNums];
                if (bodyNums > 0) {
                    self.noTrackLabel.hidden = YES;
                } else {
                    self.noTrackLabel.hidden = NO;
                }
            }
        }
    });
}


-(void)bottomDidChangeViewModel:(FUBaseViewModel *)viewModel {
    if (viewModel.type == FUDataTypeBeauty || viewModel.type == FUDataTypebody) {
        self.renderSwitch.hidden = NO;
    } else {
        self.renderSwitch.hidden = YES;
    }
    [self.renderSwitch setOn:viewModel.switchIsOn];

    [[FUManager shareManager].viewModelManager addToRenderLoop:viewModel];
    // 设置人脸数
    [[FUManager shareManager].viewModelManager resetMaxFacesNumber:viewModel.type];
}

- (void)showTopView:(BOOL)shown {
    FUDataType type = [FUManager shareManager].viewModelManager.curType;
    if ((type == FUDataTypeBeauty || type == FUDataTypebody) && shown) {
        self.renderSwitch.hidden = NO;
    } else {
        self.renderSwitch.hidden = YES;
    }
}



//action
- (void)renderSwitchAction:(UISwitch *)btn {
    FUDataType type = [FUManager shareManager].viewModelManager.curType;
    
    FUBaseViewModel *curViewModel = [FUManager shareManager].viewModelManager.selectedViewModel;
    curViewModel.switchIsOn = btn.isOn;
    if (btn.isOn) {
        [[FUManager shareManager].viewModelManager startRender:type];
    } else {
        //美颜有效
        [[FUManager shareManager].viewModelManager stopRender:type];
    }
}



#pragma mark - Set/Get
- (void)setRenderSwitch:(UISwitch *)renderSwitch {
    objc_setAssociatedObject(self,  &switchKey, renderSwitch, OBJC_ASSOCIATION_RETAIN);
}

- (UISwitch *)renderSwitch {
    UISwitch *btn = objc_getAssociatedObject(self, &switchKey);
    if (!btn) {
        btn = [self createSwitch];
        [self setRenderSwitch:btn];
    }
    return objc_getAssociatedObject(self, &switchKey);
}


- (void)setNoTrackLabel:(UILabel *)noTrackLabel {
    objc_setAssociatedObject(self,  &tipsLabelKey, noTrackLabel, OBJC_ASSOCIATION_RETAIN);
}

- (UILabel *)noTrackLabel {
    UILabel *label = objc_getAssociatedObject(self, &tipsLabelKey);
    if (!label) {
        label = [self createTipsLabel];
        [self setNoTrackLabel:label];;
    }
    return objc_getAssociatedObject(self, &tipsLabelKey);
}

- (void)setDemoBar:(FUAPIDemoBar *)demoBar {
    objc_setAssociatedObject(self,  &fuApiDemoBarKey, demoBar, OBJC_ASSOCIATION_RETAIN);
}


- (FUAPIDemoBar *)demoBar {
    FUAPIDemoBar *view = objc_getAssociatedObject(self, &fuApiDemoBarKey);
    if (!view) {
        view = [[FUAPIDemoBar alloc] init];
        view.mDelegate = self;
        [self setDemoBar:view];
    }
    return view;
}

@end
