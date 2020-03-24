
// AgoraFaceUnityTutorialDlg.h : 头文件
//

#pragma once

#include "AGButton.h"
#include "AGComboBox.h"
#include "AGSliderCtrl.h"
#include "AGEdit.h"

#include "CameraDS.h"
#include "Nama.h"
#include "afxwin.h"
#include "AgoraOpenGL.h"
#include "YUVTrans.h"
#include "ExtendVideoFrameObserver.h"
#include <map>
#define VIDEO_PROFILE_COUNT 32
// CAgoraFaceUnityTutorialDlg 对话框
typedef struct tagVideoResolution{
	int width;
	int height;
}VideoResolution;
class CAgoraFaceUnityTutorialDlg : public CDialogEx
{
// 构造
public:
	CAgoraFaceUnityTutorialDlg(CWnd* pParent = NULL);	// 标准构造函数

// 对话框数据
	enum { IDD = IDD_AGORAFACEUNITYTUTORIAL_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持

// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg void OnClose();
	afx_msg void OnTimer(UINT_PTR nIDEvent);
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

	afx_msg void OnCbnSelchangeComboCamerads();
	afx_msg void OnBnClickedButtonJoinchannel();

	afx_msg void OnBnClickedButtonSticker0();
	afx_msg void OnBnClickedButtonSticker1();
	afx_msg void OnBnClickedButtonSticker2();
	afx_msg void OnBnClickedButtonSticker3();
	afx_msg void OnBnClickedButtonSticker4();
	afx_msg void OnBnClickedButtonSticker5();
	afx_msg void OnBnClickedButtonSticker6();
	afx_msg void OnBnClickedButtonSticker7();
	afx_msg void OnBnClickedButtonSticker8();
	afx_msg void OnBnClickedButtonFilter0();
	afx_msg void OnBnClickedButtonFilter1();
	afx_msg void OnBnClickedButtonFilter2();
	afx_msg void OnBnClickedButtonFilter3();
	afx_msg void OnBnClickedButtonFilter4();
	afx_msg void OnBnClickedButtonFilter5();

	afx_msg void OnBnClickedCheckSticker();
	afx_msg void OnBnClickedCheckBeauty();
	afx_msg void OnBnClickedCheckFilter();

	LRESULT onJoinChannelSuccess(WPARAM wParam, LPARAM lParam);
	LRESULT onWarning(WPARAM wParam, LPARAM lParam);
	LRESULT onError(WPARAM wParam, LPARAM lParam);
	LRESULT onLeaveChannel(WPARAM wParam, LPARAM lParam);
	LRESULT onRequestChannelKey(WPARAM wParam, LPARAM lParam);
	LRESULT onLastMileQuality(WPARAM wParam, LPARAM lParam);
	LRESULT onFirstLocalVideoFrame(WPARAM wParam, LPARAM lParam);
	LRESULT onFirstRemoteVideoDecoded(WPARAM wParam, LPARAM lParam);
	LRESULT onFirstRmoteVideoFrame(WPARAM wParam, LPARAM lParam);
	LRESULT onUserJoined(WPARAM wParam, LPARAM lParam);
	LRESULT onUserOff(WPARAM wParam, LPARAM lParam);
	LRESULT onUserMuteVideo(WPARAM wParam, LPARAM lParam);
	LRESULT onConnectionLost(WPARAM wParam, LPARAM lParam);

	LRESULT onEIDRemoteVideoStats(WPARAM wParam, LPARAM lParam);
	LRESULT onEIDAudioDeviceStateChanged(WPARAM wParam, LPARAM lParam);
	LRESULT onEIDonVideoDeviceStateChanged(WPARAM wParam, LPARAM lParam);

	
		
		
protected:

	inline void initCtrl();
	inline void uninitCtrl();

	inline void initAgoraMedia();
	inline void uninitAgoraMedia();

	inline void initFaceUnity();
	inline void uninitFaceUnity();

	//static DWORD ThreadFaceUntiyDataProc(LPVOID lpParameter);
	
private:
	bool m_bTerminated;

	CComboBox m_ComCamera;
	CAGEdit m_AgEditMediaUid;
	CAGEdit m_AgEditChannelName;
	CAGButton m_AgBtnJoinChannel;

	CStatic m_PicCtlLocal;
	CStatic m_PicCtlRemote;

	CCameraDS m_cameraDS;

	CString  m_strAppId;
	uid_t m_uMediaUid;
	CString m_ChannelName;

	CAgoraObject* m_lpAgoraObject;
	IRtcEngine* m_lpRtcEngine;

	NamaExampleNameSpace::Nama m_FaceNama;
	const int m_nWidth = 640;
	const int m_nHeight = 480;
	bool is_need_draw_landmarks;
	bool m_isJoinChannel;

	CAGButton m_AgBtnSticker_0;
	CAGButton m_AgBtnSticker_1;
	CAGButton m_AgBtnSticker_2;
	CAGButton m_AgBtnSticker_3;
	CAGButton m_AgBtnSticker_4;
	CAGButton m_AgBtnSticker_5;
	CAGButton m_AgBtnSticker_6;
	CAGButton m_AgBtnSticker_7;
	CAGButton m_AgBtnSticker_8; 
	
	CAGButton m_AgBtnFilter_0;
	CAGButton m_AgBtnFilter_1;
	CAGButton m_AgBtnFilter_2;
	CAGButton m_AgBtnFilter_3;
	CAGButton m_AgBtnFilter_4;
	CAGButton m_AgBtnFilter_5;
	
	CButton m_BtnCheckSticker;
	CButton m_BtnCheckBeauty;
	CButton m_BtnCheckFilter;

	CSliderCtrl m_SliderBeautyBlur;
	CSliderCtrl m_SliderBeautyColor;
	CSliderCtrl m_SliderBeautyRed;


	CAgoraOpenGl m_openGl;
	CFileIO m_mediafile;

	LPBYTE m_lpBufferYUV;
	int m_nLenYUV;
	LPBYTE m_lpBufferYUVRotate;
	CYUVTrans m_yuvTrans;

	CExtendVideoFrameObserver m_ExtendVideoObserver;

public:
	afx_msg void OnNMCustomdrawBeauty0(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawBeauty1(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawBeauty2(NMHDR *pNMHDR, LRESULT *pResult);
};
