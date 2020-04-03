package io.agora.processor.video.encoder;

import java.io.IOException;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import android.opengl.EGLContext;
import android.view.Surface;

import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.MediaVideoCodecInfo;
import io.agora.processor.media.internal.MediaCodecEncoder;
import io.agora.processor.media.data.ProcessedData;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.MediaFrameFormat;
import io.agora.processor.media.data.VideoEncoderConfigInfo;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.internal.VideoPreProcessor;

import static io.agora.processor.common.constant.Constant.recognizedRawDataFormats;
import static io.agora.processor.common.constant.Constant.recognizedSurfaceFormats;
import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_VIDEO_ENCODER_FAILED;
import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_VIDEO_ENCODER_SUCCESS;
import static io.agora.processor.common.constant.ConstantCode.START_VIDEO_ENCODER_FAILED;
import static io.agora.processor.common.constant.ConstantCode.START_VIDEO_ENCODER_SUCCESS;
import static io.agora.processor.common.constant.ConstantMediaConfig.VIDEO_KEY_IFRAME_RATE;


public class MediaCodecVideoEncoder extends MediaCodecEncoder {
    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "MediaCodecVideoEncoder";


    private VideoEncoderConfigInfo mVideoEncoderConfigInfo;
    private VideoCaptureConfigInfo mVideoCaptureConfigInfo;
    private VideoPreProcessor mVideoPreProcessor;
    private Surface mSurface;
    private MediaVideoCodecInfo videoCodecInfo;

    public MediaCodecVideoEncoder(VideoCaptureConfigInfo videoCaptureConfigInfo, VideoEncoderConfigInfo videoEncoderConfigInfo) {
        super("MediaCodecVideoEncoder");
        mVideoEncoderConfigInfo = videoEncoderConfigInfo;
        mVideoCaptureConfigInfo = videoCaptureConfigInfo;
    }

    @Override
    public void allocate() {
        if (mVideoPreProcessor == null) {
            mVideoPreProcessor = new VideoPreProcessor(mVideoCaptureConfigInfo);
        }
    }


