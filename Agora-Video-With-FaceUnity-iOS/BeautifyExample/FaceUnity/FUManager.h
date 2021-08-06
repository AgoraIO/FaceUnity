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
#import <FURenderKit/FURenderKit.h>

#import "FUBaseModel.h"
#import "VideoFilterDelegate.h"
#import "FUViewModelManager.h"

@protocol FUManagerProtocol <NSObject>

//用于检测是否有ai人脸和人形
- (void)checkAI;

@end

@class FULiveModel ;

@interface FUManager : NSObject <VideoFilterDelegate>

@property (nonatomic, assign) BOOL flipx;
@property (nonatomic, assign) BOOL isRender;


@property (nonatomic, weak) id<FUManagerProtocol>delegate;

@property (nonatomic, strong) FUViewModelManager *viewModelManager;

/* 美肤参数 */
@property (nonatomic, strong) NSMutableArray<FUBaseModel *> *skinParams;
/* 美型参数 */
@property (nonatomic, strong) NSMutableArray<FUBaseModel *> *shapeParams;
/* 滤镜参数 */
@property (nonatomic, strong) NSMutableArray<FUBaseModel *> *filters;
/* 贴纸参数 */
@property (nonatomic, strong) NSMutableArray<FUBaseModel *> *stickers;
/* 美妆参数 */
@property (nonatomic, strong) NSMutableArray<FUBaseModel *> *makeupParams;
/* 美体参数 */
@property (nonatomic, strong) NSMutableArray<FUBaseModel *> *bodyParams;

+ (FUManager *)shareManager;


/// 销毁全部道具
- (void)destoryItems;

/// 切换前后摄像头
- (void)onCameraChange;

/// 获取错误信息
- (NSString *)getError;

@end
