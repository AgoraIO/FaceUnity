package io.agora.rtcwithfu;

/**
 * Created by Yao Ximing on 2018/2/4.
 */

public class Constants {
    public static final String ACTION_KEY_ROOM_NAME = "ecHANEL";
    public static final int UID = 0;

    public static int[] VIDEO_PROFILES = new int[]{
            io.agora.rtc.Constants.VIDEO_PROFILE_120P,
            io.agora.rtc.Constants.VIDEO_PROFILE_180P,
            io.agora.rtc.Constants.VIDEO_PROFILE_240P,
            io.agora.rtc.Constants.VIDEO_PROFILE_360P,
            io.agora.rtc.Constants.VIDEO_PROFILE_480P,
            io.agora.rtc.Constants.VIDEO_PROFILE_720P};

    public static final int DEFAULT_PROFILE_IDX = 3;
}
