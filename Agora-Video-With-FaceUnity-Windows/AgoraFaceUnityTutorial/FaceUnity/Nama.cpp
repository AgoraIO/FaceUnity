
#include "stdafx.h"
#include <map>
#include "Nama.h"
#include "FConfig.h"	

#include "rapidjson/filereadstream.h"
#include "rapidjson/document.h"
#include "Language.h"

#include <funama.h>				//nama SDK header file
#include <authpack.h>			//nama SDK key header file
#pragma comment(lib, "nama.lib")//nama SDK lib file
//play music
#include "Sound/MP3.h"

std::map<int,Mp3*> mp3Map;
using namespace NamaNameSpace;
//static
int Nama::bundleCategory = -1;
int Nama::renderBundleCategory = -1;
std::vector<std::string> Nama::categoryBundles[BundleCategory::Count];
int Nama::faceType = 0;

bool Nama::showItemSelectWindow = false;
bool Nama::showItemTipsWindow = false;
bool Nama::showDegubInfoWindow = false;
bool Nama::showFilterSlider = false;
int Nama::showMakeUpWindow = false;
uint32_t Nama::mFPS = 60;
uint32_t Nama::mResolutionWidth = 1280;
uint32_t Nama::mResolutionHeight = 720;
uint32_t Nama::mRenderTime = 33;

int Nama::m_curFilterIdx;
int Nama::m_curRenderItem = -1;
int Nama::m_curBindedItem = -1;
//ImGuiID Nama::m_curRenderItemUIID = -1;
std::string Nama::mCurRenderItemName = "";
double Nama::mLastTime = 0.0;
int Nama::mEnableSkinDect = 1;
int Nama::mEnableHeayBlur = 0;
int Nama::mEnableExBlur = 0;
int Nama::mSelectedCamera = 0;
float Nama::mFaceBeautyLevel[5] = { 0.0f };
float Nama::mFaceShapeLevel[9] = { 0.0f };
float Nama::mFilterLevel[10] = { 100,100,100,100,100, 100,100,100,100,100 };
float Nama::mMakeupLevel[10] = { 100,100,100,100,100, 100,100,100,100,100 };

bool Nama::mNeedIpcWrite = false;
bool Nama::mNeedPlayMP3 = false;
bool Nama::mNeedStopMP3 = false;
//end
bool Nama::mHasSetup = false;
bool Nama::mEnableNama = false;

std::string Nama::initFuError;
std::string Nama::bundleInfo;
std::string Nama::filterInfo;

static HGLRC new_context;

std::string Nama::mFilters[6] = { "origin", "bailiang1", "fennen1", "xiaoqingxin1", "lengsediao1", "nuansediao1" };

std::map<int, int> modules = { {Animoji,16},{ItemSticker,2},{ARMask,32},{ChangeFace,128},
{ExpressionRecognition,2048},{MusicFilter,131072},{BackgroundSegmentation,256},
{GestureRecognition,512},{MagicMirror,65536},{PortraitDrive,32768},{Makeup,524288},
{Hair,524288},{ChangeFaceEx,8388608},{ExpressionGif,16777216}, {Facebeauty,1} ,{LightMakeup,0} ,{Facepup,0} };

std::map<int, int> modules1 = { {Animoji,0},{ItemSticker,0},{ARMask,0},{ChangeFace,0},
{ExpressionRecognition,0},{MusicFilter,0},{BackgroundSegmentation,0},
{GestureRecognition,0},{MagicMirror,0},{PortraitDrive,0},{Makeup,0},
{Hair,0},{ChangeFaceEx,0},{ExpressionGif,0}, {Facebeauty,0} ,{LightMakeup,8} ,{Facepup,16} };

std::string Nama::gBundlePath[BUNDLE_COUNT_IN_USE] = {
 "items\\Animoji\\",
 "items\\ItemSticker\\",
 "items\\ARMask\\",
 "items\\ChangeFace\\",
 "items\\ExpressionRecognition\\",
 "items\\MusicFilter\\",
 "items\\BackgroundSegmentation\\",
 "items\\GestureRecognition\\",
 "items\\MagicMirror\\",
 "items\\PortraitDrive\\",
 "items\\Makeup\\"
};