    @Override
    public int start() throws IOException {
        LogUtil.i("start");

        if (mVideoPreProcessor == null) {
            LogUtil.e("mVideoPreProcessor is null");
            return START_VIDEO_ENCODER_FAILED;
        }
        if (mIsCapturing) {
            LogUtil.e("MediaCodecVideoEncoder have stared");
            return START_VIDEO_ENCODER_FAILED;
        }
        //start codec thread neeted
        startNewMediaCodecEncoderThread();
        frameType = MediaFrameFormat.FrameType.VIDEO;
        mOutputBufferEnabled = mIsEOS = false;
        int[] recognizedFormat = null;
        if (mVideoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            recognizedFormat = recognizedSurfaceFormats;
        } else {
            recognizedFormat = recognizedRawDataFormats;
        }
        videoCodecInfo = selectVideoCodec(mVideoEncoderConfigInfo.getVideoMimeType(), recognizedFormat);
        if (videoCodecInfo == null) {
            LogUtil.e("Unable to find an appropriate codec for " + mVideoEncoderConfigInfo.getVideoMimeType());
            return START_VIDEO_ENCODER_FAILED;
        }
        mVideoPreProcessor.setCodecFormatType(videoCodecInfo.getFormat());
        LogUtil.i("selected codec: " + videoCodecInfo.getMediaCodecInfo().getName() + " color format:" + videoCodecInfo.getFormat());
        int encoderWidth = 0;
        int encoderHeight = 0;
        if(mVideoCaptureConfigInfo.isHorizontal()){
            if (mVideoCaptureConfigInfo.getVideoCaptureWidth() > mVideoCaptureConfigInfo.getVideoCaptureHeight()) {
                encoderWidth = mVideoCaptureConfigInfo.getVideoCaptureWidth();
                encoderHeight = mVideoCaptureConfigInfo.getVideoCaptureHeight();
            } else {
                encoderWidth = mVideoCaptureConfigInfo.getVideoCaptureHeight();
                encoderHeight = mVideoCaptureConfigInfo.getVideoCaptureWidth();
            }
        }else{
            if (mVideoCaptureConfigInfo.getVideoCaptureWidth() > mVideoCaptureConfigInfo.getVideoCaptureHeight()) {
                encoderWidth = mVideoCaptureConfigInfo.getVideoCaptureHeight();
                encoderHeight = mVideoCaptureConfigInfo.getVideoCaptureWidth();
            } else {
                encoderWidth = mVideoCaptureConfigInfo.getVideoCaptureWidth();
                encoderHeight = mVideoCaptureConfigInfo.getVideoCaptureHeight();
            }

        }

        LogUtil.i("encoderWidth: " + encoderWidth + " encoderHeight:" + encoderHeight);
        final MediaFormat format = MediaFormat.createVideoFormat(
                mVideoEncoderConfigInfo.getVideoMimeType(),
                encoderWidth,
                encoderHeight

        );
        format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31);

        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, videoCodecInfo.getFormat());
        //recalculate video output bitrate
        mVideoEncoderConfigInfo.calcBitRate(mVideoCaptureConfigInfo.getVideoCaptureWidth(), mVideoCaptureConfigInfo.getVideoCaptureHeight(), mVideoCaptureConfigInfo.getVideoCaptureFps());
        format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoEncoderConfigInfo.getVideoEncodeBitrate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_KEY_IFRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mVideoEncoderConfigInfo.getVideoGop());
        if (videoCodecInfo.getMediaCodecInfo().getName().startsWith("OMX.rk.")) {
            format.setInteger("bitrate-mode", MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        } else {
            format.setInteger("bitrate-mode", MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        }

        LogUtil.i("format: " + format);

        mMediaCodec = MediaCodec.createEncoderByType(mVideoEncoderConfigInfo.getVideoMimeType());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // get Surface for encoder input
        // this method only can call between #configure and #start
        if (mVideoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            mSurface = mMediaCodec.createInputSurface();    // API >= 18
            mVideoPreProcessor.setEncoderInputSurface(mSurface);
        }
        mMediaCodec.start();
        this.startEncoderInternal();
        LogUtil.i("prepareEncoders finishing");
        return START_VIDEO_ENCODER_SUCCESS;
    }


    private boolean checkIsSupportedHardWareEncoder() {
        return false;
    }

    @Override
    public int deallocate() {
        LogUtil.i("deallocate " + mIsCapturing);
        mEncoderedDataConnector.clear();
        if (mIsCapturing) {
            return DEALLOCATE_VIDEO_ENCODER_FAILED;
        }
        if (mVideoPreProcessor != null) {
            mVideoPreProcessor.stopEncoderDataPrepare();
        }
        videoCodecInfo = null;
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mVideoPreProcessor = null;
        return DEALLOCATE_VIDEO_ENCODER_SUCCESS;
    }


    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaVideoCodecInfo selectVideoCodec(final String mimeType, int[] recognizedFormat) {
        LogUtil.v("selectVideoCodec");

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    LogUtil.i("codec:" + codecInfo.getName() + ",MIME=" + types[j]);
                    final int format = selectColorFormat(codecInfo, mimeType, recognizedFormat);
                    if (format > 0) {
                        return new MediaVideoCodecInfo(codecInfo, format);
                    }
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     *
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType, int[] recognizedFormat) {
        LogUtil.i("selectColorFormat");
        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            LogUtil.i("codec:" + codecInfo.getName() + ",colorFormats=" + caps.colorFormats[i]);
        }
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat, recognizedFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            LogUtil.e("couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }


    private static final boolean isRecognizedViewoFormat(final int colorFormat, int[] recognizedFormat) {
        final int n = recognizedFormat != null ? recognizedFormat.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormat[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void signalEndOfInputStream() {
        LogUtil.d("sending2 EOS to encoder frameType type " + frameType);
        //
        if (mVideoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            //only used in surface input
            mMediaCodec.signalEndOfInputStream();    // API >= 18
        } else {
            encode(null, 0, getPTSUs());
        }
        mIsEOS = true;
        LogUtil.d("sending1 EOS to encoder frameType type " + frameType);
    }


    public boolean updateSharedContext(EGLContext sharedContext) {
        if (mVideoPreProcessor == null) {
            return false;
        }
        return mVideoPreProcessor.updateSharedContext(sharedContext);
    }

    public boolean initEncoderContext(EGLContext eglContext) {
        if (mVideoPreProcessor == null) {
            return false;
        }
        return mVideoPreProcessor.initEncoderContext(eglContext);
    }

    @Override
    public void stop() {
        super.stop();
        if (mVideoPreProcessor != null) {
            mVideoPreProcessor.enableWriteVideoRawData(null, null, false);
        }
    }

    @Override
    public void onDataAvailable(CapturedFrame data) {
        if (mRequestStop) {
            return;
        }
        ProcessedData processedData = mVideoPreProcessor.preProcessVideoData(data);
        if (mVideoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.TEXTURE) {
            LogUtil.d("video onDataAvailable");
            if (processedData != null) {
                super.frameAvailableSoon();
            } else {
                LogUtil.e("texture input video not send to codec");
                return;
            }
        } else if (mVideoCaptureConfigInfo.getVideoCaptureType() == VideoCaptureConfigInfo.CaptureType.BYTE_ARRAY) {
            LogUtil.d("reLoadEncoder video raw onDataAvailable encode start:" + mRequestStop);
            if (processedData != null) {
                LogUtil.d("reLoadEncoder video raw onDataAvailable encode:" + processedData.mBuffer + " length:" + processedData.mLength + " time:" + processedData.mTimeStamp);
                encode(processedData.mBuffer, processedData.mLength, processedData.mTimeStamp);
            } else {
                LogUtil.e("rawData input video not send to codec");
                return;
            }
            super.frameAvailableSoon();

        }
    }

    public boolean reLoadEncoder(Object mediaData) throws Exception {
        this.stop();
        while (mBufferInfo != null) {
            try {
                //make sure thread stoped
                Thread.sleep(20);
            } catch (Exception e) {
                LogUtil.e(e.toString());
                return false;
            }
        }
        mVideoEncoderConfigInfo = (VideoEncoderConfigInfo) mediaData;
        this.start();
        LogUtil.d("try to reLoadEncoder :" + mRequestStop);
        return true;
    }

    public void enableWriteVideoRawData(String rawfilePath, String yuvfilePath) {
        if (mVideoPreProcessor != null) {
            mVideoPreProcessor.enableWriteVideoRawData(rawfilePath, yuvfilePath, true);
        }
    }
}
