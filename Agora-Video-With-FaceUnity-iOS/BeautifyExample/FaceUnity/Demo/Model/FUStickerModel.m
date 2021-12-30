//
//  FUStickerModel.m
//  XPYCamera
//
//  Created by 项林平 on 2021/7/19.
//

#import "FUStickerModel.h"

@implementation FUStickerModel

@synthesize moduleData = _moduleData;

#pragma mark - Override properties
- (FUModuleType)type {
    return FUModuleTypeSticker;
}

- (NSString *)name {
    return FULocalizedString(@"贴纸");
}

- (NSArray<FUSubModel *> *)moduleData {
    if (!_moduleData) {
        NSArray *items = @[@"remove", @"sdlu", @"DaisyPig", @"fashi", @"xueqiu_lm_fu"];
        NSMutableArray *models = [[NSMutableArray alloc] init];
        for (NSString *item in items) {
            FUSubModel *model = [[FUSubModel alloc] init];
            model.imageName = item;
            [models addObject:model];
        }
        _moduleData = [models copy];
    }
    return _moduleData;
}

@end
