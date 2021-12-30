//
//  FUModel.m
//  FUDemo
//
//  Created by 项林平 on 2021/6/15.
//

#import "FUModel.h"

@implementation FUSubModel

- (instancetype)init {
    self = [super init];
    if (self) {
        _isBidirection = NO;
        _ratio = 1.0;
    }
    return self;
}

@end

@implementation FUModel

- (NSString *)tip {
    return @"未检测到人脸";
}

@end


