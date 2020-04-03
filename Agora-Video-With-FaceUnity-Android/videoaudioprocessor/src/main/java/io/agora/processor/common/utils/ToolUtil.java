package io.agora.processor.common.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.io.FileOutputStream;
import java.io.IOException;

import static io.agora.processor.common.constant.ConstantCode.CONNECT_ERROR_INTERNAL;
import static io.agora.processor.common.constant.ConstantCode.CONNECT_ERROR_RTMP;
import static io.agora.processor.common.constant.ConstantCode.CONNECT_ERROR_SOCKET;
import static io.agora.processor.common.constant.ConstantCode.CONNECT_ERROR_URL;

/**
 * Created by yong on 2019/9/6.
 */

public class ToolUtil {
    /**
     * @param fileName
     * @param arrayBytes
     * @throws IOException
     */
    public static void saveDataToFile(String fileName, byte[] arrayBytes) throws IOException {
        FileOutputStream file = new FileOutputStream(fileName, true);
        file.write(arrayBytes);
        file.close();
    }

    //TODO FLV只支持22,44 11 5.5

    /**
     * add adts head in audio packet
     * freqIdx:
     * 0: 96000 Hz
     * 1: 88200 Hz
     * 2: 64000 Hz
     * 3: 48000 Hz
     * 4: 44100 Hz
     * 5: 32000 Hz
     * 6: 24000 Hz
     * 7: 22050 Hz
     * 8: 16000 Hz
     * 9: 12000 Hz
     * 10: 11025 Hz
     * 11: 8000 Hz
     * 12: 7350 Hz
     * <p>
     * channelCfg:
     * 0: default
     * 1: 1
     * 2: 2
     */
    public static void addADTStoPacket(byte[] packet, int packetLen, int sampleRate, int channelCount) {
        int profile = 2; // AAC LC
        int freqIdx = 4; //4 标识44100，取特定
        switch (sampleRate) {
            case 44100:
                freqIdx = 4;
                break;
            case 22050:
                freqIdx = 7;
                break;
            case 11025:
                freqIdx = 10;
                break;
            default:
                freqIdx = 4;
                break;
        }

        int channelCfg = channelCount; // 音频声道数为两个

        // fill in ADTS data
        packet[0] = (byte) 0xFF;//1111 1111
        packet[1] = (byte) 0xF9;//1111 1001  1111 还是syncword
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (channelCfg >> 2));
        packet[3] = (byte) (((channelCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    public static void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    public static long getPTSUs(long prevOutputPTSUs) {
        long result = System.currentTimeMillis();
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }


    public static int getErrorCode(int connetErrrorCode) {
        int returnCode;
        if ((-24 <= connetErrrorCode) && (connetErrrorCode <= -22)) {
            returnCode = CONNECT_ERROR_URL;
        } else if (connetErrrorCode == -1) {
            returnCode = CONNECT_ERROR_INTERNAL;
        } else if ((-23 <= connetErrrorCode) && (connetErrrorCode <= -2)) {
            returnCode = CONNECT_ERROR_SOCKET;
        } else {
            returnCode = CONNECT_ERROR_RTMP;
        }
        return returnCode;
    }


    private static byte[] NV21_rotate_to_270(byte[] nv21_data, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        byte[] nv21_rotated = new byte[buffser_size];
        int i = 0;

// Rotate the Y luma
        for (int x = width - 1; x >= 0; x--) {
            int offset = 0;
            for (int y = 0; y < height; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }

// Rotate the U and V color components
        i = y_size;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i++;
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }
        return nv21_rotated;
    }


    private static byte[] NV21_rotate_to_90(byte[] nv21_data, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        byte[] nv21_rotated = new byte[buffser_size];
// Rotate the Y luma


        int i = 0;
        int startPos = (height - 1) * width;
        for (int x = 0; x < width; x++) {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset -= width;
            }
        }

// Rotate the U and V color components
        i = buffser_size - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i--;
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i--;
                offset += width;
            }
        }
        return nv21_rotated;
    }

    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }


}