
// AgoraFaceUnityTutorialDlg.cpp : 实现文件
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "AgoraFaceUnityTutorialDlg.h"
#include "afxdialogex.h"
#include "commonfun.h"
#include "FrameFrequencyCtrl.h"
using namespace plusFCL_BTL;
#include "libyuv.h"
#include "VideoPackageQueue.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define HAVE_JPEG

// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// 对话框数据
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CAgoraFaceUnityTutorialDlg 对话框



CAgoraFaceUnityTutorialDlg::CAgoraFaceUnityTutorialDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(CAgoraFaceUnityTutorialDlg::IDD, pParent),
	is_need_draw_landmarks(FALSE)
	, m_bTerminated(TRUE)
	, m_isJoinChannel(FALSE)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CAgoraFaceUnityTutorialDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_CameraDS, m_ComCamera);
	DDX_Control(pDX, IDC_EDIT_MediaUID, m_AgEditMediaUid);
	DDX_Control(pDX, IDC_EDIT_ChannelID, m_AgEditChannelName);
	DDX_Control(pDX, IDC_BUTTON_JoinChannel, m_AgBtnJoinChannel);
	DDX_Control(pDX, IDC_STATIC_Local, m_PicCtlLocal);
	DDX_Control(pDX, IDC_STATIC_Remote, m_PicCtlRemote);
	DDX_Control(pDX, IDC_BUTTON_Sticker_0, m_AgBtnSticker_0);
	DDX_Control(pDX, IDC_BUTTON_Sticker_1, m_AgBtnSticker_1);
	DDX_Control(pDX, IDC_BUTTON_Sticker_2, m_AgBtnSticker_2);
	DDX_Control(pDX, IDC_BUTTON_Sticker_3, m_AgBtnSticker_3);
	DDX_Control(pDX, IDC_BUTTON_Sticker_4, m_AgBtnSticker_4);
	DDX_Control(pDX, IDC_BUTTON_Sticker_5, m_AgBtnSticker_5);
	DDX_Control(pDX, IDC_BUTTON_Sticker_6, m_AgBtnSticker_6);
	DDX_Control(pDX, IDC_BUTTON_Sticker_7, m_AgBtnSticker_7);
	DDX_Control(pDX, IDC_BUTTON_Sticker_8, m_AgBtnSticker_8);
	DDX_Control(pDX, IDC_BUTTON_Filter_0, m_AgBtnFilter_0);
	DDX_Control(pDX, IDC_BUTTON_Filter_1, m_AgBtnFilter_1);
	DDX_Control(pDX, IDC_BUTTON_Filter_2, m_AgBtnFilter_2);
	DDX_Control(pDX, IDC_BUTTON_Filter_3, m_AgBtnFilter_3);
	DDX_Control(pDX, IDC_BUTTON_Filter_4, m_AgBtnFilter_4);
	DDX_Control(pDX, IDC_BUTTON_Filter_5, m_AgBtnFilter_5);
	DDX_Control(pDX, IDC_Check_Sticker, m_BtnCheckSticker);
	DDX_Control(pDX, IDC_Check_Beauty, m_BtnCheckBeauty);
	DDX_Control(pDX, IDC_Check_Filter, m_BtnCheckFilter);
	DDX_Control(pDX, IDC_Beauty_0, m_SliderBeautyBlur);
	DDX_Control(pDX, IDC_Beauty_1, m_SliderBeautyColor);
	DDX_Control(pDX, IDC_Beauty_2, m_SliderBeautyRed);
}

