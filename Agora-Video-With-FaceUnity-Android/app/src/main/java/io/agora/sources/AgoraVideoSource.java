package io.agora.sources;

import android.util.Log;

import io.agora.processor.common.connector.SinkConnector;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.VideoCapturedFrame;
import io.agora.processor.media.gles.VaryTools;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;

import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureFormat.NV21;
import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureFormat.TEXTURE_2D;
import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureFormat.TEXTURE_OES;
import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureType.BYTE_ARRAY;
import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureType.TEXTURE;

/**
 * Created by yong on 2019/10/6.
 */

public class AgoraVideoSource implements IVideoSource, SinkConnector<VideoCapturedFrame> {

    private static final String TAG = "AgoraVideoSource";

    private static final boolean DEBUG = false;

    private IVideoFrameConsumer mConsumer;
    private int bufferType = 0;
    private VideoCaptureConfigInfo mVideoCaptureConfigInfo;
    private boolean enablePushDataToAgora = false;
    private int rotation = 0;
    private VaryTools varyTools;

    public AgoraVideoSource(VideoCaptureConfigInfo videoCaptureConfigInfo) {
        this.mVideoCaptureConfigInfo = videoCaptureConfigInfo;
        this.enablePushDataToAgora = false;
    }

    @Override
    public boolean onInitialize(IVideoFrameConsumer observer) {
        Log.i(TAG, "onInitialize " + observer);
        mConsumer = observer;
        return true;
    }

    @Override
    public int getBufferType() {

        if (this.mVideoCaptureConfigInfo.getVideoCaptureType() == TEXTURE) {
            bufferType = MediaIO.BufferType.TEXTURE.intValue();
        }
        if (this.mVideoCaptureConfigInfo.getVideoCaptureType() == BYTE_ARRAY) {
            bufferType = MediaIO.BufferType.BYTE_ARRAY.intValue();
        }
        Log.i(TAG, "getBufferType " + bufferType);
        return bufferType;
    }

    @Override
    public void onDispose() {
        Log.i(TAG, "onDispose");
        mConsumer = null;
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
    }

    @Override
    public boolean onStart() {
        Log.i(TAG, "onStart");
        return true;
    }

    public IVideoFrameConsumer getConsumer() {
        Log.i(TAG, "getConsumer " + mConsumer);
        return mConsumer;
    }

    public void changeOrientation(int rotation) {
        this.rotation = rotation;
    }

    @Override
    public void onDataAvailable(VideoCapturedFrame data) {
        if (DEBUG) {
            Log.d(TAG, this + " onDataAvailable enablePushDataToAgora: " + enablePushDataToAgora + ", mConsumer: " + data);
        }
        if (!enablePushDataToAgora) {
            return;
        }
        if (mConsumer != null) {
            boolean needsFixWidthAndHeight = data.mRotation == 90 || data.mRotation == 270;
            if (this.rotation != 0) {
                needsFixWidthAndHeight = !needsFixWidthAndHeight;
                if (this.rotation == 1) {
                    data.mRotation = 0;
                } else {
                    data.mRotation = 180;
                }
            }
            if (this.mVideoCaptureConfigInfo.getVideoCaptureFormat() == TEXTURE_OES) {
                if (this.rotation != 0) {
                    varyTools = new VaryTools(data.mTexMatrix);
                    varyTools.translate(0.5f, 0.5f, 0);
                    if (this.rotation == 1) {
                        varyTools.rotate(-90, 0, 0, 1);
                    } else {
                        varyTools.rotate(90, 0, 0, 1);
                    }
                    varyTools.translate(-0.5f, -0.5f, 0);
                    data.mTexMatrix = varyTools.getFinalMatrix();
                }
                if (DEBUG) {
                    Log.d(TAG, "onDataAvailable consumeTextureFrame: TEXTURE_OES " + data);
                }
                mConsumer.consumeTextureFrame(data.mTextureId,
                        MediaIO.PixelFormat.TEXTURE_OES.intValue(), needsFixWidthAndHeight ? data.videoHeight : data.videoWidth,
                        needsFixWidthAndHeight ? data.videoWidth : data.videoHeight, 0, data.mTimeStamp, data.mMvpMatrix);

            }
            if (this.mVideoCaptureConfigInfo.getVideoCaptureFormat() == TEXTURE_2D) {
                if (this.rotation != 0) {
                    varyTools = new VaryTools(data.mTexMatrix);
                    varyTools.translate(0.5f, 0.5f, 0);
                    if (this.rotation == 1) {
                        varyTools.rotate(-90, 0, 0, 1);
                    } else {
                        varyTools.rotate(90, 0, 0, 1);
                    }
                    varyTools.translate(-0.5f, -0.5f, 0);
                    data.mTexMatrix = varyTools.getFinalMatrix();
                }
                if (DEBUG) {
                    Log.d(TAG, "onDataAvailable consumeTextureFrame: TEXTURE_2D " + data);
                }
                mConsumer.consumeTextureFrame(data.mEffectTextureId,
                        MediaIO.PixelFormat.TEXTURE_2D.intValue(), needsFixWidthAndHeight ? data.videoHeight : data.videoWidth,
                        needsFixWidthAndHeight ? data.videoWidth : data.videoHeight, 0, data.mTimeStamp, data.mTexMatrix);

            }
            if (this.mVideoCaptureConfigInfo.getVideoCaptureFormat() == NV21) {
//                try {
//                    ToolUtil.saveDataToFile("/sdcard/testnv12.yuv",data.rawData);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                if (DEBUG) {
                    Log.d(TAG, "onDataAvailable consumeByteArrayFrame: NV21 " + data);
                }
                mConsumer.consumeByteArrayFrame(data.rawData,
                        MediaIO.PixelFormat.NV21.intValue(), data.videoWidth,
                        data.videoHeight, data.mRotation, System.currentTimeMillis());

            }
        }

    }

    public void enablePushDataForAgora(boolean enabled) {
        this.enablePushDataToAgora = enabled;
    }
}
