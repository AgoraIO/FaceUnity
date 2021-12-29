//
//  FUFilterModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/7/19.
//

#import "FUFilterModel.h"

@implementation FUFilterModel

@synthesize moduleData = _moduleData;

#pragma mark - Override properties
- (FUModuleType)type {
    return FUModuleTypeFilter;
}

- (NSString *)name {
    return FULocalizedString(@"滤镜");
}

- (NSArray<FUSubModel *> *)moduleData {
    if (!_moduleData) {
        NSArray *params = @[
            @{@"origin" : @"原图"},
            @{@"ziran1" : @"自然1"},
            @{@"zhiganhui1" : @"质感灰1"},
            @{@"bailiang1" : @"白亮1"},
            @{@"fennen1" : @"粉嫩1"},
            @{@"lengsediao1" : @"冷色调1"}
        ];
        NSMutableArray *models = [[NSMutableArray alloc] init];
        for (NSDictionary *param in params) {
            FUSubModel *model = [[FUSubModel alloc] init];
            model.imageName = param.allKeys[0];
            model.title = param.allValues[0];
            model.defaultValue = 0.4;
            model.currentValue = 0.4;
            [models addObject:model];
        }
        _moduleData = [models copy];
    }
    return _moduleData;
}

@end
