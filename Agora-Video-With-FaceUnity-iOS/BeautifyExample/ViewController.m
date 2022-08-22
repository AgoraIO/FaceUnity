//
//  ViewController.m
//  BeautifyExample
//
//  Created by LSQ on 2020/8/3.
//  Copyright © 2020 Agora. All rights reserved.
//


#import "ViewController.h"
#import <AgoraRtcKit/AgoraRtcEngineKit.h>
#import "VideoProcessingManager.h"
#import "KeyCenter.h"

#import "FUDemoManager.h"

#import <Masonry/Masonry.h>

@interface ViewController () <AgoraRtcEngineDelegate, AgoraVideoFrameDelegate>

//@property (nonatomic, strong) CapturerManager *capturerManager;
@property (nonatomic, strong) FUManager *videoFilter;
//@property (nonatomic, strong) VideoProcessingManager *processingManager;
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

@end

@implementation ViewController

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
    
    [self.rtcEngineKit setVideoFrameDelegate:self];
    
    [self.rtcEngineKit setClientRole:AgoraClientRoleBroadcaster];
    AgoraCameraCapturerConfiguration *captuer = [[AgoraCameraCapturerConfiguration alloc] init];
    captuer.cameraDirection = AgoraCameraDirectionFront;
    [self.rtcEngineKit setCameraCapturerConfiguration:captuer];
    
    
    AgoraVideoEncoderConfiguration *configuration = [[AgoraVideoEncoderConfiguration alloc] init];
    configuration.dimensions = CGSizeMake(1280, 720);

    [self.rtcEngineKit setVideoEncoderConfiguration: configuration];
    
    
    // add FaceUnity filter and add to process manager
    self.videoFilter = [FUManager shareManager];
    
    // set up local video to render your local camera preview
    self.videoCanvas = [AgoraRtcVideoCanvas new];
    self.videoCanvas.uid = 0;
    // the view to be binded
    self.videoCanvas.view = self.localView;
    self.videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    self.videoCanvas.mirrorMode = AgoraVideoMirrorModeDisabled;
    [self.rtcEngineKit setupLocalVideo:self.videoCanvas];
    
    [self.localView layoutIfNeeded];
    // set custom capturer as video source
    
    AgoraRtcChannelMediaOptions *option = [[AgoraRtcChannelMediaOptions alloc] init];
    option.clientRoleType = [AgoraRtcIntOptional of: AgoraClientRoleBroadcaster];
    option.publishMicrophoneTrack = [AgoraRtcBoolOptional of:YES];
    option.publishCameraTrack = [AgoraRtcBoolOptional of:YES];
    
    [self.rtcEngineKit joinChannelByToken:nil channelId:self.channelName uid:10 mediaOptions:option joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) { }];

    AgoraRtcVideoCanvas *videoCanvas = [[AgoraRtcVideoCanvas alloc] init];
    videoCanvas.uid = 0;
    // Since we are making a simple 1:1 video chat app, for simplicity sake, we are not storing the UIDs. You could use a mechanism such as an array to store the UIDs in a channel.

    videoCanvas.view = self.localView;
    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    [self.rtcEngineKit setupRemoteVideo:videoCanvas];
    
    [self.rtcEngineKit enableVideo];
    [self.rtcEngineKit enableAudio];
    [self.rtcEngineKit startPreview];
}


- (void)viewDidLayoutSubviews {
//    self.localView.frame = self.view.bounds;
}

- (void)dealloc {
    
    [[FUManager shareManager] destoryItems];
    [self.rtcEngineKit leaveChannel:nil];
    [self.rtcEngineKit stopPreview];
    [AgoraRtcEngineKit destroy];
    
}

- (IBAction)switchCamera:(UIButton *)button
{
    [self.rtcEngineKit switchCamera];
}

- (IBAction)toggleRemoteMirror:(UIButton *)button
{
    self.remoteVideoMirrored = self.remoteVideoMirrored == AgoraVideoMirrorModeEnabled ? AgoraVideoMirrorModeDisabled : AgoraVideoMirrorModeEnabled;
    [self.rtcEngineKit setLocalRenderMode:(AgoraVideoRenderModeHidden) mirror:self.remoteVideoMirrored];
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

- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)videoFrame {
    CVPixelBufferRef pixelBuffer = [self.videoFilter processFrame:videoFrame.pixelBuffer];
    videoFrame.pixelBuffer = pixelBuffer;
    return YES;
}

- (AgoraVideoFormat)getVideoPixelFormatPreference{
    return AgoraVideoFormatBGRA;
}
- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode{
    return AgoraVideoFrameProcessModeReadWrite;
}

- (BOOL)getMirrorApplied{
    return YES;
}

- (BOOL)getRotationApplied {
    return NO;
}

@end
