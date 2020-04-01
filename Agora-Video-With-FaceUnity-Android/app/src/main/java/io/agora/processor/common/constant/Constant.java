package io.agora.processor.common.constant;

import android.media.MediaCodecInfo;
import android.media.MediaRecorder;

public class Constant {
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_FACING_BACK = 0;
    public static final int CAMERA_FACING_INVALID = -1;

    //audio source priority
    public static final int[] AUDIO_SOURCES = new int[]{
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };


    /**
     * color formats that we can use in this class
     */
    public static final int[] recognizedSurfaceFormats = new int[]{

                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };

    public static final int[] recognizedRawDataFormats = new int[]{
            //most android phone camera only support those two format
            //and most mediaCodec only support I420
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,  //I420  19
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,  //NV12   21 support most

            //MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
    };

    public final static int AUDIO_PER_FRAME = 1024;    // AAC, bytes/frame/channel
    public final static int AUDIO_FRAME_BUFFER = 25;    // AAC, frame/buffer/sec
    public final static int AUDIO_BIT_RATE = 64000;
    //encoder config
    public static final int TIMEOUT_USEC = 10000;    // 10[msec]

    public final static int FIX_VIDEO_FPS = 30;

    //enalbe audio raw data record
    public static final String LOCAL_RAW_AUDIO_FILE_PATH = "/sdcard/test.pcm";

    public static final String LOCAL_ENCODED_AUDIO_FILE_PATH = "/sdcard/test.aac";

    public static final String LOCAL_ENCODED_VIDEO_FILE_PATH = "/sdcard/test.H264";

    public static final String LOCAL_RAW_VIDEO_FILE_PATH = "/sdcard/testVideo.raw";

    public static final String LOCAL_YUV_VIDEO_FILE_PATH = "/sdcard/testVideo.yuv";
}
