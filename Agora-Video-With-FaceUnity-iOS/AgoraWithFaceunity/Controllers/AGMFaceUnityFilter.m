//
//  AGMFaceUnityFilter.m
//  AgoraWithFaceunity
//
//  Created by LSQ on 2019/12/3.
//  Copyright © 2019 Agora. All rights reserved.
//

#import "AGMFaceUnityFilter.h"
#import "FUManager.h"

@implementation AGMFaceUnityFilter

- (void)onFrame:(AGMVideoFrame *)videoFrame {
#pragma mark 写入滤镜处理
    AGMCVPixelBuffer *AGMPixelBuffer = videoFrame.buffer;
    
    if (_didCompletion) {
        self.didCompletion(AGMPixelBuffer.pixelBuffer, videoFrame.timeStamp, videoFrame.rotation);
    }
    
    if (self.allSinks) {
        for (id<AGMVideoSink> sink in self.allSinks) {
            [sink onFrame:videoFrame];
        }
    }
}

@end
