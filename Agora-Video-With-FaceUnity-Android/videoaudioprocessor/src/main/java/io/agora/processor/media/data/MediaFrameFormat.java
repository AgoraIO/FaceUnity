package io.agora.processor.media.data;

import android.media.MediaFormat;

/**
 * Created by yong on 2019/8/30.
 */

public class MediaFrameFormat {
    public enum FrameType {
        AUDIO, VIDEO
    }

    private MediaFormat mediaFormat;

    public MediaFrameFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    public void setMediaFormat(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }
}
