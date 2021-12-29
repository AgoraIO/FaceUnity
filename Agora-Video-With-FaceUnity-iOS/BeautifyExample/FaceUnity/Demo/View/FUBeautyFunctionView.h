//
//  FUBeautyFunctionView.h
//  FUDemo
//
//  Created by 项林平 on 2021/6/16.
//  美肤、美型、美体类型功能视图
//

#import <UIKit/UIKit.h>

#import "FUFunctionView.h"

@class FUSubModel;

NS_ASSUME_NONNULL_BEGIN

@interface FUBeautyFunctionView : FUFunctionView

@end

@interface FUBeautyFunctionCell : FUFunctionCell

@property (nonatomic, strong) FUSubModel *subModel;

@end

NS_ASSUME_NONNULL_END
