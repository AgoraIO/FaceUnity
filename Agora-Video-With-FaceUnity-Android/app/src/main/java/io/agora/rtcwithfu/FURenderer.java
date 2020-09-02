package io.agora.rtcwithfu;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.wrapper.faceunity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.agora.capture.video.camera.Constant.CAMERA_FACING_FRONT;

public class FURenderer implements OnFUControlListener {
    private static final String TAG = FURenderer.class.getSimpleName();
    public static final int FU_ADM_FLAG_EXTERNAL_OES_TEXTURE = faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE;

    /**
     * 外部输入类型
     */
    public static final int EXTERNAL_INPUT_TYPE_NONE = -1;
    public static final int EXTERNAL_INPUT_TYPE_IMAGE = 0;
    public static final int EXTERNAL_INPUT_TYPE_VIDEO = 1;

    /**
     * 算法检测类型
     */
    public static final int TRACK_TYPE_FACE = faceunity.FUAITYPE_FACEPROCESSOR;
    public static final int TRACK_TYPE_HUMAN = faceunity.FUAITYPE_HUMAN_PROCESSOR;

    private Context mContext;

    // 图形道具文件夹
    private static final String GRAPHICS_ASSETS_DIR = "graphics/";
    // 美颜 bundle
    private static final String BUNDLE_FACE_BEAUTIFICATION = GRAPHICS_ASSETS_DIR + "face_beautification.bundle";
    // 海报换脸 bundle
    private static final String BUNDLE_CHANGE_FACE = "change_face/change_face.bundle";
    // 美妆 bundle
    private static final String BUNDLE_FACE_MAKEUP = GRAPHICS_ASSETS_DIR + "face_makeup.bundle";
    /* 美妆组合妆bundle文件 */
    private static final String MAKEUP_RESOURCE_DIR = "makeup" + File.separator;
    private static final String BUNDLE_HONGFENG_MAKEUP = MAKEUP_RESOURCE_DIR + "hongfeng.bundle";
    // 美体 bundle
    private static final String BUNDLE_BEAUTIFY_BODY = GRAPHICS_ASSETS_DIR + "body_slim.bundle";
    // 算法模型文件夹
    private static final String AI_MODEL_ASSETS_DIR = "model/";
    // 人脸识别算法模型
    private static final String BUNDLE_AI_MODEL_FACE_PROCESSOR = AI_MODEL_ASSETS_DIR + "ai_face_processor.bundle";

    private static float sIsBeautyOn = 1.0F;

    private int mFrameId = 0;

    // 句柄索引
    private static final int ITEM_ARRAYS_FACE_BEAUTY_INDEX = 0;
    public static final int ITEM_ARRAYS_EFFECT_INDEX = 1;
    private static final int ITEM_ARRAYS_LIGHT_MAKEUP_INDEX = 2;
    private static final int ITEM_ARRAYS_ABIMOJI_3D_INDEX = 3;
    private static final int ITEM_ARRAYS_BEAUTY_HAIR_INDEX = 4;
    private static final int ITEM_ARRAYS_CHANGE_FACE_INDEX = 5;
    private static final int ITEM_ARRAYS_CARTOON_FILTER_INDEX = 6;
    private static final int ITEM_ARRAYS_FACE_MAKEUP_INDEX = 7;
    private static final int ITEM_ARRAYS_BEAUTIFY_BODY = 10;
    private static final int ITEM_ARRAYS_FACE_MAKEUP_BIND_INDEX = 11;
    // 句柄数量
    private static final int ITEM_ARRAYS_COUNT = 12;

