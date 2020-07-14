// CDlgBeautyFaceShape.cpp : implementation file
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "CDlgBeautyFaceShape.h"
#include "afxdialogex.h"


// CDlgBeautyFaceShape dialog

IMPLEMENT_DYNAMIC(CDlgBeautyFaceShape, CDialogEx)

CDlgBeautyFaceShape::CDlgBeautyFaceShape(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DIALOG_BEAUTY_FACE, pParent)
{

}

CDlgBeautyFaceShape::~CDlgBeautyFaceShape()
{
}

void CDlgBeautyFaceShape::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_COMBO_BEAUTYFACE_TYPE, m_cmbBeautyFaceShape);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_THINFACE, m_sldThinFace);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_BIGEYE, m_sldBigEye);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_CHIN, m_sldChin);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_FOREHEAD, m_sldForehead);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_THINNOSE, m_sldThinNose);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_MOUSETYPE, m_sldMouseType);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_VFACE, m_sldVFace);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_NARRPOWFACE, m_sldNarrowFace);
    DDX_Control(pDX, IDC_SLIDER_BEAUTY_LITTLEFACE, m_sldLittleFace);
    DDX_Control(pDX, IDC_STATIC_THINFACE, m_staThinFace);
    DDX_Control(pDX, IDC_STATIC_BIGEYE, m_staBigEye);
    DDX_Control(pDX, IDC_STATIC_CHIN, m_staChin);
    DDX_Control(pDX, IDC_STATIC_FOREHEAD, m_staForeHead);
    DDX_Control(pDX, IDC_STATIC_THINNOSE, m_staThinNose);
    DDX_Control(pDX, IDC_STATIC_MOUSETYPE, m_staMouseType);
    DDX_Control(pDX, IDC_STATIC_VFACE, m_staVFace);
    DDX_Control(pDX, IDC_STATIC_LITTLEFACE, m_staLittleFace);
    DDX_Control(pDX, IDC_STATIC_NARROWFACE, m_staNarrowFace);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_THINFACE, m_staThinFaceInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_BIGEYE, m_staBigEyeInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_CHIN, m_staChinInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_FOREHEAD, m_staForeHeadInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_THINNOSE, m_staThinNoseInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_MOUSETYPE, m_staMouseTypeInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_VFACE, m_staVFaceInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_NARROWFACE, m_staNarrowFaceInfo);
    DDX_Control(pDX, IDC_STATIC_BEAUTY_LITTLEFACE, m_staLittleFaceInfo);
}


BEGIN_MESSAGE_MAP(CDlgBeautyFaceShape, CDialogEx)
    ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_BEAUTY_THINFACE, &CDlgBeautyFaceShape::OnNMCustomdrawSliderBeautyThinface)
    ON_WM_HSCROLL()
    ON_BN_CLICKED(IDC_BUTTON_RESETSHAPE, &CDlgBeautyFaceShape::OnBnClickedButtonResetshape)
    ON_CBN_SELCHANGE(IDC_COMBO_BEAUTYFACE_TYPE, &CDlgBeautyFaceShape::OnCbnSelchangeComboBeautyfaceType)
END_MESSAGE_MAP()


// CDlgBeautyFaceShape message handlers


BOOL CDlgBeautyFaceShape::OnInitDialog()
{
    CDialogEx::OnInitDialog();

    // TODO:  Add extra initialization here
    int i = 0;
    m_cmbBeautyFaceShape.InsertString(i++, _T("自定义"));
    m_cmbBeautyFaceShape.InsertString(i++, _T("默认"));
    m_cmbBeautyFaceShape.InsertString(i++, _T("女神"));
    m_cmbBeautyFaceShape.InsertString(i++, _T("网红"));
    m_cmbBeautyFaceShape.InsertString(i++, _T("自然"));
    m_cmbBeautyFaceShape.SetCurSel(0);

    InitSlider();
    return TRUE;  // return TRUE unless you set the focus to a control
                  // EXCEPTION: OCX Property Pages should return FALSE
}

