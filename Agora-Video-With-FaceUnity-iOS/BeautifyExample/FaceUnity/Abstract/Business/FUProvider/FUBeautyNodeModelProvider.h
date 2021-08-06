//
//  FUBeautyNodeModelProvider.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/27.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBaseNodeModelProvider.h"
#import "FUBeautySkinNodeModelProvider.h"
#import "FUBeautyFilterNodeModelProvider.h"
#import "FUBeautyShapeNodeModelProvider.h"
NS_ASSUME_NONNULL_BEGIN
/**
 * 美颜 包含 美体，美肤，滤镜，所以在业务形态上需要有一个美颜， 对外也是操作美颜这个整体
 */
@interface FUBeautyNodeModelProvider : FUBaseNodeModelProvider
@property (nonatomic, strong, readonly) FUBeautySkinNodeModelProvider *skinProvider;
@property (nonatomic, strong, readonly) FUBeautyFilterNodeModelProvider *filterProvider;
@property (nonatomic, strong, readonly) FUBeautyShapeNodeModelProvider *shapeProvider;

@end

NS_ASSUME_NONNULL_END
