//
//  FUBeautyBodyModel.m
//  XPYCamera
//
//  Created by 项林平 on 2021/7/19.
//

#import "FUBeautyBodyModel.h"

@implementation FUBeautyBodyModel

@synthesize moduleData = _moduleData;

- (FUModuleType)type {
    return FUModuleTypeBeautyBody;
}

- (NSString *)name {
    return FULocalizedString(@"美体");
}

- (NSString *)tip {
    return @"未检测到人体";
}

- (NSArray<FUSubModel *> *)moduleData {
    if (!_moduleData) {
        NSMutableArray *models = [[NSMutableArray alloc] init];
        for (NSInteger i = 0; i < FUBeautyBodyItemMax; i++) {
            FUSubModel *model = [[FUSubModel alloc] init];
            model.functionType = i;
            switch (i) {
                case FUBeautyBodyItemSlim:{
                    model.title = FULocalizedString(@"瘦身");
                    model.imageName = @"瘦身";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyBodyItemLongLeg:{
                    model.title = FULocalizedString(@"长腿");
                    model.imageName = @"长腿";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyBodyItemThinWaist:{
                    model.title = FULocalizedString(@"细腰");
                    model.imageName = @"细腰";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyBodyItemBeautyShoulder:{
                    model.title = FULocalizedString(@"美肩");
                    model.imageName = @"美肩";
                    model.currentValue = 0.5;
                    model.defaultValue = 0.5;
                    model.isBidirection = YES;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyBodyItemBeautyButtock:{
                    model.title = FULocalizedString(@"美臀");
                    model.imageName = @"美臀";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyBodyItemSmallHead:{
                    model.title = FULocalizedString(@"小头");
                    model.imageName = @"小头";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautyBodyItemThinLeg:{
                    model.title = FULocalizedString(@"瘦腿");
                    model.imageName = @"瘦腿";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
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
