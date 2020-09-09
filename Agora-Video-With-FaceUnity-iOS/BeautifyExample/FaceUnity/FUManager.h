//
//  FUManager.h
//  FULiveDemo
//
//  Created by 刘洋 on 2017/8/18.
//  Copyright © 2017年 刘洋. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <libCNamaSDK/FURenderer.h>
#import "FUBeautyParam.h"
#import "FUDateHandle.h"

@class FULiveModel ;

typedef NS_ENUM(NSUInteger, FUNamaHandleType) {
    FUNamaHandleTypeBeauty = 0,   /* items[0] ------ 放置 美颜道具句柄 */
    FUNamaHandleTypeItem = 1,     /* items[1] ------ 放置 普通道具句柄（包含很多，如：贴纸，aoimoji...若不单一存在，可放句柄集其他位置） */
    FUNamaHandleTypeFxaa = 2,     /* items[2] ------ fxaa抗锯齿道具句柄 */
    FUNamaHandleTypeMakeup = 3,     /* items[3] ------ 美妆道具句柄 */
    FUNamaHandleTypeBodySlim = 4,  /* items[4] ------ 美体道具 */
    FUNamaHandleTypeBodyAvtar = 5,
    FUNamaHandleTotal = 6,
};

@interface FUManager : NSObject

@property (nonatomic, strong) dispatch_queue_t asyncLoadQueue;
@property (nonatomic, assign) BOOL showFaceUnityEffect ;
@property (nonatomic, assign) BOOL flipx ;
@property (nonatomic, assign) BOOL trackFlipx ;
@property (nonatomic, assign) BOOL isRender;
@property (nonatomic,assign)FUDataType currentType;


/* 滤镜参数 */
@property (nonatomic, strong) NSMutableArray<FUBeautyParam *> *filters;
/* 美肤参数 */
@property (nonatomic, strong) NSMutableArray<FUBeautyParam *> *skinParams;
/* 美型参数 */
@property (nonatomic, strong) NSMutableArray<FUBeautyParam *> *shapeParams;

/** 选中的滤镜 */
@property (nonatomic, strong) FUBeautyParam *seletedFliter;


+ (FUManager *)shareManager;

- (void)setAsyncTrackFaceEnable:(BOOL)enable;

/**销毁全部道具*/
- (void)destoryItems;

/* 加载bundle 到指定items位置 */
- (void)loadBundleWithName:(NSString *)name aboutType:(FUNamaHandleType)type;
/* 获取handle */
- (int)getHandleAboutType:(FUNamaHandleType)type;

-(void)setParamItemAboutType:(FUNamaHandleType)type name:(NSString *)paramName value:(float)value;
/*
 销毁指定道具
 */
- (void)destoryItemAboutType:(FUNamaHandleType)type;

-(void)filterValueChange:(FUBeautyParam *)param;

- (void)loadFilter;

-(void)setRenderType:(FUDataType)dateType;

#pragma  mark -  render
- (CVPixelBufferRef)renderItemsToPixelBuffer:(CVPixelBufferRef)pixelBuffer;

- (int)renderItemWithTexture:(int)texture Width:(int)width Height:(int)height ;
- (void)processFrameWithY:(void*)y U:(void*)u V:(void*)v yStride:(int)ystride uStride:(int)ustride vStride:(int)vstride FrameWidth:(int)width FrameHeight:(int)height;
/**获取75个人脸特征点*/
- (void)getLandmarks:(float *)landmarks;

/**
 获取图像中人脸中心点位置

 @param frameSize 图像的尺寸，该尺寸要与视频处理接口或人脸信息跟踪接口中传入的图像宽高相一致
 @return 返回一个以图像左上角为原点的中心点
 */
- (CGPoint)getFaceCenterInFrameSize:(CGSize)frameSize;

/**判断是否检测到人脸*/
- (BOOL)isTracking;

/**切换摄像头要调用此函数*/
- (void)onCameraChange;

/**获取错误信息*/
- (NSString *)getError;
@end