BEGIN_MESSAGE_MAP(CAgoraFaceUnityTutorialDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_CLOSE()
	ON_WM_TIMER()
	ON_WM_QUERYDRAGICON()
	ON_CBN_SELCHANGE(IDC_COMBO_CameraDS, &CAgoraFaceUnityTutorialDlg::OnCbnSelchangeComboCamerads)
	ON_BN_CLICKED(IDC_BUTTON_JoinChannel, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonJoinchannel)
	ON_MESSAGE(WM_MSGID(EID_JOINCHANNEL_SUCCESS), onJoinChannelSuccess)
	ON_MESSAGE(WM_MSGID(EID_WARNING), onWarning)
	ON_MESSAGE(WM_MSGID(EID_ERROR), onError)
	ON_MESSAGE(WM_MSGID(EID_LEAVE_CHANNEL), onLeaveChannel)
	ON_MESSAGE(WM_MSGID(EID_REQUEST_CHANNELKEY), onRequestChannelKey)
	ON_MESSAGE(WM_MSGID(EID_LASTMILE_QUALITY), onLastMileQuality)
	ON_MESSAGE(WM_MSGID(EID_FIRST_LOCAL_VIDEO_FRAME), onFirstLocalVideoFrame)
	ON_MESSAGE(WM_MSGID(EID_FIRST_REMOTE_VIDEO_DECODED), onFirstRemoteVideoDecoded)
	ON_MESSAGE(WM_MSGID(EID_FIRST_REMOTE_VIDEO_FRAME), onFirstRmoteVideoFrame)
	ON_MESSAGE(WM_MSGID(EID_USER_JOINED), onUserJoined)
	ON_MESSAGE(WM_MSGID(EID_USER_OFFLINE), onUserOff)
	ON_MESSAGE(WM_MSGID(EID_USER_MUTE_VIDEO), onUserMuteVideo)
	ON_MESSAGE(WM_MSGID(EID_CONNECTION_LOST), onConnectionLost)

	ON_MESSAGE(WM_MSGID(EID_VIDEO_DEVICE_STATE_CHANGED), onEIDonVideoDeviceStateChanged)
	ON_MESSAGE(WM_MSGID(EID_AUDIO_DEVICE_STATE_CHANGED), onEIDAudioDeviceStateChanged)
	ON_MESSAGE(WM_MSGID(EID_REMOTE_VIDEO_STAT), onEIDRemoteVideoStats)


	
	ON_BN_CLICKED(IDC_BUTTON_Sticker_0, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker0)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_1, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker1)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_2, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker2)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_3, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker3)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_4, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker4)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_5, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker5)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_6, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker6)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_7, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker7)
	ON_BN_CLICKED(IDC_BUTTON_Sticker_8, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker8)
	ON_BN_CLICKED(IDC_BUTTON_Filter_0, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter0)
	ON_BN_CLICKED(IDC_BUTTON_Filter_1, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter1)
	ON_BN_CLICKED(IDC_BUTTON_Filter_2, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter2)
	ON_BN_CLICKED(IDC_BUTTON_Filter_3, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter3)
	ON_BN_CLICKED(IDC_BUTTON_Filter_4, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter4)
	ON_BN_CLICKED(IDC_BUTTON_Filter_5, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter5)
	ON_BN_CLICKED(IDC_Check_Sticker, &CAgoraFaceUnityTutorialDlg::OnBnClickedCheckSticker)
	ON_BN_CLICKED(IDC_Check_Beauty, &CAgoraFaceUnityTutorialDlg::OnBnClickedCheckBeauty)
	ON_BN_CLICKED(IDC_Check_Filter, &CAgoraFaceUnityTutorialDlg::OnBnClickedCheckFilter)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_Beauty_0, &CAgoraFaceUnityTutorialDlg::OnNMCustomdrawBeauty0)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_Beauty_1, &CAgoraFaceUnityTutorialDlg::OnNMCustomdrawBeauty1)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_Beauty_2, &CAgoraFaceUnityTutorialDlg::OnNMCustomdrawBeauty2)
END_MESSAGE_MAP()


// CAgoraFaceUnityTutorialDlg 消息处理程序

BOOL CAgoraFaceUnityTutorialDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO:  在此添加额外的初始化代码
	SetBackgroundColor(RGB(0xff, 0xff, 0xff), TRUE);

	m_strAppId = s2cs(gAgoraFaceUnityConfig.getAppId());
	if (_T("") == m_strAppId){
		AfxMessageBox(_T("APPID is empty .please input again."));
		std::string iniFilePath = gAgoraFaceUnityConfig.getFilePah();
		gAgoraFaceUnityConfig.setAppId("");
		ShellExecute(NULL, _T("open"), s2cs(iniFilePath), NULL, NULL, SW_SHOW);
		::PostQuitMessage(0);
		return FALSE;
	}

	initCtrl();
	initAgoraMedia();
	initFaceUnity();
	
	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CAgoraFaceUnityTutorialDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CAgoraFaceUnityTutorialDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CAgoraFaceUnityTutorialDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


void CAgoraFaceUnityTutorialDlg::OnCbnSelchangeComboCamerads()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curCameraIdx = m_ComCamera.GetCurSel();
	m_FaceNama.ReOpenCamera();
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonJoinchannel()
{
	// TODO:  在此添加控件通知处理程序代码
	CString strParam;
	m_AgBtnJoinChannel.GetWindowTextW(strParam);
	if (_T("JoinChannel") == strParam){

		m_AgEditChannelName.GetWindowTextW(m_ChannelName);
		gAgoraFaceUnityConfig.setChannelName(cs2s(m_ChannelName));
		m_AgEditMediaUid.GetWindowTextW(strParam);
		gAgoraFaceUnityConfig.setLoginUid(cs2s(strParam));
		m_uMediaUid = str2long(cs2s(strParam));

		m_lpAgoraObject->EnableLastmileTest(FALSE);
		m_lpAgoraObject->SetChannelProfile(TRUE);
		m_lpAgoraObject->SetClientRole(CLIENT_ROLE_TYPE::CLIENT_ROLE_BROADCASTER);
	
		int nVideoIndex = (int)VIDEO_PROFILE_LANDSCAPE_480P;// because m_FaceNama.Init use 640 and 480
		m_lpAgoraObject->SetVideoProfile(nVideoIndex, FALSE);//640*480 15 500
		m_lpAgoraObject->EnableExtendVideoCapture(TRUE, &m_ExtendVideoObserver);

		m_uMediaUid = str2int(gAgoraFaceUnityConfig.getLoginUid());
		std::string strAppcertificatId = gAgoraFaceUnityConfig.getAppCertificateId();
		m_lpAgoraObject->SetSelfUID(m_uMediaUid);
		m_lpAgoraObject->SetAppCert(s2cs(strAppcertificatId));

		VideoCanvas vc;
		vc.renderMode = RENDER_MODE_HIDDEN;
		vc.uid = m_uMediaUid;
		vc.view = m_PicCtlLocal;
		m_lpRtcEngine->setupLocalVideo(vc);

		m_lpRtcEngine->startPreview();

		bool bAppCertEnalbe = str2int(gAgoraFaceUnityConfig.getAppCertEnable());
		if (bAppCertEnalbe){

			CStringA strMediaChannelKey = m_lpAgoraObject->getDynamicMediaChannelKey(m_ChannelName);
			m_lpAgoraObject->JoinChannel(m_ChannelName, m_uMediaUid, strMediaChannelKey);
		}
		else{

			m_lpAgoraObject->JoinChannel(m_ChannelName, m_uMediaUid);
		}
	}
	else if (_T("LeaveChannel") == strParam){

		m_lpAgoraObject->EnableExtendVideoCapture(FALSE, NULL);
		m_lpAgoraObject->LeaveCahnnel();
		m_lpRtcEngine->stopPreview();
	}
}

