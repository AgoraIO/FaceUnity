//
//  CapturerManager.m
//  BeautifyExample
//
//  Created by LSQ on 2020/8/3.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "CapturerManager.h"

#define CM_DEGREES_TO_RADIANS(x) (x * M_PI/180.0)

@interface CapturerManager ()
@property (nonatomic, strong) AGMCameraCapturer *cameraCapturer;
@property (nonatomic, strong) AGMVideoAdapterFilter *videoAdapterFilter;
@property (nonatomic, strong) AGMCapturerVideoConfig *videoConfig;
@property (nonatomic, weak) id<CapturerManagerDelegate> delegate;
@end

@implementation CapturerManager
@synthesize consumer;

- (void)initCapturer {
    self.cameraCapturer = [[AGMCameraCapturer alloc] initWithConfig:self.videoConfig];
    self.videoAdapterFilter = [[AGMVideoAdapterFilter alloc] init];
    self.videoAdapterFilter.ignoreAspectRatio = YES;
    [self.cameraCapturer addVideoSink:self.videoAdapterFilter];
    __weak typeof(self) weakSelf = self;
    [self.videoAdapterFilter setFrameProcessingCompletionBlock:^(AGMVideoSource * _Nonnull videoSource, CMTime time) {
        CVPixelBufferRef pixelBuffer = videoSource.framebufferForOutput.pixelBuffer;
        [weakSelf didOutputPixelBuffer:pixelBuffer frameTime:time];
    }];
}

- (void)didOutputPixelBuffer:(CVPixelBufferRef)pixelBuffer frameTime:(CMTime)time {
    if ([self.delegate respondsToSelector:@selector(processFrame:)]) {
        CVPixelBufferRef outputPixelBuffer = [self.delegate processFrame:pixelBuffer];
        if (!outputPixelBuffer) return;
        [self.consumer consumePixelBuffer:outputPixelBuffer withTimestamp:time rotation:AgoraVideoRotationNone];
    }
}

- (void)applicationDidChangeStatusBar {
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    [self updateOrientation:orientation];
}

- (void)updateOrientation:(UIInterfaceOrientation)orientation {
    CGAffineTransform rotation = CGAffineTransformMakeRotation(CM_DEGREES_TO_RADIANS(90));
    switch (orientation) {
        case UIInterfaceOrientationPortrait:
            rotation = CGAffineTransformMakeRotation(CM_DEGREES_TO_RADIANS(90));
            break;
        case UIInterfaceOrientationPortraitUpsideDown:
            break;
        case UIInterfaceOrientationLandscapeLeft:
            rotation = CGAffineTransformMakeRotation(CM_DEGREES_TO_RADIANS(180));
            break;
        case UIInterfaceOrientationLandscapeRight:
            rotation = CGAffineTransformMakeRotation(CM_DEGREES_TO_RADIANS(0));
            break;

        default:
            break;
    }
    self.videoAdapterFilter.affineTransform = rotation;
}

#pragma mark Public
- (instancetype)initWithVideoConfig:(AGMCapturerVideoConfig *)config delegate:(id <CapturerManagerDelegate>)delegate {
    self = [super init];
    if (self) {
        self.videoConfig = config;
        self.delegate = delegate;
        [self initCapturer];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidChangeStatusBar) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
        dispatch_async(dispatch_get_main_queue(), ^{
           UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
            [self updateOrientation:orientation];
        });
    }
    return self;
}

- (void)startCapture {
    [self.cameraCapturer start];
}

- (void)stopCapture {
    [self.cameraCapturer stop];
}

- (void)switchCamera {
    [self.cameraCapturer switchCamera];
}

#pragma mark - AgoraVideoSourceProtocol
- (AgoraVideoBufferType)bufferType {
    return AgoraVideoBufferTypePixelBuffer;
}

- (void)shouldDispose {
    
}

- (BOOL)shouldInitialize {
    return YES;
}

- (void)shouldStart {
    
}

- (void)shouldStop {
    
}


@end
