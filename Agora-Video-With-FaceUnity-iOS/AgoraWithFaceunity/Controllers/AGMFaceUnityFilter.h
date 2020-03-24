//
//  AGMFaceUnityFilter.h
//  AgoraWithFaceunity
//
//  Created by LSQ on 2019/12/3.
//  Copyright © 2019 Agora. All rights reserved.
//

#import <AGMBase/AGMBase.h>

NS_ASSUME_NONNULL_BEGIN

@interface AGMFaceUnityFilter : AGMVideoSource <AGMVideoSink>

@property (nonatomic, copy) void (^didCompletion)(CVPixelBufferRef resultPixelBuffer, CMTime timeStamp, AGMVideoRotation rotation);

@end

NS_ASSUME_NONNULL_END
