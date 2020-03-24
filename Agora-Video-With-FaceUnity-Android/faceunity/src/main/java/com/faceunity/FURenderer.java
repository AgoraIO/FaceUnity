package com.faceunity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.faceunity.entity.Effect;
import com.faceunity.entity.Filter;
import com.faceunity.utils.Constant;
import com.faceunity.utils.FileUtils;
import com.faceunity.wrapper.faceunity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.faceunity.wrapper.faceunity.FU_ADM_FLAG_FLIP_X;

/**
 * 一个基于 Faceunity Nama SDK 的简单封装，方便简单集成，理论上简单需求的步骤：
 * <p>
 * 1.通过 OnEffectSelectedListener 在 UI 上进行交互
 * 2.合理调用 FURenderer 构造函数
 * 3.对应的时机调用 onSurfaceCreated 和 onSurfaceDestroyed
 * 4.处理图像时调用 onDrawFrame
 */
public class FURenderer implements OnFUControlListener {
    private static final String TAG = FURenderer.class.getSimpleName();

    public static final int FU_ADM_FLAG_EXTERNAL_OES_TEXTURE = faceunity.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE;

    private Context mContext;

    /**
     * 目录 assets 下的 *.bundle 为程序的数据文件。
     * 其中 v3.bundle：人脸识别数据文件，缺少该文件会导致系统初始化失败；
     * face_beautification.bundle：美颜和美型相关的数据文件；
     * anim_model.bundle：优化表情跟踪功能所需要加载的动画数据文件；适用于使用 Animoji 和 Avatar 功能的用户，如果不是，可不加载
     * ardata_ex.bundle：高精度模式的三维张量数据文件。适用于换脸功能，如果没用该功能可不加载
     * fxaa.bundle：3D 绘制抗锯齿数据文件。加载后，会使得 3D 绘制效果更加平滑。
     * 目录effects下是我们打包签名好的道具
     */
    public static final String BUNDLE_V3 = "v3.bundle";
    public static final String BUNDLE_ANIMOJI_3D = "fxaa.bundle";
    // AI 模型文件夹
    private static final String AI_MODEL_ASSETS_DIR = "AI_model/";
    // AI 人脸识别模型
    private static final String BUNDLE_AI_MODEL_FACE_PROCESSOR = AI_MODEL_ASSETS_DIR + "ai_face_processor.bundle";
    // Animoji 舌头 bundle
    private static final String BUNDLE_TONGUE = AI_MODEL_ASSETS_DIR + "tongue.bundle";
    // 美颜 bundle
    public static final String BUNDLE_FACE_BEAUTIFICATION = "face_beautification.bundle";

    private static float mFilterLevel = 1.0f; // 滤镜强度
    private static float mSkinDetect = 1.0f; // 肤色检测开关
    private static float mHeavyBlur = 0.0f; // 重度磨皮开关
    private static float mBlurLevel = 0.7f; // 磨皮程度
    private static float mColorLevel = 0.3f; // 美白
    private static float mRedLevel = 0.3f; // 红润
    private static float mEyeBright = 0.0f; // 亮眼
    private static float mToothWhiten = 0.0f;//美牙
    private static float mFaceShape = BeautificationParams.FACE_SHAPE_CUSTOM; // 脸型
    private static float mFaceShapeLevel = 1.0f; // 程度
    private static float mCheekThinning = 0f; // 瘦脸
    private static float mCheekV = 0.5f; // V 脸
    private static float mCheekNarrow = 0f; // 窄脸
    private static float mCheekSmall = 0f; // 小脸
    private static float mEyeEnlarging = 0.4f; // 大眼
    private static float mIntensityChin = 0.3f; // 下巴
    private static float mIntensityForehead = 0.3f; // 额头
    private static float mIntensityMouth = 0.4f; // 嘴形
    private static float mIntensityNose = 0.5f; // 瘦鼻
    private static String mFilterName = Filter.Key.FENNEN_1; // 粉嫩效果

    private int mFrameId = 0;

    // 句柄索引
    private static final int ITEM_ARRAYS_FACE_BEAUTY_INDEX = 0;
    private static final int ITEM_ARRAYS_EFFECT_INDEX = 1;
    private static final int ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX = 2;
    // 句柄数量
    private static final int ITEM_ARRAYS_COUNT = 3;
    // 句柄数组
    private int[] mItemsArray = new int[ITEM_ARRAYS_COUNT];

    private Handler mFuItemHandler;

    private boolean isNeedFaceBeauty = true;
    private boolean isNeedAnimoji3D = false;
    private Effect mDefaultEffect; // 默认道具（同步加载）
    private boolean mIsCreateEGLContext; //是否需要手动创建 EGLContext
    private int mInputTextureType = 0; // 输入的图像 texture 类型，Camera 提供的默认为 EXTERNAL OES
    private int mInputImageFormat = 0;
    // 美颜和滤镜的默认参数
    private volatile boolean mIsNeedUpdateFaceBeauty = true;

    private int mInputImageOrientation = 270;
    private int mCurrentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mMaxFaces = 4; // 同时识别的最大人脸数

    private float[] landmarksData = new float[150];
    private float[] expressionData = new float[46];
    private float[] rotationData = new float[4];
    private float[] pupilPosData = new float[2];
    private float[] rotationModeData = new float[1];
    private float[] faceRectData = new float[4];

    private List<Runnable> mEventQueue;
    private static boolean sIsInited;
    private OnBundleLoadCompleteListener mOnBundleLoadCompleteListener;

