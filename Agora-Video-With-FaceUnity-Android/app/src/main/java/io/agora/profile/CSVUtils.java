package io.agora.profile;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.agora.rtcwithfu.RtcEngineEventHandlerProxy;

/**
 * Created by tujh on 2017/11/2.
 */
public class CSVUtils {
    public static final String TAG = CSVUtils.class.getSimpleName();
    /* 每 100 帧统计一次 */
    public static final int FRAME_STEP = 100;

    public static final String COMMA = ",";

    private OutputStreamWriter mStreamWriter;

    private ActivityManager mActivityManager;

    private Handler mHandler;

    private CPUInfoUtil mCPUInfoUtil;

    private int mFrameRate;
    private volatile double mCpuUsed;
    private volatile double mAverageFps;
    private volatile double mAverageRenderTime;
    private volatile double mMemory;
    private long mSumRenderTimeInNano;
    private volatile long mTimestamp;

    private RtcEngineEventHandlerProxy mRtcEngineEventHandler;

    public void setRtcEngineEventHandler(RtcEngineEventHandlerProxy rtcEngineEventHandlerProxy) {
        mRtcEngineEventHandler = rtcEngineEventHandlerProxy;
    }

    public CSVUtils(Context context) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mCPUInfoUtil = new CPUInfoUtil(context);

        HandlerThread handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void initHeader(String folderName, StringBuilder headerInfo) {
        Log.d(TAG, "initHeader() called with: folderName = [" + folderName + "], headerInfo = [" + headerInfo + "]");
        StringBuilder stringBuilder = new StringBuilder().append("时间").append(COMMA)
                .append("帧率").append(COMMA)
                .append("渲染耗时").append(COMMA)
                .append("CPU").append(COMMA)
                .append("内存").append(COMMA)
                .append("远端分辨率").append(COMMA)
                .append("远端渲染帧率").append(COMMA)
                .append("远端解码帧率").append(COMMA)
                .append("远端接收码率").append(COMMA);
        if (headerInfo != null) {
            stringBuilder.append(headerInfo);
        }
        stringBuilder.append("\n");

        File file = new File(folderName);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            mStreamWriter = new OutputStreamWriter(new FileOutputStream(file, false), "GBK");
        } catch (IOException e) {
            Log.e(TAG, "CSVUtils: ", e);
        }
        flush(stringBuilder);
        mTimestamp = System.currentTimeMillis();
    }

    public void writeCsv(final StringBuilder extraInfo, long renderTimeInNano) {
        if (mStreamWriter == null) {
            return;
        }

        mSumRenderTimeInNano += renderTimeInNano;
        if (mFrameRate % FRAME_STEP == FRAME_STEP - 1) {
            mTimestamp = System.currentTimeMillis();
            mAverageFps = FPSUtil.fpsAVG(FRAME_STEP);
            mAverageRenderTime = (double) mSumRenderTimeInNano / FRAME_STEP / 1_000_000;
            mSumRenderTimeInNano = 0;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCpuUsed = mCPUInfoUtil.getProcessCpuUsed();
                    mMemory = MemoryInfoUtil.getMemory(mActivityManager.getProcessMemoryInfo(new int[]{Process.myPid()}));
                    String strCPU = String.format(Locale.getDefault(), "%.2f", mCpuUsed);
                    String strMemory = String.format(Locale.getDefault(), "%.2f", mMemory);

                    StringBuilder stringBuilder = new StringBuilder();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
                    RtcEngineEventHandlerProxy.StatsInfo statsInfo = mRtcEngineEventHandler.retrieveStatsInfo();
                    stringBuilder.append(dateFormat.format(new Date(mTimestamp))).append(COMMA)
                            .append(String.format(Locale.getDefault(), "%.2f", mAverageFps)).append(COMMA)
                            .append(String.format(Locale.getDefault(), "%.2f", mAverageRenderTime)).append(COMMA)
                            .append(strCPU).append(COMMA)
                            .append(strMemory).append(COMMA)
                            .append(statsInfo.width).append("x")
                            .append(statsInfo.height).append(COMMA)
                            .append(statsInfo.renderFps).append(COMMA)
                            .append(statsInfo.decoderFps).append(COMMA)
                            .append(statsInfo.receivedBitrate).append(COMMA);
                    Log.d(TAG, "Fps:" + String.format(Locale.getDefault(), "%.2f", mAverageFps)
                            + "    Render Time:" + String.format(Locale.getDefault(), "%.2f", mAverageRenderTime));
                    if (extraInfo != null) {
                        stringBuilder.append(extraInfo);
                    }
                    stringBuilder.append("\n");
                    flush(stringBuilder);
                }
            });
        }
        mFrameRate++;
    }

    private void flush(StringBuilder stringBuilder) {
        if (mStreamWriter == null) {
            return;
        }
        try {
            mStreamWriter.write(stringBuilder.toString());
            mStreamWriter.flush();
        } catch (IOException e) {
            Log.e(TAG, "flush: ", e);
        }
    }

    public void close() {
        Log.d(TAG, "close: ");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mStreamWriter != null) {
                    try {
                        mStreamWriter.close();
                        mStreamWriter = null;
                    } catch (IOException e) {
                        Log.e(TAG, "close: ", e);
                    }
                }
            }
        });
        mHandler.getLooper().quitSafely();
        mHandler = null;
        mCPUInfoUtil.close();
    }

    public double getCpuUsed() {
        return mCpuUsed;
    }

    public double getMemory() {
        return mMemory;
    }

    public double getAverageRenderTime() {
        return mAverageRenderTime;
    }

    public double getAverageFps() {
        return mAverageFps;
    }
}
