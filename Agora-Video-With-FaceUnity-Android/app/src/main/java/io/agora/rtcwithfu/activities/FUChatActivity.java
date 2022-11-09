package io.agora.rtcwithfu.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.faceunity.nama.ui.FaceUnityView;

import java.util.List;

import io.agora.capture.framework.gles.MatrixOperator;
import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.capture.video.camera.Constant;
import io.agora.capture.video.camera.VideoCapture;
import io.agora.capture.video.camera.WatermarkConfig;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.RtcVideoConsumer;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.RtcEngineEventHandler;
import io.agora.rtcwithfu.utils.Constants;

/**
 * This activity demonstrates how to make FU and Agora RTC SDK work together
 * <p>
 * The FU activity which possesses remote video chatting ability.
 */
@SuppressWarnings("deprecation")
public class FUChatActivity extends RtcBasedActivity implements RtcEngineEventHandler, SensorEventListener {
    private final static String TAG = FUChatActivity.class.getSimpleName();

    private static final int CAPTURE_WIDTH = 640;
    private static final int CAPTURE_HEIGHT = 480;
    private static final int CAPTURE_FRAME_RATE = 15;

    private CameraVideoManager mVideoManager;
    private FURenderer mFURenderer = FURenderer.getInstance();
    private FaceUnityDataFactory mFaceUnityDataFactory;
    private PreprocessorFaceUnity preprocessor;

    private FrameLayout mRemoteViewContainer;
    private TextView mTrackingText;


    private int mRemoteUid = -1;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_base);
        initUI();
        initRoom();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mFURenderer.bindListener(mFURendererListener);
        String sdkVersion = RtcEngine.getSdkVersion();
        Log.i(TAG, "onCreate: agora sdk version " + sdkVersion);
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
        rtcEngine().setVideoSource(new RtcVideoConsumer(videoManager()));
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

            @Override
            public void onCameraOpen() {

            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public VideoCapture.FrameRateRange onSelectCameraFpsRange(List<VideoCapture.FrameRateRange> supportFpsRange, VideoCapture.FrameRateRange selectedRange) {
                return null;
            }
        });
        preprocessor = (PreprocessorFaceUnity) mVideoManager.getPreprocessor();
        mTrackingText = findViewById(R.id.iv_face_detect);
        FaceUnityView faceUnityView = findViewById(R.id.fu_view);
        mFaceUnityDataFactory = new FaceUnityDataFactory(0);
        faceUnityView.bindDataFactory(mFaceUnityDataFactory);

        mVideoManager.setPictureSize(CAPTURE_WIDTH, CAPTURE_HEIGHT);
        mVideoManager.setFrameRate(CAPTURE_FRAME_RATE);
        mVideoManager.setFacing(Constant.CAMERA_FACING_FRONT);
        mVideoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_AUTO);

        TextureView localVideo = findViewById(R.id.local_video_view);
        mVideoManager.setLocalPreview(localVideo, MatrixOperator.ScaleType.FitCenter, "");

        Bitmap waterMarkBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        MatrixOperator matrixOperator = mVideoManager.setWaterMark(waterMarkBitmap, new WatermarkConfig(360, 640));
        matrixOperator.translateX(-0.8f);
        matrixOperator.translateY(-0.8f);
        matrixOperator.setScaleRadio(0.3f);

        preprocessor.setSurfaceListener(new PreprocessorFaceUnity.SurfaceViewListener() {
            @Override
            public void onSurfaceCreated() {
                mFaceUnityDataFactory.bindCurrentRenderer();
            }

            @Override
            public void onSurfaceDestroyed() {
                mFURenderer.release();
            }
        });
    }

    private void joinChannel() {
        rtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        rtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
        rtcEngine().enableLocalAudio(false);
        String roomName = getIntent().getStringExtra(Constants.ACTION_KEY_ROOM_NAME);
        rtcEngine().joinChannel(null, roomName, null, 0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_camera:
                preprocessor.skipFrame();
                onCameraChangeRequested();
                break;
        }
    }

    private void onCameraChangeRequested() {
        preprocessor.doGLAction(() -> Log.e("ECRP", "test doGLAction thread id:" + Thread.currentThread().getId()));

        mVideoManager.switchCamera();
    }

    /**
     * FURenderer状态回调
     */
    private FURendererListener mFURendererListener = new FURendererListener() {


        @Override
        public void onTrackStatusChanged(FUAIProcessorEnum type, int status) {
            runOnUiThread(() -> {
                mTrackingText.setText(type == FUAIProcessorEnum.FACE_PROCESSOR ? R.string.toast_not_detect_face : R.string.toast_not_detect_body);
                mTrackingText.setVisibility(status > 0 ? View.INVISIBLE : View.VISIBLE);
            });
        }

        @Override
        public void onFpsChanged(double fps, double callTime) {

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        preprocessor.setRenderEnable(true);
        mVideoManager.startCapture();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preprocessor.releaseFURender();
        mVideoManager.stopCapture();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        rtcEngine().leaveChannel();
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
                    mFURenderer.setDeviceOrientation(x > 0 ? 0 : 180);
                } else {
                    mFURenderer.setDeviceOrientation(y > 0 ? 90 : 270);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
