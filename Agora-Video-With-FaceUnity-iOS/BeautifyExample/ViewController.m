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
#import "KeyCenter.h"

#import "FUDemoManager.h"

#import <Masonry/Masonry.h>
#import <AGMRenderer/AGMRenderer.h>

@interface ViewController () <AgoraRtcEngineDelegate, AgoraVideoSourceProtocol>

@property (nonatomic, strong) CapturerManager *capturerManager;
@property (nonatomic, strong) FUManager *videoFilter;
@property (nonatomic, strong) VideoProcessingManager *processingManager;
@property (nonatomic, strong) AgoraRtcEngineKit *rtcEngineKit;
@property (nonatomic, strong) IBOutlet UIView *localView;

@property (weak, nonatomic) IBOutlet UIView *remoteView;

@property (nonatomic, strong) IBOutlet UIButton *switchBtn;
@property (nonatomic, strong) IBOutlet UIButton *remoteMirrorBtn;
@property (nonatomic, strong) IBOutlet UIView *missingAuthpackLabel;
@property (weak, nonatomic) IBOutlet UIButton *muteAudioBtn;
@property (nonatomic, strong) AgoraRtcVideoCanvas *videoCanvas;
@property (nonatomic, assign) AgoraVideoMirrorMode localVideoMirrored;
@property (nonatomic, assign) AgoraVideoMirrorMode remoteVideoMirrored;
@property (nonatomic, strong) AGMEAGLVideoView *glVideoView;

@end

@implementation ViewController
@synthesize consumer;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.remoteView.hidden = YES;
    
    // FaceUnity UI
    CGFloat safeAreaBottom = 0;
    if (@available(iOS 11.0, *)) {
        safeAreaBottom = [UIApplication sharedApplication].delegate.window.safeAreaInsets.bottom;
    }
    [FUDemoManager setupFaceUnityDemoInController:self originY:CGRectGetHeight(self.view.frame) - FUBottomBarHeight - safeAreaBottom];

    // 初始化 rte engine
    self.rtcEngineKit = [AgoraRtcEngineKit sharedEngineWithAppId:[KeyCenter AppId] delegate:self];
    
    [self.rtcEngineKit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    [self.rtcEngineKit setClientRole:AgoraClientRoleBroadcaster];
    [self.rtcEngineKit enableVideo];
    [self.rtcEngineKit setParameters:@"{\"che.video.zerocopy\":true}"];
    AgoraVideoEncoderConfiguration* config = [[AgoraVideoEncoderConfiguration alloc] initWithSize:AgoraVideoDimension1280x720
                                                                                        frameRate:AgoraVideoFrameRateFps30
                                                                                          bitrate:AgoraVideoBitrateStandard
                                                                                  orientationMode:AgoraVideoOutputOrientationModeFixedPortrait];
    [self.rtcEngineKit setVideoEncoderConfiguration:config];
    
    // init process manager
    self.processingManager = [[VideoProcessingManager alloc] init];
    
    // init capturer, it will push pixelbuffer to rtc channel
    AGMCapturerVideoConfig *videoConfig = [AGMCapturerVideoConfig defaultConfig];
    videoConfig.sessionPreset = AVCaptureSessionPreset1280x720;
    videoConfig.fps = 30;
    videoConfig.pixelFormat =  AGMVideoPixelFormatNV12;
    self.capturerManager = [[CapturerManager alloc] initWithVideoConfig:videoConfig delegate:self.processingManager];
    
    // add FaceUnity filter and add to process manager
    self.videoFilter = [FUManager shareManager];
    [self.processingManager addVideoFilter:self.videoFilter];
    
    // self.processingManager.enableFilter = NO;
    
    [self.capturerManager startCapture];
    
    // set up local video to render your local camera preview
//    self.videoCanvas = [AgoraRtcVideoCanvas new];
//    self.videoCanvas.uid = 0;
//    // the view to be binded
//    self.videoCanvas.view = self.localView;
//    self.videoCanvas.renderMode = AgoraVideoRenderModeHidden;
//    self.videoCanvas.mirrorMode = AgoraVideoMirrorModeDisabled;
//    [self.rtcEngineKit setupLocalVideo:self.videoCanvas];
    
    [self.localView layoutIfNeeded];
    self.glVideoView = [[AGMEAGLVideoView alloc] initWithFrame:self.localView.frame];
//    [self.glVideoView setRenderMode:(AGMRenderMode_Fit)];
    [self.localView addSubview:self.glVideoView];
    [self.capturerManager setVideoView:self.glVideoView];
    // set custom capturer as video source
    [self.rtcEngineKit setVideoSource:self.capturerManager];
    
    [self.rtcEngineKit joinChannelByToken:nil channelId:self.channelName info:nil uid:0 joinSuccess:nil];

}


