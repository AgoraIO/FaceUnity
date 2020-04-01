package io.agora.processor.media.internal;

/**
 * Created by yong on 2019/9/24.
 */

public interface IRenderListener {

    void onEGLContextReady();

    void onViewIsPortrait(boolean isPortrait);
}
