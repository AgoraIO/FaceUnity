//
//  FaceUnityVideoFilter.m
//  BeautifyExample
//
//  Created by LSQ on 2020/8/6.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "FaceUnityVideoFilter.h"
#import <libCNamaSDK/FURenderer.h>
#import "authpack.h"

@interface FaceUnityVideoFilter()
{
    int items[FUNamaHandleTotal];
    int frameID;
}
@end

@implementation FaceUnityVideoFilter

- (instancetype)init
{
    if (self = [super init]) {
        _asyncLoadQueue = dispatch_queue_create("com.faceLoadItem", DISPATCH_QUEUE_SERIAL);
        if(sizeof(g_auth_package) == 0) {
            NSLog(@"please provide your own authpack.h and replace it into this project");
        } else {
            [[FURenderer shareRenderer] setupWithData:nil dataSize:0 ardata:nil authPackage:&g_auth_package authSize:sizeof(g_auth_package) shouldCreateContext:YES];
            [self loadAIModle];
            [self loadBeautyFace];
//            [self loadSticker];
//            [self loadBeautyBody];
            [self loadMakeup];
            self.authpackLoaded = YES;
        }
    }
    return self;
}

-(void)loadAIModle{
    NSData *ai_human_processor = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_human_processor.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_human_processor.bytes size:(int)ai_human_processor.length aitype:FUAITYPE_HUMAN_PROCESSOR];
    NSData *ai_face_processor = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ai_face_processor.bundle" ofType:nil]];
    [FURenderer loadAIModelFromPackage:(void *)ai_face_processor.bytes size:(int)ai_face_processor.length aitype:FUAITYPE_FACEPROCESSOR];
//
}

/**
 * load beauty items and filter
 */
- (void)loadBeautyFace{
    __weak __typeof(self)weakSelf = self;
    dispatch_async(_asyncLoadQueue, ^{
        FaceUnityVideoFilter* strongSelf = weakSelf;
        if(strongSelf) {
            if (strongSelf->items[FUNamaHandleTypeBeauty] == 0) {

                CFAbsoluteTime startTime = CFAbsoluteTimeGetCurrent();

                NSString *path = [[NSBundle mainBundle] pathForResource:@"face_beautification.bundle" ofType:nil];
                strongSelf->items[FUNamaHandleTypeBeauty] = [FURenderer itemWithContentsOfFile:path];

                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"heavy_blur" value:@(0)];
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"blur_type" value:@(2)];
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"blur_level" value:@(6)];
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"face_shape" value:@(4)];
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"cheek_thinning" value:@(0.7)];
                
                CFAbsoluteTime endTime = (CFAbsoluteTimeGetCurrent() - startTime);

                
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"filter_name" value:@"ziran1"];
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBeauty] withName:@"filter_level" value:@(1)];
                
                NSLog(@"Load beauty items takes: %f ms", endTime * 1000.0);
         
            }
        }
    });
}

- (void)loadBeautyBody{
    __weak __typeof(self)weakSelf = self;
    dispatch_async(_asyncLoadQueue, ^{
        FaceUnityVideoFilter* strongSelf = weakSelf;
        if(strongSelf) {
            if (strongSelf->items[FUNamaHandleTypeBodySlim] == 0) {

                CFAbsoluteTime startTime = CFAbsoluteTimeGetCurrent();

                NSString *path = [[NSBundle mainBundle] pathForResource:@"body_slim.bundle" ofType:nil];
                strongSelf->items[FUNamaHandleTypeBodySlim] = [FURenderer itemWithContentsOfFile:path];
                
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeBodySlim] withName:@"HeadSlim" value:@(1.0)];
                
                CFAbsoluteTime endTime = (CFAbsoluteTimeGetCurrent() - startTime);
                
                NSLog(@"Load beauty items takes: %f ms", endTime * 1000.0);
         
            }
        }
    });
}

- (void)loadMakeup{
    __weak __typeof(self)weakSelf = self;
    dispatch_async(_asyncLoadQueue, ^{
        FaceUnityVideoFilter* strongSelf = weakSelf;
        if(strongSelf) {
            if (strongSelf->items[FUNamaHandleTypeMakeup] == 0) {

                CFAbsoluteTime startTime = CFAbsoluteTimeGetCurrent();

                NSString *path = [[NSBundle mainBundle] pathForResource:@"face_makeup.bundle" ofType:nil];
                strongSelf->items[FUNamaHandleTypeMakeup] = [FURenderer itemWithContentsOfFile:path];
                
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeMakeup] withName:@"makeup_intensity_pupil" value:@(1.0)];
                [FURenderer itemSetParam:strongSelf->items[FUNamaHandleTypeMakeup] withName:@"makeup_intensity_eye" value:@(1.0)];
                
                CFAbsoluteTime endTime = (CFAbsoluteTimeGetCurrent() - startTime);
                
                NSLog(@"Load beauty items takes: %f ms", endTime * 1000.0);
         
            }
        }
    });
}

- (void)loadSticker{
    __weak __typeof(self)weakSelf = self;
    dispatch_async(_asyncLoadQueue, ^{
        FaceUnityVideoFilter* strongSelf = weakSelf;
        if(strongSelf) {
            if (strongSelf->items[FUNamaHandleTypeItem] == 0) {

                CFAbsoluteTime startTime = CFAbsoluteTimeGetCurrent();

                NSString *path = [[NSBundle mainBundle] pathForResource:@"sticker.bundle" ofType:nil];
                strongSelf->items[FUNamaHandleTypeItem] = [FURenderer itemWithContentsOfFile:path];
                
                CFAbsoluteTime endTime = (CFAbsoluteTimeGetCurrent() - startTime);
                
                NSLog(@"Load beauty items takes: %f ms", endTime * 1000.0);
         
            }
        }
    });
}

#pragma mark - VideoFilterDelegate
/// process your video frame here
- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame {
    if(self.enabled) {
        CVPixelBufferRef buffer = [[FURenderer shareRenderer] renderPixelBuffer:frame withFrameId:frameID items:items itemCount:sizeof(items)/sizeof(int) flipx:YES];
        return buffer;
    }
    frameID += 1;
    return frame;
}

@end
