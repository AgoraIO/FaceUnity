
// AgoraFaceUnityTutorialDlg.cpp : implementation file
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "AgoraFaceUnityTutorialDlg.h"
#include "afxdialogex.h"
#include "CConfig.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CAboutDlg dialog used for App About

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(IDD_ABOUTBOX)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CAgoraFaceUnityTutorialDlg dialog



CAgoraFaceUnityTutorialDlg::CAgoraFaceUnityTutorialDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_AGORAFACEUNITYTUTORIAL_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CAgoraFaceUnityTutorialDlg::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_TAB_FACEUNITY, m_tabFaceUnity);
    DDX_Control(pDX, IDC_COMBO_CAMERA, m_cmbCamera);
    DDX_Control(pDX, IDC_EDIT_CHANNEL, m_edtChannelName);
    DDX_Control(pDX, IDC_BUTTON_JOINCHANNEL, m_btnJoinChannel);
    DDX_Control(pDX, IDC_STATIC_LOCALVIDEO, m_staLocalVideo);
    DDX_Control(pDX, IDC_STATIC_INFO, m_staFacunityInfo);
    DDX_Control(pDX, IDC_TAB_FACEUNITY2, m_tabFaceunity2);
}

BEGIN_MESSAGE_MAP(CAgoraFaceUnityTutorialDlg, CDialogEx)
    ON_WM_SYSCOMMAND()
    ON_WM_PAINT()
    ON_WM_QUERYDRAGICON()
    ON_MESSAGE(WM_FU_MSGID(EID_FU_INIT_ERROR), &CAgoraFaceUnityTutorialDlg::OnEIDInitError)
    ON_MESSAGE(WM_FU_MSGID(EID_FU_BUNDLE_ERROR), &CAgoraFaceUnityTutorialDlg::OnEIDBundleError)
 ON_BN_CLICKED(IDC_BUTTON_JOINCHANNEL, &CAgoraFaceUnityTutorialDlg::OnBnClickedButtonJoinchannel)
 ON_CBN_SELCHANGE(IDC_COMBO_CAMERA, &CAgoraFaceUnityTutorialDlg::OnSelchangeComboCamera)
    ON_NOTIFY(TCN_SELCHANGE, IDC_TAB_FACEUNITY, &CAgoraFaceUnityTutorialDlg::OnSelchangeTabFaceunity)
    ON_NOTIFY(TCN_SELCHANGE, IDC_TAB_FACEUNITY2, &CAgoraFaceUnityTutorialDlg::OnSelchangeTabFaceunity2)
END_MESSAGE_MAP()


// CAgoraFaceUnityTutorialDlg message handlers

BOOL CAgoraFaceUnityTutorialDlg::OnInitDialog()
{
    CDialogEx::OnInitDialog();

    // Add "About..." menu item to system menu.

    // IDM_ABOUTBOX must be in the system command range.
    ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
    ASSERT(IDM_ABOUTBOX < 0xF000);

    CMenu* pSysMenu = GetSystemMenu(FALSE);
    if (pSysMenu != nullptr)
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

    // Set the icon for this dialog.  The framework does this automatically
    //  when the application's main window is not a dialog
    SetIcon(m_hIcon, TRUE);			// Set big icon
    SetIcon(m_hIcon, FALSE);		// Set small icon

    // TODO: Add extra initialization here
    if (!InitAgora())
        return FALSE;

    ShowLocalVideo();
    InitFaceUnity();


    InitKeyInfomation();
    InitTabCtrl();
    InitTabDlg();

    
    //Init camera Device
    std::unordered_map<std::string, std::string> devices;
    CAgoraObject::GetAgoraObject()->GetVideoDevices(devices);
    int i = 0;
    std::string currentDeviceId = CAgoraObject::GetAgoraObject()->GetCurrentVideoDevice();
    for (auto iter= devices.begin(); iter != devices.end(); ++iter) {
        m_cmbCamera.InsertString(i, utf82cs(iter->second));
        if (iter->first.compare(currentDeviceId) == 0) {
            m_cmbCamera.SetCurSel(i);
        }
        ++i;
    }
    
    CAgoraObject::GetAgoraObject()->SetMsgReceiver(m_hWnd);
    return TRUE;  // return TRUE  unless you set the focus to a control
}

