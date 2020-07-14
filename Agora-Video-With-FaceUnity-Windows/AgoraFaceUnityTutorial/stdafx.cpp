
// stdafx.cpp : source file that includes just the standard includes
// AgoraFaceUnityTutorial.pch will be the pre-compiled header
// stdafx.obj will contain the pre-compiled type information

#include "stdafx.h"
#include "CConfig.h"
std::string exePath = "";
std::string g_fuDataDir;
wchar_t faceuSkinBeauty[INFO_LEN] = {0};
wchar_t faceuTypeBeauty[INFO_LEN] = { 0 };
wchar_t faceuFilterBeauty[INFO_LEN] = { 0 };
wchar_t faceuMakeupBeauty[INFO_LEN] = { 0 };
wchar_t faceuARFuction[INFO_LEN] = { 0 };

wchar_t fuUIEnableBeauty[INFO_LEN] = { 0 };
wchar_t fuBeautyStyle[INFO_LEN] = { 0 };
wchar_t fuUIBlur1[INFO_LEN] = { 0 };
wchar_t fuUIBlur2[INFO_LEN] = { 0 };
wchar_t fuUIBlur3[INFO_LEN] = { 0 };

wchar_t fuUIBlur[INFO_LEN] = { 0 };
wchar_t fuUIWhite[INFO_LEN] = { 0 };
wchar_t fuUIRed[INFO_LEN] = { 0 };
wchar_t fuUILight[INFO_LEN] = { 0 };
wchar_t fuUITooth[INFO_LEN] = { 0 };
wchar_t fuUIReset[INFO_LEN] = { 0 };


wchar_t fuUIMuCh[INFO_LEN] = { 0 };
wchar_t fuUIMuFl[INFO_LEN] = { 0 };
wchar_t fuUIMuLa[INFO_LEN] = { 0 };
wchar_t fuUIMuMo[INFO_LEN] = { 0 };
wchar_t fuUIMuNe[INFO_LEN] = { 0 };
wchar_t fuUIMuOc[INFO_LEN] = { 0 };
wchar_t fuUIMuSe[INFO_LEN] = { 0 };
wchar_t fuUIMuSw[INFO_LEN] = { 0 };
wchar_t fuUIMuTo[INFO_LEN] = { 0 };

wchar_t fuExpMouseOpen[INFO_LEN] = { 0 };
wchar_t fuExpCheek[INFO_LEN] = { 0 };
wchar_t fuExpMouse[INFO_LEN] = { 0 };
wchar_t fuExpFrown[INFO_LEN] = { 0 };
wchar_t fuExpSmile[INFO_LEN] = { 0 };
wchar_t fuExpBlow[INFO_LEN] = { 0 };
wchar_t fuExpGetstureTowHands[INFO_LEN] = { 0 };
wchar_t fuExpGetstureHeart[INFO_LEN] = { 0 };
wchar_t fuExpGetstureSix[INFO_LEN] = { 0 };
wchar_t fuExpGetstureThumb[INFO_LEN] = { 0 };

wchar_t fuUIAR0[INFO_LEN] = { 0 };
wchar_t fuUIAR1[INFO_LEN] = { 0 };
wchar_t fuUIAR2[INFO_LEN] = { 0 };
wchar_t fuUIAR3[INFO_LEN] = { 0 };
wchar_t fuUIAR4[INFO_LEN] = { 0 };
wchar_t fuUIAR5[INFO_LEN] = { 0 };
wchar_t fuUIAR6[INFO_LEN] = { 0 };
wchar_t fuUIAR7[INFO_LEN] = { 0 };
wchar_t fuUIAR8[INFO_LEN] = { 0 };

wchar_t fuSDKErrorNoFile[INFO_LEN] = { 0 };
wchar_t fuSDKErrorNoAuth[INFO_LEN] = { 0 };
std::string cs2utf8(CString str)
{
 char szBuf[2 * MAX_PATH] = { 0 };
 WideCharToMultiByte(CP_UTF8, 0, str.GetBuffer(0), str.GetLength(), szBuf, 2 * MAX_PATH, NULL, NULL);
 return szBuf;
}

CString utf82cs(std::string utf8)
{
 TCHAR szBuf[2 * MAX_PATH] = { 0 };
 MultiByteToWideChar(CP_UTF8, 0, utf8.c_str(), 2 * MAX_PATH, szBuf, 2 * MAX_PATH);
 return szBuf;
}

