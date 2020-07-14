// CDlgBeautySkin.cpp : implementation file
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "CDlgBeautySkin.h"
#include "afxdialogex.h"
#include "Agora/AgoraObject.h"

// CDlgBeautySkin dialog

IMPLEMENT_DYNAMIC(CDlgBeautySkin, CDialogEx)

CDlgBeautySkin::CDlgBeautySkin(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DIALOG_BEAUTY_SKIN, pParent)
{

}

CDlgBeautySkin::~CDlgBeautySkin()
{
}

void CDlgBeautySkin::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);

    DDX_Control(pDX, IDC_COMBO_BEAUTYSKIN_STYLE, m_cmbBeautySkinStyle);
    DDX_Control(pDX, IDC_SLIDER_BEAUTYSKIN_BLUR, m_sldBlur);
    DDX_Control(pDX, IDC_STATIC_BLUR, m_staBlur);
    DDX_Control(pDX, IDC_STATIC_WHITE, m_staWhite);
    DDX_Control(pDX, IDC_STATIC_BLUR3, m_staRed);
    DDX_Control(pDX, IDC_STATIC_LIGHT, m_staLight);
    DDX_Control(pDX, IDC_SLIDER_BEAUTYSKIN_WHITE, m_sldWhite);
    DDX_Control(pDX, IDC_SLIDER_BEAUTYSKIN_RED, m_sldRed);
    DDX_Control(pDX, IDC_SLIDER_BEAUTYSKIN_LIGHT, m_sldLight);
    DDX_Control(pDX, IDC_SLIDER_BEAUTYSKIN_TOOTH, m_sldTooth);
    DDX_Control(pDX, IDC_STATIC_TOOTH, m_staTooth);
    DDX_Control(pDX, IDC_CHECK_ACCURATE_SKIN, m_chkEnableSkinDetect);
    DDX_Control(pDX, IDC_STATIC_BEAUTYSKIN_STYLE, m_staBeautyStyle);
    DDX_Control(pDX, IDC_BUTTON_RESET, m_btnReset);
}


BEGIN_MESSAGE_MAP(CDlgBeautySkin, CDialogEx)
    ON_WM_HSCROLL()
    ON_BN_CLICKED(IDC_CHECK_ACCURATE_SKIN, &CDlgBeautySkin::OnClickedCheckAccurateSkin)
    ON_CBN_SELCHANGE(IDC_COMBO_BEAUTYSKIN_STYLE, &CDlgBeautySkin::OnCbnSelchangeComboBeautyskinStyle)
    ON_BN_CLICKED(IDC_BUTTON_RESET, &CDlgBeautySkin::OnBnClickedButtonReset)
END_MESSAGE_MAP()


// CDlgBeautySkin message handlers


BOOL CDlgBeautySkin::OnInitDialog()
{
    CDialogEx::OnInitDialog();
    m_chkEnableSkinDetect.SetCheck(1);
    // TODO:  Add extra initialization here
    //
    int i = 0;
    m_cmbBeautySkinStyle.InsertString(i++, fuUIBlur1);
    m_cmbBeautySkinStyle.InsertString(i++, fuUIBlur1);
    m_cmbBeautySkinStyle.InsertString(i++, fuUIBlur1);
    m_cmbBeautySkinStyle.SetCurSel(0);
   
    InitSlider();
    return TRUE;  // return TRUE unless you set the focus to a control
                  // EXCEPTION: OCX Property Pages should return FALSE
}

void CDlgBeautySkin::InitText()
{
    m_chkEnableSkinDetect.SetWindowText(fuUIEnableBeauty);
    m_staBeautyStyle.SetWindowText(fuBeautyStyle);
    m_staBlur.SetWindowText(fuUIBlur);
    m_staWhite.SetWindowText(fuUIWhite);
    m_staRed.SetWindowText(fuUIRed);
    m_staLight.SetWindowText(fuUILight);
    m_staTooth.SetWindowText(fuUITooth);
    m_btnReset.SetWindowText(fuUIReset);
}

