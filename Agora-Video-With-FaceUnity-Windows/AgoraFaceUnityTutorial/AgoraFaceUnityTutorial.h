
// AgoraFaceUnityTutorial.h : PROJECT_NAME Ӧ�ó������ͷ�ļ�
//

#pragma once

#ifndef __AFXWIN_H__
	#error "�ڰ������ļ�֮ǰ������stdafx.h�������� PCH �ļ�"
#endif

#include "resource.h"		// ������


// CAgoraFaceUnityTutorialApp: 
// �йش����ʵ�֣������ AgoraFaceUnityTutorial.cpp
//

class CAgoraFaceUnityTutorialApp : public CWinApp
{
public:
	CAgoraFaceUnityTutorialApp();

// ��д
public:
	virtual BOOL InitInstance();

// ʵ��

	DECLARE_MESSAGE_MAP()
};

extern CAgoraFaceUnityTutorialApp theApp;