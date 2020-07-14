#pragma once


// CDlgBeautyAR dialog
#define AR_COUNT 9
#define AR_ANIMOJI_COUNT 8
#define AR_PROMAP_COUNT 8
#define AR_AR_COUNT 10

#define AR_EXPRESSION_COUNT 6
#define AR_MUSIC_COUNT 2
#define AR_BG_COUNT 5
#define AR_GESTURE_COUNT 4
#define AR_HAHA_COUNT 5
#define AR_PORTRAIT_COUNT 3
class CDlgBeautyAR : public CDialogEx
{
    DECLARE_DYNAMIC(CDlgBeautyAR)

public:
    CDlgBeautyAR(CWnd* pParent = nullptr);   // standard constructor
    virtual ~CDlgBeautyAR();

    // Dialog Data

    enum { IDD = IDD_DIALOG_BEAUTY_AR };

    enum {
        AR_ANIMOJI = 0,
        AR_PROMAP,
        AR_AR,
        AR_EXPRESSION,
        AR_MUSIC,
        AR_BG,
        AR_GESTURE,
        AR_HAHA,
        AR_PORTRAIT
        
    };
protected:
    virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

    DECLARE_MESSAGE_MAP()

public:
    void HideListCtrl();
    void InitListCtrls();
    void InitChildLists();
    void InitChildBmp(int type, int *ARBmpId, int count);
    void UpdateFaceUnityBundle(int type);
    CImageList	m_cImageListNormal;
    CImageList m_cImageListArs[AR_COUNT];//animoji ar ...
    virtual BOOL OnInitDialog();
    CListCtrl m_lstAR;
    CListCtrl m_lstAnimoji;
    CListCtrl m_lstARBG;
    CListCtrl m_lstARAR;
    
    CListCtrl m_lstARExpression;
    CListCtrl m_lstARGesture;
    CListCtrl m_lstARHAHA;
    CListCtrl m_lstARMusic;
    CListCtrl m_lstPortrait;
    CListCtrl m_lstItemSticker;
    std::vector<CListCtrl*> m_vecListCtrls;
    std::vector<LPTSTR> m_vecExpressions;
    std::vector<LPTSTR> m_vecGestures;
    afx_msg void OnItemchangedListAr(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnItemchangedListArAr(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArBg(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArExpression(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArGesture(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArHaha(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArMusic(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArPortrait(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArPropmap(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnLvnItemchangedListArAnimoji(NMHDR *pNMHDR, LRESULT *pResult);
    CStatic m_staARInfo;
};
