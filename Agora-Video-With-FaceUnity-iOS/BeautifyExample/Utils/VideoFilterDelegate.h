//
//  VideoFilterDelegate.h
//  BeautifyExample
//
//  Created by LSQ on 2020/8/6.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreMedia/CoreMedia.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VideoFilterDelegate <NSObject>

- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame;

@end

NS_ASSUME_NONNULL_END