void Nama::resetBeautyParam()
{
    mFaceBeautyLevel[0] = 70;
    mFaceBeautyLevel[1] = 50;
    mFaceBeautyLevel[2] = 50;
    mFaceBeautyLevel[3] = 70;
    mFaceBeautyLevel[4] = 0;
}

void Nama::resetShapeParam()
{
    faceType = 0;
    mFaceShapeLevel[0] = 40;
    mFaceShapeLevel[1] = 40;
    mFaceShapeLevel[2] = -20;
    mFaceShapeLevel[3] = -20;
    mFaceShapeLevel[4] = 50;
    mFaceShapeLevel[5] = -10;

    mFaceShapeLevel[6] = 0;
    mFaceShapeLevel[7] = 0;
    mFaceShapeLevel[8] = 0;
}

void Nama::loadAllBundles()
{
    for (int i = 0; i < BUNDLE_COUNT_IN_USE; i++) {
        Nama::FindAllBundle(g_fuDataDir + gBundlePath[i], categoryBundles[i]);
    }
}

Nama::UniquePtr Nama::create(uint32_t width, uint32_t height, bool enable)
{
    UniquePtr pNama = UniquePtr(new Nama);
    mEnableNama = enable;
    pNama->Init(width, height);
    return pNama;
}

Nama::Nama()
{
    mFrameID = 0;
    mMaxFace = 1;
    mIsBeautyOn = true;
    mBeautyHandles = 0;
    mMakeUpHandle = 0;
    //mCapture = std::tr1::shared_ptr<CCameraDS>(new CCameraDS);
    mNewFaceTracker = -1;
}

Nama::~Nama()
{
    if (true == mHasSetup)
    {
        fuDestroyAllItems();//Note: Do not use an items are destroyed 
        fuOnDeviceLost();//Note:  destroy OpenGL resources nama created
        fuDestroyLibData();//Note: destory thread resources nama created
    }
    //call fuSetup once 整个程序只需要运行一次，销毁某个子窗口时只需要调用上述两个函数。 
    //Tips:If other windows will use these resources, the resources should be created on parent window. 
    //Then the resources will be valid when program is running.

    std::map<int, Mp3*>::iterator it;
    for (it = mp3Map.begin(); it != mp3Map.end(); it++)
    {
        it->second->Cleanup();
        delete it->second;
 //       Nama::mNeedPlayMP3 = false;
    }
}

/*std::vector<std::string> Nama::CameraList()
{
	return mCapture->getDeviceNameList();
}

cv::Mat Nama::GetFrame()
{
	return mCapture->getFrame();
}

bool Nama::ReOpenCamera(int camID)
{
	if (mCapture->isInit())
	{
		mCapture->closeCamera();
		mCapture->initCamera(mCapture->rs_width, mCapture->rs_height,camID);
		fuOnCameraChange();//Note: 重置人脸检测的信息
	}
	return true;
}*/

PIXELFORMATDESCRIPTOR pfd = {
 sizeof(PIXELFORMATDESCRIPTOR),
 1u,
 PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER | PFD_DRAW_TO_WINDOW,
 PFD_TYPE_RGBA,
 32u,
 0u, 0u, 0u, 0u, 0u, 0u,
 8u,
 0u,
 0u,
 0u, 0u, 0u, 0u,
 24u,
 8u,
 0u,
 PFD_MAIN_PLANE,
 0u,
 0u, 0u };

void InitOpenGL()
{
    HWND hw = CreateWindowExA(
        0, "EDIT", "", ES_READONLY,
        0, 0, 1, 1,
        NULL, NULL,
        GetModuleHandleA(NULL), NULL);
    HDC hgldc = GetDC(hw);
    int spf = ChoosePixelFormat(hgldc, &pfd);
    int ret = SetPixelFormat(hgldc, spf, &pfd);
    HGLRC hglrc = wglCreateContext(hgldc);
    wglMakeCurrent(hgldc, hglrc);

    //hglrc is OpenGL context
    printf("hw=%08x hgldc=%08x spf=%d ret=%d hglrc=%08x\n",
        hw, hgldc, spf, ret, hglrc);
}

