package io.agora.rtcwithfu.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.regex.Pattern;

import io.agora.rtcwithfu.Constants;
import io.agora.rtcwithfu.R;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_CODE_ALL_PERMISSIONS = 999;

    private EditText mChannelName;
    private boolean enableCustomizedAudioRecording;
    private boolean enableHorizontal;
    private OnCameraAndAudioPermissionListener mListener = new OnCameraAndAudioPermissionListener() {
        @Override
        public void onGrantResult(boolean granted) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mChannelName = (EditText) findViewById(R.id.edt_channel);
        checkCameraPermission(this, mListener);
    }

    public void onCheckLocalRecordBoxCLick(View view) {
        CheckBox checkBox = (CheckBox) view;
        enableCustomizedAudioRecording = checkBox.isChecked();
    }

    public void onStartBroadcastClick(View view) {
        String name = mChannelName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "please input the channel name", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, FUChatActivity.class);
            intent.putExtra(Constants.ACTION_KEY_ROOM_NAME, name);
            intent.putExtra(Constants.ACTION_KEY_ENABLE_CUSTOMIZED_AUDIO_RECORD, enableCustomizedAudioRecording);
            startActivity(intent);
        }
    }

    private boolean checkCameraPermission(Context context, OnCameraAndAudioPermissionListener listener) {
        boolean granted = true;
        boolean needToDoRealTest = isFlyme() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M;

        if (needToDoRealTest) {
            Camera mCamera = null;
            try {
                mCamera = Camera.open();
                Camera.Parameters mParameters = mCamera.getParameters();
                mCamera.setParameters(mParameters);
            } catch (Exception e) {
                granted = false;
                Log.i(TAG, Log.getStackTraceString(e));
            }
            if (mCamera != null) {
                mCamera.release();
            }

            AudioRecord mAudioRecord = null;
            try {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 32000,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(32000, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT));
                mAudioRecord.startRecording();
            } catch (Exception e) {
                granted = false;
                Log.i(TAG, Log.getStackTraceString(e));
            }

            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
            }

            File mFile = null;
            try {
                mFile = new File(Environment.getExternalStorageDirectory() + "/io.agora.rtcwithfu_test_per");
                mFile.createNewFile();
            } catch (Exception e) {
                granted = false;
            }
            if (mFile != null) {
                mFile.delete();
            }
        } else {
            granted = !(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED);
        }

        if (granted) {
            if (listener != null) {
                listener.onGrantResult(true);
            }
        } else if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_ALL_PERMISSIONS);
        }

        return granted;
    }

    private static boolean isFlyme() {
        if (Build.FINGERPRINT.contains("Flyme")
                || Pattern.compile("Flyme", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("Meizu")
                || Build.BRAND.contains("MeiZu")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.msg_permission_rejected),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this, getString(R.string.msg_permission_granted),
                Toast.LENGTH_SHORT).show();
    }


    public interface OnCameraAndAudioPermissionListener {
        void onGrantResult(boolean granted);
    }
}
