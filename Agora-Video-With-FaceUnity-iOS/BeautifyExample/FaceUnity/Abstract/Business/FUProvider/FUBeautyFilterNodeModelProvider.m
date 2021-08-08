//
//  FUBeautyFilterProducer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyFilterNodeModelProvider.h"
#import "FUBeautyDefine.h"
#import "FUBaseModel.h"
#import "FUManager.h"

@implementation FUBeautyFilterNodeModelProvider
@synthesize dataSource = _dataSource;
- (id)dataSource {
    if (!_dataSource) {
        _dataSource = [self producerDataSource];
    }
    return _dataSource;
}
//同步状态，本地数据组装
- (NSArray *)producerDataSource {
    NSMutableArray *source = [NSMutableArray array];
    if ([FUManager shareManager].filters && [FUManager shareManager].filters.count > 0) {
        source = [FUManager shareManager].filters;
    } else {
        NSArray *beautyFiltersDataSource = @[@"origin",@"ziran1",@"zhiganhui1",@"bailiang1"
                                             ,@"fennen1",@"lengsediao1"];
        
        NSDictionary *filtersCHName = @{
            @"origin":@"原图",
            @"ziran1":@"自然1",
            @"zhiganhui1":@"质感灰1",
            @"bailiang1":@"白亮1",
            @"fennen1":@"粉嫩1",
            @"lengsediao1":@"冷色调1"
        };
        
        for (NSUInteger i = 0; i < beautyFiltersDataSource.count; i ++) {
            NSString *str = [beautyFiltersDataSource objectAtIndex:i];
            
            FUBaseModel *model = [[FUBaseModel alloc] init];
            model.mTitle = [filtersCHName valueForKey:str];
            model.imageName = str;
            model.mValue = @0.4;
            model.indexPath = [NSIndexPath indexPathForRow:i inSection:FUBeautyDefineFilter];
            [source addObject:model];
        }
    }
    return [NSArray arrayWithArray:source];
}

- (void)cacheData {
//    [FUManager shareManager].filters = [NSMutableArray arrayWithArray:self.dataSource];
}
@end
