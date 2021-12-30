package io.agora.rtcwithfu;

import java.util.ArrayList;

import io.agora.rtc.IRtcEngineEventHandler;

public class RtcEngineEventHandlerProxy extends IRtcEngineEventHandler {
    private static final String TAG = "RtcEngineEventHandlerPr";
    private ArrayList<RtcEngineEventHandler> mEventHandlers = new ArrayList<>();

    public void addEventHandler(RtcEngineEventHandler handler) {
        if (!mEventHandlers.contains(handler)) mEventHandlers.add(handler);
    }

    public void removeEventHandler(RtcEngineEventHandler handler) {
        mEventHandlers.remove(handler);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        for (RtcEngineEventHandler handler : mEventHandlers) {
            handler.onJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        for (RtcEngineEventHandler handler : mEventHandlers) {
            handler.onUserJoined(uid, elapsed);
        }
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        for (RtcEngineEventHandler handler : mEventHandlers) {
            handler.onUserOffline(uid, reason);
        }
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        for (RtcEngineEventHandler handler : mEventHandlers) {
            handler.onRemoteVideoStateChanged(uid, state, reason, elapsed);
        }
    }

    private int mCallbackCount;
    private int mWidth;
    private int mHeight;
    private int mSumRenderFps;
    private int mSumDecoderFps;
    private int mSumReceivedBitrate;

    public static class StatsInfo {
        public int width;
        public int height;
        public int renderFps;
        public int decoderFps;

        public int receivedBitrate;
    }

    public StatsInfo retrieveStatsInfo() {
        StatsInfo statsInfo = new StatsInfo();
        synchronized (this) {
            statsInfo.width = mWidth;
            statsInfo.height = mHeight;
            int count = mCallbackCount;
            if (count > 0) {
                statsInfo.renderFps = mSumRenderFps / count;
                statsInfo.decoderFps = mSumDecoderFps / count;
                statsInfo.receivedBitrate = mSumReceivedBitrate / count;
            }
            mCallbackCount = 0;
            mSumRenderFps = 0;
            mSumDecoderFps = 0;
            mSumReceivedBitrate = 0;
        }
        return statsInfo;
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats remoteVideoStats) {
        super.onRemoteVideoStats(remoteVideoStats);
        //        Log.d(TAG, String.format("onRemoteVideoStats. width %d, height %d, " +
//                        "delay %d, receivedBitrate %d, decoderOutputFrameRate %d, rendererOutputFrameRate %d, " +
//                        "packetLossRate %d, totalFrozenTime %d, totalActiveTime %d, rxStreamType %d",
//                remoteVideoStats.width, remoteVideoStats.height, remoteVideoStats.delay, remoteVideoStats.receivedBitrate,
//                remoteVideoStats.decoderOutputFrameRate, remoteVideoStats.rendererOutputFrameRate,
//                remoteVideoStats.packetLossRate, remoteVideoStats.totalFrozenTime, remoteVideoStats.totalActiveTime,
//                remoteVideoStats.rxStreamType));
        synchronized (this) {
            mWidth = remoteVideoStats.width;
            mHeight = remoteVideoStats.height;
            mSumRenderFps += remoteVideoStats.rendererOutputFrameRate;
            mSumDecoderFps += remoteVideoStats.decoderOutputFrameRate;
            mSumReceivedBitrate += remoteVideoStats.receivedBitrate;
            ++mCallbackCount;
        }
    }
}