BOOL CAgoraFaceUnityTutorialDlg::InitAgora()
{
    CString strAppID = CAgoraObject::LoadAppID();
    if (!strAppID.IsEmpty())
        return FALSE;

    CAgoraObject::GetAgoraObject(strAppID);
    CAgoraObject::GetAgoraObject()->SetMsgHandlerWnd(m_hWnd);
    CAgoraObject::GetAgoraObject()->SetChannelProfile(TRUE);
    CAgoraObject::GetAgoraObject()->EnableVideo();
    CAgoraObject::GetAgoraObject()->SetClientRole(CLIENT_ROLE_BROADCASTER);
}

void CAgoraFaceUnityTutorialDlg::ShowLocalVideo()
{
    CAgoraObject::GetEngine()->startPreview();
    VideoCanvas canvas;
    canvas.uid = 0;
    
    canvas.renderMode = RENDER_MODE_FIT;
    canvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
    canvas.channelId[0] = 0;
    canvas.view = m_staLocalVideo.GetSafeHwnd();
    CAgoraObject::GetEngine()->setupLocalVideo(canvas);
}

void CAgoraFaceUnityTutorialDlg::InitFaceUnity()
{
    Nama::resetBeautyParam();//beautyFace
    Nama::resetShapeParam();//faceshape
    Nama::loadAllBundles();
    Nama::bundleCategory = BundleCategory::Makeup;//makeup is selected default
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

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CAgoraFaceUnityTutorialDlg::OnPaint()
{
    if (IsIconic())
    {
        CPaintDC dc(this); // device context for painting

        SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

        // Center icon in client rectangle
        int cxIcon = GetSystemMetrics(SM_CXICON);
        int cyIcon = GetSystemMetrics(SM_CYICON);
        CRect rect;
        GetClientRect(&rect);
        int x = (rect.Width() - cxIcon + 1) / 2;
        int y = (rect.Height() - cyIcon + 1) / 2;

        // Draw the icon
        dc.DrawIcon(x, y, m_hIcon);
    }
    else
    {
        CDialogEx::OnPaint();
    }
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CAgoraFaceUnityTutorialDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CAgoraFaceUnityTutorialDlg::InitTabCtrl()
{
    std::vector<wchar_t*> vecKeys;

    vecKeys.push_back(faceuSkinBeauty);
    vecKeys.push_back(faceuTypeBeauty);
    vecKeys.push_back(faceuFilterBeauty);

    for (int i = 0; i < vecKeys.size(); ++i) {
        TCITEM tcItem;
        tcItem.pszText = vecKeys[i];
        tcItem.mask = TCIF_TEXT;
        m_tabFaceUnity.InsertItem(i, &tcItem);
    }

    std::vector<wchar_t*> vecKeys2;
    vecKeys2.push_back(faceuMakeupBeauty);
    vecKeys2.push_back(faceuARFuction);

    for (int i = 0; i < vecKeys2.size(); ++i) {
        TCITEM tcItem;
        tcItem.pszText = vecKeys2[i];
        tcItem.mask = TCIF_TEXT;
        m_tabFaceunity2.InsertItem(i, &tcItem);
    }
    
    m_tabFaceUnity.GetWindowRect(&m_rcTabCtrl);

    CRect rcItem;
    m_tabFaceUnity.GetItemRect(0, &rcItem);
    m_rcTabCtrl.top += (rcItem.bottom - rcItem.top) + 1;


    m_rcBeauty.left   = 0;
    m_rcBeauty.top    = (rcItem.bottom - rcItem.top);
    m_rcBeauty.bottom = m_rcTabCtrl.bottom - m_rcTabCtrl.top + m_rcBeauty.top;
    m_rcBeauty.right  = m_rcTabCtrl.right - m_rcTabCtrl.left + m_rcBeauty.left;
}

void CAgoraFaceUnityTutorialDlg::InitTabDlg()
{
    m_dlgBeautySkin.Create(CDlgBeautySkin::IDD, &m_tabFaceUnity);
    m_dlgBeautySkin.SetParent(&m_tabFaceUnity);

    m_dlgBeautySkin.MoveWindow(&m_rcBeauty);
    m_dlgBeautySkin.ShowWindow(SW_SHOW);

    m_dlgBeautyFaceShape.Create(CDlgBeautyFaceShape::IDD, &m_tabFaceUnity);
    m_dlgBeautyFaceShape.SetParent(&m_tabFaceUnity);
    m_dlgBeautyFaceShape.MoveWindow(&m_rcBeauty);
    m_dlgBeautyFaceShape.ShowWindow(SW_HIDE);


    m_dltgBeautyFilter.Create(CDlgBeautyFilter::IDD, &m_tabFaceUnity);
    m_dltgBeautyFilter.SetParent(&m_tabFaceUnity);
    m_dltgBeautyFilter.MoveWindow(&m_rcBeauty);
    m_dltgBeautyFilter.ShowWindow(SW_HIDE);

    m_dlgBeautyMakeup.Create(CDlgBeautyMakeup::IDD, &m_tabFaceunity2);
    m_dlgBeautyMakeup.SetParent(&m_tabFaceunity2);
    m_dlgBeautyMakeup.MoveWindow(&m_rcBeauty);
    m_dlgBeautyMakeup.ShowWindow(SW_SHOW);

    m_dlgBeautyAR.Create(CDlgBeautyAR::IDD, &m_tabFaceunity2);
    m_dlgBeautyAR.SetParent(&m_tabFaceunity2);
    m_dlgBeautyAR.MoveWindow(&m_rcBeauty);
    m_dlgBeautyAR.ShowWindow(SW_HIDE);
}

void CAgoraFaceUnityTutorialDlg::OnBnClickedButtonJoinchannel()
{
    if (CAgoraObject::GetAgoraObject()->IsJoinChannel()) {
        CAgoraObject::GetAgoraObject()->EnableVideoFrameObserver(FALSE);
        CAgoraObject::GetAgoraObject()->LeaveCahnnel();
        m_btnJoinChannel.SetWindowText(_T("Joinchannel"));
        ShowLocalVideo();
    }
    else {
        CString strChannel;
        m_edtChannelName.GetWindowText(strChannel);
        if (strChannel.IsEmpty()) {
            AfxMessageBox(_T("Input Channel Name"));
            return;
        }
        CAgoraObject::GetAgoraObject()->EnableVideoFrameObserver(TRUE);
        CAgoraObject::GetAgoraObject()->JoinChannel(strChannel);
        m_btnJoinChannel.SetWindowText(_T("LeaveChannel"));
    }
}


void CAgoraFaceUnityTutorialDlg::OnSelchangeComboCamera()
{
    int sel = m_cmbCamera.GetCurSel();

}

void CAgoraFaceUnityTutorialDlg::HideTab1ChildDlg()
{
    m_dlgBeautySkin.ShowWindow(SW_HIDE);
    m_dlgBeautyFaceShape.ShowWindow(SW_HIDE);
    m_dltgBeautyFilter.ShowWindow(SW_HIDE);
}

void CAgoraFaceUnityTutorialDlg::OnSelchangeTabFaceunity(NMHDR *pNMHDR, LRESULT *pResult)
{
    HideTab1ChildDlg();
    int sel = m_tabFaceUnity.GetCurSel();

    switch (sel) {
    case 0:
        m_dlgBeautySkin.ShowWindow(SW_SHOW);
        break;
    case 1:
        m_dlgBeautyFaceShape.ShowWindow(SW_SHOW);
        break;
    case 2:
        m_dltgBeautyFilter.ShowWindow(SW_SHOW);
        break;
    
    }
    // TODO: Add your control notification handler code here
    *pResult = 0;
}


void CAgoraFaceUnityTutorialDlg::OnSelchangeTabFaceunity2(NMHDR *pNMHDR, LRESULT *pResult)
{
    // TODO: Add your control notification handler code here
    *pResult = 0;
    int sel = m_tabFaceunity2.GetCurSel();

    switch (sel) {
    case 0:
        m_dlgBeautyAR.ShowWindow(SW_HIDE);
        m_dlgBeautyMakeup.ShowWindow(SW_SHOW);
        Nama::bundleCategory = BundleCategory::Makeup;
        break;
    case 1:
        m_dlgBeautyMakeup.ShowWindow(SW_HIDE);
        m_dlgBeautyAR.ShowWindow(SW_SHOW);
       
        break;
    }
}

LRESULT CAgoraFaceUnityTutorialDlg::OnEIDInitError(WPARAM wParam, LPARAM lParam)
{
    CString str = _T("Error:");
    str += fuSDKErrorNoFile + utf82cs(Nama::initFuError);
    m_staFacunityInfo.SetWindowText(str);
    return 0;
}

LRESULT CAgoraFaceUnityTutorialDlg::OnEIDBundleError(WPARAM wParam, LPARAM lParam)
{

    m_staFacunityInfo.SetWindowText(fuSDKErrorNoAuth);
    return 0;
}