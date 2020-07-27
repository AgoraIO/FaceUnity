#pragma once


// CDlgBeautyMakeup dialog

class CDlgBeautyMakeup : public CDialogEx
{
    DECLARE_DYNAMIC(CDlgBeautyMakeup)

public:
    enum {
        MAKUP_CHARMING = 0,
        MAKUP_FLOWER,
        MAKUP_LADY,
        MAKUP_MOON,
        MAKUP_NEIGHBOUR,
        MAKUP_OCCIDENT,
        MAKUP_SEXY,
        MAKUP_SWEET,
        MAKUP_TOUGH_GUY
    };
    CDlgBeautyMakeup(CWnd* pParent = nullptr);   // standard constructor
    virtual ~CDlgBeautyMakeup();

    // Dialog Data

    enum { IDD = IDD_DIALOG_BEAUTY_MAKEUP };


protected:
    virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

    DECLARE_MESSAGE_MAP()

   
public:
    virtual BOOL OnInitDialog();
    CListCtrl m_lstMakeup;
    CImageList	m_cMakeupImageListNormal;
    std::vector<LPTSTR> m_vecMakeupNames;
    std::vector<LPTSTR> m_vecMakeupErrors;
    afx_msg void OnItemchangedListMakeup(NMHDR *pNMHDR, LRESULT *pResult);
    CStatic m_staMakupInfo;
    CStatic m_staMakeupErr;
};
