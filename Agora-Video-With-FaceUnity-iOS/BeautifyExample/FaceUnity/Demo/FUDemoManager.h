//
//  FUDemoManager.h
//  FUDemo
//
//  Created by 项林平 on 2021/6/17.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "FUDefines.h"
#import "FUManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface FUDemoManager : NSObject

/// Initializer
/// @param controller 目标控制器
/// @param originY Demo视图在目标视图上的Y坐标（这里指的是底部功能选择栏的Y坐标，X坐标默认为0）
- (instancetype)initWithTargetController:(UIViewController *)controller originY:(CGFloat)originY;

@end

NS_ASSUME_NONNULL_END
