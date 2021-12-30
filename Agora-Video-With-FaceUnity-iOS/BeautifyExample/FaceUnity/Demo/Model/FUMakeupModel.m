//
//  FUMakeupModel.m
//  XPYCamera
//
//  Created by 项林平 on 2021/7/19.
//

#import "FUMakeupModel.h"

@implementation FUMakeupModel

@synthesize moduleData = _moduleData;

#pragma mark - Override properties
- (FUModuleType)type {
    return FUModuleTypeMakeup;
}

- (NSString *)name {
    return FULocalizedString(@"美妆");
}

- (NSArray<FUSubModel *> *)moduleData {
    if (!_moduleData) {
        NSArray *params = @[
            @{@"remove" : @"卸妆"},
            @{@"jianling" : @"减龄"},
            @{@"nuandong" : @"暖冬"},
            @{@"hongfeng" : @"红枫"},
            @{@"Rose" : @"玫瑰"},
            @{@"shaonv" : @"少女"},
            @{@"ziyun" : @"紫韵"},
            @{@"yanshimao" : @"厌世猫"},
            @{@"renyu" : @"人鱼"},
            @{@"chuqiu" : @"初秋"},
            @{@"qianzhihe" : @"千纸鹤"},
            @{@"chaomo" : @"超模"},
            @{@"chuju" : @"雏菊"},
            @{@"gangfeng" : @"港风"},
            @{@"xinggan" : @"性感"},
            @{@"tianmei" : @"甜美"},
            @{@"linjia" : @"邻家"},
            @{@"oumei" : @"欧美"},
            @{@"wumei" : @"妩媚"}
        ];
        NSMutableArray *models = [[NSMutableArray alloc] init];
        for (NSDictionary *param in params) {
            FUSubModel *model = [[FUSubModel alloc] init];
            model.imageName = param.allKeys[0];
            model.title = param.allValues[0];
            model.defaultValue = 0.7;
            model.currentValue = 0.7;
            [models addObject:model];
        }
        _moduleData = [models copy];
    }
    return _moduleData;
}

@end
