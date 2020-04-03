package io.agora.processor.audio.encoder;


import java.io.IOException;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;


import io.agora.processor.media.data.AudioCaptureConfigInfo;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.internal.AudioPreProcessor;
import io.agora.processor.media.internal.MediaCodecEncoder;
import io.agora.processor.media.data.AudioEncoderConfigInfo;
import io.agora.processor.media.data.ProcessedData;
import io.agora.processor.media.data.MediaFrameFormat;
import io.agora.processor.common.utils.LogUtil;

import static io.agora.processor.common.constant.Constant.AUDIO_BIT_RATE;
import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_AUDIO_ENCODER_FAILED;
import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_AUDIO_ENCODER_SUCCESS;
import static io.agora.processor.common.constant.ConstantCode.START_AUDIO_ENCODER_FAILED;
import static io.agora.processor.common.constant.ConstantCode.START_AUDIO_ENCODER_SUCCESS;

public class MediaCodecAudioEncoder extends MediaCodecEncoder {
    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "MediaCodecAudioEncoder";

    private AudioEncoderConfigInfo mAudioEncoderConfigInfo;
    private AudioCaptureConfigInfo mAudioCaptureConfigInfo;
    private AudioPreProcessor audioPreProcessor = null;

    public MediaCodecAudioEncoder(AudioCaptureConfigInfo audioCaptureConfigInfo, AudioEncoderConfigInfo audioEncoderConfigInfo) {
        super("MediaCodecAudioEncoder");
        mAudioEncoderConfigInfo = audioEncoderConfigInfo;
        mAudioCaptureConfigInfo = audioCaptureConfigInfo;
    }

    @Override
    public void allocate() {
        if (audioPreProcessor == null) {
            audioPreProcessor = new AudioPreProcessor();
        }
    }


    @Override
    public int start() throws IOException {
        if (audioPreProcessor == null) {
            LogUtil.e("audioPreProcessor is null");
            return START_AUDIO_ENCODER_FAILED;
        }
        if (mIsCapturing) {
            LogUtil.e("MediaCodecAudioEncoder have stared");
            return START_AUDIO_ENCODER_FAILED;
        }
        //start codec thread neeted
        startNewMediaCodecEncoderThread();
        frameType = MediaFrameFormat.FrameType.AUDIO;
        mOutputBufferEnabled = mIsEOS = false;
        // prepareEncoders MediaCodec for AAC encoding of audio data from inernal mic.
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(mAudioEncoderConfigInfo.getAudioMimeType());
        if (audioCodecInfo == null) {
            LogUtil.e("Unable to find an appropriate codec for " + mAudioEncoderConfigInfo.getAudioMimeType());
            return START_AUDIO_ENCODER_FAILED;
        }
        LogUtil.d("selected codec: " + audioCodecInfo.getName());

        final MediaFormat audioFormat = MediaFormat.createAudioFormat(
                mAudioEncoderConfigInfo.getAudioMimeType(),
                mAudioCaptureConfigInfo.getAudioSampleRate(),
                mAudioCaptureConfigInfo.getAudioChannelCount());
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, mAudioCaptureConfigInfo.getAudioChannelFormat());
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
//		audioFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, inputFile.length());
//      audioFormat.setLong(MediaFormat.KEY_DURATION, (long)durationInMs );
        LogUtil.i("format: " + audioFormat);
        mMediaCodec = MediaCodec.createEncoderByType(mAudioEncoderConfigInfo.getAudioMimeType());
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        LogUtil.d("MediaCodec thread enable to input data");
        LogUtil.i("prepareEncoders finishing " + mMediaCodec);
        this.startEncoderInternal();
        return START_AUDIO_ENCODER_SUCCESS;
    }


    @Override
    public int deallocate() {
        mEncoderedDataConnector.clear();
        if (mIsCapturing) {
            return DEALLOCATE_AUDIO_ENCODER_FAILED;
        }
        audioPreProcessor = null;
        return DEALLOCATE_AUDIO_ENCODER_SUCCESS;
    }

    @Override
    protected void signalEndOfInputStream() {
        LogUtil.d("sending0 EOS to encoder frameType" + frameType);
        encode(null, 0, getPTSUs());
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        LogUtil.v("selectAudioCodec:");

        MediaCodecInfo result = null;
        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        LOOP:
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                LogUtil.i("supportedType:" + codecInfo.getName() + ",MIME=" + types[j]);
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (result == null) {
                        result = codecInfo;
                        break LOOP;
                    }
                }
            }
        }
        return result;
    }


    @Override
    public void onDataAvailable(CapturedFrame data) {
        if (mRequestStop) {
            return;
        }
        if (data != null) {
            ProcessedData processedData = audioPreProcessor.preProcessAudioData(data);
            encode(processedData.mBuffer, processedData.mLength, processedData.mTimeStamp);
        }
        super.frameAvailableSoon();
    }

    public boolean reLoadEncoder(Object mediaData) throws Exception {
        this.stop();
        while (mIsCapturing) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                LogUtil.e(e.toString());
                return false;
            }
        }
        mAudioEncoderConfigInfo = (AudioEncoderConfigInfo) mediaData;
        startNewMediaCodecEncoderThread();
        this.start();
        return true;
    }

}
