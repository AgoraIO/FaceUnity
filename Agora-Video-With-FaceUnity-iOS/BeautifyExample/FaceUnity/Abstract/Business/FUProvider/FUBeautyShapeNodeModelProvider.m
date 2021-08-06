//
//  FUBeautyShapeProducer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBeautyShapeNodeModelProvider.h"
#import "FUBeautyDefine.h"
#import "FUBaseModel.h"
#import "FUManager.h"


@implementation FUBeautyShapeNodeModelProvider
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
    if ([FUManager shareManager].shapeParams && [FUManager shareManager].shapeParams.count > 0) {
        source = [FUManager shareManager].shapeParams;
    } else {
        NSArray *prams = @[@"cheek_thinning",@"cheek_v",@"cheek_narrow",@"cheek_small",@"intensity_cheekbones",@"intensity_lower_jaw",@"eye_enlarging", @"intensity_eye_circle",@"intensity_chin",@"intensity_forehead",@"intensity_nose",@"intensity_mouth",@"intensity_canthus",@"intensity_eye_space",@"intensity_eye_rotate",@"intensity_long_nose",@"intensity_philtrum",@"intensity_smile"];
        NSDictionary *titelDic = @{@"cheek_thinning":@"瘦脸",@"cheek_v":@"v脸",@"cheek_narrow":@"窄脸",@"cheek_small":@"小脸",@"intensity_cheekbones":@"瘦颧骨",@"intensity_lower_jaw":@"瘦下颌骨",@"eye_enlarging":@"大眼",@"intensity_eye_circle": @"圆眼",@"intensity_chin":@"下巴",
                                   @"intensity_forehead":@"额头",@"intensity_nose":@"瘦鼻",@"intensity_mouth":@"嘴型",@"intensity_canthus":@"开眼角",@"intensity_eye_space":@"眼距",@"intensity_eye_rotate":@"眼睛角度",@"intensity_long_nose":@"长鼻",@"intensity_philtrum":@"缩人中",@"intensity_smile":@"微笑嘴角"
        };
        NSDictionary *defaultValueDic = @{@"cheek_thinning":@(0),@"cheek_v":@(0.5),@"cheek_narrow":@(0),@"cheek_small":@(0),@"intensity_cheekbones":@(0),@"intensity_lower_jaw":@(0),@"eye_enlarging":@(0.4),@"intensity_eye_circle":@(0.0), @"intensity_chin":@(0.3),
                                          @"intensity_forehead":@(0.3),@"intensity_nose":@(0.5),@"intensity_mouth":@(0.4),@"intensity_canthus":@(0),@"intensity_eye_space":@(0.5),@"intensity_eye_rotate":@(0.5),@"intensity_long_nose":@(0.5),@"intensity_philtrum":@(0.5),@"intensity_smile":@(0)
        };
        
        //slider 进度条显示比例 原因是: 部分属性值取值范围并不是0 - 1.0， 所以进度条为了归一化必须进行倍率处理
        float ratio[FUBeautifyShapeMax] = {1.0, 1.0, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        for (NSUInteger i = 0; i < FUBeautifyShapeMax; i ++) {
            NSString *str = [prams objectAtIndex:i];
            BOOL isStyle101 = NO;
            if ([str isEqualToString:@"intensity_chin"] || [str isEqualToString:@"intensity_forehead"] || [str isEqualToString:@"intensity_mouth"] || [str isEqualToString:@"intensity_eye_space"] || [str isEqualToString:@"intensity_eye_rotate"] || [str isEqualToString:@"intensity_long_nose"] || [str isEqualToString:@"intensity_philtrum"]) {
                isStyle101 = YES;
            }
            
            FUBaseModel *model = [[FUBaseModel alloc] init];
            model.mTitle = [titelDic valueForKey:str];
            model.imageName = [titelDic valueForKey:str];
            model.mValue = [defaultValueDic valueForKey:str];
            model.defaultValue = model.mValue;
            model.iSStyle101 = isStyle101;
            model.ratio = ratio[i];
            model.indexPath = [NSIndexPath indexPathForRow:i inSection:FUBeautyDefineShape];
            [source addObject:model];
        }
    }
    return [NSArray arrayWithArray: source];
}

- (void)cacheData {
//    [FUManager shareManager].shapeParams = [NSMutableArray arrayWithArray:self.dataSource];
}
@end