bool Nama::CheckGLContext()
{
    int add0, add1, add2, add3;
    add0 = (int)wglGetProcAddress("glGenFramebuffersARB");
    add1 = (int)wglGetProcAddress("glGenFramebuffersOES");
    add2 = (int)wglGetProcAddress("glGenFramebuffersEXT");
    add3 = (int)wglGetProcAddress("glGenFramebuffers");
    printf("gl ver test (%s:%d): %08x %08x %08x %08x\n", __FILE__, __LINE__, add0, add1, add2, add3);
    return add0 | add1 | add2 | add3;
}

bool Nama::Init(uint32_t& width, uint32_t& height)
{
    InitOpenGL();
    mFrameWidth = width;
    mFrameHeight = height;
    if (false == mHasSetup && true == mEnableNama)
    {
        //read nama data and initialize
        std::vector<char> v3data;
        
        if (false == LoadBundle(g_fuDataDir + g_v3Data, v3data))
        {  
            initFuError = initFuError +  g_fuDataDir + g_v3Data;
            return false;
        }
        //CheckGLContext();
        fuSetup(reinterpret_cast<float*>(&v3data[0]), v3data.size(), NULL, g_auth_package, sizeof(g_auth_package));

        printf("Nama version:%s \n", fuGetVersion());
        std::vector<char> tongue_model_data;
        if (false == LoadBundle(g_fuDataDir + g_tongue, tongue_model_data))
        {
            initFuError +=  g_fuDataDir + g_tongue;
            return false;
        }
        // tongue
        fuLoadTongueModel(reinterpret_cast<float*>(&tongue_model_data[0]), tongue_model_data.size());

        std::vector<char> ai_model_data;
        if (false == LoadBundle(g_fuDataDir + g_ai_faceprocessor, ai_model_data))
        {
            initFuError +=  g_fuDataDir + g_ai_faceprocessor;
            return false;
        }
        fuLoadAIModelFromPackage(reinterpret_cast<float*>(&ai_model_data[0]), ai_model_data.size(), FUAITYPE::FUAITYPE_FACEPROCESSOR);

        std::vector<char> ai239_model_data;
        if (false == LoadBundle(g_fuDataDir + g_ai_landmark239, ai239_model_data))
        {
            initFuError +=  g_fuDataDir + g_ai_landmark239;
            return false;
        }
        fuLoadAIModelFromPackage(reinterpret_cast<float*>(&ai239_model_data[0]), ai239_model_data.size(), FUAITYPE::FUAITYPE_FACELANDMARKS239);

        std::vector<char> fxaa_data;
        if (false == LoadBundle(g_fuDataDir + g_fxaa, fxaa_data))
        {
            initFuError +=  g_fuDataDir + g_fxaa;
            return false;
        }
        mFxaaHandles = fuCreateItemFromPackage(fxaa_data.data(), fxaa_data.size());
        fuSetExpressionCalibration(1);

        mModuleCode = fuGetModuleCode(0);
        mModuleCode1 = fuGetModuleCode(1);
        //read beauty data and set beauty parameters
        if (CheckModuleCode(Facebeauty))
        {
            std::vector<char> propData;
            if (false == LoadBundle(g_fuDataDir + g_faceBeautification, propData))
            {
                initFuError = "load face beautification data failed.";
                return false;
            }
            std::cout << "load face beautification data." << std::endl;

            mBeautyHandles = fuCreateItemFromPackage(&propData[0], propData.size());
        }

        //read makeup data
        if (CheckModuleCode(Makeup))
        {
            std::vector<char> propData;
            if (false == LoadBundle(g_fuDataDir + g_Makeup, propData))
            {
                initFuError = "load face makeup data failed.";
                return false;
            }
            std::cout << "load face makeup data." << std::endl;

            mMakeUpHandle = fuCreateItemFromPackage(&propData[0], propData.size());

        }
        initFuError = "";
        fuSetDefaultOrientation(0);
        float fValue = 0.5f;
        fuSetFaceTrackParam("mouth_expression_more_flexible", &fValue);

        mHasSetup = true;
    }
    else
    {
        if (mEnableNama) fuOnDeviceLost();
        mHasSetup = false;
    }
    return true;
}

