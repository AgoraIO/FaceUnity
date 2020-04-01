package io.agora.processor.media.internal;


import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;


import io.agora.processor.common.utils.ToolUtil;
import io.agora.processor.media.base.BaseEncoder;
import io.agora.processor.media.data.EncodedFrame;
import io.agora.processor.media.data.MediaFrameFormat;
import io.agora.processor.media.data.VideoBufferWithMetaData;
import io.agora.processor.common.utils.LogUtil;

import static io.agora.processor.common.constant.Constant.LOCAL_ENCODED_AUDIO_FILE_PATH;
import static io.agora.processor.common.constant.Constant.LOCAL_ENCODED_VIDEO_FILE_PATH;
import static io.agora.processor.common.constant.Constant.LOCAL_RAW_AUDIO_FILE_PATH;
import static io.agora.processor.common.constant.Constant.TIMEOUT_USEC;


public abstract class MediaCodecEncoder extends BaseEncoder implements Runnable {
    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "MediaCodecEncoder";

    protected final Object mSync = new Object();
    /**
     * Flag that indicate this encoder is capturing now.
     */
    protected volatile boolean mIsCapturing;
    /**
     * Flag that indicate the frame data will be available soon.
     */
    private int mRequestDrain;
    /**
     * Flag to request stop capturing
     */
    protected volatile boolean mRequestStop;
    /**
     * Flag that indicate encoder received EOS(End Of Stream)
     */
    protected boolean mIsEOS;
    /**
     * Flag the indicate the muxer is running
     */
    protected boolean mOutputBufferEnabled;
    /**
     * MediaCodec instance for encoding
     */
    protected MediaCodec mMediaCodec;                // API >= 16(Android4.1.2)
    /**
     * BufferInfo instance for dequeuing
     */
    protected MediaCodec.BufferInfo mBufferInfo;        // API >= 16(Android4.1.2)

    protected MediaFrameFormat.FrameType frameType; //0为audio 1为video
    protected String codecThreadName;

    //sps &pps info
    private ByteBuffer configData = null;

    private MediaFormat mediaFormat = null;

    public MediaCodecEncoder(String codecThreadName) {
        this.codecThreadName = codecThreadName;

        // create BufferInfo here for effectiveness(to reduce GC)
        // wait for starting thread
        //启动当前线程
    }

    protected void startNewMediaCodecEncoderThread() {
        mBufferInfo = new MediaCodec.BufferInfo();
        new Thread(this, this.codecThreadName).start();
        //wait for thread start
        synchronized (mSync) {
            try {
                mSync.wait();
            } catch (final InterruptedException e) {
                LogUtil.e("MediaCodec thread start error");
            }
        }
        LogUtil.d("MediaCodec thread start");
    }


    /**
     * the method to indicate frame data is soon available or already available
     *
     * @return return true if encoder is ready to encod.
     */

