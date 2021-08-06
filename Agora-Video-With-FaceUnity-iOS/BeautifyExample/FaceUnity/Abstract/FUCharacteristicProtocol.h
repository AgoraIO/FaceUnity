//
//  FUCharacteristicProtocol.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/26.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/**
 * 主要用于切面 差异性接口实现
 */
@protocol FUCharacteristicProtocol <NSObject>

@optional
/**
 * 判断是否都是默认值的接口，适用于带reset默认参数功能
 */
- (BOOL)isDefaultValue;

- (void)resetDefaultValue;


@optional
//是否需要显示slider进度条
- (BOOL)isNeedSlider;

//缓存数据
- (void)cacheData;
@end

NS_ASSUME_NONNULL_END
