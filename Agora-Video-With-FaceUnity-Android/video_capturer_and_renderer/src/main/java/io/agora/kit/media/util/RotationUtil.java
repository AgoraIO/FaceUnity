package io.agora.kit.media.util;

public class RotationUtil {
    /**
     * Change the surface orientation into the actual
     * rotation in degrees
     * @param orientation Only one of Surface.ROTATION_0,
     *                    Surface.ROTATION_90, Surface.ROTATION_180,
     *                    and Surface.ROTATION_270
     */
    public static int getRotation(int orientation) {
        return orientation * 90;
    }
}
