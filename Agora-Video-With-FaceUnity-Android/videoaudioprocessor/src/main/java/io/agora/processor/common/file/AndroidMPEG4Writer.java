package io.agora.processor.common.file;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.media.MediaCodec;
import android.media.MediaMuxer;


import io.agora.processor.common.utils.ToolUtil;
import io.agora.processor.media.base.BaseMuxer;
import io.agora.processor.media.data.MediaFrameFormat;
import io.agora.processor.media.data.EncodedFrame;
import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.media.data.RtmpConfigInfo;

import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_MUXER_FAILED;
import static io.agora.processor.common.constant.ConstantCode.DEALLOCATE_MUXER_SUCCESS;
import static io.agora.processor.common.constant.ConstantCode.START_MUXER_FAILED;
import static io.agora.processor.common.constant.ConstantCode.START_MUXER_SUCCESS;

public class AndroidMPEG4Writer extends BaseMuxer {
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private String mOutputPath;
    private MediaMuxer mMediaMuxer;    // API >= 18
    private int videoIndex, audioIndex;
    private boolean mIsStarted = false;
    private boolean mIsFirstAudioPacketRecived = false;
    private boolean mIsFirstVideoPacketRecived = false;
    private boolean mIsMuxerRealyStarted = false;
    private boolean mIsFirstIframeCome = false;
    /**
     * Constructor
     * @param localFilePath
     * @throws Exception
     */
    public AndroidMPEG4Writer(String localFilePath) throws Exception {
        super(true,true);
        try {
            if (localFilePath != null) {
                mOutputPath = localFilePath;
            } else {
                throw new Exception("local file path is null");
            }
        } catch (final NullPointerException e) {
            throw new RuntimeException(e + " no permission to write local file");
        }

    }


    @Override
    public synchronized boolean isMuxerStarted() {
        return mIsStarted;
    }

//**********************************************************************
//**********************************************************************


    @Override
    public void allocate() throws IOException {
        mIsStarted = false;
    }

    /**
     * request start recording from encoder
     *
     * @return true when muxer is ready to write
     */
    @Override
    public int start() {
        if (mIsStarted) {
            return START_MUXER_FAILED;
        }
        try {
            mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIsStarted = true;
        mIsMuxerRealyStarted = false;
        //notifyAll();
        LogUtil.v("MediaMuxer started");
        return START_MUXER_SUCCESS;
    }

    /**
     * request stop recording from encoder when encoder received EOS
     */
    @Override
    public void stop() {
        //avoid error;
        if(!mIsStarted){
           return;
        }
        mIsStarted = false;
        mIsFirstIframeCome = false;
        mIsFirstAudioPacketRecived = false;
        mIsFirstVideoPacketRecived = false;
        mMediaMuxer.stop();
        mMediaMuxer.release();
        LogUtil.i("MediaMuxer stopped");
    }

    @Override
    public int deallocate() {
        if (mIsStarted) {
            return DEALLOCATE_MUXER_FAILED;
        }
        mIsStarted = false;
        mIsMuxerRealyStarted = false;
        mMediaMuxer = null;
        return DEALLOCATE_MUXER_SUCCESS;
    }

    /**
     * @param encodedFrame these code is strange for MediaMuxer need mediaFornat info to init before start
     */
    @Override
    protected synchronized void writeEncodedData(EncodedFrame encodedFrame) {
        if (!mIsStarted) {
            return;
        }
        if (encodedFrame.getFrameType() == MediaFrameFormat.FrameType.AUDIO) {
            if (enableAudio) {
                //LogUtil.w("writeEncodedData encodedFrame "+encodedFrame.getFrameType()+" "+encodedFrame.getmBufferInfo().size);
                if (!mIsFirstAudioPacketRecived) {
                    mIsFirstAudioPacketRecived = true;
                    audioIndex = mMediaMuxer.addTrack(encodedFrame.getMediaFormat());
                    //LogUtil.w("audioIndex addTrack:"+audioIndex);
                }
                if (isEnbaleToWrite()) {
                    if (!mIsMuxerRealyStarted) {
                        mIsMuxerRealyStarted = true;
                        mMediaMuxer.start();
                        mMediaMuxer.writeSampleData(videoIndex, encodedFrame.getEncodedByteBuffer(), encodedFrame.getmBufferInfo());
                    } else {
                        //LogUtil.w("audioIndex writeEncodedData 2");
                        mMediaMuxer.writeSampleData(audioIndex, encodedFrame.getEncodedByteBuffer(), encodedFrame.getmBufferInfo());




//                        ByteBuffer mBuffer = encodedFrame.getEncodedByteBuffer();
//                        mBuffer.position(0);
//                        int readLength = encodedFrame.getmBufferInfo().size - mBuffer.position();
//                        byte[] buffer = new byte[readLength];
//                        mBuffer.get(buffer, 0, readLength);
//                        try {
//                            ToolUtil.saveDataToFile("/sdcard/test.aac",buffer);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            }
        } else {
            boolean keyFrame = (encodedFrame.getmBufferInfo().flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
            if (enableVideo) {
                //LogUtil.w("writeEncodedData encodedFrame "+encodedFrame.getFrameType()+" "+encodedFrame.getmBufferInfo().size);
                if (!mIsFirstVideoPacketRecived) {
                    mIsFirstVideoPacketRecived = true;
                    videoIndex = mMediaMuxer.addTrack(encodedFrame.getMediaFormat());
                    LogUtil.w("videoIndex addTrack:"+videoIndex+" formate:"+encodedFrame.getMediaFormat());
                }
                if (isEnbaleToWrite()) {
                    if (!mIsMuxerRealyStarted) {
                        mIsMuxerRealyStarted = true;
                        mMediaMuxer.start();
                        mMediaMuxer.writeSampleData(videoIndex, encodedFrame.getEncodedByteBuffer(), encodedFrame.getmBufferInfo());
                    } else {
                        mMediaMuxer.writeSampleData(videoIndex, encodedFrame.getEncodedByteBuffer(), encodedFrame.getmBufferInfo());
//                        if(keyFrame&&!mIsFirstIframeCome){
//                            mIsFirstIframeCome = true;
//                        }
//                        if(mIsFirstIframeCome){
//                            LogUtil.w("videoIndex writeEncodedData 2:"+keyFrame);
//
//                            mMediaMuxer.writeSampleData(videoIndex, encodedFrame.getEncodedByteBuffer(), encodedFrame.getmBufferInfo());
//
//
//                            ByteBuffer mBuffer = encodedFrame.getEncodedByteBuffer();
//                            mBuffer.position(0);
//                            int readLength = encodedFrame.getmBufferInfo().size - mBuffer.position();
//                            byte[] buffer = new byte[readLength];
//                            mBuffer.get(buffer, 0, readLength);
//                            try {
//                                ToolUtil.saveDataToFile("/sdcard/test.h264",buffer);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
                    }
                }
            }
        }
    }

    public boolean isEnbaleToWrite() {
        if (enableAudio && enableVideo) {
            return mIsFirstAudioPacketRecived && mIsFirstVideoPacketRecived;
        }
        if (enableVideo) {
            return mIsFirstVideoPacketRecived;
        }
        if (enableAudio) {
            return mIsFirstAudioPacketRecived;
        }
        return false;
    }


    @Override
    public void onDataAvailable(EncodedFrame data) {
        this.writeEncodedData(data);
        return;
    }

}
