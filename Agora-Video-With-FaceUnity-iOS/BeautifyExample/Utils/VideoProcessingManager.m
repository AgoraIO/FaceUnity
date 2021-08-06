//
//  VideoProcessingManager.m
//  BeautifyExample
//
//  Created by LSQ on 2020/8/6.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "VideoProcessingManager.h"

@interface VideoProcessingManager ()
@property (nonatomic, strong) NSMutableArray *filterArray;
@end

@implementation VideoProcessingManager

- (instancetype)init {
    self = [super init];
    if (self) {
        self.enableFilter = YES;
        self.filterArray = [[NSMutableArray alloc] init];
    }
    return self;
}

- (void)addVideoFilter:(id<VideoFilterDelegate>)filter {
    if (!filter) return;
    if (![self.filterArray containsObject:filter]) {
        [self.filterArray addObject:filter];
    }
}

- (void)removeVideoFilter:(id<VideoFilterDelegate>)filter {
    if (!filter) return;
    if ([self.filterArray containsObject:filter]) {
        [self.filterArray removeObject:filter];
    }
}

#pragma mark - CapturerManagerDelegate
- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)pixelBuffer {
    CVPixelBufferRef outputPixelBuffer = pixelBuffer;
    if (self.enableFilter) {
        for (id<VideoFilterDelegate> filter in self.filterArray) {
            outputPixelBuffer = [filter processFrame:outputPixelBuffer];
        }
    }
    return outputPixelBuffer;
}

@end
