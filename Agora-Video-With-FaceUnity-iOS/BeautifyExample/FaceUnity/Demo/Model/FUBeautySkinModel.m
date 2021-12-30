//
//  FUBeautySkinModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/7/19.
//

#import "FUBeautySkinModel.h"

@implementation FUBeautySkinModel

@synthesize moduleData = _moduleData;

#pragma mark - Override properties
- (FUModuleType)type {
    return FUModuleTypeBeautySkin;
}

- (NSString *)name {
    return FULocalizedString(@"美肤");
}

- (NSArray<FUSubModel *> *)moduleData {
    if (!_moduleData) {
        NSMutableArray *models = [[NSMutableArray alloc] init];
        for (NSInteger i = 0; i < FUBeautySkinItemMax; i++) {
            FUSubModel *model = [[FUSubModel alloc] init];
            model.functionType = i;
            switch (i) {
                case FUBeautySkinItemFineSmooth:{
                    model.title = FULocalizedString(@"精细磨皮");
                    model.imageName = @"精细磨皮";
                    model.currentValue = 4.2;
                    model.defaultValue = 4.2;
                    model.ratio = 6.0;
                }
                    break;
                case FUBeautySkinItemWhiten:{
                    model.title = FULocalizedString(@"美白");
                    model.imageName = @"美白";
                    model.currentValue = 0.3;
                    model.defaultValue = 0.3;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautySkinItemRuddy:{
                    model.title = FULocalizedString(@"红润");
                    model.imageName = @"红润";
                    model.currentValue = 0.3;
                    model.defaultValue = 0.3;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautySkinItemSharpen:{
                    model.title = FULocalizedString(@"锐化");
                    model.imageName = @"锐化";
                    model.currentValue = 0.2;
                    model.defaultValue = 0.2;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautySkinItemEyeBrighten:{
                    model.title = FULocalizedString(@"亮眼");
                    model.imageName = @"亮眼";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautySkinItemToothWhiten:{
                    model.title = FULocalizedString(@"美牙");
                    model.imageName = @"美牙";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautySkinItemCircles:{
                    model.title = FULocalizedString(@"去黑眼圈");
                    model.imageName = @"去黑眼圈";
                    model.currentValue = 0.0;
                    model.defaultValue = 0.0;
                    model.ratio = 1.0;
                }
                    break;
                case FUBeautySkinItemWrinkles:{
                    model.title = FULocalizedString(@"去法令纹");
                    model.imageName = @"去法令纹";
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
