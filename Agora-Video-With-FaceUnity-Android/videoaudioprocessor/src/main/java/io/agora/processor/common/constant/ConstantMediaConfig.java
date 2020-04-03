package io.agora.processor.common.constant;

import android.media.AudioFormat;
import android.opengl.EGLContext;

import io.agora.processor.media.data.VideoCaptureConfigInfo;

import static io.agora.processor.common.constant.Constant.CAMERA_FACING_BACK;
import static io.agora.processor.common.constant.Constant.CAMERA_FACING_FRONT;
import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureFormat.TEXTURE_OES;
import static io.agora.processor.media.data.VideoCaptureConfigInfo.CaptureType.TEXTURE;

/**
 * Created by yong on 2019/9/1.
 */

public class ConstantMediaConfig {
    public final static String VIDEO_MIME_TYPE = "video/avc";


    //video encoder
    public final static int VIDEO_GOP = 1;
    public final static float VIDEO_BPP = 0.08f;
    public final static int VIDEO_KEY_IFRAME_RATE = 30;

    //video capture
    public final static VideoCaptureConfigInfo.CaptureType VIDEO_CAPTURE_TYPE = TEXTURE; //0 texture ;1 byte array
    public final static VideoCaptureConfigInfo.CaptureFormat VIDEO_CAPTURE_FORMAT = TEXTURE_OES; //0 texture oes ;1 texture2d; 2 yuv420p //3 nv21;
    public final static int VIDEO_CAPTURE_WIDHT = 1280;
    public final static int VIDEO_CAPTURE_HEIGHT = 720;
    public final static int VIDEO_CAPTURE_FPS = 30;
    public final static boolean DEFAULT_ORIENTATION = false;
    public final static EGLContext VIDEO_CAPTURE_EGL_CONTEXT = null;
    public final static int CAMERA_FACE = CAMERA_FACING_FRONT;

    /********************************************************************************/
    //audio
    public final static String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    //audio encoder
    public final static int AUDIO_SAMPLE_RATE = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.

    //audio capture info
    public final static int AUDIO_CHANNEL_FORMAT = AudioFormat.CHANNEL_IN_MONO;    // 声道数 CHANNEL_IN_MONO 1;  CHANNEL_IN_STEREO 2
    public final static int AUDIO_PCM_BIT = AudioFormat.ENCODING_PCM_16BIT;



}
