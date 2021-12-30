package io.agora.profile;

/**
 * FPS工具类
 * Created by tujh on 2018/5/24.
 */
public class FPSUtil {
    private static final int NANO_IN_ONE_MILLI_SECOND = 1000000;
    private static final int NANO_IN_ONE_SECOND = 1000 * NANO_IN_ONE_MILLI_SECOND;

    private static long sLastFrameTimeStamp = 0;

    /**
     * 每帧都计算
     *
     * @return
     */
    public static double fps() {
        long tmp = System.nanoTime();
        double fps = ((double) NANO_IN_ONE_SECOND) / (tmp - sLastFrameTimeStamp);
        sLastFrameTimeStamp = tmp;
//        Log.e(TAG, "FPS : " + fps);
        return fps;
    }

    private static long mStartTime = 0;

    /**
     * 平均值
     *
     * @return
     */
    public static double fpsAVG(int time) {
        long tmp = System.nanoTime();
        double fps = ((double) NANO_IN_ONE_SECOND) * time / (tmp - mStartTime);
        mStartTime = tmp;
//        Log.e(TAG, "FPS : " + fps);
        return fps;
    }

    private long mLimitMinTime = 33333333;
    private long mLimitStartTime;
    private int mLimitFrameRate;

    public void setLimitMinTime(long limitMinTime) {
        mLimitMinTime = limitMinTime;
    }

    public void limit() {
        try {
            if (mLimitFrameRate == 0 || mLimitFrameRate > 600000) {
                mLimitStartTime = System.nanoTime();
                mLimitFrameRate = 0;
            }
            long sleepTime = mLimitMinTime * mLimitFrameRate++ - (System.nanoTime() - mLimitStartTime);
            if (sleepTime > 0) {
                Thread.sleep(sleepTime / NANO_IN_ONE_MILLI_SECOND, (int) (sleepTime % NANO_IN_ONE_MILLI_SECOND));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