- (void)viewDidLayoutSubviews {
    self.glVideoView.frame = self.view.bounds;
}

- (void)dealloc {
    
    [[FUManager shareManager] destoryItems];
    [self.capturerManager stopCapture];
    [self.rtcEngineKit leaveChannel:nil];
    [self.rtcEngineKit stopPreview];
    [self.rtcEngineKit setVideoSource:nil];
    [AgoraRtcEngineKit destroy];
    
}

- (IBAction)switchCamera:(UIButton *)button
{
    [self.capturerManager switchCamera];
    
    [[FUManager shareManager] onCameraChange];
    
}

- (IBAction)toggleRemoteMirror:(UIButton *)button
{
    self.remoteVideoMirrored = self.remoteVideoMirrored == AgoraVideoMirrorModeEnabled ? AgoraVideoMirrorModeDisabled : AgoraVideoMirrorModeEnabled;
    AgoraVideoEncoderConfiguration* config = [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(720, 1280) frameRate:30 bitrate:0 orientationMode:AgoraVideoOutputOrientationModeAdaptative];
    config.mirrorMode = self.remoteVideoMirrored;
    [self.rtcEngineKit setVideoEncoderConfiguration:config];
    
    
}

- (IBAction)muteAudioBtn:(UIButton *)sender {
    sender.selected = !sender.selected;
    if (sender.selected) {
        
        [sender setTitleColor:[UIColor blueColor] forState:(UIControlStateSelected)];
    }
    [self.rtcEngineKit muteLocalAudioStream:sender.selected];
}

- (IBAction)backBtnClick:(UIButton *)sender {
    [[FUManager shareManager] destoryItems];
    [self dismissViewControllerAnimated:YES completion:nil];
}


/// firstRemoteVideoDecoded
- (void)rtcEngine:(AgoraRtcEngineKit *)engine firstRemoteVideoDecodedOfUid:(NSUInteger)uid size: (CGSize)size elapsed:(NSInteger)elapsed {
//    if (self.remoteView.hidden) {
//        self.remoteView.hidden = NO;
//    }
//
//    AgoraRtcVideoCanvas *videoCanvas = [[AgoraRtcVideoCanvas alloc] init];
//    videoCanvas.uid = uid;
//    // Since we are making a simple 1:1 video chat app, for simplicity sake, we are not storing the UIDs. You could use a mechanism such as an array to store the UIDs in a channel.
//
//    videoCanvas.view = self.remoteView;
//    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
//    [self.rtcEngineKit setupRemoteVideo:videoCanvas];
    // Bind remote video stream to view
    
}

#pragma mark - AgoraRtcEngineDelegate
- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine didJoinChannel:(NSString * _Nonnull)channel withUid:(NSUInteger)uid elapsed:(NSInteger) elapsed {
    NSLog(@"加入房间");
}


- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine remoteVideoStateChangedOfUid:(NSUInteger)uid state:(AgoraVideoRemoteState)state reason:(AgoraVideoRemoteStateReason)reason elapsed:(NSInteger)elapsed {
    switch (state) {
        case AgoraVideoRemoteStateStarting: {
            if (self.remoteView.hidden) {
                self.remoteView.hidden = NO;
            }
        }
            break;
        case AgoraVideoRemoteStateStopped: {
            if (!self.remoteView.hidden) {
                self.remoteView.hidden = YES;
            }
        }
            
        default:
            break;
    }
    
    
    AgoraRtcVideoCanvas *videoCanvas = [[AgoraRtcVideoCanvas alloc] init];
    videoCanvas.uid = uid;
    // Since we are making a simple 1:1 video chat app, for simplicity sake, we are not storing the UIDs. You could use a mechanism such as an array to store the UIDs in a channel.
    
    videoCanvas.view = self.remoteView;
    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    [self.rtcEngineKit setupRemoteVideo:videoCanvas];
}
@end
