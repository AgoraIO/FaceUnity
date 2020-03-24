package io.agora.rtcwithfu;

public class EngineConfig {
    public int mClientRole;
    public int mVideoProfile;
    public int mUid;
    public String mChannel;

    public void reset() {
        mChannel = null;
    }

    EngineConfig() {

    }
}