    private boolean mIsLoadAiFaceLandmark75 = false;
    /* 设备方向 */
    private int mDeviceOrientation = 90;
    /* 人脸识别方向 */
    private int mRotationMode = faceunity.FU_ROTATION_MODE_90;

    /**
     * 创建及初始化 Faceunity 相应的资源
     */
    public void onSurfaceCreated() {
        onSurfaceDestroyed();
        Log.e(TAG, "onSurfaceCreated");
        mEventQueue = Collections.synchronizedList(new ArrayList<Runnable>(16));

        HandlerThread handlerThread = new HandlerThread("FUItemWorker");
        handlerThread.start();
        mFuItemHandler = new FUItemHandler(handlerThread.getLooper());

        /**
         * fuCreateEGLContext 创建OpenGL环境
         * 适用于没OpenGL环境时调用
         * 如果调用了fuCreateEGLContext，在销毁时需要调用fuReleaseEGLContext
         */
        if (mIsCreateEGLContext) {
            faceunity.fuCreateEGLContext();
        }

        mFrameId = 0;
        // 设置表情校准
        faceunity.fuSetExpressionCalibration(2);
        // 设置多人脸，目前最多支持8人
        faceunity.fuSetMaxFaces(mMaxFaces);
        mRotationMode = calculateRotationMode();
        // 设置旋转方向
        faceunity.fuSetDefaultRotationMode(mRotationMode);
        // 设置同步模式
        faceunity.fuSetAsyncTrackFace(0);

        if (isNeedFaceBeauty) {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_FACE_BEAUTY_INDEX);
        }
        if (isNeedAnimoji3D) {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX);
        }
        if (mIsLoadAiFaceLandmark75) {
            mFuItemHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadAiModel(mContext, AI_MODEL_ASSETS_DIR + "ai_facelandmarks75.bundle", faceunity.FUAITYPE_FACELANDMARKS75);
                }
            });
        }

        // 异步加载默认道具，放在加载 animoji 3D 和动漫滤镜之后
        if (mDefaultEffect != null) {
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
        }
    }

    /**
     * 获取 Faceunity sdk 版本库
     */
    public static String getVersion() {
        return faceunity.fuGetVersion();
    }

    /**
     * 获取证书相关的权限码
     */
    public static int getModuleCode(int index) {
        return faceunity.fuGetModuleCode(index);
    }

    /**
     * FURenderer 构造函数
     */
    private FURenderer(Context context, boolean isCreateEGLContext) {
        this.mContext = context;
        this.mIsCreateEGLContext = isCreateEGLContext;
    }

    /**
     * 销毁 Faceunity 相关的资源
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

        mFrameId = 0;
        mIsNeedUpdateFaceBeauty = true;
        releaseAllAiModel();
        for (int i = 0; i < mItemsArray.length; i++) {
            if (mItemsArray[i] > 0) {
                faceunity.fuDestroyItem(mItemsArray[i]);
            }
        }
        Arrays.fill(mItemsArray, 0);
        faceunity.fuOnDeviceLost();
        faceunity.fuDone();
        if (mIsCreateEGLContext) {
            faceunity.fuReleaseEGLContext();
        }
    }

    /**
     * 单输入接口(fuRenderToNV21Image)
     *
     * @param img NV21 数据
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(byte[] img, int w, int h) {
        if (img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, mItemsArray, flags);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 单输入接口(fuRenderToNV21Image)，自定义画面数据需要回写到的 byte[]
     *
     * @param img         NV21数据
     * @param w
     * @param h
     * @param readBackImg 画面数据需要回写到的byte[]
     * @param readBackW
     * @param readBackH
     * @return
     */
    public int onDrawFrame(byte[] img, int w, int h, byte[] readBackImg, int readBackW, int readBackH) {
        if (img == null || w <= 0 || h <= 0 || readBackImg == null || readBackW <= 0 || readBackH <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuRenderToNV21Image(img, w, h, mFrameId++, mItemsArray, flags,
                readBackW, readBackH, readBackImg);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 双输入接口(fuDualInputToTexture)，处理后的画面数据并不会回写到数组，由于省去相应的数据拷贝性能相对最优，推荐使用。
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
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 双输入接口(fuDualInputToTexture)，自定义画面数据需要回写到的 byte[]
     *
     * @param img         NV21数据
     * @param tex         纹理ID
     * @param w
     * @param h
     * @param readBackImg 画面数据需要回写到的byte[]
     * @param readBackW
     * @param readBackH
     * @return
     */
    public int onDrawFrame(byte[] img, int tex, int w, int h, byte[] readBackImg, int readBackW, int readBackH) {
        if (tex <= 0 || img == null || w <= 0 || h <= 0 || readBackImg == null || readBackW <= 0 || readBackH <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType | mInputImageFormat;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuDualInputToTexture(img, tex, flags, w, h, mFrameId++, mItemsArray,
                readBackW, readBackH, readBackImg);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 单输入接口(fuRenderToTexture)
     *
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrame(int tex, int w, int h) {
        if (tex <= 0 || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType;
        if (mCurrentCameraType != Camera.CameraInfo.CAMERA_FACING_FRONT)
            flags |= FU_ADM_FLAG_FLIP_X;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuRenderToTexture(tex, w, h, mFrameId++, mItemsArray, flags);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    /**
     * 单美颜接口(fuBeautifyImage)，将输入的图像数据，送入SDK流水线进行全图美化，并输出处理之后的图像数据。
     * 该接口仅执行图像层面的美化处 理（包括滤镜、美肤），不执行人脸跟踪及所有人脸相关的操作（如美型）。
     * 由于功能集中，相比 fuDualInputToTexture 接口执行美颜道具，该接口所需计算更少，执行效率更高。
     *
     * @param tex 纹理ID
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrameBeautify(int tex, int w, int h) {
        if (tex <= 0 || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrame data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputTextureType;

        if (mNeedBenchmark)
            mFuCallStartTime = System.nanoTime();
        int fuTex = faceunity.fuBeautifyImage(tex, flags, w, h, mFrameId++, mItemsArray);
        if (mNeedBenchmark)
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        return fuTex;
    }

    public float[] getRotationData() {
        Arrays.fill(rotationData, 0.0f);
        faceunity.fuGetFaceInfo(0, "rotation", rotationData);
        return rotationData;
    }

    /**
     * 初始化系统环境，加载底层数据，并进行网络鉴权。
     * 应用使用期间只需要初始化一次，无需释放数据。
     * 必须在SDK其他接口前调用，否则会引起应用崩溃。
     */
    public static void initFURenderer(Context context) {
        Log.e(TAG, "initFURenderer: " + sIsInited);
        if (sIsInited) {
            return;
        }
        // 获取 Nama SDK 版本信息
        Log.e(TAG, "fu sdk version " + faceunity.fuGetVersion());
        fuSetup(context, BUNDLE_AI_MODEL_FACE_PROCESSOR, authpack.A());
        loadTongueModel(context, BUNDLE_TONGUE);
        sIsInited = true;
    }

    /**
     * 初始化 SDK，进行联网鉴权，必须在其他函数之前调用。
     *
     * @param context
     * @param bundlePath ai_face_processor.bundle 人脸识别数据包
     * @param authpack   authpack.java 鉴权证书
     */
    private static void fuSetup(Context context, String bundlePath, byte[] authpack) {
        int isSetup = faceunity.fuSetup(new byte[]{}, authpack);
        Log.d(TAG, "fuSetup. isSetup: " + (isSetup == 0 ? "no" : "yes"));
        loadAiModel(context, bundlePath, faceunity.FUAITYPE_FACEPROCESSOR);
    }

    /**
     * 加载 AI 模型资源
     *
     * @param context
     * @param bundlePath ai_model.bundle
     * @param type       faceunity.FUAITYPE_XXX
     */
    private static void loadAiModel(Context context, String bundlePath, int type) {
        byte[] buffer = FileUtils.readFile(context, bundlePath);
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

    /**
     * 加载舌头跟踪数据包，开启舌头跟踪
     *
     * @param context
     * @param bundlePath tongue.bundle
     */
    private static void loadTongueModel(Context context, String bundlePath) {
        byte[] buffer = FileUtils.readFile(context, bundlePath);
        if (buffer != null) {
            int isLoaded = faceunity.fuLoadTongueModel(buffer);
            Log.d(TAG, "loadTongueModel. isLoaded: " + (isLoaded == 0 ? "no" : "yes"));
        }
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
            byte[] buffer = FileUtils.readFile(context, bundlePath);
            if (buffer != null) {
                handle = faceunity.fuCreateItemFromPackage(buffer);
            }
        }
        Log.d(TAG, "loadItem. bundlePath: " + bundlePath + ", itemHandle: " + handle);
        return handle;
    }

    private static void releaseAllAiModel() {
        releaseAiModel(faceunity.FUAITYPE_BACKGROUNDSEGMENTATION);
        releaseAiModel(faceunity.FUAITYPE_BACKGROUNDSEGMENTATION_GREEN);
        releaseAiModel(faceunity.FUAITYPE_FACELANDMARKS209);
        releaseAiModel(faceunity.FUAITYPE_FACELANDMARKS239);
        releaseAiModel(faceunity.FUAITYPE_HANDGESTURE);
        releaseAiModel(faceunity.FUAITYPE_HAIRSEGMENTATION);
        releaseAiModel(faceunity.FUAITYPE_HUMANPOSE2D);
    }

    /**
     * 计算 RotationMode
     * 相机方向和 RotationMode 参数对照：
     * - 前置 270：home 下 1，home 右 0，home 上 3，home 左 2
     * - 后置 90： home 下 3，home 右 0，home 上 1，home 左 2
     */
    private int calculateRotationMode() {
        int rotMode = faceunity.FU_ROTATION_MODE_0;
        if (mInputImageOrientation == 270) {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotMode = mDeviceOrientation / 90;
            } else {
                if (mDeviceOrientation == 90) {
                    rotMode = faceunity.FU_ROTATION_MODE_270;
                } else if (mDeviceOrientation == 270) {
                    rotMode = faceunity.FU_ROTATION_MODE_90;
                } else {
                    rotMode = mDeviceOrientation / 90;
                }
            }
        } else if (mInputImageOrientation == 90) {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
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

    private int calculateRotModeLegacy() {
        int mode;
        if (mInputImageOrientation == 270) {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mode = mDeviceOrientation / 90;
            } else {
                mode = (mDeviceOrientation - 180) / 90;
            }
        } else {
            if (mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mode = (mDeviceOrientation + 180) / 90;
            } else {
                mode = mDeviceOrientation / 90;
            }
        }
        return mode;
    }

    /**
     * 获取 landmark 点位
     *
     * @param faceId
     * @return 调用时不要修改返回值，如需修改或传入 Nama 接口，请拷贝一份
     */
    public float[] getLandmarksData(int faceId) {
        int isTracking = faceunity.fuIsTracking();
        Arrays.fill(landmarksData, 0.0f);
        if (isTracking > 0) {
            faceunity.fuGetFaceInfo(faceId, "landmarks", landmarksData);
        }
        return landmarksData;
    }

    public int trackFace(byte[] img, int w, int h) {
        if (img == null) {
            return 0;
        }
        faceunity.fuOnCameraChange();
        int flags = mInputImageFormat;
        faceunity.fuTrackFace(img, flags, w, h);
        return faceunity.fuIsTracking();
    }

    public float[] getFaceRectData(int i) {
        Arrays.fill(faceRectData, 0.0f);
        faceunity.fuGetFaceInfo(i, "face_rect", faceRectData);
        return faceRectData;
    }

    //--------------------------------------对外可使用的接口----------------------------------------

    /**
     * 使用 fuTrackFace + fuAvatarToTexture 的方法组合绘制画面，该组合没有 camera 画面绘制，适用于 animoji 等相关道具的绘制。
     * fuTrackFace 获取识别到的人脸信息
     * fuAvatarToTexture 依据人脸信息绘制道具
     *
     * @param img 数据格式可由 flags 定义
     * @param w
     * @param h
     * @return
     */
    public int onDrawFrameAvatar(byte[] img, int w, int h) {
        if (img == null || w <= 0 || h <= 0) {
            Log.e(TAG, "onDrawFrameAvatar data null");
            return 0;
        }
        prepareDrawFrame();

        int flags = mInputImageFormat;
        if (mNeedBenchmark) {
            mFuCallStartTime = System.nanoTime();
        }

        faceunity.fuTrackFace(img, flags, w, h);
        int isTracking = faceunity.fuIsTracking();

        Arrays.fill(landmarksData, 0.0f);
        Arrays.fill(rotationData, 0.0f);
        Arrays.fill(expressionData, 0.0f);
        Arrays.fill(pupilPosData, 0.0f);
        Arrays.fill(rotationModeData, 0.0f);

        if (isTracking > 0) {
            /**
             * landmarks 2D 人脸特征点，返回值为 75 个二维坐标，长度 75*2
             */
            faceunity.fuGetFaceInfo(0, "landmarks", landmarksData);
            /**
             *rotation 人脸三维旋转，返回值为旋转四元数，长度 4
             */
            faceunity.fuGetFaceInfo(0, "rotation", rotationData);
            /**
             * expression  表情系数，长度 46
             */
            faceunity.fuGetFaceInfo(0, "expression", expressionData);
            /**
             * pupil pos 眼球旋转，长度 2
             */
            faceunity.fuGetFaceInfo(0, "pupil_pos", pupilPosData);
            /**
             * rotation mode 人脸朝向，0-3 分别对应手机四种朝向，长度 1
             */
            faceunity.fuGetFaceInfo(0, "rotation_mode", rotationModeData);
        } else {
            rotationData[3] = 1.0f;
            rotationModeData[0] = 1.0f * (360 - mInputImageOrientation) / 90;
        }

        int tex = faceunity.fuAvatarToTexture(AvatarConstant.PUP_POS_DATA, AvatarConstant.EXPRESSIONS,
                AvatarConstant.ROTATION_DATA, rotationModeData, 0, w, h, mFrameId++, mItemsArray,
                AvatarConstant.VALID_DATA);
        if (mNeedBenchmark) {
            mOneHundredFrameFUTime += System.nanoTime() - mFuCallStartTime;
        }
        return tex;
    }

    /**
     * 类似 GLSurfaceView 的 queueEvent 机制
     */
    public void queueEvent(Runnable r) {
        if (mEventQueue == null)
            return;
        mEventQueue.add(r);
    }

    /**
     * 设置需要识别的人脸个数
     *
     * @param maxFaces
     */
    public void setMaxFaces(final int maxFaces) {
        if (mMaxFaces != maxFaces && maxFaces > 0) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mMaxFaces = maxFaces;
                    faceunity.fuSetMaxFaces(mMaxFaces);
                }
            });
        }
    }

    /**
     * 每帧处理画面时被调用
     */
    private void prepareDrawFrame() {
        // 计算 FPS 等数据
        benchmarkFPS();

        // 获取人脸是否识别，并调用回调接口
        int isTracking = faceunity.fuIsTracking();
        if (mOnTrackingStatusChangedListener != null && mTrackingStatus != isTracking) {
            mOnTrackingStatusChangedListener.onTrackingStatusChanged(mTrackingStatus = isTracking);
        }

        // 获取 faceunity 错误信息，并调用回调接口
        int error = faceunity.fuGetSystemError();
        if (error != 0) {
            Log.e(TAG, "fuGetSystemErrorString " + faceunity.fuGetSystemErrorString(error));
            if (mOnSystemErrorListener != null) {
                mOnSystemErrorListener.onSystemError(faceunity.fuGetSystemErrorString(error));
            }
        }

        // 修改美颜参数
        if (mIsNeedUpdateFaceBeauty && mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] > 0) {
            int itemBeauty = mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX];
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.IS_BEAUTY_ON, 1.0);
            // filter_name 滤镜名称
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.FILTER_NAME, mFilterName);
            // filter_level 滤镜强度 范围 0~1 SDK 默认为 1
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.FILTER_LEVEL, mFilterLevel);

            // skin_detect 精准美肤（肤色检测开关） 0:关闭 1:开启 SDK默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.SKIN_DETECT, mSkinDetect);
            // heavy_blur 磨皮类型 0:清晰磨皮 1:重度磨皮 SDK默认为 1
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.HEAVY_BLUR, mHeavyBlur);
            // blur_level 磨皮 范围 0~6 SDK 默认为 6
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.BLUR_LEVEL, 6 * mBlurLevel);
            // color_level 美白 范围 0~1 SDK 默认为 0.2
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.COLOR_LEVEL, mColorLevel);
            // red_level 红润 范围 0~1 SDK默认为 0.5
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.RED_LEVEL, mRedLevel);
            // eye_bright 亮眼 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.EYE_BRIGHT, mEyeBright);
            // tooth_whiten 美牙 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.TOOTH_WHITEN, mToothWhiten);

            // face_shape_level 美型程度 范围 0~1 SDK 默认为 1
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.FACE_SHAPE_LEVEL, mFaceShapeLevel);
            // face_shape 脸型 0：女神 1：网红，2：自然，3：默认，4：精细变形，5 用户自定义，SDK 默认为 3
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.FACE_SHAPE, mFaceShape);
            // eye_enlarging 大眼 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.EYE_ENLARGING, mEyeEnlarging);
            // cheek_thinning 瘦脸 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.CHEEK_THINNING, mCheekThinning);
            // cheek_narrow 窄脸 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.CHEEK_NARROW, mCheekNarrow);
            // cheek_small 小脸 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.CHEEK_SMALL, mCheekSmall);
            // cheek_v V 脸 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.CHEEK_V, mCheekV);
            // intensity_nose 鼻子 范围 0~1 SDK 默认为 0
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.INTENSITY_NOSE, mIntensityNose);
            // intensity_chin 下巴 范围 0~1 SDK 默认为 0.5    大于 0.5 变大，小于 0.5 变小
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.INTENSITY_CHIN, mIntensityChin);
            // intensity_forehead 额头 范围 0~1 SDK默认为 0.5    大于 0.5 变大，小于 0.5 变小
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.INTENSITY_FOREHEAD, mIntensityForehead);
            // intensity_mouth 嘴型 范围 0~1 SDK 默认为 0.5   大于 0.5 变大，小于 0.5 变小
            faceunity.fuItemSetParam(itemBeauty, BeautificationParams.INTENSITY_MOUTH, mIntensityMouth);
            mIsNeedUpdateFaceBeauty = false;
        }

        //queueEvent的Runnable在此处被调用
        while (!mEventQueue.isEmpty()) {
            mEventQueue.remove(0).run();
        }
    }

    /**
     * camera 切换时需要调用
     *
     * @param currentCameraType     前后置摄像头ID
     * @param inputImageOrientation
     */
    public void onCameraChange(final int currentCameraType, final int inputImageOrientation) {
        Log.d(TAG, "onCameraChange() called with: currentCameraType = [" + currentCameraType
                + "], inputImageOrientation = [" + inputImageOrientation + "]");
        if (mCurrentCameraType == currentCameraType && mInputImageOrientation == inputImageOrientation) {
            return;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mFrameId = 0;
                mCurrentCameraType = currentCameraType;
                mInputImageOrientation = inputImageOrientation;
                faceunity.fuOnCameraChange();
                mRotationMode = calculateRotationMode();
                faceunity.fuSetDefaultRotationMode(mRotationMode);
                updateEffectItemParams(mDefaultEffect, mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
            }
        });
    }

    /**
     * 设置识别方向
     *
     * @param rotation
     */
    public void setTrackOrientation(final int rotation) {
        if (mDeviceOrientation == rotation) {
            return;
        }
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mDeviceOrientation = rotation;
                mRotationMode = calculateRotationMode();
                // 背景分割 Animoji 表情识别 人像驱动 手势识别，转动手机时，重置人脸识别
                if (mDefaultEffect != null && (mDefaultEffect.effectType() == Effect.EFFECT_TYPE_BACKGROUND
                        || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_ANIMOJI
                        || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_EXPRESSION
                        || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_GESTURE
                        || mDefaultEffect.effectType() == Effect.EFFECT_TYPE_PORTRAIT_DRIVE)) {
                    faceunity.fuOnCameraChange();
                    setEffectRotationMode(mDefaultEffect, mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
                }
                faceunity.fuSetDefaultRotationMode(mRotationMode);
                Log.d(TAG, "setTrackOrientation: " + rotation + ", rotationMode:" + mRotationMode);
            }
        });
    }

    public void setDefaultEffect(Effect defaultEffect) {
        mDefaultEffect = defaultEffect;
    }

    public void setNeedAnimoji3D(boolean needAnimoji3D) {
        isNeedAnimoji3D = needAnimoji3D;
        if (isNeedAnimoji3D) {
            mFuItemHandler.sendEmptyMessage(ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX);
        } else {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mItemsArray[ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX] > 0) {
                        Log.d(TAG, "destroy animoji3D item");
                        faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX]);
                        mItemsArray[ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX] = 0;
                    }
                }
            });
        }
    }

    //--------------------------------------美颜参数与道具回调----------------------------------------

    @Override
    public void onMusicFilterTime(final long time) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX], "music_time", time);
            }
        });
    }

    @Override
    public void onEffectSelected(Effect effect) {
        if (effect == null) {
            return;
        }
        mDefaultEffect = effect;
        if (mFuItemHandler == null) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mFuItemHandler.removeMessages(ITEM_ARRAYS_EFFECT_INDEX);
                    mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
                }
            });
        } else {
            mFuItemHandler.removeMessages(ITEM_ARRAYS_EFFECT_INDEX);
            mFuItemHandler.sendMessage(Message.obtain(mFuItemHandler, ITEM_ARRAYS_EFFECT_INDEX, mDefaultEffect));
        }
    }

    @Override
    public void onFilterLevelSelected(float progress) {
        mFilterLevel = progress;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onFilterNameSelected(String filterName) {
        mFilterName = filterName;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onSkinDetectSelected(float isOpen) {
        mSkinDetect = isOpen;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onHeavyBlurSelected(float isOpen) {
        mHeavyBlur = isOpen;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onBlurLevelSelected(float level) {
        mBlurLevel = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onColorLevelSelected(float level) {
        mColorLevel = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onRedLevelSelected(float level) {
        mRedLevel = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onEyeBrightSelected(float level) {
        mEyeBright = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onToothWhitenSelected(float level) {
        mIsNeedUpdateFaceBeauty = true;
        mToothWhiten = level;
    }

    @Override
    public void onEyeEnlargeSelected(float level) {
        mEyeEnlarging = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekThinningSelected(float level) {
        mCheekThinning = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekNarrowSelected(float level) {
        // 窄脸参数上限为 0.5
        mCheekNarrow = level / 2;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekSmallSelected(float level) {
        // 小脸参数上限为 0.5
        mCheekSmall = level / 2;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onCheekVSelected(float level) {
        mCheekV = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onIntensityChinSelected(float level) {
        mIntensityChin = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onIntensityForeheadSelected(float level) {
        mIntensityForehead = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    @Override
    public void onIntensityNoseSelected(float level) {
        mIsNeedUpdateFaceBeauty = true;
        mIntensityNose = level;
    }

    @Override
    public void onIntensityMouthSelected(float level) {
        mIntensityMouth = level;
        mIsNeedUpdateFaceBeauty = true;
    }

    //--------------------------------------IsTracking（人脸识别回调相关定义）----------------------------------------

    private int mTrackingStatus = 0;

    public interface OnTrackingStatusChangedListener {
        void onTrackingStatusChanged(int status);
    }

    private OnTrackingStatusChangedListener mOnTrackingStatusChangedListener;

    //--------------------------------------FaceUnitySystemError（faceunity错误信息回调相关定义）----------------------------------------

    public interface OnSystemErrorListener {
        void onSystemError(String error);
    }

    private OnSystemErrorListener mOnSystemErrorListener;


    //--------------------------------------OnBundleLoadCompleteListener（faceunity道具加载完成）----------------------------------------

    public void setOnBundleLoadCompleteListener(OnBundleLoadCompleteListener onBundleLoadCompleteListener) {
        mOnBundleLoadCompleteListener = onBundleLoadCompleteListener;
    }

    /**
     * fuCreateItemFromPackage 加载道具
     *
     * @param bundlePath 道具 bundle 的路径
     * @return 大于 0 时加载成功
     */
    private int loadItem(String bundlePath) {
        int item = 0;
        try {
            if (!TextUtils.isEmpty(bundlePath)) {
                InputStream is = bundlePath.startsWith(Constant.filePath) ? new FileInputStream(new File(bundlePath)) : mContext.getAssets().open(bundlePath);
                byte[] itemData = new byte[is.available()];
                int len = is.read(itemData);
                is.close();
                item = faceunity.fuCreateItemFromPackage(itemData);
                Log.e(TAG, "bundle path: " + bundlePath + ", length: " + len + "Byte, handle:" + item);
            }
        } catch (IOException e) {
            Log.e(TAG, "loadItem error ", e);
        }
        return item;
    }


    //--------------------------------------FPS（FPS相关定义）----------------------------------------

    private static final float NANO_IN_ONE_MILLI_SECOND = 1000000.0f;
    private static final float TIME = 5f;
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
            long tmp = System.nanoTime();
            double fps = (1000.0f * NANO_IN_ONE_MILLI_SECOND / ((tmp - mLastOneHundredFrameTimeStamp) / TIME));
            mLastOneHundredFrameTimeStamp = tmp;
            double renderTime = mOneHundredFrameFUTime / TIME / NANO_IN_ONE_MILLI_SECOND;
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
        if (effect == null || itemHandle <= 0) {
            return;
        }
        faceunity.fuItemSetParam(itemHandle, "isAndroid", 1.0);
        mRotationMode = calculateRotationMode();
        int effectType = effect.effectType();
        if (effectType == Effect.EFFECT_TYPE_NORMAL) {
            // rotationAngle 参数是用于旋转普通道具
            faceunity.fuItemSetParam(itemHandle, "rotationAngle", mRotationMode * 90);
        }
        int back = mCurrentCameraType == Camera.CameraInfo.CAMERA_FACING_BACK ? 1 : 0;

        if (effectType == Effect.EFFECT_TYPE_ANIMOJI || effectType == Effect.EFFECT_TYPE_PORTRAIT_DRIVE) {
            // 镜像顶点
            faceunity.fuItemSetParam(itemHandle, "is3DFlipH", back);
            // 镜像表情
            faceunity.fuItemSetParam(itemHandle, "isFlipExpr", back);
            // 这两句代码用于识别人脸默认方向的修改，主要针对 animoji 道具的切换摄像头倒置问题
            faceunity.fuItemSetParam(itemHandle, "camera_change", 1.0);
        }

        if (effectType == Effect.EFFECT_TYPE_GESTURE) {
            // loc_y_flip 与 loc_x_flip 参数是用于对手势识别道具的镜像
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
        setMaxFaces(effect.maxFace());
    }

    private void setEffectRotationMode(Effect effect, int itemHandle) {
        int rotMode;
        if (effect.effectType() == Effect.EFFECT_TYPE_GESTURE && effect.bundleName().startsWith("ctrl")) {
            rotMode = calculateRotModeLegacy();
        } else {
            rotMode = mRotationMode;
        }
        faceunity.fuItemSetParam(itemHandle, "rotMode", rotMode);
        faceunity.fuItemSetParam(itemHandle, "rotationMode", rotMode);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * 美颜道具参数，包含红润、美白、清晰磨皮、重度磨皮、滤镜、变形、亮眼、美牙功能。
     */
    static class BeautificationParams {
        // 滤镜名称，默认 origin
        public static final String FILTER_NAME = "filter_name";
        // 滤镜程度，0-1，默认 1
        public static final String FILTER_LEVEL = "filter_level";
        // 美白程度，0-1，默认 0.2
        public static final String COLOR_LEVEL = "color_level";
        // 红润程度，0-1，默认 0.5
        public static final String RED_LEVEL = "red_level";
        // 磨皮程度，0-6，默认 6
        public static final String BLUR_LEVEL = "blur_level";
        // 肤色检测开关，0 代表关，1 代表开，默认 0
        public static final String SKIN_DETECT = "skin_detect";
        // 肤色检测开启后，非肤色区域的融合程度，0-1，默认 0.45
        public static final String NONSKIN_BLUR_SCALE = "nonskin_blur_scale";
        // 磨皮类型，0 代表清晰磨皮，1 代表重度磨皮，默认 1
        public static final String HEAVY_BLUR = "heavy_blur";
        // 变形选择，0 代表女神，1 网红，2 自然，3 预设，4，精细变形，5 用户自定义，默认 3
        public static final String FACE_SHAPE = "face_shape";
        // 变形程度，0-1，默认 1
        public static final String FACE_SHAPE_LEVEL = "face_shape_level";
        // 大眼程度，0-1，默认 0.5
        public static final String EYE_ENLARGING = "eye_enlarging";
        // 瘦脸程度，0-1，默认 0
        public static final String CHEEK_THINNING = "cheek_thinning";
        // 窄脸程度，0-1，默认 0
        public static final String CHEEK_NARROW = "cheek_narrow";
        // 小脸程度，0-1，默认 0
        public static final String CHEEK_SMALL = "cheek_small";
        // V脸程度，0-1，默认 0
        public static final String CHEEK_V = "cheek_v";
        // 瘦鼻程度，0-1，默认 0
        public static final String INTENSITY_NOSE = "intensity_nose";
        // 嘴巴调整程度，0-1，默认 0.5
        public static final String INTENSITY_MOUTH = "intensity_mouth";
        // 额头调整程度，0-1，默认 0.5
        public static final String INTENSITY_FOREHEAD = "intensity_forehead";
        // 下巴调整程度，0-1，默认 0.5
        public static final String INTENSITY_CHIN = "intensity_chin";
        // 变形渐变调整参数，0 渐变关闭，大于 0 渐变开启，值为渐变需要的帧数
        public static final String CHANGE_FRAMES = "change_frames";
        // 亮眼程度，0-1，默认 1
        public static final String EYE_BRIGHT = "eye_bright";
        // 美牙程度，0-1，默认 1
        public static final String TOOTH_WHITEN = "tooth_whiten";
        // 美颜参数全局开关，0 代表关，1 代表开
        public static final String IS_BEAUTY_ON = "is_beauty_on";

        // 女神
        public static final int FACE_SHAPE_GODDESS = 0;
        // 网红
        public static final int FACE_SHAPE_NET_RED = 1;
        // 自然
        public static final int FACE_SHAPE_NATURE = 2;
        // 默认
        public static final int FACE_SHAPE_DEFAULT = 3;
        // 精细变形
        public static final int FACE_SHAPE_CUSTOM = 4;
    }

    /*----------------------------------Builder---------------------------------------*/

    /**
     * FURenderer Builder
     */
    public static class Builder {

        private boolean createEGLContext = false;
        private Effect defaultEffect;
        private int maxFaces = 4;
        private Context context;
        private int inputTextureType = 0;
        private int inputImageFormat = 0;
        private int inputImageRotation = 270;
        private boolean isNeedAnimoji3D = false;
        private boolean isNeedFaceBeauty = true;
        private int currentCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
        private OnBundleLoadCompleteListener onBundleLoadCompleteListener;
        private OnFUDebugListener onFUDebugListener;
        private OnTrackingStatusChangedListener onTrackingStatusChangedListener;
        private OnSystemErrorListener onSystemErrorListener;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        /**
         * 是否需要自己创建EGLContext
         *
         * @param createEGLContext
         * @return
         */
        public Builder createEGLContext(boolean createEGLContext) {
            this.createEGLContext = createEGLContext;
            return this;
        }

        /**
         * 是否需要立即加载道具
         *
         * @param defaultEffect
         * @return
         */
        public Builder defaultEffect(Effect defaultEffect) {
            this.defaultEffect = defaultEffect;
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
         * 传入纹理的类型（传入数据没有纹理则无需调用）
         * camera OES 纹理：1
         * 普通 2D 纹理：0
         *
         * @param textureType
         * @return
         */
        public Builder inputTextureType(int textureType) {
            this.inputTextureType = textureType;
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

        /**
         * 输入的画面数据方向
         *
         * @param inputImageRotation
         * @return
         */
        public Builder inputImageOrientation(int inputImageRotation) {
            this.inputImageRotation = inputImageRotation;
            return this;
        }

        /**
         * 是否需要3D道具的抗锯齿功能
         *
         * @param needAnimoji3D
         * @return
         */
        public Builder setNeedAnimoji3D(boolean needAnimoji3D) {
            this.isNeedAnimoji3D = needAnimoji3D;
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
         * 当前的摄像头（前后置摄像头）
         *
         * @param cameraType
         * @return
         */
        public Builder setCurrentCameraType(int cameraType) {
            currentCameraType = cameraType;
            return this;
        }

        /**
         * 设置 debug 数据回调
         *
         * @param onFUDebugListener
         * @return
         */
        public Builder setOnFUDebugListener(OnFUDebugListener onFUDebugListener) {
            this.onFUDebugListener = onFUDebugListener;
            return this;
        }

        /**
         * 设置是否检查到人脸的回调
         *
         * @param onTrackingStatusChangedListener
         * @return
         */
        public Builder setOnTrackingStatusChangedListener(OnTrackingStatusChangedListener onTrackingStatusChangedListener) {
            this.onTrackingStatusChangedListener = onTrackingStatusChangedListener;
            return this;
        }

        /**
         * 设置bundle加载完成回调
         *
         * @param onBundleLoadCompleteListener
         * @return
         */
        public Builder setOnBundleLoadCompleteListener(OnBundleLoadCompleteListener onBundleLoadCompleteListener) {
            this.onBundleLoadCompleteListener = onBundleLoadCompleteListener;
            return this;
        }


        /**
         * 设置 SDK 使用错误回调
         *
         * @param onSystemErrorListener
         * @return
         */
        public Builder setOnSystemErrorListener(OnSystemErrorListener onSystemErrorListener) {
            this.onSystemErrorListener = onSystemErrorListener;
            return this;
        }

        public FURenderer build() {
            FURenderer fuRenderer = new FURenderer(context, createEGLContext);
            fuRenderer.mMaxFaces = maxFaces;
            fuRenderer.mInputTextureType = inputTextureType;
            fuRenderer.mInputImageFormat = inputImageFormat;
            fuRenderer.mInputImageOrientation = inputImageRotation;
            fuRenderer.mDefaultEffect = defaultEffect;
            fuRenderer.isNeedAnimoji3D = isNeedAnimoji3D;
            fuRenderer.isNeedFaceBeauty = isNeedFaceBeauty;
            fuRenderer.mCurrentCameraType = currentCameraType;
            fuRenderer.mOnFUDebugListener = onFUDebugListener;
            fuRenderer.mOnTrackingStatusChangedListener = onTrackingStatusChangedListener;
            fuRenderer.mOnSystemErrorListener = onSystemErrorListener;
            fuRenderer.mOnBundleLoadCompleteListener = onBundleLoadCompleteListener;
            return fuRenderer;
        }
    }

    static class AvatarConstant {
        public static final int EXPRESSION_LENGTH = 46;
        public static final float[] ROTATION_DATA = new float[]{0f, 0f, 0f, 1f};
        public static final float[] PUP_POS_DATA = new float[]{0f, 0f};
        public static final int VALID_DATA = 1;
        public static final float[] EXPRESSIONS = new float[EXPRESSION_LENGTH];

        static {
            Arrays.fill(EXPRESSIONS, 0f);
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
                // 加载普通道具
                case ITEM_ARRAYS_EFFECT_INDEX: {
                    final Effect effect = (Effect) msg.obj;
                    if (effect == null) {
                        return;
                    }
                    boolean isNone = effect.effectType() == Effect.EFFECT_TYPE_NONE;
                    final int itemEffect = isNone ? 0 : loadItem(effect.path());
                    if (!isNone && itemEffect <= 0) {
                        Log.w(TAG, "create effect item failed: " + itemEffect);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            if (mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] > 0) {
                                faceunity.fuDestroyItem(mItemsArray[ITEM_ARRAYS_EFFECT_INDEX]);
                                mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = 0;
                            }
                            if (itemEffect > 0) {
                                updateEffectItemParams(effect, itemEffect);
                            }
                            mItemsArray[ITEM_ARRAYS_EFFECT_INDEX] = itemEffect;
                        }
                    });
                }
                break;
                // 加载美颜 bundle
                case ITEM_ARRAYS_FACE_BEAUTY_INDEX: {
                    final int itemBeauty = loadItem(BUNDLE_FACE_BEAUTIFICATION);
                    if (itemBeauty <= 0) {
                        Log.w(TAG, "load face beauty item failed: " + itemBeauty);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mItemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = itemBeauty;
                            mIsNeedUpdateFaceBeauty = true;
                        }
                    });
                }
                break;
                // 加载 Animoji 道具 3D 抗锯齿 bundle
                case ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX: {
                    final int itemAnimoji3D = loadItem(BUNDLE_ANIMOJI_3D);
                    if (itemAnimoji3D <= 0) {
                        Log.w(TAG, "create Animoji3D item failed: " + itemAnimoji3D);
                        return;
                    }
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mItemsArray[ITEM_ARRAYS_EFFECT_ABIMOJI_3D_INDEX] = itemAnimoji3D;
                        }
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
