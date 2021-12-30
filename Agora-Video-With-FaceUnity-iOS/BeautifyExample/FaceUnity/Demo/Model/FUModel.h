//
//  FUModel.h
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import <Foundation/Foundation.h>

#import "FUDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface FUSubModel : NSObject

/// 功能类型
@property (nonatomic, assign) NSUInteger functionType;

/// 名称
@property (nonatomic, copy) NSString *title;

/// 需要显示的icon名称
@property (nonatomic, strong) NSString *imageName;

/// 是否从中间双向滑动调节值大小，默认为NO
@property (nonatomic, assign) BOOL isBidirection;

/// 默认值
@property (nonatomic) double defaultValue;

/// 当前值（用于调整滤镜程度、美肤程度等）
@property (nonatomic) double currentValue;

/// slider 进度条显示比例
/// 原因是: 部分属性值取值范围并不是0 - 1.0， 所以进度条为了归一化必须进行倍率处理
/// 默认值1.0
@property (nonatomic) float ratio;

@end

@interface FUModel : NSObject

/// 功能模块分类
@property (nonatomic, assign) FUModuleType type;
/// 功能模块名称
@property (nonatomic, copy) NSString *name;
/// 检测提示
@property (nonatomic, copy) NSString *tip;
/// 功能模块数据
@property (nonatomic, copy) NSArray<FUSubModel *> *moduleData;

@end

NS_ASSUME_NONNULL_END
