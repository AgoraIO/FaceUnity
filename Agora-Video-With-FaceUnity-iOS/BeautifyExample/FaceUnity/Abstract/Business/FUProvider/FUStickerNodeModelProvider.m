//
//  FUStickerProducer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUStickerNodeModelProvider.h"
#import "FUBaseModel.h"
//#import "FUManager.h"

@implementation FUStickerNodeModelProvider
@synthesize dataSource = _dataSource;
- (id)dataSource {
    if (!_dataSource) {
        _dataSource = [self producerDataSource];
    }
    return _dataSource;
}

//同步状态，本地数据组装
- (NSArray *)producerDataSource {
    NSArray *prams = @[@"makeup_noitem",@"sdlu",@"fashi"];//,@"chri1"
    NSMutableArray *source = [NSMutableArray arrayWithCapacity:prams.count];
    for (NSUInteger i = 0; i < prams.count; i ++) {
        NSString *str = [prams objectAtIndex:i];
        FUBaseModel *model = [[FUBaseModel alloc] init];
        model.imageName = str;
        model.indexPath = [NSIndexPath indexPathForRow:i inSection:FUDataTypeSticker];
        [source addObject:model];
    }
    return [NSArray arrayWithArray:source];
}
@end
