#pragma once

#include <vector>
#include <memory>
#include <string>
#include <unordered_map>
#include <sstream>
#include <fstream>
#include <iostream>
#include <opencv2/highgui.hpp>
class CCameraDS;
typedef unsigned char uchar;
#define BUNDLE_COUNT_IN_USE 11
#define WM_FU_MSGID(code) (WM_USER+0x1000+code)
#define EID_FU_INIT_ERROR         0x00000001
#define EID_FU_BUNDLE_ERROR       0x00000002
namespace NamaNameSpace
{
    enum BundleCategory
    {
        Animoji,
        ItemSticker,
        ARMask,
        ChangeFace,//not used now
        ExpressionRecognition,
        MusicFilter,
        BackgroundSegmentation,
        GestureRecognition,
        MagicMirror,
        PortraitDrive,
        Makeup,
        Hair,
        ChangeFaceEx,
        ExpressionGif,
        Facebeauty,
        LightMakeup,
        Facepup,

        Count
    };

    enum MakeupParamType
    {
        MString,
        MArray,
        MInt,
        MFloat,
    };
    struct MakeupParam
    {
        int type;
        std::string name;
        float value;
        std::vector<float> colorArr;
        std::string tex;
        int lip_type;
        float brow_warp;
        int brow_warp_type;
    };
    class Nama
    {
    public:

        static int bundleCategory;
        static int renderBundleCategory;
        static int faceType;

        static bool showItemSelectWindow;
        static bool showItemTipsWindow;
        static bool showDegubInfoWindow;
        static bool showFilterSlider;
        static int showMakeUpWindow;
        static bool mNeedIpcWrite;
        static bool mNeedPlayMP3;
        static bool mNeedStopMP3;

        static uint32_t mFPS;
        static uint32_t mResolutionWidth;
        static uint32_t mResolutionHeight;
        static uint32_t mRenderTime;

        static int m_curFilterIdx;
        static int m_curRenderItem;
        static int m_curBindedItem; ;
       // static ImGuiID m_curRenderItemUIID;

        static int mEnableSkinDect;
        static int mEnableHeayBlur;
        static int mEnableExBlur;
        static float mFaceBeautyLevel[5];
        static float mFaceShapeLevel[9];
        static float mFilterLevel[10];
        static float mMakeupLevel[10];

        static int mSelectedCamera;
        static double mLastTime;
        static std::string mCurRenderItemName;
        static std::vector<std::string> categoryBundles[BundleCategory::Count];

        static std::string gBundlePath[BUNDLE_COUNT_IN_USE];

        static void resetBeautyParam();
        static void resetShapeParam();
        static void loadAllBundles();

        static void IteratorFolder(const char* lpPath, std::vector<std::string> &fileList);
        static void Wchar_tToString(std::string& szDst, wchar_t *wchar);
        static void FindAllBundle(std::string folder, std::vector<std::string> &files);

        static std::string initFuError;
        static std::string bundleInfo;
        static std::string filterInfo;

        using UniquePtr = std::unique_ptr<Nama>;
        static UniquePtr create(uint32_t width, uint32_t height, bool enable = true);

        Nama();
        ~Nama();
       // std::vector<std::string> CameraList();
        //cv::Mat GetFrame();
       // bool ReOpenCamera(int);
        bool CheckGLContext();
        bool Init(uint32_t& width, uint32_t& height);
        bool IsInited() { return mHasSetup; }
        bool SelectBundle(std::string bundleName);
        bool CheckModuleCode(int category);
        int  IsTracking();
        void SetCurrentShape(int index);
        int CreateMakeupBundle(std::string bundleName);
        void SelectMakeupBundle(std::string bundleName);
        void UpdateFilter(int);
        void UpdateBeauty();
        void SwitchBeauty(bool);
        void RenderItems(uchar* frame, int inframeType);
        void DrawLandmarks(uchar*  frame);
        uchar*  RenderEx(uchar*);
        void DrawPoint(uchar*  frame, int x, int y, unsigned char r = 255, unsigned char g = 240, unsigned char b = 33);
    private:
        int mBeautyHandles;
        int mMakeUpHandle;
        int mNewFaceTracker;
        int mGestureHandles;
        int mFxaaHandles;
        uint32_t mFrameWidth, mFrameHeight;
        static bool mHasSetup;
        
    public:
        static bool mEnableNama;
        int mIsBeautyOn;
        int mIsDrawPoints;
        int mMaxFace;
        int mFrameID;
        int mModuleCode, mModuleCode1;
      //  std::tr1::shared_ptr<CCameraDS> mCapture;
        static std::string mFilters[6];
        std::unordered_map<std::string, int> mBundlesMap;
        std::unordered_map<std::string, std::vector<MakeupParam>> mMakeupsMap;
    };

    size_t FileSize(std::ifstream& file);

    bool LoadBundle(const std::string& filepath, std::vector<char>& data);
}

template < class T>
std::string ConvertToString(T value) {
    std::stringstream ss;
    ss << value;
    return ss.str();
}

struct MakeupParam
{
	std::string typeName;
	std::string valueName;
	std::string textureName;
	int value;
};
