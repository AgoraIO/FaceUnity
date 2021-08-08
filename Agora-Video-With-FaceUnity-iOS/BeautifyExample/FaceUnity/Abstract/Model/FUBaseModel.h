//
//  FUBaseModel.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/NSIndexPath+UIKitAdditions.h>
#import "FUModuleDefine.h"

NS_ASSUME_NONNULL_BEGIN

@interface FUBaseModel : NSObject

@property (nonatomic, strong) NSString *mTitle;

//目前支持 NSNumber 和 NSString (滤镜业务用,加载bundle 名称的贴纸类道具用)
@property (nonatomic, strong) id mValue;

//图片用到的名称
@property (nonatomic, strong) NSString *imageName;

/* 双向的参数设置MinimumTrackTintColor 0.5是原始值*/
@property (nonatomic,assign) BOOL iSStyle101;

/* 默认值用于，设置默认和恢复 */
@property (nonatomic, strong) NSNumber *defaultValue;

/**
 * slider 进度条显示比例 原因是: 部分属性值取值范围并不是0 - 1.0， 所以进度条为了归一化必须进行倍率处理
 * 默认值1.0
 */
@property (nonatomic, assign) float ratio;

/**
 * seciton is  FUDataType
 * row is sub of FUDataType
 */
@property (nonatomic, strong) NSIndexPath *indexPath;
@end

NS_ASSUME_NONNULL_END