int Nama::IsTracking()
{
    if (false == mEnableNama)
    {
        return 0;
    }
    return fuIsTracking();
}

void Nama::SwitchBeauty(bool bValue)
{
    mIsBeautyOn = bValue;
}

void Nama::SetCurrentShape(int index)
{
    if (false == mEnableNama || false == mIsBeautyOn || mBeautyHandles == 0)return;
    if (0 <= index <= 4)
    {
        int res = fuItemSetParamd(mBeautyHandles, "face_shape", index);
    }
}

void Nama::UpdateFilter(int index)
{
    if (false == mEnableNama || false == mIsBeautyOn || mBeautyHandles == 0)return;

    fuItemSetParams(mBeautyHandles, "filter_name", &mFilters[index][0]);
}

void Nama::UpdateBeauty()
{
    if (false == mEnableNama || mBeautyHandles == 0)
    {
        return;
    }
    if (false == mIsBeautyOn)return;

    for (int i = 0; i < MAX_BEAUTYFACEPARAMTER; i++)
    {
        if (i == 0)//Blur
        {
            fuItemSetParamd(mBeautyHandles, const_cast<char*>(faceBeautyParamName[i].c_str()), Nama::mFaceBeautyLevel[i] * 6.0 / 100.f);
        }
        else
        {
            fuItemSetParamd(mBeautyHandles, const_cast<char*>(faceBeautyParamName[i].c_str()), Nama::mFaceBeautyLevel[i] / 100.f);
        }
    }
    std::string faceShapeParamName[] = { "cheek_thinning","eye_enlarging", "intensity_chin", "intensity_forehead", "intensity_nose","intensity_mouth",
     "cheek_v","cheek_narrow","cheek_small" };
    for (int i = 0; i < MAX_FACESHAPEPARAMTER; i++)
    {
        if (i == 2 || i == 3 || i == 5)
        {
            Nama::mFaceShapeLevel[i] += 50;
        }
        fuItemSetParamd(mBeautyHandles, const_cast<char*>(faceShapeParamName[i].c_str()), Nama::mFaceShapeLevel[i] / 100.0f);
        if (i == 2 || i == 3 || i == 5)
        {
            Nama::mFaceShapeLevel[i] -= 50;
        }
    }
    fuItemSetParamd(mBeautyHandles, "skin_detect", Nama::mEnableSkinDect);
    std::map<int, int> blurType = { {0,2},{1,0},{2,1} };
    fuItemSetParamd(mBeautyHandles, "blur_type", blurType[Nama::mEnableHeayBlur]);
    fuItemSetParamd(mBeautyHandles, "face_shape_level", 1);
    fuItemSetParamd(mBeautyHandles, "filter_level", Nama::mFilterLevel[Nama::m_curFilterIdx] / 100.0f);
}

