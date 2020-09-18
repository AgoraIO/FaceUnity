//
//  CapturerManager.h
//  BeautifyExample
//
//  Created by LSQ on 2020/8/3.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>
#import <AGMCapturer/AGMCapturer.h>
#import <AGMBase/AGMBase.h>
#import "CapturerManagerDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface CapturerManager : NSObject <AgoraVideoSourceProtocol>

- (instancetype)initWithVideoConfig:(AGMCapturerVideoConfig *)config delegate:(id <CapturerManagerDelegate>)delegate;
- (void)startCapture;
- (void)stopCapture;
- (void)switchCamera;

@end

NS_ASSUME_NONNULL_END
