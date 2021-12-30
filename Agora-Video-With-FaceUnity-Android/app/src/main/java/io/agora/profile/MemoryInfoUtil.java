package io.agora.profile;

import android.os.Build;
import android.os.Debug;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Field;

/**
 * 内存使用率获取工具类
 * Created by tujh on 2018/5/24.
 */
public abstract class MemoryInfoUtil {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static double getMemory(Debug.MemoryInfo[] memoryInfos) {
        try {
            if (Build.VERSION.SDK_INT >= 27) {
                return compute(memoryInfos);
            }

            Field otherStats_Field = memoryInfos[0].getClass().getDeclaredField("otherStats");
            otherStats_Field.setAccessible(true);
            int[] otherStats = (int[]) otherStats_Field.get(memoryInfos[0]);

            Field NUM_CATEGORIES_Field = memoryInfos[0].getClass().getDeclaredField("NUM_CATEGORIES");
            Field offsetPrivateClean_Field = memoryInfos[0].getClass().getDeclaredField("offsetPrivateClean");
            Field offsetPrivateDirty_Field = memoryInfos[0].getClass().getDeclaredField("offsetPrivateDirty");
            final int NUM_CATEGORIES = (int) NUM_CATEGORIES_Field.get(memoryInfos[0]);
            final int offsetPrivateClean = (int) offsetPrivateClean_Field.get(memoryInfos[0]);
            final int offsetPrivateDirty = (int) offsetPrivateDirty_Field.get(memoryInfos[0]);

            int javaHeap = memoryInfos[0].dalvikPrivateDirty
                    + otherStats[12 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[12 * NUM_CATEGORIES + offsetPrivateDirty];
            int nativeHeap = memoryInfos[0].nativePrivateDirty;
            int code = otherStats[6 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[6 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[7 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[7 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[8 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[8 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[9 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[9 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[10 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[10 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[11 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[11 * NUM_CATEGORIES + offsetPrivateDirty];
            int stack = otherStats[NUM_CATEGORIES + offsetPrivateDirty];
            int graphics = otherStats[4 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[4 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[14 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[14 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[15 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[15 * NUM_CATEGORIES + offsetPrivateDirty];
            int other = otherStats[0 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[0 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[2 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[2 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[3 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[3 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[5 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[5 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[13 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[13 * NUM_CATEGORIES + offsetPrivateDirty]
                    + otherStats[16 * NUM_CATEGORIES + offsetPrivateClean] + otherStats[16 * NUM_CATEGORIES + offsetPrivateDirty];
            int all = javaHeap + nativeHeap + code + stack + graphics + other;
//            Log.d("MemoryAll", "javaHeap=" + javaHeap
//                    + "\nnativeHeap=" + nativeHeap + "\ncode=" + code + "\nstack=" + stack
//                    + "\ngraphics=" + graphics + "\nother=" + other);
//                    Log.e(TAG, "memory " + memoryStr
//                            + " java-heap " + String.format("%.2f", ((double) javaHeap) / 1024)
//                            + " native-heap " + String.format("%.2f", ((double) nativeHeap) / 1024)
//                            + " code " + String.format("%.2f", ((double) code) / 1024)
//                            + " stack " + String.format("%.2f", ((double) stack) / 1024)
//                            + " graphics " + String.format("%.2f", ((double) graphics) / 1024)
//                            + " other " + String.format("%.2f", ((double) other) / 1024)
//                            + " pps " + String.format("%.2f", ((double) memoryInfos[0].getTotalPss()) / 1024)
//                    );
            return ((double) all) / 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private static double compute(Debug.MemoryInfo[] memInfo) {
        String java_mem = memInfo[0].getMemoryStat("summary.java-heap");
        String native_mem = memInfo[0].getMemoryStat("summary.native-heap");
        String graphics_mem = memInfo[0].getMemoryStat("summary.graphics");
        String stack_mem = memInfo[0].getMemoryStat("summary.stack");
        String code_mem = memInfo[0].getMemoryStat("summary.code");
        String others_mem = memInfo[0].getMemoryStat("summary.system");
        int all = Integer.parseInt(java_mem)
                + Integer.parseInt(native_mem)
                + Integer.parseInt(graphics_mem)
                + Integer.parseInt(stack_mem)
                + Integer.parseInt(code_mem)
                + Integer.parseInt(others_mem);
        return ((double) all) / 1024;
    }
}