bool Nama::SelectBundle(std::string bundleName)
{
    //if (false == mEnableNama)
    {
    //    return false;
    }

    int bundleID = -1;
    //stop playback music
    std::map<int, Mp3*>::iterator it;
    for (it = mp3Map.begin(); it != mp3Map.end(); it++)
    {
        long long current = 0;
        it->second->SetPositions(&current, NULL, true);
        it->second->Stop();
        Nama::mNeedPlayMP3 = false;
    }
    //if not load bundle, then load
    if (0 == mBundlesMap[bundleName])
    {
        //certificate doesn't have authority to read this bundle
        if (!CheckModuleCode(Nama::bundleCategory))
        {
            return false;
        }
        std::vector<char> propData;
        if (false == LoadBundle(bundleName, propData))
        {
            bundleInfo = "load prop data failed.";
            //std::cout << "load prop data failed." << std::endl;
            Nama::m_curRenderItem = -1;
            return false;
        }
        std::cout << "load prop data." << std::endl;
        //max Map size
        if (mBundlesMap.size() > MAX_NAMA_BUNDLE_NUM)
        {
            fuDestroyItem(mBundlesMap.begin()->second);
            mBundlesMap.erase(mBundlesMap.begin());
            printf("cur map size : %d \n", mBundlesMap.size());
        }

        bundleID = fuCreateItemFromPackage(&propData[0], propData.size());
        mBundlesMap[bundleName] = bundleID;
        //Bind makeup
        if (Nama::bundleCategory == BundleCategory::Makeup)
        {
            if (Nama::m_curBindedItem != -1)
            {
                fuUnbindItems(mMakeUpHandle, &Nama::m_curBindedItem, 1);
            }
            fuBindItems(mMakeUpHandle, &bundleID, 1);
            Nama::m_curBindedItem = bundleID;
        }
        else
        {
            //fuItemSetParamd(mMakeUpHandle, "is_makeup_on", 0);
        }
        //Load and playback music
        if (Nama::bundleCategory == BundleCategory::MusicFilter)
        {
            std::string itemName = Nama::mCurRenderItemName.substr(0, Nama::mCurRenderItemName.find_last_of("."));
            if (mp3Map.find(bundleID) == mp3Map.end())
            {
                Mp3 *mp3 = new Mp3;
                std::string bundlePath = g_fuDataDir + "items\\MusicFilter\\" + itemName + ".mp3";
                mp3->Load(bundlePath);
                mp3Map[bundleID] = mp3;
            }
            mp3Map[bundleID]->Play();
            Nama::mNeedPlayMP3 = true;
        }
    }
    else
    {
        bundleID = mBundlesMap[bundleName];
        //bind makeup
        if (Nama::bundleCategory == BundleCategory::Makeup)
        {
            if (Nama::m_curBindedItem != -1)
            {
                fuUnbindItems(mMakeUpHandle, &Nama::m_curBindedItem, 1);
            }
            fuBindItems(mMakeUpHandle, &bundleID, 1);
            Nama::m_curBindedItem = bundleID;
        }
        else
        {
            //fuItemSetParamd(mMakeUpHandle, "is_makeup_on", 0);
        }
        if (Nama::bundleCategory == BundleCategory::MusicFilter)
        {
            mp3Map[bundleID]->Play();
            Nama::mNeedPlayMP3 = true;
        }
    }

    if (Nama::m_curRenderItem == bundleID)
    {
        //ubbind makeup data
        if (Nama::bundleCategory == BundleCategory::Makeup)
        {
            fuUnbindItems(mMakeUpHandle, &Nama::m_curRenderItem, 1);
            Nama::m_curBindedItem = -1;
        }
        Nama::m_curRenderItem = -1;
    }
    else
    {
        Nama::m_curRenderItem = bundleID;
        Nama::renderBundleCategory = Nama::bundleCategory;
    }
    if (Nama::bundleCategory == PortraitDrive || Nama::bundleCategory == Animoji)
    {
        mMaxFace = 1;
    }
    else
    {
        mMaxFace = 4;
    }

    if (Nama::renderBundleCategory == Animoji)
    {
        fuItemSetParamd(Nama::m_curRenderItem, "{\"thing\":\"<global>\",\"param\":\"follow\"} ", 1);
    }
    else
    {
        fuItemSetParamd(Nama::m_curRenderItem, "{\"thing\":\"<global>\",\"param\":\"follow\"} ", 0);
    }
    bundleInfo = "";
    return true;
}