    public boolean frameAvailableSoon() {

        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return false;
            }
            //use mRequestDrain as request num
            mRequestDrain++;
            mSync.notifyAll();
        }
        return true;
    }

    /**
     * encoding loop on private thread
     */
    @Override
    public void run() {
//		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        synchronized (mSync) {
            mRequestStop = false;
            mRequestDrain = 0;
            mSync.notify();
        }
        final boolean isRunning = true;
        boolean localRequestStop;
        boolean localRequestDrain;
        while (isRunning) {
            LogUtil.d("MediaCodecEncoder run MediaCodecEncoder type：" + frameType + " mRequestStop:" + mRequestStop);
            synchronized (mSync) {
                localRequestStop = mRequestStop;
                localRequestDrain = (mRequestDrain > 0);
                if (localRequestDrain)
                    mRequestDrain--;
            }
            if (localRequestStop) {
                drain();
                // request stop recording
                LogUtil.d("signalEndOfInputStream frameType " + frameType);
                signalEndOfInputStream();
                // process output data again for EOS signale
                drain();
                // release all related objects
                release();
                break;
            }
            if (localRequestDrain) {
                if (frameType == MediaFrameFormat.FrameType.VIDEO) {
                    LogUtil.d("drain() mRequestDrain:" + mRequestDrain);
                }
                drain();
            } else {
                synchronized (mSync) {
                    try {
                        LogUtil.d("video drain() mSync.wait() mRequestDrain:" + mRequestDrain);
                        mSync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        } // end of while
        LogUtil.d("Encoder thread exiting");
        synchronized (mSync) {
            mRequestStop = true;
            mIsCapturing = false;
        }
    }

    /*
    * prepareing method for each sub class
    * this method should be implemented in sub class, so set this as abstract method
    * @throws IOException
    */
    @Override
    public abstract int start() throws IOException;

    @Override
    public abstract void allocate();

    @Override
    public abstract int deallocate();


    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }


    protected void startEncoderInternal() {
        LogUtil.v("encoder : startEncoders");
        synchronized (mSync) {
            mIsCapturing = true;
            mRequestStop = false;
            mSync.notifyAll();
        }
    }

    /**
     * the method to request stop encoding
     */
    @Override
    public void stop() {
        LogUtil.i("try to stop "+frameType +" codec stop " + mIsCapturing + " " + mRequestStop);
        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return;
            }
            mRequestStop = true;    // for rejecting newer frame
            mSync.notifyAll();
            LogUtil.i("stop frameType:" + frameType);
            // We can not know when the encoding and writing finish.
            // so we return immediately after request to avoid delay of caller thread
        }
        while (mIsCapturing) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                LogUtil.e(e.toString());
            }
        }
        LogUtil.i(frameType +" codec stop " + mIsCapturing + " " + mRequestStop);
    }

