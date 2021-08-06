//
//  FUBaseProducer.h
//  BeautifyExample
//
//  Created by Chen on 2021/4/25.
//  Copyright © 2021 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
NS_ASSUME_NONNULL_BEGIN
@interface FUBaseNodeModelProvider : NSObject
+ (instancetype)instanceProducer;


@property (nonatomic, strong) id dataSource;

//缓存数据接口
- (void)cacheData;

//网络数据组装, 有需求增加按照参数增加接口


//提示语
@property (nonatomic, strong) NSString *tipsStr;
@end

NS_ASSUME_NONNULL_END
