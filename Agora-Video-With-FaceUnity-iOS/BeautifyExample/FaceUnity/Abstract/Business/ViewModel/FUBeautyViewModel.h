//
//  FUBeautyViewModel.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/27.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBaseViewModel.h"
#import "FUBeautyNodeModelProvider.h"

#import "FUBeautyFilterViewModel.h"
#import "FUBeautyShapeViewModel.h"
#import "FUBeautySkinViewModel.h"
#import "FUBeautyDefine.h"

NS_ASSUME_NONNULL_BEGIN
/**
 * 美颜 包含 美体，美肤，滤镜，所以在业务形态上需要有一个美颜， 对外也是操作美颜这个整体
 */
@interface FUBeautyViewModel : FUBaseViewModel

@property (nonatomic, assign) FUBeautySubDefine beautySubType;
@end

NS_ASSUME_NONNULL_END
