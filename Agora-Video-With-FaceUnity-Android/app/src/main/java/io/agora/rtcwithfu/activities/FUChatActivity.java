package io.agora.rtcwithfu.activities;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.faceunity.FURenderer;

import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.capture.video.camera.Constant;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.RtcVideoConsumer;
import io.agora.rtc.mediaio.AgoraTextureView;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtcwithfu.Constants;
import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.RtcEngineEventHandler;
import io.agora.rtcwithfu.view.EffectPanel;

/**
 * This activity demonstrates how to make FU and Agora RTC SDK work together
 * <p>
 * The FU activity which possesses remote video chatting ability.
 */
@SuppressWarnings("deprecation")
public class FUChatActivity extends FUBaseActivity implements RtcEngineEventHandler {
    private final static String TAG = FUChatActivity.class.getSimpleName();
    private final static String KEY_MUTED = "muted";
    private final static String KEY_MIRRORED = "mirrored";
    private final static String KEY_LOCAL_BIG = "local-big";

    private static final int CAPTURE_WIDTH = 1280;
    private static final int CAPTURE_HEIGHT = 720;
    private static final int CAPTURE_FRAME_RATE = 24;

    private final static int DESC_SHOW_LENGTH = 1500;

    private FURenderer mFURenderer;
    private SurfaceView mLocalSurfaceView;
    private FrameLayout mLocalViewContainer;
    private AgoraTextureView mRemoteView;

    private boolean mLocalViewIsBig = true;
    private int mRemoteUid = -1;

    private TextView mDescriptionText;
    private TextView mTrackingText;

    private int mSmallHeight;
    private int mSmallWidth;
    private CameraVideoManager mVideoManager;
    private boolean mMirrored = true;
    private boolean mMuted;

    private boolean mFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoManager = videoManager();

        if (savedInstanceState != null) {
            mMuted = savedInstanceState.getBoolean(KEY_MUTED);
            broadcastingStatus = !mMuted;
            mMirrored = savedInstanceState.getBoolean(KEY_MIRRORED);
            mLocalViewIsBig = savedInstanceState.getBoolean(KEY_LOCAL_BIG);
        }
        mirrorVideoPreviewStatus = mMirrored;

