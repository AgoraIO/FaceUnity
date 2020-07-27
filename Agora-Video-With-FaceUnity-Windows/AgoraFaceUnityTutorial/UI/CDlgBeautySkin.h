#pragma once


// CDlgBeautySkin dialog

class CDlgBeautySkin : public CDialogEx
{
    DECLARE_DYNAMIC(CDlgBeautySkin)

public:
    CDlgBeautySkin(CWnd* pParent = nullptr);   // standard constructor
    virtual ~CDlgBeautySkin();

    // Dialog Data
    enum { IDD = IDD_DIALOG_BEAUTY_SKIN };

    enum {
        BEAUTY_SKIN_BLUR=0,
        BEAUTY_SKIN_WHITE,
        BEAUTY_SKIN_RED,
        BEAUTY_SKIN_LIGHT,
        BEAUTY_SKIN_TOOTH,
        BEAUTY_SKIN_COUNT
    };
protected:
    virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

    DECLARE_MESSAGE_MAP()
public:
    void InitText();
    void InitSlider();
    virtual BOOL OnInitDialog();
    afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
    afx_msg void OnClickedCheckAccurateSkin();

    CStatic m_staBeautySkin;
    CComboBox m_cmbBeautyskinStyle;
    CComboBox m_cmbBeautySkinStyle;
    CSliderCtrl m_sldBlur;
    CStatic m_staBlur;
    CStatic m_staWhite;
    CStatic m_staRed;
    CStatic m_staLight;

    CSliderCtrl m_sldWhite;
    CSliderCtrl m_sldRed;
    CSliderCtrl m_sldLight;
    CSliderCtrl m_sldTooth;
    CStatic m_staTooth;

    int m_defaultFaceBeautyLevel[BEAUTY_SKIN_COUNT] = { 70, 50, 50, 70,0 };
    int minpos = 0;
    int maxpos = 100;

    CButton m_chkEnableSkinDetect;  
    afx_msg void OnCbnSelchangeComboBeautyskinStyle();
    afx_msg void OnBnClickedButtonReset();
    CStatic m_staBeautyStyle;
    CButton m_btnReset;
};