bool Nama::CheckModuleCode(int category)
{	
	return (mModuleCode & modules[category])  || (mModuleCode1 & modules1[category]);
}
//render function
void Nama::RenderItems(uchar* frame, int inframeType)
{
    if (true == mEnableNama)
    {
        fuSetMaxFaces(mMaxFace);

        if (Nama::mNeedPlayMP3)
        {
            if (mp3Map.find(Nama::m_curRenderItem) != mp3Map.end())
            {
                fuItemSetParamd(Nama::m_curRenderItem, "music_time", mp3Map[Nama::m_curRenderItem]->GetCurrentPosition() / 1e4);
                mp3Map[Nama::m_curRenderItem]->CirculationPlayCheck();
            }
        }
        if (Nama::mNeedStopMP3)
        {
            std::map<int, Mp3*>::iterator it = mp3Map.begin();
            for (; it != mp3Map.end(); it++)
            {
                it->second->Stop();
            }
            Nama::m_curRenderItem = -1;
            Nama::mNeedStopMP3 = false;
        }

        if (Nama::showMakeUpWindow)
        {
            if (mNewFaceTracker == -1)
            {
                std::vector<char> propData;
                if (false == LoadBundle(g_fuDataDir + g_NewFaceTracker, propData))
                {
                    std::cout << "load face newfacetracker data failed." << std::endl;
                    return;
                }
                std::cout << "load face newfacetracker data." << std::endl;

                mNewFaceTracker = fuCreateItemFromPackage(&propData[0], propData.size());
            }
            int handle[] = { mBeautyHandles,mMakeUpHandle, mNewFaceTracker };
            int handleSize = sizeof(handle) / sizeof(handle[0]);
            fuRenderItemsEx2(/*FU_FORMAT_RGBA_BUFFER*/inframeType, reinterpret_cast<int*>(frame), /*FU_FORMAT_RGBA_BUFFER*/inframeType, reinterpret_cast<int*>(frame),
                mFrameWidth, mFrameHeight, mFrameID, handle, handleSize, NAMA_RENDER_FEATURE_FULL, NULL);//FU_FORMAT_RGBA_BUFFER
        }
        else
        {
            if (mNewFaceTracker != -1)
            {
                fuDestroyItem(mNewFaceTracker);
                mNewFaceTracker = -1;
            }
            int handle[] = { mBeautyHandles, Nama::m_curRenderItem };
            int handleSize = sizeof(handle) / sizeof(handle[0]);
            //support format: FU_FORMAT_BGRA_BUFFER 、 FU_FORMAT_NV21_BUFFER 、FU_FORMAT_I420_BUFFER 、FU_FORMAT_RGBA_BUFFER		
            fuRenderItemsEx2(/*FU_FORMAT_RGBA_BUFFER*/FU_FORMAT_I420_BUFFER, reinterpret_cast<int*>(frame), /*FU_FORMAT_RGBA_BUFFER*/FU_FORMAT_I420_BUFFER, reinterpret_cast<int*>(frame),
                mFrameWidth, mFrameHeight, mFrameID, handle, handleSize, NAMA_RENDER_FEATURE_FULL, NULL); 
        }

        if (fuGetSystemError())
        {
            printf("%s \n", fuGetSystemErrorString(fuGetSystemError()));
        }
        ++mFrameID;
    }

 
    return;
}
//only call beauty module in nama
uchar* Nama::RenderEx(uchar* frame)
{
    HGLRC context = wglGetCurrentContext();
    HWND wnd = NULL;//(HWND)Gui::hWindow;
    wglMakeCurrent(GetDC(wnd), new_context);
    if (true == mEnableNama)
    {
        fuBeautifyImage(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(frame),
            FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(frame),
            mFrameWidth, mFrameHeight, mFrameID, &mBeautyHandles, 1);

        ++mFrameID;
    }
    wglMakeCurrent(GetDC(wnd), context);
    return frame;
}

//
void Nama::DrawLandmarks(uchar* frame)
{
    if (false == mEnableNama) return;
    float landmarks[150];
    float trans[3];
    float rotat[4];
    int ret = 0;

    ret = fuGetFaceInfo(0, "landmarks", landmarks, sizeof(landmarks) / sizeof(landmarks[0]));
    for (int i(0); i != 75; ++i)
    {
        DrawPoint(frame, static_cast<int>(landmarks[2 * i]), static_cast<int>(landmarks[2 * i + 1]));
    }

}