void CDlgBeautySkin::InitSlider()
{
    std::vector<CSliderCtrl*> m_vecSliders;
    m_vecSliders.push_back(&m_sldBlur);
    m_vecSliders.push_back(&m_sldWhite);
    m_vecSliders.push_back(&m_sldRed);
    m_vecSliders.push_back(&m_sldLight);
    m_vecSliders.push_back(&m_sldTooth);

    for (int i = 0; i < m_vecSliders.size(); ++i) {
        m_vecSliders[i]->SetRange(minpos, maxpos);
        m_vecSliders[i]->SetPageSize(10);
        m_vecSliders[i]->SetPos(m_defaultFaceBeautyLevel[i]);
    }

    std::vector<CStatic*> m_vecStaticInfos;
    m_vecStaticInfos.push_back(&m_staBlur);
    m_vecStaticInfos.push_back(&m_staWhite);
    m_vecStaticInfos.push_back(&m_staRed);
    m_vecStaticInfos.push_back(&m_staLight);
    m_vecStaticInfos.push_back(&m_staTooth);
    for (int i = 0; i < m_vecStaticInfos.size(); ++i) {
        CString str;
        str.Format(_T("%d"), m_defaultFaceBeautyLevel[i]);
        m_vecStaticInfos[i]->SetWindowText(str);
    }
}

void CDlgBeautySkin::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
    if (TB_THUMBTRACK != nSBCode
        && TB_THUMBPOSITION != nSBCode
        && TB_PAGEUP != nSBCode
        && TB_PAGEDOWN != nSBCode) {
        return;
    }
    CString strPos;
  
   
    if (pScrollBar == (CScrollBar*)&m_sldBlur) {
        int pos = m_sldBlur.GetPos();
        strPos.Format(_T("%d"), pos);
        m_staBlur.SetWindowText(strPos);//ĥƤ

        Nama::mFaceBeautyLevel[BEAUTY_SKIN_BLUR] = (float)pos;
    }
    else if (pScrollBar == (CScrollBar*)&m_sldWhite) {
        int pos = m_sldWhite.GetPos();
        strPos.Format(_T("%d"), pos);
        m_staWhite.SetWindowText(strPos);
        Nama::mFaceBeautyLevel[BEAUTY_SKIN_WHITE] = (float)pos;
    }
    else if (pScrollBar == (CScrollBar*)&m_sldRed) {
        int pos = m_sldRed.GetPos();
        strPos.Format(_T("%d"), pos);
        m_staRed.SetWindowText(strPos);
        Nama::mFaceBeautyLevel[BEAUTY_SKIN_RED] = (float)pos;
    }
    else if (pScrollBar == (CScrollBar*)&m_sldLight) {
        int pos = m_sldLight.GetPos();
        strPos.Format(_T("%d"), pos);
        m_staLight.SetWindowText(strPos);
        Nama::mFaceBeautyLevel[BEAUTY_SKIN_LIGHT] = (float)pos;
    }
    else if (pScrollBar == (CScrollBar*)&m_sldTooth) {
        int pos = m_sldTooth.GetPos();
        strPos.Format(_T("%d"), pos);
        m_staTooth.SetWindowText(strPos);
        Nama::mFaceBeautyLevel[BEAUTY_SKIN_TOOTH] = (float)pos;
    }

    CAgoraObject::GetAgoraObject()->UpdateBeauty();
    CDialogEx::OnHScroll(nSBCode, nPos, pScrollBar);
}

void CDlgBeautySkin::OnClickedCheckAccurateSkin()
{
    Nama::mEnableSkinDect = m_chkEnableSkinDetect.GetCheck();
    CAgoraObject::GetAgoraObject()->UpdateBeauty();
}


void CDlgBeautySkin::OnCbnSelchangeComboBeautyskinStyle()
{
    Nama::mEnableHeayBlur = m_cmbBeautySkinStyle.GetCurSel();
    CAgoraObject::GetAgoraObject()->UpdateBeauty();
}


void CDlgBeautySkin::OnBnClickedButtonReset()
{
    for (int i = 0; i < BEAUTY_SKIN_COUNT; ++i) {
        Nama::mFaceBeautyLevel[i] = m_defaultFaceBeautyLevel[i];
   }
    CAgoraObject::GetAgoraObject()->UpdateBeauty();
}
