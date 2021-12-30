//
//  FUManager.m
//  FULiveDemo
//
//  Created by 刘洋 on 2017/8/18.
//  Copyright © 2017年 刘洋. All rights reserved.
//

#import "FUManager.h"

#import "authpack.h"

#import "FUTestRecorder.h"

static FUManager *shareManager = NULL;

@implementation FUManager

+ (FUManager *)shareManager
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        shareManager = [[FUManager alloc] init];
    });

    return shareManager;
}

- (instancetype)init
{
    if (self = [super init]) {
        
        CFAbsoluteTime startTime = CFAbsoluteTimeGetCurrent();

        NSString *controllerPath = [[NSBundle mainBundle] pathForResource:@"controller_cpp" ofType:@"bundle"];
        NSString *controllerConfigPath = [[NSBundle mainBundle] pathForResource:@"controller_config" ofType:@"bundle"];
        FUSetupConfig *setupConfig = [[FUSetupConfig alloc] init];
        setupConfig.authPack = FUAuthPackMake(g_auth_package, sizeof(g_auth_package));
        setupConfig.controllerPath = controllerPath;
        setupConfig.controllerConfigPath = controllerConfigPath;
        
        // 初始化 FURenderKit
        [FURenderKit setupWithSetupConfig:setupConfig];
        
        [FURenderKit setLogLevel:FU_LOG_LEVEL_INFO];
        
        dispatch_async(dispatch_get_global_queue(0, 0), ^{
            // 加载人脸 AI 模型
            NSString *faceAIPath = [[NSBundle mainBundle] pathForResource:@"ai_face_processor" ofType:@"bundle"];
            [FUAIKit loadAIModeWithAIType:FUAITYPE_FACEPROCESSOR dataPath:faceAIPath];
            
            // 加载身体 AI 模型
            NSString *bodyAIPath = [[NSBundle mainBundle] pathForResource:@"ai_human_processor" ofType:@"bundle"];
            [FUAIKit loadAIModeWithAIType:FUAITYPE_HUMAN_PROCESSOR dataPath:bodyAIPath];
            
            CFAbsoluteTime endTime = (CFAbsoluteTimeGetCurrent() - startTime);
            
            NSString *path = [[NSBundle mainBundle] pathForResource:@"tongue" ofType:@"bundle"];
            [FUAIKit loadTongueMode:path];
            
            //TODO: todo 是否需要用？？？？？
            /* 设置嘴巴灵活度 默认= 0*/ //
            float flexible = 0.5;
            [FUAIKit setFaceTrackParam:@"mouth_expression_more_flexible" value:flexible];
            NSLog(@"---%lf",endTime);
            
            // 设置人脸算法质量
            [FUAIKit shareKit].faceProcessorFaceLandmarkQuality = [FURenderKit devicePerformanceLevel] == FUDevicePerformanceLevelHigh ? FUFaceProcessorFaceLandmarkQualityHigh : FUFaceProcessorFaceLandmarkQualityMedium;
        });

        [[FUTestRecorder shareRecorder] setupRecord];
        
        [FUAIKit shareKit].maxTrackFaces = 4;
    }
    
    return self;
}


- (void)destoryItems {
    [FURenderKit shareRenderKit].beauty = nil;
    [FURenderKit shareRenderKit].bodyBeauty = nil;
    [FURenderKit shareRenderKit].makeup = nil;
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
}


- (void)onCameraChange {
    [FUAIKit resetTrackedResult];
}

- (void)updateBeautyBlurEffect {
    if (![FURenderKit shareRenderKit].beauty || ![FURenderKit shareRenderKit].beauty.enable) {
        return;
    }
    if ([FURenderKit devicePerformanceLevel] == FUDevicePerformanceLevelHigh) {
        // 根据人脸置信度设置不同磨皮效果
        CGFloat score = [FUAIKit fuFaceProcessorGetConfidenceScore:0];
        if (score > 0.95) {
            [FURenderKit shareRenderKit].beauty.blurType = 3;
            [FURenderKit shareRenderKit].beauty.blurUseMask = YES;
        } else {
            [FURenderKit shareRenderKit].beauty.blurType = 2;
            [FURenderKit shareRenderKit].beauty.blurUseMask = NO;
        }
    } else {
        // 设置精细磨皮效果
        [FURenderKit shareRenderKit].beauty.blurType = 2;
        [FURenderKit shareRenderKit].beauty.blurUseMask = NO;
    }
}


#pragma mark - VideoFilterDelegate

- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame {
    [[FUTestRecorder shareRecorder] processFrameWithLog];
    [self updateBeautyBlurEffect];
    if ([self.delegate respondsToSelector:@selector(faceUnityManagerCheckAI)]) {
        [self.delegate faceUnityManagerCheckAI];
    }
    FURenderInput *input = [[FURenderInput alloc] init];
    input.pixelBuffer = frame;
    //默认图片内部的人脸始终是朝上，旋转屏幕也无需修改该属性。
    input.renderConfig.imageOrientation = FUImageOrientationUP;
    //开启重力感应，内部会自动计算正确方向，设置fuSetDefaultRotationMode，无须外面设置
    input.renderConfig.gravityEnable = YES;
    //如果来源相机捕获的图片一定要设置，否则将会导致内部检测异常
    input.renderConfig.isFromFrontCamera = YES;
    //该属性是指系统相机是否做了镜像: 一般情况前置摄像头出来的帧都是设置过镜像，所以默认需要设置下。如果相机属性未设置镜像，改属性不用设置。
    input.renderConfig.isFromMirroredCamera = YES;
    FURenderOutput *output = [[FURenderKit shareRenderKit] renderWithInput:input];
    return output.pixelBuffer;
}

@end
