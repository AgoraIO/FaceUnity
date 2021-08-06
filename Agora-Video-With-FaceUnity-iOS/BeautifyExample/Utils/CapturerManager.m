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
@property (nonatomic, strong) AGMCapturerVideoConfig *videoConfig;
@property (nonatomic, weak) id<CapturerManagerDelegate> delegate;
@end

@implementation CapturerManager
@synthesize consumer;

- (void)initCapturer {
    self.cameraCapturer = [[AGMCameraCapturer alloc] initWithConfig:self.videoConfig];
    self.cameraCapturer.delegate = self;
}

- (void)didOutputPixelBuffer:(CVPixelBufferRef)pixelBuffer frameTime:(CMTime)time {
    if ([self.delegate respondsToSelector:@selector(processFrame:)]) {
        CVPixelBufferRef outputPixelBuffer = [self.delegate processFrame:pixelBuffer];
        if (!outputPixelBuffer) return;
        [self.consumer consumePixelBuffer:outputPixelBuffer withTimestamp:time rotation:AgoraVideoRotationNone];
    }
}

- (void)didOutputVideoFrame:(id<AGMVideoFrame>)frame {
    if ([frame isKindOfClass:AGMCVPixelBuffer.class]) {
        if ([self.delegate respondsToSelector:@selector(processFrame:)]) {
            AGMCVPixelBuffer *agmPixelBuffer = frame;
            CVPixelBufferRef outputPixelBuffer = [self.delegate processFrame:agmPixelBuffer.pixelBuffer];
            if (!outputPixelBuffer) return;
            [self.consumer consumePixelBuffer:outputPixelBuffer withTimestamp:CMTimeMake(CACurrentMediaTime() * 1000, 1000) rotation:AgoraVideoRotationNone];
            if (self.videoView) {
                AGMCVPixelBuffer *newPixelBuffer = [[AGMCVPixelBuffer alloc] initWithPixelBuffer:outputPixelBuffer];
                [newPixelBuffer setParamWithWidth:agmPixelBuffer.width height:agmPixelBuffer.height rotation:agmPixelBuffer.rotation timeStampMs:agmPixelBuffer.timeStampMs];
                [self.videoView renderFrame:newPixelBuffer];
            }
        }
    } else if ([frame isKindOfClass:AGMNV12Texture.class]) {
        AGMNV12Texture *nv12Texture = frame;
        CVPixelBufferRef outputPixelBuffer = [self.delegate processFrame:nv12Texture.pixelBuffer];
        if (!outputPixelBuffer) return;
        [self.consumer consumePixelBuffer:outputPixelBuffer withTimestamp:CMTimeMake(1, (int32_t)frame.timeStampMs) rotation:AgoraVideoRotationNone];
        if (self.videoView) {
            AGMNV12Texture *newTexture = [[AGMNV12Texture alloc] init];
            [newTexture uploadPixelBufferToTextures:outputPixelBuffer];
            [newTexture setParamWithWidth:nv12Texture.width height:nv12Texture.height rotation:nv12Texture.rotation timeStampMs:nv12Texture.timeStampMs];
            [self.videoView renderFrame:nv12Texture];
        }
    }
}

#pragma mark Public
- (instancetype)initWithVideoConfig:(AGMCapturerVideoConfig *)config delegate:(id <CapturerManagerDelegate>)delegate {
    self = [super init];
    if (self) {
        self.videoConfig = config;
        self.delegate = delegate;
        [self initCapturer];
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


- (AgoraVideoCaptureType)captureType {
    return AgoraVideoCaptureTypeCamera;
}



@end
