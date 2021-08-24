package io.agora.rtcwithfu.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.GLES20;

import java.io.*;
import java.nio.ByteBuffer;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.Toast;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.rtc.gl.GlRectDrawer;
import io.agora.rtc.gl.GlTextureFrameBuffer;
import io.agora.rtc.gl.RendererCommon;
import io.agora.rtc.gl.VideoFrame;

/**
 * @author chenhengfei(Aslanchen)
 * @date 2021/7/1
 */
public class TextureIdHelp {
    private GlTextureFrameBuffer bitmapTextureFramebuffer;
    private GlRectDrawer textureDrawer;

    public Bitmap FrameToBitmap(VideoCaptureFrame frame, boolean raw){
        Matrix matrix = RendererCommon.convertMatrixToAndroidGraphicsMatrix(frame.textureTransform);
        VideoFrame.TextureBuffer.Type type;
        if (raw) type = VideoFrame.TextureBuffer.Type.OES;
        else type = VideoFrame.TextureBuffer.Type.RGB;
        return textureIdToBitmap(frame.format.getWidth(), frame.format.getHeight(), frame.rotation, type, frame.textureId, matrix);
    }

    public Bitmap textureIdToBitmap(int width, int height, int rotation, VideoFrame.TextureBuffer.Type type, int textureId, Matrix transformMatrix) {
        if (textureDrawer == null) {
            textureDrawer = new GlRectDrawer();
        }

        if (bitmapTextureFramebuffer == null) {
            bitmapTextureFramebuffer = new GlTextureFrameBuffer(GLES20.GL_RGBA);
        }

        int frameWidth = rotation % 180 == 0 ? width : height;
        int frameHeight = rotation % 180 == 0 ? height : width;
        bitmapTextureFramebuffer.setSize(frameWidth, frameHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bitmapTextureFramebuffer.getFrameBufferId());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix renderMatrix = new Matrix();
        renderMatrix.preTranslate(0.5F, 0.5F);
        renderMatrix.preRotate((float) rotation + 270); // need rotate 180 from texture to bitmap
        renderMatrix.preTranslate(-0.5F, -0.5F);
        renderMatrix.postConcat(transformMatrix);
        float[] finalGlMatrix = RendererCommon.convertMatrixFromAndroidGraphicsMatrix(renderMatrix);
        if (type == VideoFrame.TextureBuffer.Type.OES) {
            textureDrawer.drawOes(textureId, finalGlMatrix, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
        } else {
            textureDrawer.drawRgb(textureId, finalGlMatrix, frameWidth, frameHeight, 0, 0, frameWidth, frameHeight);
        }

        final ByteBuffer bitmapBuffer = ByteBuffer.allocateDirect(frameWidth * frameHeight * 4);
        GLES20.glViewport(0, 0, frameWidth, frameHeight);
        GLES20.glReadPixels(
                0, 0, frameWidth, frameHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmapBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Bitmap mBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        mBitmap.copyPixelsFromBuffer(bitmapBuffer);
        return mBitmap;
    }

    public void release() {
        if (textureDrawer != null) {
//            textureDrawer.release();
            textureDrawer = null;
        }
        if (bitmapTextureFramebuffer != null) {
            bitmapTextureFramebuffer.release();
            bitmapTextureFramebuffer = null;
        }
    }

    public void saveBitmap2Gallery(Context context, Bitmap bm, long currentTime, String tag){
        // name the file
        String imageFileName = "IMG_AGORA_"+ currentTime + "_" + tag+".jpg";
        String imageFilePath;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//            imageFilePath = Environment.DIRECTORY_PICTURES + File.separator + "Agora" + File.separator;
//        else
            imageFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + File.separator + "Agora"+ File.separator;

        // write to file

        OutputStream outputStream;
        ContentResolver resolver = context.getContentResolver();
        ContentValues newScreenshot = new ContentValues();
        Uri insert;
        newScreenshot.put(MediaStore.Images.ImageColumns.DATE_ADDED,currentTime);
        newScreenshot.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, imageFileName);
        newScreenshot.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg");
        newScreenshot.put(MediaStore.Images.ImageColumns.WIDTH, bm.getWidth());
        newScreenshot.put(MediaStore.Images.ImageColumns.HEIGHT, bm.getHeight());
        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                newScreenshot.put(MediaStore.Images.ImageColumns.RELATIVE_PATH,imageFilePath);
//            }else{
                // make sure the path is existed
                File imageFileDir = new File(imageFilePath);
                if(!imageFileDir.exists()){
                    boolean mkdir = imageFileDir.mkdirs();
                    if(!mkdir) {
                        Toast.makeText(context, "save failed, error: cannot create folder. Make sure app has the permission.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                newScreenshot.put(MediaStore.Images.ImageColumns.DATA, imageFilePath+imageFileName);
                newScreenshot.put(MediaStore.Images.ImageColumns.TITLE, imageFileName);
//            }

            // insert a new image
            insert = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, newScreenshot);
            // write data
            outputStream = resolver.openOutputStream(insert);

            bm.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            newScreenshot.clear();
            newScreenshot.put(MediaStore.Images.ImageColumns.SIZE, new File(imageFilePath).length());
            resolver.update(insert, newScreenshot, null, null);

            Toast.makeText(context, "save success, you can view it in gallery",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "save failed, error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void saveBitmap(@NonNull File file, @NonNull Bitmap bmp) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
