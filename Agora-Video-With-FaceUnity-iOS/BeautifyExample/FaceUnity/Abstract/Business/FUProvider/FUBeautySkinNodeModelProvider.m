//
//  FUBeautySkinProducer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautySkinNodeModelProvider.h"
#import "FUBeautyDefine.h"
#import "FUBaseModel.h"

#import "FUManager.h"


@implementation FUBeautySkinNodeModelProvider
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
    if ([FUManager shareManager].skinParams && [FUManager shareManager].skinParams.count > 0) {
        source = [FUManager shareManager].skinParams;
    } else {
        NSArray *prams = @[@"blur_level",@"color_level",@"red_level",@"sharpen",@"eye_bright",@"tooth_whiten",@"remove_pouch_strength",@"remove_nasolabial_folds_strength"];//
        NSDictionary *titelDic = @{@"blur_level":@"精细磨皮",@"color_level":@"美白",@"red_level":@"红润",@"sharpen":@"锐化",@"remove_pouch_strength":@"去黑眼圈",@"remove_nasolabial_folds_strength":@"去法令纹",@"eye_bright":@"亮眼",@"tooth_whiten":@"美牙"};
        
        NSDictionary *defaultValueDic = @{@"blur_level":@(4.2),@"color_level":@(0.3),@"red_level":@(0.3),@"sharpen":@(0.2),@"remove_pouch_strength":@(0),@"remove_nasolabial_folds_strength":@(0),@"eye_bright":@(0),@"tooth_whiten":@(0)};
        
        //slider 进度条显示比例 原因是: 部分属性值取值范围并不是0 - 1.0， 所以进度条为了归一化必须进行倍率处理
        float ratio[FUBeautifySkinMax] = {6.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        NSMutableArray *source = [NSMutableArray arrayWithCapacity:prams.count];
        for (NSUInteger i = 0; i < FUBeautifySkinMax; i ++) {
            NSString *str = [prams objectAtIndex:i];
            FUBaseModel *model = [[FUBaseModel alloc] init];
            model.mTitle = [titelDic valueForKey:str];
            model.imageName = [titelDic valueForKey:str];
            model.mValue = [defaultValueDic valueForKey:str];
            model.defaultValue = model.mValue;
            model.ratio = ratio[i];
            model.indexPath = [NSIndexPath indexPathForRow:i inSection:FUBeautyDefineSkin];
            [source addObject:model];
        }
        return [NSArray arrayWithArray: source];
    }
    
    return [NSArray arrayWithArray: source];
}


- (void)cacheData {
//    [FUManager shareManager].skinParams = [NSMutableArray arrayWithArray:self.dataSource];
}

@end
