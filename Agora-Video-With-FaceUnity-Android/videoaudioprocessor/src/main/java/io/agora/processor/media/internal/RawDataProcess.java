package io.agora.processor.media.internal;

/**
 * Created by yong on 2019/9/29.
 */

public class RawDataProcess {
    /**
     *
     * @param pData
     * @param nLen
     * @param nWidth
     * @param nHeight
     * @param pixFmt 0:NV21 1 NV12
     * @param nDegree
     * @param pOu
     */
    static {
        System.loadLibrary("raw_data_process");
    }
    public native static int formatToI420(byte[] pData, int nLen, int nWidth, int nHeight, int pixFmt, int nDegree, byte[] pOu);


    public native static int I420toNV12(byte[] srcData, byte[] destData, int nWidth, int nHeight);


    public native static int I420Mirror(byte[] srcData, byte[] destData, int nWidth, int nHeight);
}
