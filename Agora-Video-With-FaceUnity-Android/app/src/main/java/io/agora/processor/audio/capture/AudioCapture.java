package io.agora.processor.audio.capture;

import android.media.AudioRecord;

import io.agora.processor.common.utils.ToolUtil;
import io.agora.processor.media.base.BaseAudioCapture;
import io.agora.processor.media.data.AudioCaptureConfigInfo;
import io.agora.processor.media.data.AudioCapturedFrame;
import io.agora.processor.media.data.MediaFrameFormat;
import io.agora.processor.common.utils.LogUtil;

import static io.agora.processor.common.constant.Constant.AUDIO_FRAME_BUFFER;
import static io.agora.processor.common.constant.Constant.AUDIO_PER_FRAME;
import static io.agora.processor.common.constant.Constant.AUDIO_SOURCES;
import static io.agora.processor.common.constant.Constant.LOCAL_RAW_AUDIO_FILE_PATH;
import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_AUDIO_CAPTURE_SUCCESS;
import static io.agora.processor.common.constant.ConstantCode.START_AUDIO_CAPTURE_FAILED;
import static io.agora.processor.common.constant.ConstantCode.START_AUDIO_CAPTURE_SUCCESS;


/**
 * Created by yong on 2019/8/31.
 */

//audio collect thread
public class AudioCapture extends BaseAudioCapture {
    private AudioRecordThread mAudioRecordThread = null;
    private boolean mIsCapturing = false;
    private boolean mIsAudioThreadRun = false;
    private AudioCaptureConfigInfo mAudioCaptureConfigInfo;
    private boolean enableWriteToFile = false;
    private String localFilePath = null;

    public AudioCapture(AudioCaptureConfigInfo audioCaptureConfigInfo) {
        super();
        this.mAudioRecordThread = null;
        this.mIsCapturing = false;
        this.mIsAudioThreadRun = false;
        this.mAudioCaptureConfigInfo = audioCaptureConfigInfo;
        this.enableWriteToFile = false;
    }

    /*******************************public api*********************************************/
    @Override
    public void allocate() {
    }

    @Override
    public int start() {
        if (!mIsAudioThreadRun) {
            LogUtil.i("startAudioCapture " + mAudioCaptureConfigInfo);
            mIsCapturing = true;
            mAudioRecordThread = new AudioRecordThread();
            mAudioRecordThread.setName("AudioCapture");
            mAudioRecordThread.start();
        } else {
            return START_AUDIO_CAPTURE_FAILED;
        }
        return START_AUDIO_CAPTURE_SUCCESS;
    }

    @Override
    public void stop() {
        mIsCapturing = false;
        while (mIsAudioThreadRun) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                LogUtil.e(e.toString());
            }
        }
        LogUtil.i("ui;ouiiiop");
        this.mAudioRecordThread = null;
        this.mIsCapturing = false;
        this.mIsAudioThreadRun = false;
        this.enableWriteToFile = false;
        this.localFilePath = null;
    }

    @Override
    public int deallocate() {
        mCaptureDataConnector.clear();
        return DEALLOCATE_AUDIO_CAPTURE_SUCCESS;
    }

    /**********************************end public api******************************************/


    public void enableWriteToFile(String filePath) {
        this.enableWriteToFile = true;
        if (filePath == null || filePath.length() == 0) {
            this.localFilePath = LOCAL_RAW_AUDIO_FILE_PATH;
        } else {
            this.localFilePath = filePath;
        }
    }

    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write them to the MediaCodec encoder
     */
    private class AudioRecordThread extends Thread {
        @Override
        public void run() {
            mIsAudioThreadRun = true;
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                final int min_buffer_size = AudioRecord.getMinBufferSize(
                        mAudioCaptureConfigInfo.getAudioSampleRate(), mAudioCaptureConfigInfo.getAudioChannelFormat(),
                        mAudioCaptureConfigInfo.getAudioPcmBit());
                int buffer_size = AUDIO_PER_FRAME * AUDIO_FRAME_BUFFER;
                if (buffer_size < min_buffer_size)
                    buffer_size = ((min_buffer_size / AUDIO_PER_FRAME) + 1) * AUDIO_PER_FRAME * 2;

                AudioRecord audioRecord = null;
                for (final int source : AUDIO_SOURCES) {
                    try {
                        audioRecord = new AudioRecord(
                                source, mAudioCaptureConfigInfo.getAudioSampleRate(),
                                mAudioCaptureConfigInfo.getAudioChannelFormat(), mAudioCaptureConfigInfo.getAudioPcmBit(), buffer_size);
                        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                            audioRecord = null;
                    } catch (final Exception e) {
                        audioRecord = null;
                        LogUtil.e(e.toString());
                    }
                    if (audioRecord != null) break;
                }
                if (audioRecord != null) {
                    try {
                        if (mIsCapturing) {
                            LogUtil.d("AudioRecordThread:prepare audio recording");
                            int readBytes;
                            audioRecord.startRecording();
                            try {
                                for (; mIsCapturing; ) {
                                    // read audio data from internal mic
                                    byte[] bytes = new byte[AUDIO_PER_FRAME];
                                    readBytes = audioRecord.read(bytes, 0, bytes.length);
                                    if (readBytes > 0 && mIsCapturing) {
                                        LogUtil.d("bytes:" + bytes.length + " readBytes" + readBytes);
                                        mCaptureDataConnector.onDataAvailable(new AudioCapturedFrame(bytes, readBytes, MediaFrameFormat.FrameType.AUDIO));
                                        if (enableWriteToFile) {
                                            ToolUtil.saveDataToFile(localFilePath, bytes);
                                        }
                                    }
                                }
                            } finally {
                                audioRecord.stop();
                            }
                        }
                    } finally {
                        audioRecord.release();
                    }
                } else {
                    LogUtil.e("failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                LogUtil.e("AudioRecordThread#run" + e);
            }
            LogUtil.i("AudioRecordThread:finished");
            mIsAudioThreadRun = false;
        }
    }


}
