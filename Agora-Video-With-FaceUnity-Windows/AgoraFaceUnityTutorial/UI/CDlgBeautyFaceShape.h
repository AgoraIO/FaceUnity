#pragma once


// CDlgBeautyFaceShape dialog

class CDlgBeautyFaceShape : public CDialogEx
{
	DECLARE_DYNAMIC(CDlgBeautyFaceShape)

public:
	CDlgBeautyFaceShape(CWnd* pParent = nullptr);   // standard constructor
	virtual ~CDlgBeautyFaceShape();

// Dialog Data
	enum { IDD = IDD_DIALOG_BEAUTY_FACE };

 enum {
     BEAUTY_SHAPE_THIN_FACE =0 ,
     BEAUTY_SHAPE_BIG_EYE,
     BEAUTY_SHAPE_CHIN,
     BEAUTY_SHAPE_FOREHEAD,
     BEAUTY_SHAPE_THINNOSE,
     BEAUTY_SHAPE_MOUSETYPE,
     BEAUTY_SHAPE_VFACE,
     BEAUTY_SHAPE_NARROW_FACE,
     BEAUTY_SHAPE_LITTLE_FACE,
     BEAUTY_SHAPE_COUNT
 };
protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
    void InitSlider();
    virtual BOOL OnInitDialog();
    void ShowCtrl(bool bShow = true);
    CComboBox m_cmbBeautyFaceShape;
    CSliderCtrl m_sldThinFace;
    CSliderCtrl m_sldBigEye;
    CSliderCtrl m_sldChin;
    CSliderCtrl m_sldForehead;
    CSliderCtrl m_sldThinNose;
    CSliderCtrl m_sldMouseType;
    CSliderCtrl m_sldVFace;
    CSliderCtrl m_sldNarrowFace;
    CSliderCtrl m_sldLittleFace;
    CStatic m_staThinFace;
    CStatic m_staBigEye;
    CStatic m_staChin;
    CStatic m_staForeHead;
    CStatic m_staThinNose;
    CStatic m_staMouseType;
    CStatic m_staVFace;
    CStatic m_staNarrowFace;
    CStatic m_staLittleFace;
    std::vector<CStatic*> m_vecStaticInfos;
    std::vector<CStatic*> m_vecStaticValue;
    std::vector<CSliderCtrl*> m_vecSliders;
    int m_defaultFaceShapeLevel[BEAUTY_SHAPE_COUNT] = { 40, 40, -20, -20, 50, -10, 0, 0, 0 };
    int m_customFaceShapeLevel[BEAUTY_SHAPE_COUNT] = { 40, 40, -20, -20, 50, -10, 0, 0, 0 };
    int m_otherFaceShapeLevel[BEAUTY_SHAPE_COUNT] = { 40, 40, 0, 0, 0, 0, 0, 0, 0 };

    afx_msg void OnNMCustomdrawSliderBeautyThinface(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnHScroll(UINT nSBCode, UINT nPos, CScrollBar* pScrollBar);
    afx_msg void OnBnClickedButtonResetshape();
    afx_msg void OnCbnSelchangeComboBeautyfaceType();
    CStatic m_staThinFaceInfo;
    CStatic m_staBigEyeInfo;
    CStatic m_staChinInfo;
    CStatic m_staForeHeadInfo;
    CStatic m_staThinNoseInfo;
    CStatic m_staMouseTypeInfo;
    CStatic m_staVFaceInfo;
    CStatic m_staNarrowFaceInfo;
    CStatic m_staLittleFaceInfo;
};
