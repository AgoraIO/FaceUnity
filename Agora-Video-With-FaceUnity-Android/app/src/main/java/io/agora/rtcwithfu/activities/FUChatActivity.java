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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.faceunity.nama.ui.FaceUnityView;

import io.agora.framework.PreprocessorFaceUnity;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.RtcEngineEventHandler;
import io.agora.rtcwithfu.utils.Constants;

import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;

/**
 * This activity demonstrates how to make FU and Agora RTC SDK work together
 * <p>
 * The FU activity which possesses remote video chatting ability.
 */
@SuppressWarnings("deprecation")
public class FUChatActivity extends RtcBasedActivity implements RtcEngineEventHandler, SensorEventListener {
    private final static String TAG = FUChatActivity.class.getSimpleName();

    private static final int CAPTURE_WIDTH = 1280;
    private static final int CAPTURE_HEIGHT = 720;
    private static final int CAPTURE_FRAME_RATE = 24;

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
        preprocessor = new PreprocessorFaceUnity(this);
        rtcEngine().registerVideoFrameObserver(preprocessor);mTrackingText = findViewById(R.id.iv_face_detect);
        FaceUnityView faceUnityView = findViewById(R.id.fu_view);
        mFaceUnityDataFactory = new FaceUnityDataFactory(0);
        faceUnityView.bindDataFactory(mFaceUnityDataFactory);
        FrameLayout localView = findViewById(R.id.local_video_view);
        // Create render view by RtcEngine
        TextureView textureView = new TextureView(this);
        // Add to the local container
        localView.addView(textureView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Setup local video to render your local camera preview
        rtcEngine().setupLocalVideo(new VideoCanvas(textureView, RENDER_MODE_HIDDEN, 0));
        joinChannel();
    }

    private void joinChannel() {
        rtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        rtcEngine().setClientRole(io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER);
        rtcEngine().enableLocalAudio(false);
        rtcEngine().startPreview();
        String roomName = getIntent().getStringExtra(Constants.ACTION_KEY_ROOM_NAME);
        rtcEngine().joinChannel(null, roomName, null, 0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_camera:
                preprocessor.skipFrame();
                break;
        }
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
    }

    @Override
    protected void onPause() {
        super.onPause();
//        preprocessor.releaseFURender();
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
        if (mRemoteUid == -1 && state == io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_STARTING) {
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
