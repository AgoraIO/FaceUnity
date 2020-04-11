package io.agora.rtcwithfu.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.FURenderer;
import com.faceunity.encoder.MediaAudioEncoder;
import com.faceunity.encoder.MediaEncoder;
import com.faceunity.encoder.MediaMuxerWrapper;
import com.faceunity.encoder.MediaVideoEncoder;
import com.faceunity.fulivedemo.ui.adapter.EffectRecyclerAdapter;
import com.faceunity.fulivedemo.utils.CameraUtils;
import com.faceunity.fulivedemo.utils.ToastUtil;
import com.faceunity.gles.core.GlUtil;
import com.faceunity.utils.Constant;
import com.faceunity.utils.MiscUtil;

import java.io.File;
import java.io.IOException;

import io.agora.kit.media.VideoManager;
import io.agora.kit.media.capture.VideoCaptureFrame;
import io.agora.kit.media.connector.SinkConnector;
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
public class FUChatActivity extends FUBaseActivity implements RtcEngineEventHandler, SensorEventListener {

    private final static String TAG = FUChatActivity.class.getSimpleName();

    private final static int DESC_SHOW_LENGTH = 1500;

    private FURenderer mFURenderer;
    private GLSurfaceView mGLSurfaceViewLocal;

    private FrameLayout mLocalViewContainer;
    private AgoraTextureView mRemoteView;
    private boolean mLocalViewIsBig = true;
    private int mRemoteUid = -1;
    private float x_position;
    private float y_position;

    private TextView mDescriptionText;
    private TextView mTrackingText;

    private int showNum = 0;

    // Video recording related
    private long mVideoRecordingStartTime = 0;
    private String mVideoFileName;
    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder mVideoEncoder;

    private int mSmallHeight;
    private int mSmallWidth;

    private VideoManager mVideoManager;

    private volatile boolean mFUInit;

