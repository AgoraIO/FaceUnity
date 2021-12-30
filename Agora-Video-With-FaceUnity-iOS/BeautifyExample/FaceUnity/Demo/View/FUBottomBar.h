//
//  FUBottomBar.h
//  FUDemo
//
//  Created by 项林平 on 2021/6/11.
//  底部功能选择栏
//

#import <UIKit/UIKit.h>

#import "FUViewModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FUBottomBar : UICollectionView

/// 初始化方法
/// @param frame Frame
/// @param viewModels 数据源
/// @param handler 选中/取消选中回调
- (instancetype)initWithFrame:(CGRect)frame viewModels:(NSArray *)viewModels moduleOperationHandler:(void (^)(NSInteger item))handler;

@end

@interface FUBottomCell : UICollectionViewCell

@property (nonatomic, copy) NSString *name;

@end

NS_ASSUME_NONNULL_END