//********************************************************************************
//********************************************************************************

    /**
     * Release all releated objects
     */
    private void release() {
        LogUtil.i("release mediaCodec " +mMediaCodec+" frameType"+frameType);
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            } catch (final Exception e) {
                LogUtil.e("failed releasing MediaCodec:" + e);
            }
        }
        LogUtil.i("release mediaCodec " + frameType);
        mBufferInfo = null;
        mIsCapturing = false;
    }

    protected abstract void signalEndOfInputStream();

    /**
     * Method to set byte array to the MediaCodec encoder
     *
     * @param buffer
     * @param length             　length of byte array, zero means EOS.
     * @param presentationTimeUs
     */
    protected void encode(final ByteBuffer buffer, final int length, final long presentationTimeUs) {
        if (!mIsCapturing) return;
        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        while (mIsCapturing) {
            final int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    // send EOS
                    mIsEOS = true;
                    LogUtil.i("reLoadEncoder send BUFFER_FLAG_END_OF_STREAM");
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    if (frameType == MediaFrameFormat.FrameType.VIDEO) {
                        LogUtil.d("queueInputBuffer:" + inputBufferIndex + " length:" + length);
                    }
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeUs, 0);
                }
                break;
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait for MediaCodec encoder is ready to encode
                // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                // will wait for maximum TIMEOUT_USEC(10msec) on each call
                LogUtil.d("encode:queueInputBuffer full");
                //break;
            }
        }
    }

    /**
     * drain encoded data and write them to muxer
     */
    @Override
    public void drain() {
        if (mMediaCodec == null) return;
        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        int encoderStatus, count = 0;
//        final AndroidMPEG4Writer muxer = mWeakMuxer.get();
//        if (muxer == null) {
////        	throw new NullPointerException("muxer is unexpectedly null");
//        	LogUtil.w("muxer is unexpectedly null");
//        	return;
//        }
        LOOP:
        while (mIsCapturing) {
            // get encoded data with maximum timeout duration of TIMEOUT_USEC(=10[msec])
            //LogUtil.d(frameType + " drain mBufferInfo" + mBufferInfo);
            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (frameType == MediaFrameFormat.FrameType.VIDEO) {
                LogUtil.d(frameType + "video drain encoderStatus" + encoderStatus);
            }
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait 5 counts(=TIMEOUT_USEC x 5 = 50msec) until data/EOS come
                if (!mIsEOS) {
                    if (++count > 5)
                        break LOOP;        // out of while
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                LogUtil.v("INFO_OUTPUT_BUFFERS_CHANGED");
                // this shoud not come when encoding
                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                LogUtil.v("INFO_OUTPUT_FORMAT_CHANGED");
                // this status indicate the output format of codec is changed
                // this should come only once before actual encoded data
                // but this status never come on Android4.3 or less
                // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
                if (mOutputBufferEnabled) {    // second time request is error
                    throw new RuntimeException("format changed twice");
                }
                // get output format from codec and pass them to muxer
                // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
                if (mRequestStop) {
                    break LOOP;
                }
                mediaFormat = mMediaCodec.getOutputFormat(); // API >= 16
                mOutputBufferEnabled = true;

            } else if (encoderStatus < 0) {
                // unexpected status
                LogUtil.w("drain:unexpected result from encoder#dequeueOutputBuffer: " + encoderStatus);
            } else {
                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    // this never should come...may be a MediaCodec internal error
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // You shoud set output format to muxer here when you target Android4.3 or less
                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                    // therefor we should expand and prepareEncoders output format from buffer data.
                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                    LogUtil.d("drain:BUFFER_FLAG_CODEC_CONFIG");
                    //audio in rtmp need this buffer
                    if (frameType == MediaFrameFormat.FrameType.VIDEO) {
                        configData = ByteBuffer.allocateDirect(mBufferInfo.size);
                        encoderOutputBuffers[encoderStatus].position(mBufferInfo.offset);
                        encoderOutputBuffers[encoderStatus].limit(mBufferInfo.offset + mBufferInfo.size);
                        configData.put(encoderOutputBuffers[encoderStatus]);
                        //remove this,may cause trouble in local record
                        /**annotation this code ,so the  first buffer muxer could remove **/
                        mBufferInfo.size = 0;
                    }
                }
                if (mBufferInfo.size != 0) {
                    // encoded data is ready, clear waiting counter
                    count = 0;
                    if (!mOutputBufferEnabled) {
                        if (mRequestStop) {
                            break LOOP;
                        }
                        throw new RuntimeException("drain:muxer hasn't started");
                    }
                    // write encoded data to muxer(need to adjust presentationTimeUs.
                    mBufferInfo.presentationTimeUs = getPTSUs();
                    //mBufferInfo.presentationTimeUs = mBufferInfo.presentationTimeUs/1000;
                    //muxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
//                    if (frameType == MediaFrameFormat.FrameType.VIDEO) {
//                        LogUtil.i(frameType + "video drain data" + mBufferInfo.presentationTimeUs);
//                    }
                    boolean keyFrame = (mBufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
                    if (frameType == MediaFrameFormat.FrameType.VIDEO && keyFrame) {
                        ByteBuffer keyFrameBuffer = ByteBuffer.allocateDirect(
                                configData.capacity() + mBufferInfo.size);
                        configData.rewind();
                        keyFrameBuffer.put(configData);
                        keyFrameBuffer.put(encodedData);
                        keyFrameBuffer.position(0);
                        mEncoderedDataConnector.onDataAvailable(new EncodedFrame(
                                frameType, encodedData, mBufferInfo,
                                mediaFormat,
                                new VideoBufferWithMetaData(keyFrameBuffer, keyFrameBuffer.capacity(), mBufferInfo.presentationTimeUs)));
//                        mBufferInfo.size = keyFrameBuffer.capacity();
//                        mEncoderedDataConnector.onDataAvailable(new EncodedFrame(
//                                frameType, keyFrameBuffer, mBufferInfo, mediaFormat,
//                                null));
                        keyFrameBuffer.clear();
                    } else {
                        mEncoderedDataConnector.onDataAvailable(new EncodedFrame(
                                frameType, encodedData, mBufferInfo, mediaFormat,
                                null));
                    }

                    prevOutputPTSUs = mBufferInfo.presentationTimeUs;
                }
                // return buffer to encoder
                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    // when EOS come.
                    mIsCapturing = false;
                    break;      // out of while
                }
            }
        }
    }

    /**
     * previous presentationTimeUs for writing
     */
    private long prevOutputPTSUs = 0;

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    protected long getPTSUs() {
        //long result = System.currentTimeMillis();
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }

}
