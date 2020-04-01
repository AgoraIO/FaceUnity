package io.agora.processor.media.data;

import android.media.MediaCodecInfo;

/**
 * Created by yong on 2019/9/29.
 */

public class MediaVideoCodecInfo {
    private MediaCodecInfo mediaCodecInfo;
    private int format;

    public MediaVideoCodecInfo(MediaCodecInfo mediaCodecInfo, int format) {
        this.mediaCodecInfo = mediaCodecInfo;
        this.format = format;
    }

    public MediaCodecInfo getMediaCodecInfo() {
        return mediaCodecInfo;
    }

    public int getFormat() {
        return format;
    }
}
