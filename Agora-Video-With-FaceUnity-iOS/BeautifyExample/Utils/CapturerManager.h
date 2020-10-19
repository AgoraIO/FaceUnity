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
#import <AGMRenderer/AGMRenderer.h>

NS_ASSUME_NONNULL_BEGIN

@interface CapturerManager : NSObject <AgoraVideoSourceProtocol, AGMVideoCameraDelegate>

- (instancetype)initWithVideoConfig:(AGMCapturerVideoConfig *)config delegate:(id <CapturerManagerDelegate>)delegate;
- (void)startCapture;
- (void)stopCapture;
- (void)switchCamera;

@property (nonatomic, strong) AGMEAGLVideoView *videoView;

@end

NS_ASSUME_NONNULL_END
