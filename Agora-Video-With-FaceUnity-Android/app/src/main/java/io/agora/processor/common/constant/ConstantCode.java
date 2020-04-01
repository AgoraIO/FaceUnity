package io.agora.processor.common.constant;

/**
 * Created by yong on 2019/9/23.
 */

public class ConstantCode {
    /*******************************AUDIO CAPTURE error code*****************************/


    public final static int START_AUDIO_CAPTURE_SUCCESS = 0;
    public final static int START_AUDIO_CAPTURE_FAILED = -1;

    public final static int DEALLOCATE_AUDIO_CAPTURE_SUCCESS = 0;
    public final static int DEALLOCATE_AUDIO_CAPTURE_FAILED = -1;

    public final static int START_VIDEO_ENCODER_SUCCESS = 0;
    public final static int START_VIDEO_ENCODER_FAILED = -1;

    public final static int DEALLOCATE_VIDEO_ENCODER_SUCCESS = 0;
    public final static int DEALLOCATE_VIDEO_ENCODER_FAILED = -1;

    public final static int START_AUDIO_ENCODER_SUCCESS = 0;
    public final static int START_AUDIO_ENCODER_FAILED = -1;

    public final static int DEALLOCATE_AUDIO_ENCODER_SUCCESS = 0;
    public final static int DEALLOCATE_AUDIO_ENCODER_FAILED = -1;

    public final static int START_MUXER_SUCCESS = 0;
    public final static int START_MUXER_FAILED = -1;

    public final static int DEALLOCATE_MUXER_SUCCESS = 0;
    public final static int DEALLOCATE_MUXER_FAILED = -1;
    /*****************************************************************************************/


    /*****************************RTMP CONNECT ERROR CODE**************************************/
    public final static int CONNECT_ERROR_INTERNAL = -1;
    public final static int CONNECT_ERROR_URL = -2;
    public final static int CONNECT_ERROR_SOCKET = -3;
    public final static int CONNECT_ERROR_RTMP = -4;
    public final static int ERROR_RTMP_DISCONNECT = -5;
    /******************************************************************************************/


    /****************************RTMP Send ERROR CODE******************************************/
    public final static int ERROR_DROP_FRAME_INTERNAL = 0;

    public final static int ERROR_NOT_INIT = -1;

    public final static int ERROR_DISCONNECT = -2;

    public final static int ERROR_RECONNECTING = -3;

    public final static int ERROR_BUFFER_FULL = -4;

    public final static int ERROR_AUDIO_PTS = -5;

    public final static int ERROR_VIDEO_PTS = -6;

    public final static int ERROR_DROP_NONE_KEY_FRAME_FOR_NET_WEAK = -7;

    public final static int ERROR_DROP_VIDEO_FRAME_FOR_NET_WEAK = -8;
    /*******************************************************************************************/


}
