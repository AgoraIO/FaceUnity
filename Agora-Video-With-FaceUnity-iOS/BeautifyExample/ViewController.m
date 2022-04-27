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
#import <AGMRenderer/AGMRenderer.h>

@interface ViewController () <AgoraRtcEngineDelegate, AgoraVideoFrameDelegate, AgoraAudioFrameDelegate>

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
@property (nonatomic, assign) AgoraVideoMirrorMode remoteVideoMirrored;

@end

@implementation ViewController
//@synthesize consumer;

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
    
    // init process manager
    self.processingManager = [[VideoProcessingManager alloc] init];
    
    // add FaceUnity filter and add to process manager
    self.videoFilter = [FUManager shareManager];
    [self.processingManager addVideoFilter:self.videoFilter];
    
    // self.processingManager.enableFilter = NO;
    
    [self.rtcEngineKit setVideoFrameDelegate:self];
    [self.rtcEngineKit setAudioFrameDelegate:self];

    [self.rtcEngineKit enableVideo];

    self.remoteVideoMirrored = AgoraVideoMirrorModeEnabled;
    AgoraVideoEncoderConfiguration* config = [[AgoraVideoEncoderConfiguration alloc] initWithSize:AgoraVideoDimension1280x720 frameRate:AgoraVideoFrameRateFps30 bitrate:AgoraVideoBitrateStandard orientationMode:AgoraVideoOutputOrientationModeAdaptative mirrorMode:self.remoteVideoMirrored];
    [self.rtcEngineKit setVideoEncoderConfiguration:config];

    // set up local video to render your local camera preview
    self.videoCanvas = [AgoraRtcVideoCanvas new];
    self.videoCanvas.uid = 0;
    // the view to be binded
    self.videoCanvas.view = self.localView;
    self.videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    self.videoCanvas.mirrorMode = AgoraVideoMirrorModeDisabled;
    [self.rtcEngineKit setupLocalVideo:self.videoCanvas];
    
    [self.rtcEngineKit startPreview];
    
    [self.rtcEngineKit joinChannelByToken:nil channelId:self.channelName info:nil uid:0 joinSuccess:nil];
}


- (void)viewDidLayoutSubviews {
    self.localView.frame = self.view.bounds;
}

- (void)dealloc {
    [[FUManager shareManager] destoryItems];
}

- (IBAction)switchCamera:(UIButton *)button
{
    [self.rtcEngineKit switchCamera];
    
    [[FUManager shareManager] onCameraChange];
    
}