inline void CAgoraFaceUnityTutorialDlg::initCtrl()
{
	int nCameraCount = m_FaceNama.CameraCount();
	for (int nCameraIndex = 0; nCameraCount > nCameraIndex; nCameraIndex++){

		char szbuf[PATH_LEN] = { '\0' };
		int nRes = m_FaceNama.CameraName(nCameraIndex, szbuf, PATH_LEN);
		if (nRes){

			m_ComCamera.AddString(s2cs(szbuf));
		}
	}

	m_ComCamera.SetCurSel(0);

	m_AgEditMediaUid.SetTip(_T("MediaUID"));
	m_AgEditChannelName.SetTip(_T("ChannelName"));

	CString csParam = s2cs(gAgoraFaceUnityConfig.getLoginUid());
	m_ChannelName = s2cs(gAgoraFaceUnityConfig.getChannelName());
	m_AgEditMediaUid.SetWindowTextW(csParam);
	m_AgEditChannelName.SetWindowTextW(m_ChannelName);
	m_AgBtnSticker_0.SetBackImage(IDB_BITMAP_Stride_0);
	m_AgBtnSticker_1.SetBackImage(IDB_BITMAP_Stride_1);
	m_AgBtnSticker_2.SetBackImage(IDB_BITMAP_Stride_2);
	m_AgBtnSticker_3.SetBackImage(IDB_BITMAP_Stride_3);
	m_AgBtnSticker_4.SetBackImage(IDB_BITMAP_Stride_4);
	m_AgBtnSticker_5.SetBackImage(IDB_BITMAP_Stride_5);
	m_AgBtnSticker_6.SetBackImage(IDB_BITMAP_Stride_6);
	m_AgBtnSticker_7.SetBackImage(IDB_BITMAP_Stride_7);
	m_AgBtnSticker_8.SetBackImage(IDB_BITMAP_Stride_8);

	m_AgBtnFilter_0.SetBackImage(IDB_BITMAP_Filter_0);
	m_AgBtnFilter_1.SetBackImage(IDB_BITMAP_Filter_1);
	m_AgBtnFilter_2.SetBackImage(IDB_BITMAP_Filter_2);
	m_AgBtnFilter_3.SetBackImage(IDB_BITMAP_Filter_3);
	m_AgBtnFilter_4.SetBackImage(IDB_BITMAP_Filter_4);
	m_AgBtnFilter_5.SetBackImage(IDB_BITMAP_Filter_5);

	m_AgBtnSticker_0.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_1.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_2.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_3.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_4.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_5.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_6.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_7.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnSticker_8.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));

	m_AgBtnFilter_0.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnFilter_1.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnFilter_2.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnFilter_3.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnFilter_4.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));
	m_AgBtnFilter_5.SetBackColor(RGB(0, 160, 239), RGB(255, 255, 0), RGB(255, 128, 128), RGB(0, 160, 239));

	m_AgBtnSticker_0.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_1.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_2.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_3.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_4.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_5.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_6.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_7.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnSticker_8.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);

	m_AgBtnFilter_0.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnFilter_1.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnFilter_2.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnFilter_3.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnFilter_4.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);
	m_AgBtnFilter_5.SwitchButtonStatus(CAGButton::AGBTN_DISABLE);

	m_SliderBeautyBlur.SetRange(0, 100, TRUE);
	m_SliderBeautyColor.SetRange(0, 100, TRUE);
	m_SliderBeautyRed.SetRange(0, 100, TRUE);
}

inline void CAgoraFaceUnityTutorialDlg::uninitCtrl()
{
	m_ComCamera.Clear();

	m_strAppId = _T("");
	m_uMediaUid = 0;
	m_ChannelName = _T("");
}

inline void CAgoraFaceUnityTutorialDlg::initAgoraMedia()
{
	if ("" == m_strAppId)
		return;

	m_lpAgoraObject = CAgoraObject::GetAgoraObject(m_strAppId);
	ASSERT(m_lpAgoraObject);
	m_lpAgoraObject->SetMsgHandlerWnd(m_hWnd);

	m_lpRtcEngine = CAgoraObject::GetEngine();
	ASSERT(m_lpRtcEngine);

	CString strSdkLogFilePath = s2cs(getMediaSdkLogPath());
	m_lpAgoraObject->SetLogFilePath(strSdkLogFilePath);
	m_lpAgoraObject->EnableLastmileTest(TRUE);
	m_lpAgoraObject->EnableLocalMirrorImage(FALSE);
	m_lpAgoraObject->EnableLoopBack(FALSE);

	m_lpAgoraObject->EnableVideo(TRUE);
}

