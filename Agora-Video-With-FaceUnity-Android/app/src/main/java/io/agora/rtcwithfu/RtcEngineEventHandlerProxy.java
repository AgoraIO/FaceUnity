package io.agora.rtcwithfu;

import java.util.ArrayList;

import io.agora.rtc.IRtcEngineEventHandler;

public class RtcEngineEventHandlerProxy extends IRtcEngineEventHandler {
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
}
