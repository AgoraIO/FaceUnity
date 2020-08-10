//
//  VideoProcessingManager.h
//  BeautifyExample
//
//  Created by LSQ on 2020/8/6.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "VideoFilterDelegate.h"
#import "CapturerManagerDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoProcessingManager : NSObject <CapturerManagerDelegate>
/**
 * Default: YES
 */
@property (nonatomic, assign) BOOL enableFilter;

- (void)addVideoFilter:(id<VideoFilterDelegate>)filter;
- (void)removeVideoFilter:(id<VideoFilterDelegate>)filter;

@end

NS_ASSUME_NONNULL_END
