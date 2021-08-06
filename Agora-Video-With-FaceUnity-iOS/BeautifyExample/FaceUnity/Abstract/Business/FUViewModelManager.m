//
//  FUViewModelManager.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/26.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUViewModelManager.h"
#import "FUBaseViewModel.h"

@interface FUViewModelManager ()
@property (nonatomic, strong) NSMutableDictionary *dic;

@property (nonatomic, strong) dispatch_semaphore_t sema;

//记录当前选中的viewModel: 最新添加到 addToRenderLoop 为最近的viewModel
@property (nonatomic, assign) FUDataType curType;

//当前选中的viewModel
@property (nonatomic, assign) FUBaseViewModel *selectedViewModel;
@end

@implementation FUViewModelManager
- (instancetype)init {
    self = [super init];
    if (self) {
        _dic = [NSMutableDictionary dictionary];
        _sema = dispatch_semaphore_create(1);
        
    }
    return self;
}

- (void)addToRenderLoop:(FUBaseViewModel *)viewModel {
    self.curType = viewModel.type;
    self.selectedViewModel = viewModel;
    if ([self.dic.allKeys containsObject:@(viewModel.type)]) {
        NSLog(@"viewModel: %@ exits",viewModel);
        return ;
    }
    
    // 防止首次切换时没有效果
    [viewModel resetMaxFacesNumber];
    
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        dispatch_semaphore_wait(self.sema, DISPATCH_TIME_FOREVER);
        [self.dic setObject:viewModel forKey:@(viewModel.type)];
        [viewModel addToRenderLoop];
        dispatch_semaphore_signal(self.sema);
    });
}

- (void)removeFromRenderLoop:(FUDataType)type {
    if (![self.dic.allKeys containsObject:@(type)]) {
        return ;
    }
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        dispatch_semaphore_wait(self.sema, DISPATCH_TIME_FOREVER);
        FUBaseViewModel *viewModel = [self.dic objectForKey:@(type)];
        [viewModel removeFromRenderLoop];
        dispatch_semaphore_signal(self.sema);
    });
}

- (void)startRender:(FUDataType)type {
    if (![self.dic.allKeys containsObject:@(type)]) {
        NSLog(@"viewModel has exits!");
        return ;
    }
    FUBaseViewModel *viewModel = [self.dic objectForKey:@(type)];
    [viewModel startRender];
}

- (void)stopRender:(FUDataType)type {
    if (![self.dic.allKeys containsObject:@(type)]) {
        NSLog(@"viewModel not exits!");
        return ;
    }
    FUBaseViewModel *viewModel = [self.dic objectForKey:@(type)];
    [viewModel stopRender];
}

- (void)resetMaxFacesNumber:(FUDataType)type {
    if (![self.dic.allKeys containsObject:@(type)]) {
        NSLog(@"viewModel not exits!");
        return ;
    }
    FUBaseViewModel *viewModel = [self.dic objectForKey:@(type)];
    [viewModel resetMaxFacesNumber];
}


//移除所有道具效果
- (void)removeAllViewModel {
    for (FUBaseViewModel *viewModel in self.dic.allValues) {
        [viewModel removeFromRenderLoop];
    }
    [self.dic removeAllObjects];
}


- (NSArray *)allViewModels {
    return self.dic.allValues;
}
@end
