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
@property (nonatomic, strong) IBOutlet UIButton *switchBtn;
@property (nonatomic, strong) IBOutlet UIButton *localMirrorBtn;
@property (nonatomic, strong) IBOutlet UIButton *remoteMirrorBtn;
@property (nonatomic, strong) IBOutlet UIView *missingAuthpackLabel;
@property (nonatomic, strong) AgoraRtcVideoCanvas *videoCanvas;
@property (nonatomic, assign) AgoraVideoMirrorMode localVideoMirrored;
@property (nonatomic, assign) AgoraVideoMirrorMode remoteVideoMirrored;

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
    AgoraVideoEncoderConfiguration* config = [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(720, 1280) frameRate:30 bitrate:0 orientationMode:AgoraVideoOutputOrientationModeAdaptative];
    [self.rtcEngineKit setVideoEncoderConfiguration:config];
    
    // init process manager
    self.processingManager = [[VideoProcessingManager alloc] init];
    
    // init capturer, it will push pixelbuffer to rtc channel
    AGMCapturerVideoConfig *videoConfig = [AGMCapturerVideoConfig defaultConfig];
    videoConfig.sessionPreset = AGMCaptureSessionPreset720x1280;
    videoConfig.fps = 30;
    self.capturerManager = [[CapturerManager alloc] initWithVideoConfig:videoConfig delegate:self.processingManager];
    
    
    // add FaceUnity filter and add to process manager
    self.videoFilter = [[FaceUnityVideoFilter alloc] init];
    [self.processingManager addVideoFilter:self.videoFilter];
    self.missingAuthpackLabel.hidden = self.videoFilter.authpackLoaded;
    
    
    [self.capturerManager startCapture];
    
    // set up local video to render your local camera preview
    self.videoCanvas = [AgoraRtcVideoCanvas new];
    self.videoCanvas.uid = 0;
    // the view to be binded
    self.videoCanvas.view = self.localView;
    self.videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    self.videoCanvas.mirrorMode = AgoraVideoMirrorModeDisabled;
    [self.rtcEngineKit setupLocalVideo:self.videoCanvas];
    
    // set custom capturer as video source
    [self.rtcEngineKit setVideoSource:self.capturerManager];
    
    [self.rtcEngineKit joinChannelByToken:nil channelId:@"test" info:nil uid:0 joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        
    }];
}

- (IBAction)toggleEnable:(UIButton *)button
{
    self.videoFilter.enabled = !self.videoFilter.enabled;
}

- (IBAction)switchCamera:(UIButton *)button
{
    [self.capturerManager switchCamera];
}

- (IBAction)toggleRemoteMirror:(UIButton *)button
{
    self.remoteVideoMirrored = self.remoteVideoMirrored == AgoraVideoMirrorModeEnabled ? AgoraVideoMirrorModeDisabled : AgoraVideoMirrorModeEnabled;
    AgoraVideoEncoderConfiguration* config = [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(720, 1280) frameRate:30 bitrate:0 orientationMode:AgoraVideoOutputOrientationModeAdaptative];
    config.mirrorMode = self.remoteVideoMirrored;
    [self.rtcEngineKit setVideoEncoderConfiguration:config];
}

- (IBAction)toggleBeauty:(UIButton *)button
{
    [self.videoFilter toggleHandle:FUNamaHandleTypeBeauty];
}

- (IBAction)toggleMakeup:(UIButton *)button
{
    [self.videoFilter toggleHandle:FUNamaHandleTypeMakeup];
}

- (IBAction)toggleSticker:(UIButton *)button
{
    [self.videoFilter toggleHandle:FUNamaHandleTypeItem];
}

- (IBAction)toggleBody:(UIButton *)button
{
    [self.videoFilter toggleHandle:FUNamaHandleTypeBodySlim];
}

@end
