//
//  UIViewController+FaceUnityUIExtension.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/30.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Masonry/Masonry.h>
#import "FUAPIDemoBar.h"
#import "FUManager.h"
NS_ASSUME_NONNULL_BEGIN
//相芯相关的UI和 第三方demo 的UI剥离开来。后续可以随意组合到其他demo
@interface UIViewController (FaceUnityUIExtension) <FUAPIDemoBarDelegate, FUManagerProtocol>

@property (nonatomic, strong) UISwitch *renderSwitch;
@property (strong, nonatomic) UILabel *noTrackLabel;
/**faceUI */
@property (nonatomic, strong) FUAPIDemoBar *demoBar;

//初始化相芯UI
- (void)setupFaceUnity;
@end

NS_ASSUME_NONNULL_END
