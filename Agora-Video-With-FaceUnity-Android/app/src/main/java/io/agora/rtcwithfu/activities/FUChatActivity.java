package io.agora.rtcwithfu.activities;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.faceunity.FURenderer;
import com.faceunity.entity.Effect;
import com.faceunity.entity.MakeupItem;

import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.capture.video.camera.Constant;
import io.agora.capture.video.camera.VideoCapture;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.RtcVideoConsumer;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtcwithfu.EffectOptionContainer;
import io.agora.rtcwithfu.utils.Constants;
import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.RtcEngineEventHandler;
import io.agora.rtcwithfu.utils.EffectUtil;
import io.agora.rtcwithfu.utils.FULicence;
import io.agora.rtcwithfu.utils.MakeupUtil;

/**
 * This activity demonstrates how to make FU and Agora RTC SDK work together
 * <p>
 * The FU activity which possesses remote video chatting ability.
 */
@SuppressWarnings("deprecation")
public class FUChatActivity extends RtcBasedActivity implements RtcEngineEventHandler {
    private final static String TAG = FUChatActivity.class.getSimpleName();

    private static final int CAPTURE_WIDTH = 1280;
    private static final int CAPTURE_HEIGHT = 720;
    private static final int CAPTURE_FRAME_RATE = 24;

    private CameraVideoManager mVideoManager;
    private FURenderer mFURenderer;
    private FrameLayout mRemoteViewContainer;
    private EffectOptionContainer mEffectContainer;

    // Default effects & beauty options to be as examples
    private MakeupItem mNoMakeup = MakeupUtil.noLipstickMakeupItem();
    private MakeupItem mDefaultMakeup = MakeupUtil.defaultLipstickMakeupItem();

    private int mRemoteUid = -1;
    private boolean mFinished;

    private class EffectListener implements EffectOptionContainer.OnEffectOptionContainerItemClickListener {
        @Override
        public void onEffectOptionItemClicked(int index, int textResource, boolean selected) {
            Log.i(TAG, "onEffectOptionItemClicked " + index + " " + selected);
            if (mFURenderer != null) {
                switch (index) {
                    case 0:
                        mFURenderer.setBeautificationOn(selected);
                        break;
                    case 1:
                        if (selected) {
                            mFURenderer.setMakeupItemParam(mDefaultMakeup.getParamMap());
                        } else {
                            mFURenderer.setMakeupItemParam(mNoMakeup.getParamMap());
                        }
                        break;
                    case 2:
                        Effect effect = selected ? EffectUtil.EFFECT_DEFAULT : EffectUtil.EFFECT_NONE;
                        mFURenderer.onEffectSelected(effect);
                        break;
                }
            }
        }

        @Override
        public void onEffectNotSupported(int index, int textResource) {
            Toast.makeText(FUChatActivity.this, R.string.sorry_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initUI();
        initRoom();
    }

    private void initUI() {
        initRemoteViewLayout();
        mEffectContainer = findViewById(R.id.effect_container);
        mEffectContainer.setEffectOptionItemListener(new EffectListener());
    }

    private void initRemoteViewLayout() {
        mRemoteViewContainer = findViewById(R.id.remote_video_view);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mRemoteViewContainer.getLayoutParams();
        params.width = displayMetrics.widthPixels / 3;
        params.height = displayMetrics.heightPixels / 3;
        mRemoteViewContainer.setLayoutParams(params);
    }

    private void initRoom() {
        initVideoModule();
        rtcEngine().setVideoSource(new RtcVideoConsumer());
        joinChannel();
    }

    private void initVideoModule() {
        mVideoManager = videoManager();
        mVideoManager.setCameraStateListener(new VideoCapture.VideoCaptureStateListener() {
            @Override
            public void onFirstCapturedFrame(int width, int height) {
                Log.i(TAG, "onFirstCapturedFrame: " + width + "x" + height);
            }

            @Override
            public void onCameraCaptureError(int error, String msg) {
                Log.i(TAG, "onCameraCaptureError: error:" + error + " " + msg);
                if (mVideoManager != null) {
                    // When there is a camera error, the capture should
                    // be stopped to reset the internal states.
                    mVideoManager.stopCapture();
                }
            }
        });

        mFURenderer = ((PreprocessorFaceUnity) mVideoManager.
                getPreprocessor()).getFURenderer();

        mVideoManager.setPictureSize(CAPTURE_WIDTH, CAPTURE_HEIGHT);
        mVideoManager.setFrameRate(CAPTURE_FRAME_RATE);
        mVideoManager.setFacing(Constant.CAMERA_FACING_FRONT);
        mVideoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_AUTO);

        TextureView localVideo = findViewById(R.id.local_video_view);
        mVideoManager.setLocalPreview(localVideo);

        disableFUEffects();
        updateEffectOptionPanel();
    }

    private void disableFUEffects() {
        mFURenderer.setBeautificationOn(false);
        mFURenderer.onEffectSelected(EffectUtil.EFFECT_NONE);
    }

    private void updateEffectOptionPanel() {
        // Beautification
        mEffectContainer.setItemViewStyles(0, false, FULicence.fuPermissionGrantedByIndex(0));

        // Makeup

        // Sticker
        mEffectContainer.setItemViewStyles(2, false, FULicence.fuPermissionGrantedByIndex(2));

        // Body Beauty
    }

    private void joinChannel() {
        rtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        rtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);

        String roomName = getIntent().getStringExtra(Constants.ACTION_KEY_ROOM_NAME);
        rtcEngine().joinChannel(null, roomName, null, 0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_camera:
                onCameraChangeRequested();
                break;
        }
    }

    private void onCameraChangeRequested() {
        mVideoManager.switchCamera();
    }

    @Override
    public void onStart() {
        super.onStart();
        mVideoManager.startCapture();
    }

    @Override
    public void finish() {
        mFinished = true;
        mVideoManager.stopCapture();
        rtcEngine().leaveChannel();
        super.finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mFinished) {
            mVideoManager.stopCapture();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.i(TAG, "onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.i(TAG, "onUserJoined " + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        Log.i(TAG, "onRemoteVideoStateChanged " + (uid & 0xFFFFFFFFL) + " " + state + " " + reason);
        if (mRemoteUid == -1 && state == io.agora.rtc.Constants.REMOTE_VIDEO_STATE_DECODING) {
            runOnUiThread(() -> {
                mRemoteUid = uid;
                setRemoteVideoView(uid);
            });
        }
    }

    private void setRemoteVideoView(int uid) {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(this);
        rtcEngine().setupRemoteVideo(new VideoCanvas(
                surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRemoteViewContainer.addView(surfaceView);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(this::onRemoteUserLeft);
    }

    private void onRemoteUserLeft() {
        mRemoteUid = -1;
        removeRemoteView();
    }

    private void removeRemoteView() {
        mRemoteViewContainer.removeAllViews();
    }
}
