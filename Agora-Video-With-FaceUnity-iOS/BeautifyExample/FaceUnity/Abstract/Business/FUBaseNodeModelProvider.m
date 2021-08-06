//
//  FUBaseProducer.m
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import "FUBaseNodeModelProvider.h"

@implementation FUBaseNodeModelProvider
+ (instancetype)instanceProducer {
    FUBaseNodeModelProvider *producer = [[[self class] alloc] init];
    
    return producer;
}

- (void)cacheData {

}

- (NSString *)tipsStr {
    return NSLocalizedString(@"未检测到人脸", nil);
}
@end
