package io.agora.rtcwithfu;

import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.rtc.IRtcEngineEventHandler;

public class MyRtcEngineEventHandler {
    public MyRtcEngineEventHandler(Context ctx, EngineConfig config) {
        this.mContext = ctx;
        this.mConfig = config;
    }

    private final EngineConfig mConfig;

    private final Context mContext;

    private final ConcurrentHashMap<RtcEngineEventHandler, Integer> mEventHandlerList = new ConcurrentHashMap<>();

    public void addEventHandler(RtcEngineEventHandler handler) {
        this.mEventHandlerList.put(handler, 0);
    }

    public void removeEventHandler(RtcEngineEventHandler handler) {
        this.mEventHandlerList.remove(handler);
    }

    final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        private final static String TAG = "IRtcEngineEventHandler";

        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            Log.d(TAG, "onFirstRemoteVideoDecoded " + (uid & 0xFFFFFFFFL) + width + " " + height + " " + elapsed);

            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
            Log.d(TAG, "onFirstLocalVideoFrame " + width + " " + height + " " + elapsed);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onUserJoined(uid, elapsed);
            }
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            // FIXME this callback may return times
            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onUserOffline(uid, reason);
            }
        }

        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
        }

        @Override
        public void onRtcStats(RtcStats stats) {
        }


        @Override
        public void onLeaveChannel(RtcStats stats) {

        }

        @Override
        public void onLastmileQuality(int quality) {
            Log.d(TAG, ("onLastmileQuality " + quality));
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            Log.e(TAG, ("onError " + err));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, ("onJoinChannelSuccess " + channel + " " + uid + " " + (uid & 0xFFFFFFFFL) + " " + elapsed));

            Iterator<RtcEngineEventHandler> it = mEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                RtcEngineEventHandler handler = it.next();
                handler.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, ("onRejoinChannelSuccess " + channel + " " + uid + " " + elapsed));
        }

        public void onWarning(int warn) {
            Log.w(TAG, ("onWarning " + warn));
        }
    };

}
