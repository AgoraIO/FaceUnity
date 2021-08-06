//
//  FUBaseConsumer.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FUBaseNodeModelProvider.h"

#import "FUCharacteristicProtocol.h"
#import "FUModuleDefine.h"


typedef void(^ViewModelBlock)(id _Nullable param);

NS_ASSUME_NONNULL_BEGIN

@interface FUBaseViewModel : NSObject <FUCharacteristicProtocol>
+ (instancetype)instanceViewModel;

//根据不同子类加载不同的provider
@property (nonatomic, strong) FUBaseNodeModelProvider *provider;

- (void)consumerWithData:(id)model viewModelBlock:(ViewModelBlock _Nullable)ViewModelBlock;

//加载到FURenderKit 渲染
- (void)addToRenderLoop;

//从渲染循环移除
- (void)removeFromRenderLoop;

//开始生效
- (void)startRender;
//不生效
- (void)stopRender;

/// 设置最大人脸数
- (void)resetMaxFacesNumber;

@property (nonatomic, assign) FUDataType type;

//是否关闭slider
@property (nonatomic, assign) BOOL sliderHidden;


//控制switch 开关属性
@property (nonatomic, assign) BOOL switchIsOn;
@end

NS_ASSUME_NONNULL_END
