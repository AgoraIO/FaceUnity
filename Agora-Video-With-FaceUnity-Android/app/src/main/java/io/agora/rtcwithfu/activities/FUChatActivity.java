package io.agora.rtcwithfu.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.nama.FURenderer;
import com.faceunity.nama.ui.FaceUnityView;
import com.faceunity.nama.utils.CameraUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.capture.video.camera.Constant;
import io.agora.capture.video.camera.VideoCapture;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.RtcVideoConsumer;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.IVideoSink;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtcwithfu.utils.Constants;
import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.RtcEngineEventHandler;

/**
 * This activity demonstrates how to make FU and Agora RTC SDK work together
 * <p>
 * The FU activity which possesses remote video chatting ability.
 */
@SuppressWarnings("deprecation")
public class FUChatActivity extends RtcBasedActivity implements RtcEngineEventHandler , SensorEventListener {
    private final static String TAG = FUChatActivity.class.getSimpleName();

    private static final int CAPTURE_WIDTH = 1280;
    private static final int CAPTURE_HEIGHT = 720;
    private static final int CAPTURE_FRAME_RATE = 24;

    private CameraVideoManager mVideoManager;
    private FURenderer mFURenderer;
    private FrameLayout mRemoteViewContainer;
    private TextView mTrackingText;


    private int mRemoteUid = -1;
    private boolean mFinished;
    private int mCameraFace = FURenderer.CAMERA_FACING_FRONT;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initUI();
        initRoom();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    private void initUI() {
        initRemoteViewLayout();
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

        mTrackingText = findViewById(R.id.iv_face_detect);
        FaceUnityView faceUnityView = findViewById(R.id.fu_view);
        mFURenderer = ((PreprocessorFaceUnity) mVideoManager.getPreprocessor()).getFURenderer();
        faceUnityView.setModuleManager(mFURenderer);
        mFURenderer.setOnTrackStatusChangedListener(new FURenderer.OnTrackStatusChangedListener() {
            @Override
            public void onTrackStatusChanged(int type, int status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingText.setText(type == FURenderer.TRACK_TYPE_FACE ? R.string.toast_not_detect_face : R.string.toast_not_detect_body);
                        mTrackingText.setVisibility(status > 0 ? View.INVISIBLE : View.VISIBLE);
                    }
                });
            }
        });

        mVideoManager.setPictureSize(CAPTURE_WIDTH, CAPTURE_HEIGHT);
        mVideoManager.setFrameRate(CAPTURE_FRAME_RATE);
        mVideoManager.setFacing(Constant.CAMERA_FACING_FRONT);
        mVideoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_AUTO);

        TextureView localVideo = findViewById(R.id.local_video_view);
        mVideoManager.setLocalPreview(localVideo);

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
        mCameraFace = FURenderer.CAMERA_FACING_FRONT - mCameraFace;
        mFURenderer.onCameraChanged(mCameraFace, CameraUtils.getCameraOrientation(mCameraFace));
    }

    @Override
    public void onStart() {
        super.onStart();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        mVideoManager.startCapture();
        mFURenderer.queueEvent(new Runnable() {
            @Override
            public void run() {
                mFURenderer.onSurfaceCreated();
            }
        });
    }

    @Override
    public void finish() {
        mFinished = true;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mFURenderer.queueEvent(new Runnable() {
            @Override
            public void run() {
                mFURenderer.onSurfaceDestroyed();
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        mSensorManager.unregisterListener(this);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (Math.abs(x) > 3 || Math.abs(y) > 3) {
                if (Math.abs(x) > Math.abs(y)) {
                    mFURenderer.onDeviceOrientationChanged(x > 0 ? 0 : 180);
                } else {
                    mFURenderer.onDeviceOrientationChanged(y > 0 ? 90 : 270);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