    //美颜和其他道具的handle数组
    private int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];
    //用于和异步加载道具的线程交互
    private Handler mFuItemHandler;

    private boolean isNeedFaceBeauty = true;
    private boolean isNeedBeautyHair = false;
    private boolean isNeedFaceMakeup = true;
    private boolean isNeedAnimoji3D = false;
    private boolean isNeedPosterFace = false;
    private boolean isNeedBodySlim = true;
    private Effect mDefaultEffect;//默认道具
    private boolean mIsCreateEGLContext; //是否需要手动创建EGLContext
    private int mInputTextureType = 0; //输入的图像texture类型，Camera提供的默认为EXTERNAL OES
    private int mInputImageFormat = 0;
    private volatile boolean mIsNeedUpdateFaceBeauty = true;
    private float mHeadSlimStrength = 1.0f; // 小头

    private int mInputOrientation = 270;
    private int mExternalInputType = EXTERNAL_INPUT_TYPE_NONE;
    private boolean mIsSystemCameraRecord;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mMaxFaces = 4; // 同时识别的最大人脸数
    private int mMaxHumans = 1; // 同时识别的最大人体数

    private float[] rotationData = new float[4];
    private float[] faceRectData = new float[4];

    private List<Runnable> mEventQueue;
    private long mGlThreadId;
    private OnBundleLoadCompleteListener mOnBundleLoadCompleteListener;
    private static boolean sIsInited;
    /* 设备方向 */
    private volatile int mDeviceOrientation = 90;
    /* 人脸识别方向 */
    private volatile int mRotationMode = faceunity.FU_ROTATION_MODE_90;

    private boolean mIsLoadAiGesture;
    private boolean mIsLoadAiHumanProcessor;

    /**
     * 初始化系统环境，加载底层数据，并进行网络鉴权。
     * 应用使用期间只需要初始化一次，无需释放数据。
     * 必须在SDK其他接口前调用，否则会引起应用崩溃。
     */
    public static void initFURenderer(Context context) {
        if (sIsInited) {
            return;
        }
        // {trace:0, debug:1, info:2, warn:3, error:4, critical:4, off:6}
        int logLevel = 6;
        faceunity.fuSetLogLevel(logLevel);
        Log.i(TAG, "initFURenderer logLevel: " + logLevel);

        // 初始化高通 DSP
        String path = context.getApplicationInfo().nativeLibraryDir;
        faceunity.fuHexagonInitWithPath(path);
        Log.d(TAG, "initFURenderer HexagonInitWithPath:" + path);

        // 获取 Nama SDK 版本信息
        Log.e(TAG, "fu sdk version " + faceunity.fuGetVersion());
        int isSetup = faceunity.fuSetup(new byte[0], authpack.A());
        Log.d(TAG, "fuSetup. isSetup: " + (isSetup == 0 ? "no" : "yes"));
        // 提前加载算法数据模型，用于人脸检测
        loadAiModel(context, BUNDLE_AI_MODEL_FACE_PROCESSOR, faceunity.FUAITYPE_FACEPROCESSOR);
        sIsInited = isLibInit();
        Log.i(TAG, "initFURenderer finish. isLibraryInit: " + (sIsInited ? "yes" : "no"));
    }

    /**
     * SDK 是否初始化。fuSetup 后表示已经初始化，fuDestroyLibData 后表示已经销毁
     *
     * @return 1 inited, 0 not init.
     */
    public static boolean isLibInit() {
        return faceunity.fuIsLibraryInit() == 1;
    }

    /**
     * 加载 AI 模型资源
     *
     * @param context
     * @param bundlePath ai_model.bundle
     * @param type       faceunity.FUAITYPE_XXX
     */
    private static void loadAiModel(Context context, String bundlePath, int type) {
        byte[] buffer = readFile(context, bundlePath);
        if (buffer != null) {
            int isLoaded = faceunity.fuLoadAIModelFromPackage(buffer, type);
            Log.d(TAG, "loadAiModel. type: " + type + ", isLoaded: " + (isLoaded == 1 ? "yes" : "no"));
        }
    }

    /**
     * 释放 AI 模型资源
     *
     * @param type
     */
    private static void releaseAiModel(int type) {
        if (faceunity.fuIsAIModelLoaded(type) == 1) {
            int isReleased = faceunity.fuReleaseAIModel(type);
            Log.d(TAG, "releaseAiModel. type: " + type + ", isReleased: " + (isReleased == 1 ? "yes" : "no"));
        }
    }

    private static void releaseAllAiModel() {
        releaseAiModel(faceunity.FUAITYPE_BACKGROUNDSEGMENTATION);
        releaseAiModel(faceunity.FUAITYPE_HAIRSEGMENTATION);
        releaseAiModel(faceunity.FUAITYPE_HANDGESTURE);
        releaseAiModel(faceunity.FUAITYPE_HUMAN_PROCESSOR);
    }

    /**
     * 加载 bundle 道具，不需要 EGL Context，可以异步执行
     *
     * @param bundlePath bundle 文件路径
     * @return 道具句柄，大于 0 表示加载成功
     */
    private static int loadItem(Context context, String bundlePath) {
        int handle = 0;
        if (!TextUtils.isEmpty(bundlePath)) {
            byte[] buffer = readFile(context, bundlePath);
            if (buffer != null) {
                handle = faceunity.fuCreateItemFromPackage(buffer);
            }
        }
        Log.d(TAG, "loadItem. bundlePath: " + bundlePath + ", itemHandle: " + handle);
        return handle;
    }

    /**
     * 从 assets 文件夹或者本地磁盘读文件
     *
     * @param context
     * @param path
     * @return
     */
    private static byte[] readFile(Context context, String path) {
        InputStream is = null;
        try {
            is = context.getAssets().open(path);
        } catch (IOException e1) {
            Log.w(TAG, "readFile: e1", e1);
            // open assets failed, then try sdcard
            try {
                is = new FileInputStream(path);
            } catch (IOException e2) {
                Log.w(TAG, "readFile: e2", e2);
            }
        }
        if (is != null) {
            try {
                byte[] buffer = new byte[is.available()];
                int length = is.read(buffer);
                Log.v(TAG, "readFile. path: " + path + ", length: " + length + " Byte");
                is.close();
                return buffer;
            } catch (IOException e3) {
                Log.e(TAG, "readFile: e3", e3);
            }
        }
        return null;
    }

    /**
     * FURenderer构造函数
     */
    private FURenderer(Context context, boolean isCreateEGLContext) {
        this.mContext = context;
        this.mIsCreateEGLContext = isCreateEGLContext;
    }

    /**
     * 创建及初始化faceunity相应的资源
     */
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated");
        mEventQueue = Collections.synchronizedList(new ArrayList<Runnable>(16));
        mGlThreadId = Thread.currentThread().getId();
        HandlerThread handlerThread = new HandlerThread("FUItemWorker");
        handlerThread.start();
        Handler fuItemHandler = new FUItemHandler(handlerThread.getLooper());
        mFuItemHandler = fuItemHandler;

        /**
         * fuCreateEGLContext 创建OpenGL环境
         * 适用于没OpenGL环境时调用
         * 如果调用了fuCreateEGLContext，在销毁时需要调用fuReleaseEGLContext
         */
        if (mIsCreateEGLContext) {
            faceunity.fuCreateEGLContext();
        }

        mFrameId = 0;
        setMaxFaces(mMaxFaces);
        int rotationMode = calculateRotationMode();
        faceunity.fuSetDefaultRotationMode(rotationMode);
        mRotationMode = rotationMode;

        if (mIsLoadAiHumanProcessor) {
            fuItemHandler.post(() -> {
                loadAiModel(mContext, AI_MODEL_ASSETS_DIR + "ai_human_processor.bundle", faceunity.FUAITYPE_HUMAN_PROCESSOR);
                setMaxHumans(mMaxHumans);
            });
        }
        if (mIsLoadAiGesture) {
            fuItemHandler.post(() -> loadAiModel(mContext, AI_MODEL_ASSETS_DIR + "ai_gesture.bundle", faceunity.FUAITYPE_HANDGESTURE));
        }
        if (isNeedFaceBeauty) {
            fuItemHandler.sendEmptyMessage(ITEM_ARRAYS_FACE_BEAUTY_INDEX);
        }
        if (isNeedFaceMakeup) {
            fuItemHandler.sendEmptyMessage(ITEM_ARRAYS_FACE_MAKEUP_INDEX);
        }
        if (isNeedBeautyHair) {
            fuItemHandler.sendEmptyMessage(ITEM_ARRAYS_BEAUTY_HAIR_INDEX);
        }
        if (isNeedAnimoji3D) {
            fuItemHandler.sendEmptyMessage(ITEM_ARRAYS_ABIMOJI_3D_INDEX);
        }
        if (isNeedBodySlim) {
            fuItemHandler.sendEmptyMessage(ITEM_ARRAYS_BEAUTIFY_BODY);
        }
        if (isNeedPosterFace) {
            mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX] = loadItem(mContext, BUNDLE_CHANGE_FACE);
        }

        // 异步加载默认道具，放在加载 animoji 3D 和动漫滤镜之后
        if (mDefaultEffect != null) {
            Message.obtain(fuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect).sendToTarget();
        }
    }

    /**
     * 双输入接口(fuDualInputToTexture)(处理后的画面数据并不会回写到数组)，由于省去相应的数据拷贝性能相对最优，推荐使用。
     *
     * @param img NV21数据
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(byte[] img, int tex, int w, int h) {
        if (tex <= 0 || img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType | mInputImageFormat;
        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 销毁faceunity相关的资源
     */
    public void onSurfaceDestroyed() {
        Log.e(TAG, "onSurfaceDestroyed");
        if (mFuItemHandler != null) {
            mFuItemHandler.removeCallbacksAndMessages(null);
            mFuItemHandler.getLooper().quit();
            mFuItemHandler = null;
        }
        if (mEventQueue != null) {
            mEventQueue.clear();
            mEventQueue = null;
        }
        mGlThreadId = 0;
        if (mItemsArray.length > 0) {
            int posterIndex = mItemsArray[ITEM_ARRAYS_CHANGE_FACE_INDEX];
            if (posterIndex > 0) {
                faceunity.fuDeleteTexForItem(posterIndex, "tex_input");
                faceunity.fuDeleteTexForItem(posterIndex, "tex_template");
            }
        }

        mFrameId = 0;
        mIsNeedUpdateFaceBeauty = true;
        resetTrackStatus();
        releaseAllAiModel();
        destroyControllerRelated();
        for (int item : mItemsArray) {
            if (item > 0) {
                faceunity.fuDestroyItem(item);
            }
        }
        Arrays.fill(mItemsArray, 0);
        faceunity.fuDestroyAllItems();
        faceunity.fuDone();
        faceunity.fuOnDeviceLost();
        if (mIsCreateEGLContext) {
            faceunity.fuReleaseEGLContext();
        }
    }

    private int[] mControllerBoundItems;

    private void destroyControllerRelated() {
        if (mControllerBoundItems != null && mControllerBoundItems[0] > 0) {
            int controllerItem = mItemsArray[0];
            faceunity.fuItemSetParam(controllerItem, "quit_human_pose_track_mode", 1.0);
            int[] controllerBoundItems = validateItems(mControllerBoundItems);
            Log.d(TAG, "destroyControllerRelated: unbind " + Arrays.toString(controllerBoundItems));
            faceunity.fuUnBindItems(controllerItem, controllerBoundItems);
            for (int i = controllerBoundItems.length - 1; i >= 0; i--) {
                faceunity.fuDestroyItem(controllerBoundItems[i]);
            }
            Arrays.fill(controllerBoundItems, 0);
            mControllerBoundItems = null;
        }
    }

    private int[] validateItems(int[] input) {
        int[] output = new int[input.length];
        int count = 0;
        for (int i : input) {
            if (i > 0) {
                output[count++] = i;
            }
        }
        return Arrays.copyOfRange(output, 0, count);
    }

    //--------------------------------------对外可使用的接口----------------------------------------

    /**
     * 类似 GLSurfaceView 的 queueEvent 机制
     *
     * @param r
     */
    public void queueEvent(Runnable r) {
        if (Thread.currentThread().getId() == mGlThreadId) {
            r.run();
        } else {
            if (mEventQueue != null) {
                mEventQueue.add(r);
            }
        }
    }

    /**
     * 设置需要识别的人脸个数
     *
     * @param maxFaces
     */
    public void setMaxFaces(final int maxFaces) {
        if (maxFaces > 0) {
            mMaxFaces = maxFaces;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "setMaxFaces() called with: maxFaces = [" + maxFaces + "]");
                    faceunity.fuSetMaxFaces(maxFaces);
                }
            });
        }
    }

    /**
     * 设置需要识别的人体个数
     *
     * @param maxHumans
     */
    public void setMaxHumans(final int maxHumans) {
        if (maxHumans > 0) {
            mMaxHumans = maxHumans;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "setMaxHumans() called with: maxHumans = [" + maxHumans + "]");
                    faceunity.fuHumanProcessorSetMaxHumans(maxHumans);
                }
            });
        }
    }

    /**
     * 每帧处理画面时被调用
     */
    private void prepareDrawFrame() {
        //计算FPS等数据
        benchmarkFPS();

        if (mIsLoadAiHumanProcessor) {
            // 获取人体是否识别，并调用回调接口
            int trackHumans = faceunity.fuHumanProcessorGetNumResults();
            if (mOnTrackingStatusChangedListener != null && mTrackHumanStatus != trackHumans) {
                mTrackHumanStatus = trackHumans;
                mOnTrackingStatusChangedListener.onTrackStatusChanged(TRACK_TYPE_HUMAN, trackHumans);
            }
        } else {
            // 获取人脸是否识别，并调用回调接口
            int trackFace = faceunity.fuIsTracking();
            if (mOnTrackingStatusChangedListener != null && mTrackFaceStatus != trackFace) {
                mTrackFaceStatus = trackFace;
                mOnTrackingStatusChangedListener.onTrackStatusChanged(TRACK_TYPE_FACE, trackFace);
            }
        }

        // 获取 SDK 错误信息，并调用回调接口
        int error = faceunity.fuGetSystemError();
        if (error != 0) {
            String errorMessage = faceunity.fuGetSystemErrorString(error);
            Log.e(TAG, "system error code: " + error + ", error message: " + errorMessage);
            if (mOnSystemErrorListener != null) {
                mOnSystemErrorListener.onSystemError(errorMessage);
            }
        }

        //修改美颜参数
        if (mIsNeedUpdateFaceBeauty && mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] > 0) {
            int itemFaceBeauty = mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX];
            faceunity.fuItemSetParam(itemFaceBeauty, "IS_BEAUTY_ON", sIsBeautyOn);
            mIsNeedUpdateFaceBeauty = false;
        }

        //queueEvent的Runnable在此处被调用
        while (!mEventQueue.isEmpty()) {
            mEventQueue.remove(0).run();
        }
    }

    private void resetTrackStatus() {
        faceunity.fuOnCameraChange();
        faceunity.fuHumanProcessorReset();
    }

    private int calculateRotModeLagacy() {
        int mode;
        if (mInputOrientation == 270) {
            if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mode = mDeviceOrientation / 90;
            } else {
                mode = (mDeviceOrientation - 180) / 90;
            }
        } else {
            if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mode = (mDeviceOrientation + 180) / 90;
            } else {
                mode = mDeviceOrientation / 90;
            }
        }
        return mode;
    }

    /**
     * 计算 RotationMode
     *
     * @return rotationMode
     */
    private int calculateRotationMode() {
        if (mExternalInputType == EXTERNAL_INPUT_TYPE_IMAGE) {
            // 外部图片
            return faceunity.FU_ROTATION_MODE_0;
        } else if (mExternalInputType == EXTERNAL_INPUT_TYPE_VIDEO) {
            // 外部视频
            switch (mInputOrientation) {
                case 90:
                    return faceunity.FU_ROTATION_MODE_270;
                case 270:
                    return faceunity.FU_ROTATION_MODE_90;
                case 180:
//                    return faceunity.FU_ROTATION_MODE_180;
                case 0:
                default:
                    return faceunity.FU_ROTATION_MODE_0;
            }
        } else {
            // 相机数据
            int rotMode = faceunity.FU_ROTATION_MODE_0;
            if (mInputOrientation == 270) {
                if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotMode = mDeviceOrientation / 90;
                } else {
                    if (mDeviceOrientation == 180) {
                        rotMode = faceunity.FU_ROTATION_MODE_0;
                    } else if (mDeviceOrientation == 0) {
                        rotMode = faceunity.FU_ROTATION_MODE_180;
                    } else {
                        rotMode = mDeviceOrientation / 90;
                    }
                }
            } else if (mInputOrientation == 90) {
                if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (mDeviceOrientation == 90) {
                        rotMode = faceunity.FU_ROTATION_MODE_270;
                    } else if (mDeviceOrientation == 270) {
                        rotMode = faceunity.FU_ROTATION_MODE_90;
                    } else {
                        rotMode = mDeviceOrientation / 90;
                    }
                } else {
                    if (mDeviceOrientation == 0) {
                        rotMode = faceunity.FU_ROTATION_MODE_180;
                    } else if (mDeviceOrientation == 90) {
                        rotMode = faceunity.FU_ROTATION_MODE_270;
                    } else if (mDeviceOrientation == 180) {
                        rotMode = faceunity.FU_ROTATION_MODE_0;
                    } else {
                        rotMode = faceunity.FU_ROTATION_MODE_90;
                    }
                }
            }
            return rotMode;
        }
    }

    //--------------------------------------IsTracking（人脸识别回调相关定义）----------------------------------------

    private int mTrackHumanStatus = -1;
    private int mTrackFaceStatus = -1;

    @Override
    public void setBeautificationOn(boolean isOn) {
        float isBeautyOn = isOn ? 1.0F : 0.0F;
        if (sIsBeautyOn == isBeautyOn) {
            return;
        }
        sIsBeautyOn = isBeautyOn;
        queueEvent(() -> {
            int itemFaceBeauty = mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX];
            if (itemFaceBeauty > 0) {
                faceunity.fuItemSetParam(itemFaceBeauty, "IS_BEAUTY_ON", sIsBeautyOn);
            }
        });
    }

    @Override
    public void onEffectSelected(Effect effect) {
        if (effect == null || effect == mDefaultEffect) {
            return;
        }
        mDefaultEffect = effect;
        if (mFuItemHandler == null) {
            queueEvent(() -> {
                mFuItemHandler.removeMessages(ITEM_ARRAYS_EFFECT_INDEX);
                mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
            });
        } else {
            mFuItemHandler.removeMessages(ITEM_ARRAYS_EFFECT_INDEX);
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
        }
    }

    @Override
    public void setMakeupItemParam(boolean isOn) {
        double isMakeupOn = isOn ? 1 : 0;
        queueEvent(() -> {
            int itemFaceMakeup = mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX];
            if (itemFaceMakeup > 0) {
                faceunity.fuItemSetParam(itemFaceMakeup, "makeup_intensity_eye", 1);
                faceunity.fuItemSetParam(itemFaceMakeup, "makeup_intensity_lip", 1);
                faceunity.fuItemSetParam(itemFaceMakeup, "makeup_intensity", 0.5);
                faceunity.fuItemSetParam(itemFaceMakeup, "is_makeup_on", isMakeupOn);
                faceunity.fuBindItems(itemFaceMakeup, new int[]{mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_BIND_INDEX]});
            }
        });
    }

    @Override
    public void setBodySlim(boolean selected) {
        queueEvent(() -> {
            int itemBodySlim = mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY];
            if (itemBodySlim > 0) {
                faceunity.fuItemSetParam(itemBodySlim, BodySlimParam.HEAD_SLIM, selected ? mHeadSlimStrength : 0);
                faceunity.fuItemSetParam(itemBodySlim, BodySlimParam.DEBUG, selected ? 1 : 0);
            }
        });
    }

    public interface OnTrackingStatusChangedListener {
        /**
         * 检测状态发生变化
         *
         * @param type
         * @param status
         */
        void onTrackStatusChanged(int type, int status);
    }

    private OnTrackingStatusChangedListener mOnTrackingStatusChangedListener;

    //--------------------------------------FaceUnitySystemError（faceunity错误信息回调相关定义）----------------------------------------

    public interface OnSystemErrorListener {
        void onSystemError(String error);
    }

    private OnSystemErrorListener mOnSystemErrorListener;

    //--------------------------------------FPS（FPS相关定义）----------------------------------------

    private static final int NANO_IN_ONE_MILLI_SECOND = 1_000_000;
    private static final int NANO_IN_ONE_NANO_SECOND = 1_000_000_000;
    private static final int TIME = 10;
    private int mCurrentFrameCnt = 0;
    private long mLastOneHundredFrameTimeStamp = 0;
    private long mOneHundredFrameFUTime = 0;
    private boolean mNeedBenchmark = true;
    private long mFuCallStartTime = 0;

    private OnFUDebugListener mOnFUDebugListener;

    public interface OnFUDebugListener {
        void onFpsChange(double fps, double renderTime);
    }

    private void benchmarkFPS() {
        if (!mNeedBenchmark) {
            return;
        }
        if (++mCurrentFrameCnt == TIME) {
            mCurrentFrameCnt = 0;
            double fps = ((float) TIME * NANO_IN_ONE_NANO_SECOND / (System.nanoTime() - mLastOneHundredFrameTimeStamp));
            double renderTime = (float) mOneHundredFrameFUTime / TIME / NANO_IN_ONE_MILLI_SECOND;
            mLastOneHundredFrameTimeStamp = System.nanoTime();
            mOneHundredFrameFUTime = 0;

            if (mOnFUDebugListener != null) {
                mOnFUDebugListener.onFpsChange(fps, renderTime);
            }
        }
    }

    //--------------------------------------道具（异步加载道具）----------------------------------------

    public interface OnBundleLoadCompleteListener {
        /**
         * bundle 加载完成
         *
         * @param what
         */
        void onBundleLoadComplete(int what);
    }

    /**
     * 设置对道具设置相应的参数
     *
     * @param itemHandle
     */
    private void updateEffectItemParams(Effect effect, final int itemHandle) {
        if (effect == null || itemHandle == 0) {
            return;
        }
        mRotationMode = calculateRotationMode();
        Log.d(TAG, "updateEffectItemParams: mRotationMode=" + mRotationMode);
        if (mExternalInputType == EXTERNAL_INPUT_TYPE_IMAGE) {
            faceunity.fuItemSetParam(itemHandle, "isAndroid", 0.0);
        } else if (mExternalInputType == EXTERNAL_INPUT_TYPE_VIDEO) {
            faceunity.fuItemSetParam(itemHandle, "isAndroid", mIsSystemCameraRecord ? 1.0 : 0.0);
        } else {
            faceunity.fuItemSetParam(itemHandle, "isAndroid", 1.0);
        }
        int effectType = effect.getType();
        if (effectType == Effect.EFFECT_TYPE_STICKER || effectType == Effect.EFFECT_TYPE_EXPRESSION_RECOGNITION) {
            //rotationAngle 参数是用于旋转普通道具
            faceunity.fuItemSetParam(itemHandle, "rotationAngle", mRotationMode * 90);
        }
        int back = mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK ? 1 : 0;
        if (effectType == Effect.EFFECT_TYPE_ANIMOJI || effectType == Effect.EFFECT_TYPE_PORTRAIT_DRIVE) {
            // 镜像顶点
            faceunity.fuItemSetParam(itemHandle, "is3DFlipH", back);
            // 镜像表情
            faceunity.fuItemSetParam(itemHandle, "isFlipExpr", back);
            //这两句代码用于识别人脸默认方向的修改，主要针对animoji道具的切换摄像头倒置问题
            faceunity.fuItemSetParam(itemHandle, "camera_change", 1.0);
        }

        if (effectType == Effect.EFFECT_TYPE_GESTURE_RECOGNITION) {
            //loc_y_flip与loc_x_flip 参数是用于对手势识别道具的镜像
            faceunity.fuItemSetParam(itemHandle, "is3DFlipH", back);
            faceunity.fuItemSetParam(itemHandle, "loc_y_flip", back);
            faceunity.fuItemSetParam(itemHandle, "loc_x_flip", back);
        }
        setEffectRotationMode(effect, itemHandle);
        if (effectType == Effect.EFFECT_TYPE_ANIMOJI) {
            // 镜像跟踪（位移和旋转）
            faceunity.fuItemSetParam(itemHandle, "isFlipTrack", back);
            // 镜像灯光
            faceunity.fuItemSetParam(itemHandle, "isFlipLight ", back);
            // 设置 Animoji 跟随人脸
            faceunity.fuItemSetParam(itemHandle, "{\"thing\":\"<global>\",\"param\":\"follow\"}", 1);
        }
        setMaxFaces(effect.getMaxFace());
    }

    private void setEffectRotationMode(Effect effect, int itemHandle) {
        int rotMode;
        if (effect.getType() == Effect.EFFECT_TYPE_GESTURE_RECOGNITION && effect.getBundleName().startsWith("ctrl")) {
            rotMode = calculateRotModeLagacy();
        } else {
            rotMode = mRotationMode;
        }
        faceunity.fuItemSetParam(itemHandle, "rotMode", rotMode);
        faceunity.fuItemSetParam(itemHandle, "rotationMode", rotMode);
    }

    /*----------------------------------Builder---------------------------------------*/

    /**
     * FURenderer Builder
     */
    public static class Builder {
        private boolean createEGLContext = false;
        private Effect defaultEffect;
        private int maxFaces = 4;
        private int maxHumans = 1;
        private Context context;
        private int inputTextureType = 0;
        private int inputImageFormat = 0;
        private int inputOrientation = 270;
        private int externalInputType = EXTERNAL_INPUT_TYPE_NONE;
        private boolean isNeedFaceBeauty = true;
        private boolean isNeedAnimoji3D = false;
        private boolean isNeedBeautyHair = false;
        private boolean isNeedPosterFace = false;
        private boolean isNeedBodySlim = true;
        private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;

        private boolean mIsLoadAiGesture;
        private boolean mIsLoadAiHumanProcessor;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }


        public Builder setLoadAiHumanProcessor(boolean needAiHumanProcessor) {
            mIsLoadAiHumanProcessor = needAiHumanProcessor;
            return this;
        }

        /**
         * 是否需要美颜效果
         *
         * @param needFaceBeauty
         * @return
         */
        public Builder setNeedFaceBeauty(boolean needFaceBeauty) {
            isNeedFaceBeauty = needFaceBeauty;
            return this;
        }

        /**
         * 是否需要美体效果
         *
         * @param needBodySlim
         * @return
         */
        public Builder setNeedBodySlim(boolean needBodySlim) {
            isNeedBodySlim = needBodySlim;
            return this;
        }

        /**
         * 识别最大人脸数
         *
         * @param maxFaces
         * @return
         */
        public Builder maxFaces(int maxFaces) {
            this.maxFaces = maxFaces;
            return this;
        }

        /**
         * 识别最大人体数
         *
         * @param maxHumans
         * @return
         */
        public Builder maxHumans(int maxHumans) {
            this.maxHumans = maxHumans;
            return this;
        }

        /**
         * 输入的byte[]数据类型
         *
         * @param inputImageFormat
         * @return
         */
        public Builder inputImageFormat(int inputImageFormat) {
            this.inputImageFormat = inputImageFormat;
            return this;
        }

        public FURenderer build() {
            FURenderer fuRenderer = new FURenderer(context, createEGLContext);
            fuRenderer.mMaxFaces = maxFaces;
            fuRenderer.mMaxHumans = maxHumans;
            fuRenderer.mInputTextureType = inputTextureType;
            fuRenderer.mInputImageFormat = inputImageFormat;
            fuRenderer.mInputOrientation = inputOrientation;
            fuRenderer.mExternalInputType = externalInputType;
            fuRenderer.mDefaultEffect = defaultEffect;
            fuRenderer.isNeedFaceBeauty = isNeedFaceBeauty;
            fuRenderer.isNeedBodySlim = isNeedBodySlim;
            fuRenderer.isNeedAnimoji3D = isNeedAnimoji3D;
            fuRenderer.isNeedBeautyHair = isNeedBeautyHair;
            fuRenderer.isNeedPosterFace = isNeedPosterFace;
            fuRenderer.mCameraFacing = cameraFacing;
            fuRenderer.mIsLoadAiGesture = mIsLoadAiGesture;
            fuRenderer.mIsLoadAiHumanProcessor = mIsLoadAiHumanProcessor;
            return fuRenderer;
        }
    }

