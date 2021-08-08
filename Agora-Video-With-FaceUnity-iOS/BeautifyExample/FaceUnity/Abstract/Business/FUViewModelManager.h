//
//  FUViewModelManager.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/26.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FUModuleDefine.h"

@class FUBaseViewModel;
NS_ASSUME_NONNULL_BEGIN
/**
 * 管理所有的viewModel 调度器
 */
@interface FUViewModelManager : NSObject

//记录当前选中的viewModel: 最新添加到 addToRenderLoop 为最近的viewModel
@property (nonatomic, assign, readonly) FUDataType curType;

//当前选中的viewMode
@property (nonatomic, assign, readonly) FUBaseViewModel *selectedViewModel;

- (void)addToRenderLoop:(FUBaseViewModel *)viewModel;
- (void)removeFromRenderLoop:(FUDataType)type;

- (void)startRender:(FUDataType)type;
- (void)stopRender:(FUDataType)type;

- (void)resetMaxFacesNumber:(FUDataType)type;

//移除所有道具效果
- (void)removeAllViewModel;

- (NSArray *)allViewModels;
@end

NS_ASSUME_NONNULL_END