void CDlgBeautyFaceShape::InitSlider()
{
    m_vecSliders.push_back(&m_sldThinFace);
    m_vecSliders.push_back(&m_sldBigEye);
    m_vecSliders.push_back(&m_sldChin);
    m_vecSliders.push_back(&m_sldForehead);
    m_vecSliders.push_back(&m_sldThinNose);
    m_vecSliders.push_back(&m_sldMouseType);
    m_vecSliders.push_back(&m_sldVFace);
    m_vecSliders.push_back(&m_sldNarrowFace);
    m_vecSliders.push_back(&m_sldLittleFace);
    for (int i = 0; i < m_vecSliders.size(); ++i) {
        if (i == BEAUTY_SHAPE_CHIN
            || i == BEAUTY_SHAPE_FOREHEAD
            || i == BEAUTY_SHAPE_MOUSETYPE) {
            m_vecSliders[i]->SetRange(-50, 50);
        }
        else {
            m_vecSliders[i]->SetRange(0, 100);          
        }
        m_vecSliders[i]->SetPageSize(10);
        m_vecSliders[i]->SetPos(m_defaultFaceShapeLevel[i]);
    }


    m_vecStaticValue.push_back(&m_staThinFace);
    m_vecStaticValue.push_back(&m_staBigEye);
    m_vecStaticValue.push_back(&m_staChin);
    m_vecStaticValue.push_back(&m_staForeHead);
    m_vecStaticValue.push_back(&m_staThinNose);
    m_vecStaticValue.push_back(&m_staMouseType);
    m_vecStaticValue.push_back(&m_staVFace);
    m_vecStaticValue.push_back(&m_staNarrowFace);
    m_vecStaticValue.push_back(&m_staLittleFace);
    for (int i = 0; i < m_vecStaticValue.size(); ++i) {
        CString str;
        str.Format(_T("%d"), m_defaultFaceShapeLevel[i]);
        m_vecStaticValue[i]->SetWindowText(str);
    }

    m_vecStaticInfos.push_back(&m_staThinFaceInfo);
    m_vecStaticInfos.push_back(&m_staBigEyeInfo);
    m_vecStaticInfos.push_back(&m_staChinInfo);
    m_vecStaticInfos.push_back(&m_staForeHeadInfo);
    m_vecStaticInfos.push_back(&m_staThinNoseInfo);
    m_vecStaticInfos.push_back(&m_staMouseTypeInfo);
    m_vecStaticInfos.push_back(&m_staVFaceInfo);
    m_vecStaticInfos.push_back(&m_staNarrowFaceInfo);
    m_vecStaticInfos.push_back(&m_staLittleFaceInfo);
}

void CDlgBeautyFaceShape::ShowCtrl(bool bShow )
{
    int nShow = SW_SHOW;
    if (!bShow)
        nShow = SW_HIDE;
    for (int i = BEAUTY_SHAPE_CHIN; i < BEAUTY_SHAPE_COUNT; ++i) {
        m_vecStaticInfos[i]->ShowWindow(nShow);
        m_vecSliders[i]->ShowWindow(nShow);
        m_vecStaticValue[i]->ShowWindow(nShow);
    }


}

void CDlgBeautyFaceShape::OnNMCustomdrawSliderBeautyThinface(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
}


void CDlgBeautyFaceShape::OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar)
{
    // TODO: Add your message handler code here and/or call default
    if (TB_THUMBTRACK != nSBCode
        && TB_THUMBPOSITION != nSBCode
        && TB_PAGEUP != nSBCode
        && TB_PAGEDOWN != nSBCode) {
        return;
    }

    int sel = -1;
    for (int i = 0; i < m_vecSliders.size(); i++){
        CSliderCtrl* pSld = m_vecSliders[i];
        if ((CScrollBar*)pSld == pScrollBar) {
            sel = i;
        }
    }

    if (sel >= 0) {
        int pos = m_vecSliders[sel]->GetPos();
        Nama::mFaceShapeLevel[sel] = (float)pos;
        CString strPos;
        strPos.Format(_T("%d"), pos);
        m_vecStaticValue[sel]->SetWindowText(strPos);

        if (m_cmbBeautyFaceShape.GetCurSel() == 0) {//custom
            m_customFaceShapeLevel[sel] = Nama::mFaceShapeLevel[sel];
        }
        else {
            m_otherFaceShapeLevel[sel] = Nama::mFaceShapeLevel[sel];
        }
    }
    
    CAgoraObject::GetAgoraObject()->UpdateBeauty();
    CDialogEx::OnHScroll(nSBCode, nPos, pScrollBar);

  
}


void CDlgBeautyFaceShape::OnBnClickedButtonResetshape()
{
    for (int i = 0; i < BEAUTY_SHAPE_COUNT; ++i) {
        Nama::mFaceShapeLevel[i] = m_defaultFaceShapeLevel[i];
        m_vecSliders[i]->SetPos(m_defaultFaceShapeLevel[i]);
        CString strPos;
        strPos.Format(_T("%d"), m_defaultFaceShapeLevel[i]);
        m_vecStaticValue[i]->SetWindowText(strPos);
    }
    CAgoraObject::GetAgoraObject()->UpdateBeauty();
}


void CDlgBeautyFaceShape::OnCbnSelchangeComboBeautyfaceType()
{
    int* FaceShapeLeave = m_defaultFaceShapeLevel;
    int count = 0;
    if (m_cmbBeautyFaceShape.GetCurSel() == 0) {
        FaceShapeLeave = m_customFaceShapeLevel;
        ShowCtrl();
        count = BEAUTY_SHAPE_COUNT;
    }
    else {
        FaceShapeLeave = m_otherFaceShapeLevel;
        ShowCtrl(false);
        count = BEAUTY_SHAPE_BIG_EYE + 1;
    }

    for (int i = 0; i < count; ++i) {
        m_vecSliders[i]->SetPos(FaceShapeLeave[i]);
        CString strPos;
        strPos.Format(_T("%d"), FaceShapeLeave[i]);
        m_vecStaticValue[i]->SetWindowText(strPos);
    }

    for (size_t i = 0; i < BEAUTY_SHAPE_COUNT; i++) {
        Nama::mFaceShapeLevel[i] = FaceShapeLeave[i];
    }
    CAgoraObject::GetAgoraObject()->UpdateBeauty();
}
