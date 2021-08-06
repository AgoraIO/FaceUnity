//
//  FUBeautyBodyNodeModelProvider.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyBodyNodeModelProvider.h"
#import "FUBaseModel.h"
#import "FUBodyBeautyDefine.h"
#import "FUManager.h"

@implementation FUBeautyBodyNodeModelProvider
@synthesize dataSource = _dataSource;
- (id)dataSource {
    if (!_dataSource) {
        _dataSource = [self producerDataSource];
    }
    return _dataSource;
}

- (NSArray *)producerDataSource {
    NSMutableArray *source = [NSMutableArray array];
    
    if ([FUManager shareManager].bodyParams && [FUManager shareManager].bodyParams.count > 0) {
        source = [FUManager shareManager].bodyParams;
    } else {
        NSArray *prams = @[@"BodySlimStrength",@"LegSlimStrength",@"WaistSlimStrength",@"ShoulderSlimStrength",@"HipSlimStrength",@"HeadSlim",@"LegSlim"];
        NSDictionary *titleDic = @{@"BodySlimStrength":@"瘦身",@"LegSlimStrength":@"长腿",@"WaistSlimStrength":@"细腰",@"ShoulderSlimStrength":@"美肩",@"HipSlimStrength":@"美臀",@"HeadSlim":@"小头",@"LegSlim":@"瘦腿"
        };
        NSDictionary *defaultValueDic = @{@"BodySlimStrength":@(0),@"LegSlimStrength":@(0),@"WaistSlimStrength":@(0),@"ShoulderSlimStrength":@(0.5),@"HipSlimStrength":@(0),@"HeadSlim":@(0),@"LegSlim":@(0)
        };
        
        for (NSUInteger i = 0; i < BODYBEAUTYTYPEMax; i ++) {
            NSString *str = [prams objectAtIndex:i];
            BOOL isStyle101 = NO;
            if ([str isEqualToString:@"ShoulderSlimStrength"]) {
                isStyle101 = YES;
            }
            
            FUBaseModel *model = [[FUBaseModel alloc] init];
            model.mTitle = [titleDic valueForKey:str];
            model.imageName = [titleDic valueForKey:str];
            model.mValue = [defaultValueDic valueForKey:str];
            model.defaultValue = model.mValue;
            model.iSStyle101 = isStyle101;
            model.indexPath = [NSIndexPath indexPathForRow:i inSection:FUDataTypebody];
            [source addObject:model];
        }
    }
    return [NSArray arrayWithArray:source];
}

- (void)cacheData {
    //标准未定，需要缓存再打开注释
//    [FUManager shareManager].bodyParams = [NSMutableArray arrayWithArray:self.dataSource];
}

- (NSString *)tipsStr {
    return NSLocalizedString(@"未检测到人体", nil);
}
@end
