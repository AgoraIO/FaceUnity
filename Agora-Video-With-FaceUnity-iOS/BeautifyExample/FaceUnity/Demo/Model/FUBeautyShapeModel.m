//
//  FUBeautyShapeModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/7/19.
//

#import "FUBeautyShapeModel.h"

@implementation FUBeautyShapeModel

@synthesize moduleData = _moduleData;

#pragma mark - Override properties
- (FUModuleType)type {
    return FUModuleTypeBeautyShape;
}

- (NSString *)name {
    return FULocalizedString(@"美型");
}

- (NSArray<FUSubModel *> *)moduleData {
    if (!_moduleData) {
        NSMutableArray *models = [[NSMutableArray alloc] init];
        for (NSInteger i = 0; i < FUBeautyShapeItemMax; i++) {
            FUSubModel *model = [[FUSubModel alloc] init];
            model.functionType = i;
            switch (i) {
                case FUBeautyShapeItemCheekThinning:{
                    model.title = FULocalizedString(@"瘦脸");
                    model.imageName = @"瘦脸";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemCheekV:{
                    model.title = FULocalizedString(@"v脸");
                    model.imageName = @"v脸";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemCheekNarrow:{
                    model.title = FULocalizedString(@"窄脸");
                    model.imageName = @"窄脸";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemCheekShort:{
                    model.title = FULocalizedString(@"短脸");
                    model.imageName = @"短脸";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemCheekSmall:{
                    model.title = FULocalizedString(@"小脸");
                    model.imageName = @"小脸";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemCheekBones:{
                    model.title = FULocalizedString(@"瘦颧骨");
                    model.imageName = @"瘦颧骨";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemLowerJaw:{
                    model.title = FULocalizedString(@"瘦下颌骨");
                    model.imageName = @"瘦下颌骨";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemEyeEnlarging:{
                    model.title = FULocalizedString(@"大眼");
                    model.imageName = @"大眼";
                    model.currentValue = 0.4;
                    model.defaultValue = 0.4;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemEyeCircle:{
                    model.title = FULocalizedString(@"圆眼");
                    model.imageName = @"圆眼";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemChin:{
                    model.title = FULocalizedString(@"下巴");
                    model.imageName = @"下巴";
                    model.currentValue = 0.3;
                    model.defaultValue = 0.3;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemForehead:{
                    model.title = FULocalizedString(@"额头");
                    model.imageName = @"额头";
                    model.currentValue = 0.3;
                    model.defaultValue = 0.3;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemNose:{
                    model.title = FULocalizedString(@"瘦鼻");
                    model.imageName = @"瘦鼻";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemMouth:{
                    model.title = FULocalizedString(@"嘴型");
                    model.imageName = @"嘴型";
                    model.currentValue = 0.4;
                    model.defaultValue = 0.4;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemCanthus:{
                    model.title = FULocalizedString(@"开眼角");
                    model.imageName = @"开眼角";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyShapeItemEyeSpace:{
                    model.title = FULocalizedString(@"眼距");
                    model.imageName = @"眼距";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemEyeRotate:{
                    model.title = FULocalizedString(@"眼睛角度");
                    model.imageName = @"眼睛角度";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemLongNose:{
                    model.title = FULocalizedString(@"长鼻");
                    model.imageName = @"长鼻";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemPhiltrum:{
                    model.title = FULocalizedString(@"缩人中");
                    model.imageName = @"缩人中";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.ratio = 1.0;
                    model.isBidirection = YES;
                }
                    break;
                case FUBeautyShapeItemSmile:{
                    model.title = FULocalizedString(@"微笑嘴角");
                    model.imageName = @"微笑嘴角";
                    model.currentValue = 0;
                    model.defaultValue = 0;
                    model.ratio = 1.0;
                }
                    break;
            }
            [models addObject:model];
        }
        _moduleData = [models copy];
    }
    return _moduleData;
}

@end
