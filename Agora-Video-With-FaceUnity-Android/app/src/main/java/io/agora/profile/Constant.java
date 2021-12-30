package io.agora.profile;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by tujh on 2018/2/7.
 */

public class Constant {
    public static final String APP_NAME = "AgoraVideo";
    public static final String filePath = Environment.getExternalStoragePublicDirectory("")
            + File.separator + "FaceUnity" + File.separator + APP_NAME + File.separator;

    public static final String DICMFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
    public static final String photoFilePath;
    public static final String cameraFilePath;

    static {
        if (Build.FINGERPRINT.contains("Flyme")
                || Pattern.compile("Flyme", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("Meizu")
                || Build.MANUFACTURER.contains("MeiZu")) {
            photoFilePath = DICMFilePath + File.separator + "Camera" + File.separator;
            cameraFilePath = DICMFilePath + File.separator + "Video" + File.separator;
        } else if (Build.FINGERPRINT.contains("vivo")
                || Pattern.compile("vivo", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("vivo")
                || Build.MANUFACTURER.contains("vivo")) {
            photoFilePath = cameraFilePath = Environment.getExternalStoragePublicDirectory("") + File.separator + "相机" + File.separator;
        } else {
            cameraFilePath = photoFilePath = DICMFilePath + File.separator + "Camera" + File.separator;
        }
        createFile(filePath);
        createFile(cameraFilePath);
        createFile(photoFilePath);
    }

    public static void createFile(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}