- (IBAction)toggleRemoteMirror:(UIButton *)button
{
    self.remoteVideoMirrored = self.remoteVideoMirrored == AgoraVideoMirrorModeEnabled ? AgoraVideoMirrorModeDisabled : AgoraVideoMirrorModeEnabled;
    AgoraVideoEncoderConfiguration* config = [[AgoraVideoEncoderConfiguration alloc] initWithSize:AgoraVideoDimension1280x720 frameRate:AgoraVideoFrameRateFps30 bitrate:AgoraVideoBitrateStandard orientationMode:AgoraVideoOutputOrientationModeAdaptative mirrorMode:self.remoteVideoMirrored];

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
    [self.rtcEngineKit setVideoFrameDelegate:nil];
    [self.rtcEngineKit setAudioFrameDelegate:nil];

    [self.rtcEngineKit leaveChannel:nil];
    [self.rtcEngineKit stopPreview];
    [AgoraRtcEngineKit destroy];

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
    
- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine remoteVideoStateChangedOfUid:(NSUInteger)uid state:(AgoraVideoRemoteState)state reason:(AgoraVideoRemoteReason)reason elapsed:(NSInteger)elapsed {
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

#pragma mark - AgoraVideoFrameDelegate

/**
 * Occurs each time the SDK receives a video frame captured by the local camera.
 * Notes: If getVideoFrameProcessMode is read-write, use MUST implement this method
 *
 * After you successfully register the video frame observer, the SDK triggers this callback each time
 * a video frame is received. In this callback, you can get the video data captured by the local
 * camera. You can then pre-process the data according to your scenarios.
 *
 * After pre-processing, you can send the processed video data back to the SDK by setting the
 * `videoFrame` parameter in this callback.
 *
 * @param srcFrame A pointer to the video frame: AgoraOutputVideoFrame
 * @param dstFrame (inout params) A pointer to the video frame: AgoraOutputVideoFrame
 * @return Determines whether to ignore the current video frame if the pre-processing fails:
 * - true: Do not ignore.
 * - false: Ignore, in which case this method does not sent the current video frame to the SDK.
 */
- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame * _Nonnull)srcFrame dstFrame:(AgoraOutputVideoFrame *_Nullable* _Nullable)dstFrame {
    if (!srcFrame || !srcFrame.pixelBuffer) {
        return NO;
    }
    
    CVPixelBufferRef outPixelBuffer = [self.processingManager processFrame:srcFrame.pixelBuffer];
    if (!outPixelBuffer) {
        return NO;
    }else {
        srcFrame.pixelBuffer = outPixelBuffer;
        *dstFrame = srcFrame;
        return YES;
    }
    
}

- (BOOL)onRenderVideoFrame:(AgoraOutputVideoFrame * _Nonnull)videoFrame uid:(NSUInteger)uid channelId:(NSString * _Nonnull)channelId {
    return YES;
}

- (BOOL)getRotationApplied {
    return YES;
}

/**
 * Occurs each time needs to get whether mirror is applied or not.
 * @return Determines whether to mirror.
 * - true: need to mirror.
 * - false: no mirror.
 */
- (BOOL)getMirrorApplied {
    return YES;
}

/**
 * Indicate the video frame mode of the observer.
 * @return AgoraVideoFrameProcessMode
 */
- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode {
    return AgoraVideoFrameProcessModeReadWrite;
}

/**
 * Occurs each time needs to get preference video frame type.
 * @return AgoraVideoFormat.
 */
- (AgoraVideoFormat)getVideoPixelFormatPreference {
    return AgoraVideoFormatCVPixel;
}

#pragma mark - AgoraAudioFrameDelegate

/**
 * Occurs when the recorded audio frame is received.
 * @param audioFrame A pointer to the audio frame: AgoraAudioFrame.
 * @param channelId Unique channel name for the AgoraRTC session in the string
 * format. The string length must be less than 64 bytes. Supported character
 * scopes are:
 * - All lowercase English letters: a to z.
 * - All uppercase English letters: A to Z.
 * - All numeric characters: 0 to 9.
 * - The space character.
 * - Punctuation characters and other symbols, including: "!", "#", "$", "%", "&", "(", ")", "+", "-", ":", ";", "<", "=", ".", ">", "?", "@", "[", "]", "^", "_", " {", "}", "|", "~", ",".
 * @return
 * - true: The recorded audio frame is valid and is encoded and sent.
 * - false: The recorded audio frame is invalid and is not encoded or sent.
 */
- (BOOL)onRecordAudioFrame:(AgoraAudioFrame* _Nonnull)frame channelId:(NSString * _Nonnull)channelId {
    return YES;
}

/**
 * Occurs when the playback audio frame is received.
 * @param channelId Unique channel name for the AgoraRTC session in the string
 * format. The string length must be less than 64 bytes. Supported character
 * scopes are:
 * - All lowercase English letters: a to z.
 * - All uppercase English letters: A to Z.
 * - All numeric characters: 0 to 9.
 * - The space character.
 * - Punctuation characters and other symbols, including: "!", "#", "$", "%", "&", "(", ")", "+", "-", ":", ";", "<", "=", ".", ">", "?", "@", "[", "]", "^", "_", " {", "}", "|", "~", ",".
 * @param audioFrame A pointer to the audio frame: AgoraAudioFrame.
 * @return
 * - true: The playback audio frame is valid and is encoded and sent.
 * - false: The playback audio frame is invalid and is not encoded or sent.
 */
- (BOOL)onPlaybackAudioFrame:(AgoraAudioFrame* _Nonnull)frame channelId:(NSString * _Nonnull)channelId {
    return YES;
}

/**
 * Occurs when the mixed audio data is received.
 * @param audioFrame The A pointer to the audio frame: AgoraAudioFrame.
 * @param channelId Unique channel name for the AgoraRTC session in the string
 * format. The string length must be less than 64 bytes. Supported character
 * scopes are:
 * - All lowercase English letters: a to z.
 * - All uppercase English letters: A to Z.
 * - All numeric characters: 0 to 9.
 * - The space character.
 * - Punctuation characters and other symbols, including: "!", "#", "$", "%", "&", "(", ")", "+", "-", ":", ";", "<", "=", ".", ">", "?", "@", "[", "]", "^", "_", " {", "}", "|", "~", ",".
 * @return
 * - true: The mixed audio data is valid and is encoded and sent.
 * - false: The mixed audio data is invalid and is not encoded or sent.
 */
- (BOOL)onMixedAudioFrame:(AgoraAudioFrame* _Nonnull)frame channelId:(NSString * _Nonnull)channelId {
    return YES;
}

/**
 * Occurs when the before-mixing playback audio frame is received.
 * @param channelId Unique channel name for the AgoraRTC session in the string
 * format. The string length must be less than 64 bytes. Supported character
 * scopes are:
 * - All lowercase English letters: a to z.
 * - All uppercase English letters: A to Z.
 * - All numeric characters: 0 to 9.
 * - The space character.
 * - Punctuation characters and other symbols, including: "!", "#", "$", "%", "&", "(", ")", "+", "-", ":", ";", "<", "=", ".", ">", "?", "@", "[", "]", "^", "_", " {", "}", "|", "~", ",".
 * @param uid ID of the remote user.
 * @param audioFrame A pointer to the audio frame: AgoraAudioFrame.
 * @return
 * - true: The before-mixing playback audio frame is valid and is encoded and sent.
 * - false: The before-mixing playback audio frame is invalid and is not encoded or sent.
 */
- (BOOL)onPlaybackAudioFrameBeforeMixing:(AgoraAudioFrame* _Nonnull)frame channelId:(NSString * _Nonnull)channelId uid:(NSUInteger)uid {
    return YES;
}

@end