void Nama::DrawPoint(uchar* frame, int x, int y, unsigned char r, unsigned char g, unsigned char b)
{
    const int offsetX[] = { -1, 0, 1 , -1, 0, 1 , -1, 0, 1 };
    const int offsetY[] = { -1, -1, -1, 0, 0, 0, 1, 1, 1 };
    const int count = sizeof(offsetX) / sizeof(offsetX[0]);

    unsigned char* data = frame;
    for (int i(0); i != count; ++i)
    {
        int xx = x + offsetX[i];
        int yy = y + offsetY[i];
        if (0 > xx || xx >= mFrameWidth || 0 > yy || yy >= mFrameHeight)
        {
            continue;
        }

        data[yy * 4 * mFrameWidth + xx * 4 + 0] = b;
        data[yy * 4 * mFrameWidth + xx * 4 + 1] = g;
        data[yy * 4 * mFrameWidth + xx * 4 + 2] = r;
    }

}

int Nama::CreateMakeupBundle(std::string bundleName)
{
    if (false == mEnableNama || false == mIsBeautyOn || mMakeUpHandle == 0)return 0;
    using namespace std;
    using namespace rapidjson;
    int fakeBundleID = 0;
    auto pos = mMakeupsMap.find(bundleName);
    if (pos == mMakeupsMap.end())
    {
        Document doc;

        Document& dd = doc;
        std::string jsonpath = bundleName + "/makeup.json";

        FILE* fp = fopen(jsonpath.c_str(), "rb");
        if (!fp)
        {
            return 0;
        }
        char readBuffer[65536];
        FileReadStream is(fp, readBuffer, sizeof(readBuffer));
        doc.ParseStream(is);
        fclose(fp);

        vector<MakeupParam> paramArr;
        for (rapidjson::Value::ConstMemberIterator itr = doc.MemberBegin(); itr != doc.MemberEnd(); itr++)
        {
            MakeupParam param;
            Value jKey;
            Value jValue;
            Document::AllocatorType allocator;
            jKey.CopyFrom(itr->name, allocator);
            jValue.CopyFrom(itr->value, allocator);
            if (jKey.IsString())
            {
                param.name = jKey.GetString();
            }
            if (jValue.IsArray())
            {
                param.type = MakeupParamType::MArray;
                for (auto& v : jValue.GetArray())
                {
                    param.colorArr.push_back(v.GetFloat());
                }
            }
            if (jValue.IsString())
            {
                param.type = MakeupParamType::MString;
                param.tex = jValue.GetString();
            }
            if (jValue.IsInt())
            {
                param.type = MakeupParamType::MInt;
                param.lip_type = jValue.GetInt();

            }
            if (jValue.IsFloat())
            {
                param.type = MakeupParamType::MFloat;
                param.brow_warp = jValue.GetFloat();
            }
            if (jValue.IsDouble())
            {
                param.type = MakeupParamType::MFloat;
                param.brow_warp = jValue.GetDouble();
            }
            paramArr.push_back(param);
        }
        mMakeupsMap[bundleName] = paramArr;
        fakeBundleID = mMakeupsMap.size() + 666;
    }
    return fakeBundleID;
}