    private int mImageWidth;
    private int mImageHeight;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private SinkConnector<VideoCaptureFrame> mEffectHandler = new SinkConnector<VideoCaptureFrame>() {
        @Override
        public int onDataAvailable(VideoCaptureFrame data) {
            mImageHeight = data.mFormat.getHeight();
            mImageWidth = data.mFormat.getWidth();

            int fuTextureId = mFURenderer.onDrawFrame(data.mImage, data.mTextureId,
                    data.mFormat.getWidth(), data.mFormat.getHeight());

            sendRecordingData(fuTextureId, data.mTexMatrix, data.mTimeStamp / Constant.NANO_IN_ONE_MILLI_SECOND);
            return fuTextureId;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUIAndEvent();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void initUIAndEvent() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.d(TAG, "width: " + width + ", height: " + height);
        mSmallHeight = height / 3;
        mSmallWidth = width / 3;
        x_position = width - mSmallWidth - convert(16);
        y_position = convert(70);

        mDescriptionText = findViewById(R.id.effect_desc_text);
        mTrackingText = findViewById(R.id.iv_face_detect);

        // The settings of FURender may be slightly different,
        // determined when initializing the effect panel
        mFURenderer = new FURenderer
                .Builder(this)
                .inputImageOrientation(CameraUtils.getFrontCameraOrientation())
                .setOnFUDebugListener(new FURenderer.OnFUDebugListener() {
                    @Override
                    public void onFpsChange(double fps, double renderTime) {
                        Log.d(TAG, "FURenderer.onFpsChange, fps: " + fps + ", renderTime: " + renderTime);
                    }
                })
                .setOnTrackingStatusChangedListener(new FURenderer.OnTrackingStatusChangedListener() {
                    @Override
                    public void onTrackingStatusChanged(final int status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTrackingText.setVisibility(status > 0 ? View.GONE : View.VISIBLE);
                            }
                        });
                    }
                })
                .inputTextureType(FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
                .build();

        mGLSurfaceViewLocal = new GLSurfaceView(this);

        bindSurfaceViewEvent();

        mLocalViewContainer = findViewById(R.id.local_video_view_container);
        if (mLocalViewContainer.getChildCount() > 0) {
            mLocalViewContainer.removeAllViews();
        }
        mLocalViewContainer.addView(mGLSurfaceViewLocal,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        mVideoManager = VideoManager.createInstance(this);

        mVideoManager.allocate(width, height, 30, io.agora.kit.media.constant.Constant.CAMERA_FACING_FRONT);
        mVideoManager.setRenderView(mGLSurfaceViewLocal);
        mVideoManager.connectEffectHandler(mEffectHandler);
        mVideoManager.attachToRTCEngine(getWorker().getRtcEngine());
        mVideoManager.startCapture();

        mRemoteView = findViewById(R.id.remote_video_view);
        RelativeLayout.LayoutParams remoteParams = (RelativeLayout.LayoutParams) mRemoteView.getLayoutParams();
        remoteParams.height = mSmallHeight;
        remoteParams.width = mSmallWidth;
        remoteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        remoteParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        remoteParams.rightMargin = convert(16);
        remoteParams.topMargin = convert(70);
        mRemoteView.setLayoutParams(remoteParams);
        mRemoteView.setOnTouchListener(this);

        mEffectPanel = new EffectPanel(findViewById(R.id.effect_container), mFURenderer, new EffectRecyclerAdapter.OnDescriptionChangeListener() {
            @Override
            public void onDescriptionChangeListener(int description) {
                showDescription(description, DESC_SHOW_LENGTH);
            }
        });

        getEventHandler().addEventHandler(this);
        joinChannel();
    }

    private void joinChannel() {
        getWorker().configEngine(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER, new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24, 800,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));

        String roomName = getIntent().getStringExtra(Constants.ACTION_KEY_ROOM_NAME);
        getWorker().joinChannel(roomName, getConfig().mUid);
    }

    private void swapLocalRemoteDisplay() {
        if (mLocalViewIsBig) {
            RelativeLayout.LayoutParams localParams = (RelativeLayout.LayoutParams) mLocalViewContainer.getLayoutParams();
            localParams.height = mSmallHeight;
            localParams.width = mSmallWidth;
            localParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            localParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            localParams.rightMargin = convert(16);
            localParams.topMargin = convert(70);
            mLocalViewContainer.setLayoutParams(localParams);
            mLocalViewContainer.bringToFront();
            mLocalViewContainer.setOnTouchListener(this);

            RelativeLayout.LayoutParams remoteParams = (RelativeLayout.LayoutParams) mRemoteView.getLayoutParams();
            remoteParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            remoteParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            remoteParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            remoteParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            remoteParams.rightMargin = 0;
            remoteParams.topMargin = 0;
            mRemoteView.setLayoutParams(remoteParams);
            mRemoteView.setX(x_position);
            mRemoteView.setY(y_position);
            mRemoteView.getParent().requestLayout();
            mRemoteView.setOnTouchListener(null);
        } else {
            RelativeLayout.LayoutParams localParams = (RelativeLayout.LayoutParams) mLocalViewContainer.getLayoutParams();
            localParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            localParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            localParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            localParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            localParams.rightMargin = 0;
            localParams.topMargin = 0;
            mLocalViewContainer.setLayoutParams(localParams);
            mLocalViewContainer.setX(x_position);
            mLocalViewContainer.setY(y_position);
            mLocalViewContainer.getParent().requestLayout();
            mLocalViewContainer.setOnTouchListener(null);

            RelativeLayout.LayoutParams remoteParams = (RelativeLayout.LayoutParams) mRemoteView.getLayoutParams();
            remoteParams.height = mSmallHeight;
            remoteParams.width = mSmallWidth;
            remoteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            remoteParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            remoteParams.rightMargin = convert(16);
            remoteParams.topMargin = convert(70);
            mRemoteView.setLayoutParams(remoteParams);
            mRemoteView.bringToFront();
            mRemoteView.setOnTouchListener(this);
        }
        mLocalViewIsBig = !mLocalViewIsBig;
    }

    private int convert(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void addViewMatchParent(FrameLayout parent, View child) {
        int matchParent = FrameLayout.LayoutParams.MATCH_PARENT;
        parent.addView(child, matchParent, matchParent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: 2020-02-17 maybe ANR @Agora team
        mVideoManager.stopCapture();
        mVideoManager.deallocate();

        mGLSurfaceViewLocal.queueEvent(new Runnable() {
            @Override
            public void run() {
                mFURenderer.onSurfaceDestroyed();
                mFUInit = false;
            }
        });
    }

    @Override
    protected void deInitUIAndEvent() {
        getEventHandler().removeEventHandler(this);
        getWorker().leaveChannel(getConfig().mChannel);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onUserOffline(int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onRemoteUserLeft();
            }
        });
    }

    private void onRemoteUserLeft() {
        mRemoteUid = -1;
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupRemoteVideo(uid);
            }
        });
    }

    private void setupRemoteVideo(int uid) {
        mRemoteUid = uid;
        mRemoteView.setBufferType(MediaIO.BufferType.BYTE_ARRAY);
        mRemoteView.setPixelFormat(MediaIO.PixelFormat.I420);
        getRtcEngine().setRemoteVideoRenderer(uid, mRemoteView);
    }

    protected void showDescription(int str, int time) {
        if (str == 0) {
            return;
        }
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

    @Override
    protected void onViewSwitchRequested() {
        swapLocalRemoteDisplay();
    }

    @Override
    protected void onMirrorPreviewRequested(boolean mirror) {
        Log.i(TAG, "onMirrorPreviewRequested " + mirror);

        mVideoManager.setMirrorMode(mirror);
    }

    @Override
    protected void onChangedToBroadcaster(boolean broadcaster) {
        Log.i(TAG, "onChangedToBroadcaster " + broadcaster);

        if (broadcaster) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            getRtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);

            mGLSurfaceViewLocal = new GLSurfaceView(this);

            bindSurfaceViewEvent();

            mVideoManager.allocate(width, height, 30, io.agora.kit.media.constant.Constant.CAMERA_FACING_FRONT);
            mVideoManager.setRenderView(mGLSurfaceViewLocal);
            mVideoManager.connectEffectHandler(mEffectHandler);
            mVideoManager.attachToRTCEngine(getWorker().getRtcEngine());
            mVideoManager.startCapture();

            mLocalViewContainer.addView(mGLSurfaceViewLocal,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);

            mVideoManager.startCapture();
        } else {
            mVideoManager.stopCapture();

            mLocalViewContainer.removeAllViews();

            getRtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE);

            mVideoManager.deallocate();

            mGLSurfaceViewLocal.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFURenderer.onSurfaceDestroyed();
                    mFUInit = false;
                }
            });

            System.gc();
        }

    }

    private void bindSurfaceViewEvent() {
        mGLSurfaceViewLocal.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mGLSurfaceViewLocal.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFUInit) {
                            mFURenderer.onSurfaceCreated();
                            mFUInit = true;
                        }
                    }
                });
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
    }

    @Override
    protected void onCameraChangeRequested() {
        // TODO Reset options when camera changed
        mVideoManager.switchCamera();
        mFURenderer.onCameraChange(mVideoManager.getCameraFacing(), mVideoManager.getCameraOrientation());
    }

    @Override
    protected void onStartRecordingRequested() {
        startRecording();
    }

    @Override
    protected void onStopRecordingRequested() {
        stopRecording();
    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder) {
                final MediaVideoEncoder videoEncoder = (MediaVideoEncoder) encoder;
                mGLSurfaceViewLocal.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        videoEncoder.setEglContext(EGL14.eglGetCurrentContext());
                        mVideoEncoder = videoEncoder;
                    }
                });
            }
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            mVideoEncoder = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(FUChatActivity.this, R.string.save_video_success);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(new File(mVideoFileName))));
                }
            });
        }
    };

    private void startRecording() {
        try {
            String videoFileName = Constant.APP_NAME + "_" + MiscUtil.getCurrentDate() + ".mp4";
            mVideoFileName = new File(Constant.cameraFilePath, videoFileName).getAbsolutePath();
            mMuxer = new MediaMuxerWrapper(mVideoFileName);

            // for video capturing
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener,
                    mImageHeight, mImageWidth);
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);

            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    protected void sendRecordingData(int texId, final float[] tex_matrix, final long timeStamp) {
        if (mVideoEncoder != null) {
            mVideoEncoder.frameAvailableSoon(texId, tex_matrix, GlUtil.IDENTITY_MATRIX);
            if (mVideoRecordingStartTime == 0) {
                mVideoRecordingStartTime = timeStamp;
            }
        }
    }

    private void stopRecording() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
        }
        System.gc();
    }

    private Runnable mCalibratingRunnable = new Runnable() {
        @Override
        public void run() {
            showNum++;
            StringBuilder builder = new StringBuilder();
            builder.append(getResources().getString(R.string.expression_calibrating));
            for (int i = 0; i < showNum; i++) {
                builder.append(".");
            }
            isCalibratingText.setText(builder);
            if (showNum < 6) {
                isCalibratingText.postDelayed(mCalibratingRunnable, 500);
            } else {
                isCalibratingText.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (Math.abs(x) > 3 || Math.abs(y) > 3) {
                if (Math.abs(x) > Math.abs(y)) {
                    mFURenderer.setTrackOrientation(x > 0 ? 0 : 180);
                } else {
                    mFURenderer.setTrackOrientation(y > 0 ? 90 : 270);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
