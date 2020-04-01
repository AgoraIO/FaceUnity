package io.agora.processor.media.internal;

/**
 * Created by faraklit on 08.02.2016.
 */
public class RTMPStreamer {

    static {
        System.loadLibrary("agora_media_kit_rtmp");
    }


    /**
     * @param url  a valid rtmp url
     * @param videoWidth
     * @param videoHeight
     * @return >0 init success ,<=0 failed;
     */
    public native int initRtmp(String url, int videoWidth, int videoHeight);

    public native void closeRtmp();

    /**
     * @return 1 if it is connected
     * 0 if it is not connected
     */
    public native boolean isConnected();

    /**
     * write a complete h264 frame
     *
     * @param data
     * @param length
     * @param timestamp recommend use millisecond
     * @param isKeyFrame  set true if it is a key frame
     * @return 0 if it writes network successfully
     * <0 if it could not write
     */
    public native int sendVideo(byte[] data, int length, long timestamp, boolean isKeyFrame);

    /**
     * Write a complete aac frame
     *
     * @param data
     * @param length
     * @param timestamp recommend use millisecond
     * @param isHasAdts  set true if the frame have a adts head
     * @return 0 if it writes network successfully
     * <0 if it could not write
     */
    public native int sendAudio(byte[] data, int length, long timestamp, boolean isHasAdts);


    /**
     * enable write local file  while send rtmp stream
     * @param filename an valid file path
     * @param isHaveAudio  set true if you need write audio data to local file
     * @param isHaveVideo  set true if you need write video data to local file
     * @return
     */
    public native boolean localFileOpen(String filename, boolean isHaveAudio, boolean isHaveVideo);

    /**
     * make sure to use fileClose when use fileOpen
     * @return
     */
    public native boolean localFileClose();

}
