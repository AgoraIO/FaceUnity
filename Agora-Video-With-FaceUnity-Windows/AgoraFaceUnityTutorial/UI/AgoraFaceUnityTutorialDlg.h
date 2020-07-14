
// AgoraFaceUnityTutorialDlg.h : header file
//

#pragma once

#include "CDlgBeautySkin.h"
#include "CDlgBeautyFaceShape.h"
#include "CDlgBeautyFilter.h"
#include "CDlgBeautyMakeup.h"
#include "CDlgBeautyAR.h"
// CAgoraFaceUnityTutorialDlg dialog
class CAgoraFaceUnityTutorialDlg : public CDialogEx
{
    // Construction
public:
    CAgoraFaceUnityTutorialDlg(CWnd* pParent = nullptr);	// standard constructor

   // Dialog Data
#ifdef AFX_DESIGN_TIME
    enum { IDD = IDD_AGORAFACEUNITYTUTORIAL_DIALOG };
#endif

protected:
    virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support


   // Implementation
protected:
    HICON m_hIcon;

    // Generated message map functions
    virtual BOOL OnInitDialog();
    afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
    afx_msg void OnPaint();
    afx_msg HCURSOR OnQueryDragIcon();
    DECLARE_MESSAGE_MAP()
public:
    CTabCtrl m_tabFaceUnity;
    CTabCtrl m_tabFaceunity2;
    CComboBox m_cmbCamera;
    CEdit m_edtChannelName;
    CButton m_btnJoinChannel;
    CStatic m_staLocalVideo;
    CStatic m_staFacunityInfo;
  
    void InitTabCtrl();
    void InitTabDlg();
    afx_msg void OnSelchangeComboCamera();
    afx_msg void OnBnClickedButtonJoinchannel();
private:
    void HideTab1ChildDlg();
    void InitAgora();
    void InitFaceUnity();
    void ShowLocalVideo();
    CRect m_rcTabCtrl;
    CRect m_rcBeauty;
   
    CDlgBeautySkin m_dlgBeautySkin;
    CDlgBeautyFaceShape m_dlgBeautyFaceShape;
    CDlgBeautyFilter m_dltgBeautyFilter;
    CDlgBeautyMakeup m_dlgBeautyMakeup;
    CDlgBeautyAR m_dlgBeautyAR;
public:
    afx_msg void OnSelchangeTabFaceunity(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg void OnSelchangeTabFaceunity2(NMHDR *pNMHDR, LRESULT *pResult);
    afx_msg LRESULT OnEIDInitError(WPARAM wParam, LPARAM lParam);
    afx_msg LRESULT OnEIDBundleError(WPARAM wParam, LPARAM lParam);
};