void InitKeyInfomation()
{
    _tcscpy_s(faceuTypeBeauty, INFO_LEN, Str(_T("fu.UI.Beauty.type")));
    _tcscpy_s(faceuSkinBeauty, INFO_LEN, Str(_T("fu.UI.Beauty.Skin")));
    _tcscpy_s(faceuFilterBeauty, INFO_LEN, Str(_T("fu.UI.Beauty.filter")));
    _tcscpy_s(faceuMakeupBeauty, INFO_LEN, Str(_T("fu.UI.Beauty.makeup")));
    _tcscpy_s(faceuARFuction, INFO_LEN, Str(_T("fu.UI.Beauty.arFunction")));

    _tcscpy_s(fuUIEnableBeauty, INFO_LEN, Str(_T("fu.UI.Enable.Beauty")));
    _tcscpy_s(fuBeautyStyle, INFO_LEN, Str(_T("fu.UI.Beauty.Style")));

    _tcscpy_s(fuUIBlur1, INFO_LEN, Str(_T("fu.UI.Blur1")));
    _tcscpy_s(fuUIBlur2, INFO_LEN, Str(_T("fu.UI.Blur2")));
    _tcscpy_s(fuUIBlur3, INFO_LEN, Str(_T("fu.UI.Blur3")));

    _tcscpy_s(fuUIBlur, INFO_LEN, Str(_T("fu.UI.Blur")));
    _tcscpy_s(fuUIWhite, INFO_LEN, Str(_T("fu.UI.White")));
    _tcscpy_s(fuUIRed, INFO_LEN, Str(_T("fu.UI.Red")));
    _tcscpy_s(fuUILight, INFO_LEN, Str(_T("fu.UI.Light")));
    _tcscpy_s(fuUITooth, INFO_LEN, Str(_T("fu.UI.Tooth")));
    _tcscpy_s(fuUIReset, INFO_LEN, Str(_T("fu.UI.Reset")));


    _tcscpy_s(fuUIMuCh, INFO_LEN, Str(_T("fu.Makeup.Charm")));
    _tcscpy_s(fuUIMuFl, INFO_LEN, Str(_T("fu.Makeup.Flower")));
    _tcscpy_s(fuUIMuLa, INFO_LEN, Str(_T("fu.Makeup.Lady")));
    _tcscpy_s(fuUIMuMo, INFO_LEN, Str(_T("fu.Makeup.Moon")));
    _tcscpy_s(fuUIMuNe, INFO_LEN, Str(_T("fu.Makeup.Neighbor")));
    _tcscpy_s(fuUIMuOc, INFO_LEN, Str(_T("fu.Makeup.Occident")));

    _tcscpy_s(fuUIMuSe, INFO_LEN, Str(_T("fu.Makeup.Sexy")));
    _tcscpy_s(fuUIMuSw, INFO_LEN, Str(_T("fu.Makeup.Sweet")));
    _tcscpy_s(fuUIMuTo, INFO_LEN, Str(_T("fu.Makeup.Tough")));

    _tcscpy_s(fuExpMouseOpen, INFO_LEN, Str(_T("fu.Exp.MouseOpen")));
    _tcscpy_s(fuExpCheek, INFO_LEN, Str(_T("fu.Exp.Cheek")));
    _tcscpy_s(fuExpMouse, INFO_LEN, Str(_T("fu.Exp.Mouse")));
    _tcscpy_s(fuExpFrown, INFO_LEN, Str(_T("fu.Exp.Frown")));
    _tcscpy_s(fuExpSmile, INFO_LEN, Str(_T("fu.Exp.Smile")));
    _tcscpy_s(fuExpBlow, INFO_LEN, Str(_T("fu.Exp.Blow")));

    _tcscpy_s(fuExpGetstureTowHands, INFO_LEN, Str(_T("fu.Gesture.TowHands")));
    _tcscpy_s(fuExpGetstureHeart, INFO_LEN, Str(_T("fu.Getsture.Heart")));
    _tcscpy_s(fuExpGetstureSix, INFO_LEN, Str(_T("fu.Getsture.Six")));
    _tcscpy_s(fuExpGetstureThumb, INFO_LEN, Str(_T("fu.Getsture.Thumb")));


    _tcscpy_s(fuUIAR0, INFO_LEN, Str(_T("fu.UI.AR0")));
    _tcscpy_s(fuUIAR1, INFO_LEN, Str(_T("fu.UI.AR1")));
    _tcscpy_s(fuUIAR2, INFO_LEN, Str(_T("fu.UI.AR2")));
    _tcscpy_s(fuUIAR3, INFO_LEN, Str(_T("fu.UI.AR3")));
    _tcscpy_s(fuUIAR4, INFO_LEN, Str(_T("fu.UI.AR4")));
    _tcscpy_s(fuUIAR5, INFO_LEN, Str(_T("fu.UI.AR5")));
    _tcscpy_s(fuUIAR6, INFO_LEN, Str(_T("fu.UI.AR6")));
    _tcscpy_s(fuUIAR7, INFO_LEN, Str(_T("fu.UI.AR7")));
    _tcscpy_s(fuUIAR8, INFO_LEN, Str(_T("fu.UI.AR8")));
    
    _tcscpy_s(fuSDKErrorNoFile, INFO_LEN, Str(_T("fu.SDK.Error.NoFile")));
    _tcscpy_s(fuSDKErrorNoAuth, INFO_LEN, Str(_T("fu.SDK.Error.NoAuth")));
  /*  _tcscpy_s(, INFO_LEN, Str(_T("fu.UI.")));
    _tcscpy_s(, INFO_LEN, Str(_T("fu.UI.")));
    _tcscpy_s(, INFO_LEN, Str(_T("fu.UI.")));
    _tcscpy_s(, INFO_LEN, Str(_T("fu.UI.")));*/
}

std::string getExePath()
{
    if (!exePath.empty()) {
        return exePath;
    }
    TCHAR szFile[MAX_PATH] = { 0 };
    GetModuleFileName(NULL, szFile, MAX_PATH);
    //strchr(szFile)
    TCHAR* lastSlashPos =_tcsrchr(szFile, '\\');
    int executableDirLen = lastSlashPos - szFile + 1;
    szFile[executableDirLen] = 0;
    exePath = cs2utf8(szFile);
    
    szFile[executableDirLen - 1] = 0;
    lastSlashPos = _tcsrchr(szFile, '\\');
    executableDirLen = lastSlashPos - szFile + 1;
    szFile[executableDirLen] = 0;
    g_fuDataDir = cs2utf8(szFile);
    g_fuDataDir += "assets\\"; 

    return exePath;
}