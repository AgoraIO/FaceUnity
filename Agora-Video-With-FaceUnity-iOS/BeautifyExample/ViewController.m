//
//  ViewController.m
//  BeautifyExample
//
//  Created by LSQ on 2020/8/3.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "ViewController.h"
#import <AgoraRtcKit/AgoraRtcEngineKit.h>
#import "CapturerManager.h"
#import "VideoProcessingManager.h"
#import "FaceUnityVideoFilter.h"
#import "KeyCenter.h"


@interface ViewController () <AgoraRtcEngineDelegate>

@property (nonatomic, strong) CapturerManager *capturerManager;
@property (nonatomic, strong) FaceUnityVideoFilter* videoFilter;
@property (nonatomic, strong) VideoProcessingManager *processingManager;
@property (nonatomic, strong) AgoraRtcEngineKit *rtcEngineKit;
@property (nonatomic, strong) IBOutlet UIView *localView;
@property (nonatomic, strong) IBOutlet UIButton *enableBtn;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    // 初始化 rte engine
    self.rtcEngineKit = [AgoraRtcEngineKit sharedEngineWithAppId:[KeyCenter AppId] delegate:self];
    
    
    [self.rtcEngineKit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    [self.rtcEngineKit setClientRole:AgoraClientRoleBroadcaster];
    [self.rtcEngineKit enableVideo];
    
    // 处理美颜
    self.processingManager = [[VideoProcessingManager alloc] init];
    
    // 自采集以及封装 MediaIO, 将美颜处理后的 pixelbuffer push 到 rtc channel
    AGMCapturerVideoConfig *videoConfig = [AGMCapturerVideoConfig defaultConfig];
    self.capturerManager = [[CapturerManager alloc] initWithVideoConfig:videoConfig delegate:self.processingManager];
    
    
    // FaceUnity
    self.videoFilter = [[FaceUnityVideoFilter alloc] init];
    [self.processingManager addVideoFilter:self.videoFilter];
    
    
    [self.capturerManager startCapture];
    
    // set up local video to render your local camera preview
    AgoraRtcVideoCanvas* videoCanvas = [AgoraRtcVideoCanvas new];
    videoCanvas.uid = 0;
    // the view to be binded
    videoCanvas.view = self.localView;
    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    [self.rtcEngineKit setupLocalVideo:videoCanvas];
    
    [self.rtcEngineKit setVideoSource:self.capturerManager];
    
    [self.rtcEngineKit joinChannelByToken:nil channelId:@"test" info:nil uid:0 joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        
    }];
}

- (IBAction)toggleEnable:(UIButton *)button
{
    self.videoFilter.enabled = !self.videoFilter.enabled;
}

@end
