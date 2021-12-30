//
//  FUTestRecorder.h
//  FUTester
//
//  Created by 刘洋 on 2017/10/26.
//  Copyright © 2017年 刘洋. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface FUTestRecorder : NSObject


+ (FUTestRecorder *)shareRecorder;

-(void)processFrameWithLog;

- (void)setupRecord;

@end