inline void CAgoraFaceUnityTutorialDlg::uninitAgoraMedia()
{
	if (nullptr == m_lpAgoraObject)
		return;

	m_lpAgoraObject->EnableVideo(FALSE);
	m_lpAgoraObject->EnableLastmileTest(FALSE);
	if (m_lpAgoraObject){
		CAgoraObject::CloseAgoraObject();
		m_lpAgoraObject = nullptr;
		m_lpRtcEngine = nullptr;
	}
}

inline void CAgoraFaceUnityTutorialDlg::initFaceUnity()
{
	//m_FaceNama的 资源申请 和 数据的逻辑处理 都必须放在同一个线程里面.
	m_mediafile.openMedia(getAbsoluteDir() + "capture.yuv");

	std::string strVersiono = m_FaceNama.getVersion();
	m_openGl.SetupPixelFormat(::GetDC(m_PicCtlLocal));
	CRect rect;
	GetClientRect(&rect);
	m_openGl.Init(rect.right, rect.bottom);
	m_FaceNama.Init(m_nWidth,m_nHeight);

	m_nLenYUV = m_nWidth * m_nHeight * 3 / 2;
	m_lpBufferYUV = new unsigned char[m_nLenYUV];
	m_lpBufferYUVRotate = new unsigned char[m_nLenYUV];
	ZeroMemory(m_lpBufferYUVRotate, m_nLenYUV);
	ZeroMemory(m_lpBufferYUV,m_nLenYUV);
	CVideoPackageQueue::GetInstance()->SetVideoFrameLen(m_nLenYUV);

	SetTimer(1, 40, 0);
}

inline void CAgoraFaceUnityTutorialDlg::uninitFaceUnity()
{
	KillTimer(1);
	if (m_lpBufferYUV)
		delete[] m_lpBufferYUV;

	if (m_lpBufferYUVRotate)
		delete[] m_lpBufferYUVRotate;

	m_nLenYUV = 0;

	if (!m_bTerminated)
		m_bTerminated = true;
	m_mediafile.close();
}

