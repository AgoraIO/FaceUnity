#pragma once


// CDlgBeautyFilter dialog

class CDlgBeautyFilter : public CDialogEx
{
    DECLARE_DYNAMIC(CDlgBeautyFilter)

public:
    CDlgBeautyFilter(CWnd* pParent = nullptr);   // standard constructor
    virtual ~CDlgBeautyFilter();

    // Dialog Data
    enum { IDD = IDD_DIALOG_BEAUTY_FILTER };

protected:
    virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

    DECLARE_MESSAGE_MAP()
public:
    CStatic m_staFilterInfo;
    CStatic m_staFilterError;
    CListCtrl m_lstFilter;
    std::vector<LPTSTR> m_vecFilterInfos;
    CImageList	m_cImageListNormal;
    virtual BOOL OnInitDialog();

    afx_msg void OnItemchangedListFilter(NMHDR *pNMHDR, LRESULT *pResult);
};
