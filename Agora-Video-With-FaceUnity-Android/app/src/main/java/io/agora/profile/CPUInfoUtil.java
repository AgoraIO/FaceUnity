package io.agora.profile;

import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/**
 * cpu使用率获取工具类
 * Created by lirui on 2017/8/2.
 */

public class CPUInfoUtil {
    private long lastTotalCpu = 0;
    private long lastProcessCpu = 0;

    private final String PackageName;

    private volatile boolean isRunningCPU = false;
    private volatile double cpuRate = 0;

    public CPUInfoUtil(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            final String pn = context.getPackageName();
            if (pn.length() <= 16) {
                PackageName = pn;
            } else {
                PackageName = pn.substring(0, 15) + "+";
            }
//            Log.e(TAG, "CSVUtils PackageName " + PackageName);
            isRunningCPU = true;
            CPUInfoThread cpuinfothread = new CPUInfoThread();
            cpuinfothread.start();
        } else {
            PackageName = null;
        }
    }

    public double getProcessCpuUsed() {
        if (Build.VERSION.SDK_INT >= 26) {
            return cpuRate;
        } else {
            double pcpu = 0;
            double tmp = 1.0;
            long nowTotalCpu = getTotalCpu();
            long nowProcessCpu = getMyProcessCpu();
            if (nowTotalCpu != 0 && (nowTotalCpu - lastTotalCpu) != 0) {
//            Log.e(TAG, "cpu used nowProcessCpu " + nowProcessCpu + " lastProcessCpu " + lastProcessCpu + " nowTotalCpu " + nowTotalCpu + " lastTotalCpu " + lastTotalCpu);
                pcpu = 100 * (tmp * (nowProcessCpu - lastProcessCpu) / (nowTotalCpu - lastTotalCpu));
            }
            lastProcessCpu = nowProcessCpu;
            lastTotalCpu = nowTotalCpu;
            return pcpu < 0 ? 0 : pcpu;
        }
    }

    public void close() {
        if (Build.VERSION.SDK_INT >= 26) {
            isRunningCPU = false;
        }
    }

    private long getTotalCpu() {
        String[] cpuInfos = null;
        try {
            RandomAccessFile reader = null;
            reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        long totalCpu = 0;
        try {
            totalCpu = Long.parseLong(cpuInfos[2])
                    + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                    + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                    + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
        return totalCpu;
    }

    private long getMyProcessCpu() {
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        long appCpuTime = 0;
        try {
            appCpuTime = Long.parseLong(cpuInfos[13])
                    + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                    + Long.parseLong(cpuInfos[16]);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
        return appCpuTime;
    }

    class CPUInfoThread extends Thread {

        private double allCPU = 0;

        @Override
        public void run() {
            String line = null;
            InputStream is = null;
            try {

                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("top -d 1");
                is = proc.getInputStream();

                // 换成BufferedReader
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                do {
                    line = buf.readLine();
                    if (allCPU == 0 && line.contains("user") && line.contains("nice") && line.contains("sys") && line.contains("idle") && line.contains("iow") && line.contains("irq") && line.contains("sirq") && line.contains("host")) {
                        if (line.indexOf("%cpu ") > 0)
                            allCPU = Double.parseDouble(line.split("%cpu ")[0]);
                        if (allCPU == 0) {
                            String[] s = line.split("%,");
                            for (String st : s) {
                                String[] sts = st.split(" ");
                                if (sts.length > 0)
                                    allCPU += Double.parseDouble(sts[sts.length - 1]);
                            }
                        }
                    }
                    // 读取到相应pkgName跳出循环（或者未找到）
                    if (line == null || line.endsWith(PackageName)) {
//                        Log.e(TAG, "cpu line : " + line);
                        String str[] = line.split(" ");
                        int t = 0;
                        for (int i = str.length - 1; i > 0; i--) {
                            if (!str[i].isEmpty() && ++t == 4) {
//                                Log.e(TAG, "cpu : " + str[i] + " allCPU " + allCPU);
                                cpuRate = 100 * Double.parseDouble(str[i]) / allCPU;
                            }
                        }
                        continue;
                    }
                } while (isRunningCPU);

                if (is != null) {
                    buf.close();
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