void Nama::SelectMakeupBundle(std::string bundleName)
{
    if (bundleName.size() == 0)
    {
        //reset params
        fuItemSetParamd(mMakeUpHandle, "is_makeup_on", 0);
    }
    fuItemSetParamd(mMakeUpHandle, "is_makeup_on", 1);
    auto paramArr = mMakeupsMap[bundleName];

    for (auto param : paramArr)
    {
        switch (param.type)
        {
        case MakeupParamType::MArray:
            fuItemSetParamdv(mMakeUpHandle, const_cast<char*>(param.name.c_str()), (double*)param.colorArr.data(), param.colorArr.size());
            break;
        case MakeupParamType::MFloat:
            fuItemSetParamd(mMakeUpHandle, const_cast<char*>(param.name.c_str()), param.brow_warp);
            break;
        case MakeupParamType::MInt:
            fuItemSetParamd(mMakeUpHandle, const_cast<char*>(param.name.c_str()), param.lip_type);
            break;
        case MakeupParamType::MString:
        {
            /*  Texture::SharedPtr pTexture = Texture::createTextureFromFile(bundleName + "/" + param.tex, false);
              if (pTexture)
              {
                  fuCreateTexForItem(mMakeUpHandle, const_cast<char*>(param.name.c_str()), pTexture->getData(), pTexture->m_width, pTexture->m_height);
              }
              else
              {
                  printf("Load makeup texture %s failed!\n", (bundleName + "/" + param.tex).c_str());
              }*/
            break;
        }
        default:
            break;
        }
    }
}

//LoadBundle
void Nama::FindAllBundle(std::string folder, std::vector<std::string> &files)
{
        IteratorFolder(folder.c_str(), files);
        //for each (auto file in files)
        //{
        //	std::cout << file << std::endl;
        //}
}
void Nama::Wchar_tToString(std::string& szDst, wchar_t *wchar)
{
    wchar_t * wText = wchar;
    DWORD dwNum = WideCharToMultiByte(CP_OEMCP, NULL, wText, -1, NULL, 0, NULL, FALSE);// WideCharToMultiByte
    char *psText; //
    psText = new char[dwNum];
    WideCharToMultiByte(CP_OEMCP, NULL, wText, -1, psText, dwNum, NULL, FALSE);// WideCharToMultiByte
    szDst = psText;
    delete[]psText;
}

void Nama::IteratorFolder(const char* lpPath, std::vector<std::string> &fileList)
{
    char szFind[MAX_PATH];
    WIN32_FIND_DATA FindFileData;
    strcpy(szFind, lpPath);
    strcat(szFind, "\\*.*");
    int len = MultiByteToWideChar(CP_ACP, 0, (LPCSTR)szFind, -1, NULL, 0);
    wchar_t * wszUtf8 = new wchar_t[len + 1];
    memset(wszUtf8, 0, len * 2 + 2);
    MultiByteToWideChar(CP_ACP, 0, (LPCSTR)szFind, -1, (LPWSTR)wszUtf8, len);
    HANDLE hFind = ::FindFirstFileW(wszUtf8, &FindFileData);
    if (INVALID_HANDLE_VALUE == hFind)    return;
    while (true) {
        if (FindFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
        {
            if (FindFileData.cFileName[0] != '.')
            {
                std::string folderName;
                Wchar_tToString(folderName, FindFileData.cFileName);
                fileList.push_back(folderName);
            }
        }
        else
        {
            //std::cout << FindFileData.cFileName << std::endl;			
            std::string str;
            Wchar_tToString(str, FindFileData.cFileName);
            if (str.find(".bundle") != std::string::npos)
            {
                fileList.push_back(str);
            }
        }
        if (!FindNextFile(hFind, &FindFileData))    break;
    }
    FindClose(hFind);
}

namespace NamaNameSpace {

    size_t FileSize(std::ifstream& file)
    {
        std::streampos oldPos = file.tellg();
        file.seekg(0, std::ios::beg);
        std::streampos beg = file.tellg();
        file.seekg(0, std::ios::end);
        std::streampos end = file.tellg();
        file.seekg(oldPos, std::ios::beg);
        return static_cast<size_t>(end - beg);
    }

    bool LoadBundle(const std::string& filepath, std::vector<char>& data)
    {
        std::ifstream fin(filepath, std::ios::binary);
        if (false == fin.good())
        {
            fin.close();
            return false;
        }
        size_t size = FileSize(fin);
        if (0 == size)
        {
            fin.close();
            return false;
        }
        data.resize(size);
        fin.read(reinterpret_cast<char*>(&data[0]), size);

        fin.close();
        return true;
    }
}