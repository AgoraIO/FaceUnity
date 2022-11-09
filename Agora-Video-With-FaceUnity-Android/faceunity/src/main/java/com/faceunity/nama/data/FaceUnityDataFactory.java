package com.faceunity.nama.data;

import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.nama.FURenderer;

/**
 * DESC：
 * Created on 2021/4/25
 */
public class FaceUnityDataFactory {

    /**
     * 道具数据工厂
     */
    public FaceBeautyDataFactory mFaceBeautyDataFactory;
    public BodyBeautyDataFactory mBodyBeautyDataFactory;
    public MakeupDataFactory mMakeupDataFactory;
    public PropDataFactory mPropDataFactory;


    private FURenderKit mFURenderKit = FURenderKit.getInstance();
    private FURenderer mFURenderer = FURenderer.getInstance();

    /**
     * 道具加载标识
     */
    public int currentFunctionIndex;
    private boolean hasFaceBeautyLoaded = false;
    private boolean hasBodyBeautyLoaded = false;
    private boolean hasMakeupLoaded = false;
    private boolean hasPropLoaded = false;


    public FaceUnityDataFactory(int index) {
        currentFunctionIndex = index;
        mFaceBeautyDataFactory = new FaceBeautyDataFactory();
        mBodyBeautyDataFactory = new BodyBeautyDataFactory();
        mMakeupDataFactory = new MakeupDataFactory(0);
        mPropDataFactory = new PropDataFactory(0);
    }

    /**
     * FURenderKit加载当前特效
     */
    public void bindCurrentRenderer() {
        switch (currentFunctionIndex) {
            case 0:
                mFaceBeautyDataFactory.bindCurrentRenderer();
                hasFaceBeautyLoaded = true;
                break;
            case 1:
                mPropDataFactory.bindCurrentRenderer();
                hasPropLoaded = true;
                break;

            case 2:
                mMakeupDataFactory.bindCurrentRenderer();
                hasMakeupLoaded = true;
                break;
            case 3:
                mBodyBeautyDataFactory.bindCurrentRenderer();
                hasBodyBeautyLoaded = true;
                break;
        }
        if (hasFaceBeautyLoaded && currentFunctionIndex != 0) {
            mFaceBeautyDataFactory.bindCurrentRenderer();
        }
        if (hasPropLoaded && currentFunctionIndex != 1) {
            mPropDataFactory.bindCurrentRenderer();
        }
        if (hasMakeupLoaded && currentFunctionIndex != 2) {
            mMakeupDataFactory.bindCurrentRenderer();
        }
        if (hasBodyBeautyLoaded && currentFunctionIndex != 3) {
            mBodyBeautyDataFactory.bindCurrentRenderer();
        }
        if (currentFunctionIndex == 3) {
            mFURenderKit.getFUAIController().setMaxFaces(1);
            mFURenderer.setAIProcessTrackType(FUAIProcessorEnum.HUMAN_PROCESSOR);
        } else {
            mFURenderKit.getFUAIController().setMaxFaces(4);
            mFURenderer.setAIProcessTrackType(FUAIProcessorEnum.FACE_PROCESSOR);
        }
    }

    /**
     * 道具功能切换
     */
    public void onFunctionSelected(int index) {
        currentFunctionIndex = index;
        switch (index) {
            case 0:
                if (!hasFaceBeautyLoaded) {
                    mFaceBeautyDataFactory.bindCurrentRenderer();
                    hasFaceBeautyLoaded = true;
                }
                mFURenderKit.getFUAIController().setMaxFaces(4);
                mFURenderer.setAIProcessTrackType(FUAIProcessorEnum.FACE_PROCESSOR);
                break;
            case 1:
                if (!hasPropLoaded) {
                    mPropDataFactory.bindCurrentRenderer();
                    hasPropLoaded = true;
                }
                mFURenderKit.getFUAIController().setMaxFaces(4);
                mFURenderer.setAIProcessTrackType(FUAIProcessorEnum.FACE_PROCESSOR);
                break;
            case 2:
                if (!hasMakeupLoaded) {
                    mMakeupDataFactory.bindCurrentRenderer();
                    hasMakeupLoaded = true;
                }
                mFURenderKit.getFUAIController().setMaxFaces(4);
                mFURenderer.setAIProcessTrackType(FUAIProcessorEnum.FACE_PROCESSOR);
                break;
            case 3:
                if (!hasBodyBeautyLoaded) {
                    mBodyBeautyDataFactory.bindCurrentRenderer();
                    hasBodyBeautyLoaded = true;
                }
                mFURenderKit.getFUAIController().setMaxFaces(1);
                mFURenderer.setAIProcessTrackType(FUAIProcessorEnum.HUMAN_PROCESSOR);
                break;
        }
    }
}
