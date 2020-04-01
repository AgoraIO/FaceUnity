package io.agora.processor.media.data;

import java.nio.ByteBuffer;

/**
 * Created by yong on 2019/9/19.
 */

public class VideoBufferWithMetaData {
    public VideoBufferWithMetaData(ByteBuffer videoKeyBufferWithMeta, int bufferSize, long presentTime) {
        this.videoKeyBufferWithMeta = videoKeyBufferWithMeta;
        this.bufferSize = bufferSize;
        this.presentTime = presentTime;
    }

    public ByteBuffer videoKeyBufferWithMeta;
    public int bufferSize;
    public long presentTime;
}
