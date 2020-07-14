// CDlgBeautyFilter.cpp : implementation file
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "CDlgBeautyFilter.h"
#include "afxdialogex.h"


// CDlgBeautyFilter dialog

IMPLEMENT_DYNAMIC(CDlgBeautyFilter, CDialogEx)

CDlgBeautyFilter::CDlgBeautyFilter(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DIALOG_BEAUTY_FILTER, pParent)
{

}

CDlgBeautyFilter::~CDlgBeautyFilter()
{
}

void CDlgBeautyFilter::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_STATIC_FILTER_INFO, m_staFilterInfo);
    DDX_Control(pDX, IDC_STATIC_FILTER_ERROR, m_staFilterError);
    DDX_Control(pDX, IDC_LIST_FILTER, m_lstFilter);
}


BEGIN_MESSAGE_MAP(CDlgBeautyFilter, CDialogEx)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_FILTER, &CDlgBeautyFilter::OnItemchangedListFilter)
END_MESSAGE_MAP()


// CDlgBeautyFilter message handlers


BOOL CDlgBeautyFilter::OnInitDialog()
{
    CDialogEx::OnInitDialog();
    HIMAGELIST hList = ImageList_Create(106, 106, ILC_COLOR8 | ILC_MASK, 6, 1);
    m_cImageListNormal.Attach(hList);
    
    int filterBmpId[6] = { 0 };
    int i = 0;
    filterBmpId[i++] = IDB_IMAGE_ORIGIN;
    filterBmpId[i++] = IDB_BMP_BAILIANG;
    filterBmpId[i++] = IDB_BMP_FN;
    filterBmpId[i++] = IDB_BMP_LSD;
    filterBmpId[i++] = IDB_BMP_NSD;
    filterBmpId[i++] = IDB_BMP_XQX;

    m_vecFilterInfos.push_back(_T("ԭͼ"));
    m_vecFilterInfos.push_back(_T("����"));
    m_vecFilterInfos.push_back(_T("����"));
    m_vecFilterInfos.push_back(_T("С����"));
    m_vecFilterInfos.push_back(_T("��ɫ��"));
    m_vecFilterInfos.push_back(_T("ůɫ��"));


    for (int i = 0; i < 6; ++i) {
        CBitmap cBmp;
        cBmp.LoadBitmap(filterBmpId[i]);
        m_cImageListNormal.Add(&cBmp, RGB(255, 0, 255));
        cBmp.DeleteObject();
    }
    m_lstFilter.SetImageList(&m_cImageListNormal, LVSIL_NORMAL);
    LVITEM lvi;
    lvi.mask = LVIF_IMAGE;
    
    for (int i = 0; i < 6; i++) {
        lvi.iItem = i;
        lvi.iSubItem = 0;
        lvi.pszText = _T("");
        lvi.iImage = i;	
        
        m_lstFilter.InsertItem(&lvi);
       
    }
    m_lstFilter.SetIconSpacing(CSize(110, 110));
    return TRUE;  // return TRUE unless you set the focus to a control
                  // EXCEPTION: OCX Property Pages should return FALSE
}


void CDlgBeautyFilter::OnItemchangedListFilter(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
   
    POSITION pos = m_lstFilter.GetFirstSelectedItemPosition();
    if (!pos)
        return;

    int sel = m_lstFilter.GetNextSelectedItem(pos);
    m_staFilterInfo.SetWindowText(m_vecFilterInfos[sel]);

    Nama::m_curFilterIdx = sel;
    CAgoraObject::GetAgoraObject()->UpdateFilter(TRUE);
  /*  switch (sel) {
    case 0:
        m_staFilterInfo.SetWindowText(_T("ԭͼ"));
        break;
    case 1:
        m_staFilterInfo.SetWindowText(_T("����"));
        break;
    case 2:
        m_staFilterInfo.SetWindowText(_T("����"));
        break;
    case 3:
        m_staFilterInfo.SetWindowText(_T("С����"));
        break;
    case 4:
        m_staFilterInfo.SetWindowText(_T("��ɫ��"));
        break;
    case 5:
        m_staFilterInfo.SetWindowText(_T("ůɫ��"));
        break;
    default:
        break;
    }*/
}