LRESULT CAgoraFaceUnityTutorialDlg::onJoinChannelSuccess(WPARAM wParam, LPARAM lParam)
{
	m_isJoinChannel = true;
	OutputDebugStringA(__FUNCTION__);
	m_AgBtnJoinChannel.SetWindowText(_T("LeaveChannel"));
	LPAGE_JOINCHANNEL_SUCCESS lpData = (LPAGE_JOINCHANNEL_SUCCESS)wParam;
	delete[] lpData->channel;
	delete lpData;
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onWarning(WPARAM wParam, LPARAM lParam)
{
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onError(WPARAM wParam, LPARAM lParam)
{
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onLeaveChannel(WPARAM wParam, LPARAM lParam)
{
	m_isJoinChannel = false;
	OutputDebugStringA(__FUNCTION__);
	m_AgBtnJoinChannel.SetWindowText(_T("JoinChannel"));
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onRequestChannelKey(WPARAM wParam, LPARAM lParam)
{
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onLastMileQuality(WPARAM wParam, LPARAM lParam)
{
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onFirstLocalVideoFrame(WPARAM wParam, LPARAM lParam)
{
	LPAGE_FIRST_LOCAL_VIDEO_FRAME lpData = (LPAGE_FIRST_LOCAL_VIDEO_FRAME)wParam;
	delete lpData;
	lpData = NULL;
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onFirstRemoteVideoDecoded(WPARAM wParam, LPARAM lParam)
{
	LPAGE_FIRST_REMOTE_VIDEO_DECODED lpData = (LPAGE_FIRST_REMOTE_VIDEO_DECODED)wParam;
	OutputDebugStringA(__FUNCTION__);
	delete lpData;
	lpData = NULL;
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onFirstRmoteVideoFrame(WPARAM wParam, LPARAM lParam)
{
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onUserJoined(WPARAM wParam, LPARAM lParam)
{
	LPAGE_USER_JOINED lpData = (LPAGE_USER_JOINED)wParam;
	delete lpData;
	lpData = NULL;
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onUserOff(WPARAM wParam, LPARAM lParam)
{
	LPAGE_USER_OFFLINE lpData = (LPAGE_USER_OFFLINE)wParam;
	delete lpData;
	lpData = NULL;
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onUserMuteVideo(WPARAM wParam, LPARAM lParam)
{
	LPAGE_USER_MUTE_VIDEO lpData = (LPAGE_USER_MUTE_VIDEO)wParam;
	delete lpData;
	lpData = NULL;
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

LRESULT CAgoraFaceUnityTutorialDlg::onConnectionLost(WPARAM wParam, LPARAM lParam)
{
	OutputDebugStringA(__FUNCTION__);
	return TRUE;
}

void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker0()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(0);
	is_need_draw_landmarks = false;
}

void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker1()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(1);
	is_need_draw_landmarks = false;
}

void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker2()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(2);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker3()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(3);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker4()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(4);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker5()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(5);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker6()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(6);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker7()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(7);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonSticker8()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.SetCurrentBundle(8);
	is_need_draw_landmarks = false;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter0()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curFilterIdx = 0;
	m_FaceNama.UpdateFilter();
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter1()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curFilterIdx = 1;
	m_FaceNama.UpdateFilter();
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter2()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curFilterIdx = 2;
	m_FaceNama.UpdateFilter();
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter3()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curFilterIdx = 3;
	m_FaceNama.UpdateFilter();
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter4()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curFilterIdx = 4;
	m_FaceNama.UpdateFilter();
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonFilter5()
{
	// TODO:  在此添加控件通知处理程序代码
	m_FaceNama.m_curFilterIdx = 5;
	m_FaceNama.UpdateFilter();
}

void CAgoraFaceUnityTutorialDlg::OnClose()
{
	//TO DO
	if (m_lpAgoraObject)
		m_lpAgoraObject->LeaveCahnnel();

	if (m_lpRtcEngine)
		m_lpRtcEngine->stopPreview();

	uninitFaceUnity();
	uninitAgoraMedia();
	uninitCtrl();

	CDialogEx::OnClose();
}

void CAgoraFaceUnityTutorialDlg::OnTimer(UINT_PTR nIDEvent)
{
	if (nIDEvent == 1 && m_isJoinChannel){
		std::tr1::shared_ptr<unsigned char> m_sharedCaptureFrame = m_FaceNama.QueryFrame();

		if (m_sharedCaptureFrame){
			
			m_FaceNama.RenderItems(m_sharedCaptureFrame);
			unsigned char* src_frame = m_sharedCaptureFrame.get();
			unsigned char* pBuffer_dst_y = m_lpBufferYUV;
			int ndst_stride_y = m_nWidth;
			unsigned char* pBuffer_dst_u = m_lpBufferYUV + m_nWidth * m_nHeight;
			int ndst_stride_u = m_nWidth / 2;
			unsigned char* pBuffer_dst_v = m_lpBufferYUV + m_nWidth * m_nHeight  + m_nWidth * m_nHeight /4;
			int ndst_stride_v = m_nWidth / 2;

			unsigned char* pBuffer_dst_y_rotate180 = m_lpBufferYUVRotate;
			unsigned char* pBuffer_dst_u_rotate180 = m_lpBufferYUVRotate + m_nWidth * m_nHeight;
			unsigned char* pBuffer_dst_v_rotate180 = m_lpBufferYUVRotate + m_nWidth * m_nHeight + m_nWidth * m_nHeight / 4;
			
			libyuv::ARGBToI420((unsigned char*)src_frame, m_nWidth * 4, pBuffer_dst_y, ndst_stride_y, pBuffer_dst_u, ndst_stride_u, pBuffer_dst_v, ndst_stride_v, m_nWidth, m_nHeight);
			libyuv::I420Rotate(pBuffer_dst_y, ndst_stride_y, pBuffer_dst_u, ndst_stride_u, pBuffer_dst_v, ndst_stride_v,pBuffer_dst_y_rotate180,ndst_stride_y,pBuffer_dst_u_rotate180,ndst_stride_u,pBuffer_dst_v_rotate180,ndst_stride_v,
				m_nWidth, m_nHeight, libyuv::RotationMode::kRotate180);
			CVideoPackageQueue::GetInstance()->PushVideoPackage(m_lpBufferYUVRotate, m_nLenYUV);
		}
	}

	CDialogEx::OnTimer(nIDEvent);
}

void CAgoraFaceUnityTutorialDlg::OnBnClickedCheckSticker()
{
	// TODO:  在此添加控件通知处理程序代码
	BOOL bRes = m_BtnCheckSticker.GetCheck();
	m_FaceNama.m_isDrawProp = bRes;
}


void CAgoraFaceUnityTutorialDlg::OnBnClickedCheckBeauty()
{
	// TODO:  在此添加控件通知处理程序代码
	BOOL bRes = m_BtnCheckBeauty.GetCheck();
	m_FaceNama.m_isBeautyOn = bRes;
}

void CAgoraFaceUnityTutorialDlg::OnBnClickedCheckFilter()
{
	// TODO:  在此添加控件通知处理程序代码
	BOOL bRes = m_BtnCheckFilter.GetCheck();
	m_FaceNama.m_isBeautyOn = bRes;
}


void CAgoraFaceUnityTutorialDlg::OnNMCustomdrawBeauty0(NMHDR *pNMHDR, LRESULT *pResult)
{
	return;
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO:  在此添加控件通知处理程序代码
	*pResult = 0;

	m_FaceNama.m_curBlurLevel = m_SliderBeautyBlur.GetPos() / 100.0f;
	m_FaceNama.UpdateBeauty();
}


void CAgoraFaceUnityTutorialDlg::OnNMCustomdrawBeauty1(NMHDR *pNMHDR, LRESULT *pResult)
{
	return;
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO:  在此添加控件通知处理程序代码
	*pResult = 0;

	m_FaceNama.m_curColorLevel = m_SliderBeautyColor.GetPos() / 100.0f;
	m_FaceNama.UpdateBeauty();
}


void CAgoraFaceUnityTutorialDlg::OnNMCustomdrawBeauty2(NMHDR *pNMHDR, LRESULT *pResult)
{
	return;
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO:  在此添加控件通知处理程序代码
	*pResult = 0;

	m_FaceNama.m_redLevel =  m_SliderBeautyRed.GetPos() / 100.0f;
	m_FaceNama.UpdateBeauty();
}


LRESULT CAgoraFaceUnityTutorialDlg::onEIDRemoteVideoStats(WPARAM wParam, LPARAM lParam)
{
	LPAGE_REMOTE_VIDEO_STAT lpData = (LPAGE_REMOTE_VIDEO_STAT)wParam;
	delete lpData;
	lpData = NULL;
	return 0;
}

LRESULT CAgoraFaceUnityTutorialDlg::onEIDAudioDeviceStateChanged(WPARAM wParam, LPARAM lParam)
{
	LPAGE_AUDIO_DEVICE_STATE_CHANGED lpData = (LPAGE_AUDIO_DEVICE_STATE_CHANGED)wParam;
	delete lpData;
	lpData = NULL;
	return 0;
}

LRESULT CAgoraFaceUnityTutorialDlg::onEIDonVideoDeviceStateChanged(WPARAM wParam, LPARAM lParam)
{
	LPAGE_VIDEO_DEVICE_STATE_CHANGED lpData = (LPAGE_VIDEO_DEVICE_STATE_CHANGED)wParam;
	delete lpData;
	lpData = NULL;
	return 0;
}

