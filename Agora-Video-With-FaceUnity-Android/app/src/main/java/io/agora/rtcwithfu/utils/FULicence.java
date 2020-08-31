package io.agora.rtcwithfu.utils;

import android.util.SparseBooleanArray;

import com.faceunity.FURenderer;

import io.agora.rtcwithfu.R;

public class FULicence {
    /**
     * Authentication code obtained from FU official demo,
     * used to check whether some effect function is granted or
     * not in the authpack.java file
     */
    private static final String[] AUTH_CODE = {
            "1-0",                    // Beauty
            "524288-0",               // Makeup
            "110-0",                  // Sticker
            "0-32",                   // Body
    };

    public static final int[] FUNCTION_NAME = {
            R.string.home_function_name_beauty,
            R.string.home_function_name_makeup,
            R.string.home_function_name_sticker,
            R.string.home_function_name_beauty_body
    };

    private static final boolean[] PERMISSIONS = new boolean[FUNCTION_NAME.length];

    private static final SparseBooleanArray PERMISSIONS_BY_RESOURCE_NAME = new SparseBooleanArray(FUNCTION_NAME.length);

    static {
        int moduleCode0 = FURenderer.getModuleCode(0);
        int moduleCode1 = FURenderer.getModuleCode(1);

        for (int i = 0; i < FUNCTION_NAME.length; i++) {
            String[] codeStr = AUTH_CODE[i].split("-");
            int code0 = Integer.parseInt(codeStr[0]);
            int code1 = Integer.parseInt(codeStr[1]);

            boolean granted = (moduleCode0 == 0 && moduleCode1 == 0) ||
                            ((code0 & moduleCode0) > 0 || (code1 & moduleCode1) > 0);

            PERMISSIONS[i] = granted;
            PERMISSIONS_BY_RESOURCE_NAME.append(FUNCTION_NAME[i], granted);
        }
    }

    public static boolean fuPermissionGrantedByIndex(int index) {
        return (0 <= index) && (index < AUTH_CODE.length) && PERMISSIONS[index];
    }

    public static boolean fuPermissionGrantedByRes(int res) {
        return PERMISSIONS_BY_RESOURCE_NAME.get(res, false);
    }
}
