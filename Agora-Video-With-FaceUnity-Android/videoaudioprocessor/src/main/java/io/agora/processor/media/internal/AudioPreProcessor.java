package io.agora.processor.media.internal;

import java.nio.ByteBuffer;

import io.agora.processor.common.utils.LogUtil;
import io.agora.processor.common.utils.ToolUtil;
import io.agora.processor.media.data.AudioCapturedFrame;
import io.agora.processor.media.data.CapturedFrame;
import io.agora.processor.media.data.MediaFrameFormat;
import io.agora.processor.media.data.ProcessedData;
import io.agora.processor.media.data.VideoCaptureConfigInfo;
import io.agora.processor.media.data.VideoCapturedFrame;
import io.agora.processor.media.gles.core.EglCore;
import io.agora.processor.media.gles.core.Program;
import io.agora.processor.media.gles.core.WindowSurface;

/**
 * Created by yong on 2019/8/31.
 */

/**
 * this class is used to divide encoder and capture module,
 * need to set to render thread after video encoder prepare
 * it worked just like a render when use with mediacodec and texture input,
 * raw data use connector ,texture direct to mediacodec input surface
 * <p>
 * usage:
 * construct the instance -->connect to the capture module-->connected by the encoder module
 * -->initEncoderContext(texture input only,make sure video codec has start and used in thread with egl conetx)-->setTextureId/frameAvailable-->
 * updateSharedContext(when needed)-->stopEncoderDataPrepare
 */
public class AudioPreProcessor {

    private Object mReadyFence = new Object();      // guards ready/running

    private long prevAudioOutputPTSUs = 0;
    private long currentAudioPTSUs = 0;

    public ProcessedData preProcessAudioData(CapturedFrame capturedFrame) {
        AudioCapturedFrame audioCapturedFrame = (AudioCapturedFrame) capturedFrame;
        final ByteBuffer buf = ByteBuffer.allocateDirect(audioCapturedFrame.mLength);
        buf.clear();
        buf.put(audioCapturedFrame.rawData);
        buf.flip();
        currentAudioPTSUs = ToolUtil.getPTSUs(prevAudioOutputPTSUs);
        ProcessedData processedData = new ProcessedData(buf, audioCapturedFrame.mLength, currentAudioPTSUs, audioCapturedFrame.frameType);
        prevAudioOutputPTSUs = currentAudioPTSUs;
        return processedData;
    }

    public void resetProcessAudioDataState() {
        prevAudioOutputPTSUs = 0;
        currentAudioPTSUs = 0;
    }

}