//--------------------------------------Builder----------------------------------------

    class FUItemHandler extends Handler {

        FUItemHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //加载普通道具 bundle
                case ITEM_ARRAYS_EFFECT_INDEX: {
                    final Effect effect = (Effect) msg.obj;
                    if (effect == null) {
                        return;
                    }
                    boolean isNone = effect.getType() == Effect.EFFECT_TYPE_NONE;
                    final int itemEffect = isNone ? 0 : loadItem(mContext, effect.getBundlePath());
                    if (!isNone && itemEffect <= 0) {
                        Log.w(TAG, "create effect item failed: " + itemEffect);
                        return;
                    }
                    queueEvent(() -> {
                        if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                            faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
                            mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = 0;
                        }
                        if (itemEffect > 0) {
                            updateEffectItemParams(effect, itemEffect);
                        }
                        mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = itemEffect;
                    });
                }
                break;
                // 加载美颜 bundle
                case ITEM_ARRAYS_FACE_BEAUTY_INDEX: {
                    final int itemBeauty = loadItem(mContext, BUNDLE_FACE_BEAUTIFICATION);
                    if (itemBeauty <= 0) {
                        Log.w(TAG, "create face beauty item failed: " + itemBeauty);
                        return;
                    }
                    queueEvent(() -> {
                        if (mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] > 0) {
                            faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX]);
                            mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = 0;
                        }
                        mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = itemBeauty;
                        mIsNeedUpdateFaceBeauty = true;
                    });
                }
                break;
                // 加载美妆 bundle
                case ITEM_ARRAYS_FACE_MAKEUP_INDEX: {
                    final int itemMarkup = loadItem(mContext, BUNDLE_FACE_MAKEUP);
                    if (itemMarkup <= 0) {
                        Log.w(TAG, "create makeup item failed: " + itemMarkup);
                        return;
                    }
                    final int itemMarkupHongfeng = loadItem(mContext, BUNDLE_HONGFENG_MAKEUP);
                    if (itemMarkupHongfeng <= 0) {
                        Log.w(TAG, "create makeup item hongfeng failed: " + itemMarkupHongfeng);
                        return;
                    }
                    queueEvent(() -> {
                        if (mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] > 0) {
                            faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX]);
                            faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_BIND_INDEX]);
                            mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] = 0;
                            mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_BIND_INDEX] = 0;
                        }
                        mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_INDEX] = itemMarkup;
                        mItemsArray[ITEM_ARRAYS_FACE_MAKEUP_BIND_INDEX] = itemMarkupHongfeng;
                    });
                }
                break;
                // 加载美体 bundle
                case ITEM_ARRAYS_BEAUTIFY_BODY: {
                    final int itemBeautifyBody = loadItem(mContext, BUNDLE_BEAUTIFY_BODY);
                    if (itemBeautifyBody <= 0) {
                        Log.w(TAG, "create beautify body item failed: " + itemBeautifyBody);
                        return;
                    }
                    queueEvent(() -> {
                        if (mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] > 0) {
                            faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY]);
                            mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] = 0;
                        }
                        mItemsArray[ITEM_ARRAYS_BEAUTIFY_BODY] = itemBeautifyBody;
                    });
                }
                break;
                default:
            }
            if (mOnBundleLoadCompleteListener != null) {
                mOnBundleLoadCompleteListener.onBundleLoadComplete(msg.what);
            }
        }
    }
}