        calculateSmallViewSize();
        initUIAndEvent();
    }

    private void calculateSmallViewSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        mSmallHeight = height / 3;
        mSmallWidth = width / 3;
    }

    @Override
    protected void initUIAndEvent() {
        mLocalViewContainer = findViewById(R.id.local_video_view_container);
        mLocalViewContainer.removeAllViews();
        mLocalSurfaceView = new SurfaceView(this);
        mLocalViewContainer.addView(mLocalSurfaceView);
        mRemoteView = findViewById(R.id.remote_video_view);

        if (mLocalViewIsBig) {
            setBigWindow(mLocalViewContainer);
            setSmallWindow(mRemoteView);
        } else {
            setBigWindow(mRemoteView);
            setSmallWindow(mLocalViewContainer);
        }

        mDescriptionText = findViewById(R.id.effect_desc_text);
        mTrackingText = findViewById(R.id.iv_face_detect);

        mFURenderer = ((PreprocessorFaceUnity) mVideoManager.
                getPreprocessor()).getFURenderer();

        mFURenderer.setOnTrackingStatusChangedListener(status ->
            runOnUiThread(() -> {
                int visibility = status == 0 ? View.VISIBLE : View.GONE;
                mTrackingText.setVisibility(visibility);
            }));

        mEffectPanel = new EffectPanel(findViewById(R.id.effect_container),
                mFURenderer, description -> showDescription(description, DESC_SHOW_LENGTH));

        mVideoManager.setPictureSize(CAPTURE_WIDTH, CAPTURE_HEIGHT);
        mVideoManager.setFrameRate(CAPTURE_FRAME_RATE);
        mVideoManager.setFacing(Constant.CAMERA_FACING_FRONT);
        mVideoManager.setLocalPreview(mLocalSurfaceView);
        mVideoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_AUTO);

        onChangedToBroadcaster(!mMuted);
        setRoleButtonText();

        Spinner spinner = findViewById(R.id.mirror_mode_spinner);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mVideoManager != null) {
                    mVideoManager.setLocalPreviewMirror(
                            indexToMirrorMode(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rtcEngine().setVideoSource(new RtcVideoConsumer());
        eventHandler().addEventHandler(this);
        joinChannel();
    }

    private void showDescription(int str, int time) {
        if (str == 0) return;
        mDescriptionText.removeCallbacks(effectDescriptionHide);
        mDescriptionText.setVisibility(View.VISIBLE);
        mDescriptionText.setText(str);
        mDescriptionText.postDelayed(effectDescriptionHide, time);
    }

    private Runnable effectDescriptionHide = new Runnable() {
        @Override
        public void run() {
            mDescriptionText.setText("");
            mDescriptionText.setVisibility(View.INVISIBLE);
        }
    };

    private int indexToMirrorMode(int position) {
         return position;
    }

    private void joinChannel() {
        int role = mMuted ? io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE :
                io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER;
        worker().configEngine(role,
                new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));

        String roomName = getIntent().getStringExtra(Constants.ACTION_KEY_ROOM_NAME);
        worker().joinChannel(roomName, config().mUid);
    }

    private int convert(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mMuted) {
            mVideoManager.startCapture();
        }
    }

    @Override
    public void finish() {
        mFinished = true;
        mVideoManager.stopCapture();
        mFURenderer.resetTrackingStatus();
        super.finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mFinished) {
            mVideoManager.stopCapture();
            mFURenderer.resetTrackingStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void deInitUIAndEvent() {
        eventHandler().removeEventHandler(this);
        worker().leaveChannel(config().mChannel);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(this::onRemoteUserLeft);
    }

    private void onRemoteUserLeft() {
        mRemoteUid = -1;
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(() -> {
            if (mRemoteUid != -1) return;
            setupRemoteVideo(uid);
        });
    }

    private void setupRemoteVideo(int uid) {
        mRemoteUid = uid;
        mRemoteView.setBufferType(MediaIO.BufferType.BYTE_ARRAY);
        mRemoteView.setPixelFormat(MediaIO.PixelFormat.I420);
        rtcEngine().setRemoteVideoRenderer(uid, mRemoteView);
    }

    @Override
    protected void onViewSwitchRequested() {
        swapLocalRemoteDisplay();
    }

    private void swapLocalRemoteDisplay() {
        if (mLocalViewIsBig) {
            setSmallWindow(mLocalViewContainer);
            setBigWindow(mRemoteView);
        } else {
            setSmallWindow(mRemoteView);
            setBigWindow(mLocalViewContainer);
        }
        mLocalViewIsBig = !mLocalViewIsBig;
    }

    private void setSmallWindow(View view) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.height = mSmallHeight;
        params.width = mSmallWidth;
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.rightMargin = convert(16);
        params.topMargin = convert(70);
        view.setLayoutParams(params);
        view.bringToFront();
        view.setOnTouchListener(this);
    }

    private void setBigWindow(View view) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.rightMargin = 0;
        params.topMargin = 0;
        view.setLayoutParams(params);
        view.getParent().requestLayout();
        view.setOnTouchListener(null);
    }

    @Override
    protected void onChangedToBroadcaster(boolean broadcaster) {
        Log.i(TAG, "onChangedToBroadcaster " + broadcaster);
        if (broadcaster) {
            rtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
            mLocalSurfaceView = new SurfaceView(this);
            mLocalViewContainer.addView(mLocalSurfaceView,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            mVideoManager.setLocalPreview(mLocalSurfaceView);
            mVideoManager.startCapture();
            mMuted = false;
        } else {
            rtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE);
            mLocalViewContainer.removeAllViews();
            mLocalSurfaceView = null;
            mVideoManager.stopCapture();
            mMuted = true;
        }
    }

    @Override
    protected void onCameraChangeRequested() {
        mVideoManager.switchCamera();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_MUTED, mMuted);
        outState.putBoolean(KEY_MIRRORED, mMirrored);
        outState.putBoolean(KEY_LOCAL_BIG, mLocalViewIsBig);
        super.onSaveInstanceState(outState);
    }
}
