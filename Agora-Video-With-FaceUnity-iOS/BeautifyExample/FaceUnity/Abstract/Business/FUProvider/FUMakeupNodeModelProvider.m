//
//  FUMakeUpProducer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUMakeupNodeModelProvider.h"
#import "FUBaseModel.h"

@implementation FUMakeupNodeModelProvider
@synthesize dataSource = _dataSource;
- (id)dataSource {
    if (!_dataSource) {
        _dataSource = [self producerDataSource];
    }
    return _dataSource;
}

- (NSArray *)producerDataSource {
    NSArray *prams = @[@"makeup_noitem",@"chaoA",@"dousha",@"naicha"];
    NSDictionary *titelDic = @{@"makeup_noitem":@"卸妆",@"naicha":@"奶茶",@"dousha":@"豆沙",@"chaoA":@"超A"};
     
    NSMutableArray *source = [NSMutableArray arrayWithCapacity:prams.count];
    for (NSUInteger i = 0; i < prams.count; i ++) {
        NSString *str = [prams objectAtIndex:i];
        FUBaseModel *model = [[FUBaseModel alloc] init];
        model.imageName = str;
        model.mTitle = [titelDic valueForKey:str];
        model.indexPath = [NSIndexPath indexPathForRow:i inSection:FUDataTypeMakeup];
        model.mValue = @0.7;
        [source addObject:model];
    }
    return [NSArray arrayWithArray:source];
}
@end
