// CDlgBeautyMakeup.cpp : implementation file
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "CDlgBeautyMakeup.h"
#include "afxdialogex.h"


// CDlgBeautyMakeup dialog

IMPLEMENT_DYNAMIC(CDlgBeautyMakeup, CDialogEx)

CDlgBeautyMakeup::CDlgBeautyMakeup(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DIALOG_BEAUTY_MAKEUP, pParent)
{

}

CDlgBeautyMakeup::~CDlgBeautyMakeup()
{
}

void CDlgBeautyMakeup::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_LIST_MAKEUP, m_lstMakeup);
    DDX_Control(pDX, IDC_STATIC_MAKEUP_INFO, m_staMakupInfo);
    DDX_Control(pDX, IDC_STATIC_MAKEUP_ERROR, m_staMakeupErr);
}


BEGIN_MESSAGE_MAP(CDlgBeautyMakeup, CDialogEx)
  
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_MAKEUP, &CDlgBeautyMakeup::OnItemchangedListMakeup)
END_MESSAGE_MAP()


// CDlgBeautyMakeup message handlers


BOOL CDlgBeautyMakeup::OnInitDialog()
{
    CDialogEx::OnInitDialog();

    // TODO:  Add extra initialization here
    HIMAGELIST hList = ImageList_Create(56, 56, ILC_COLOR8 | ILC_MASK, 6, 1);
    m_cMakeupImageListNormal.Attach(hList);

    
    int size = 9;
    int MakeupBmpId[9] = { 0 };
    int i = 0;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_CHARMING;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_FLOWER;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_LADY;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_MOON;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_NEIGHBOUR;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_OCCIDENT;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_SEXY;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_SWEET;
    MakeupBmpId[i++] = IDB_BMP_MAKEUP_TOUGH_GUY;


    for (int i = 0; i < size; ++i) {
        CBitmap cBmp;
        cBmp.LoadBitmap(MakeupBmpId[i]);
        m_cMakeupImageListNormal.Add(&cBmp, RGB(255, 0, 255));
        cBmp.DeleteObject();
    }

    m_vecMakeupNames.push_back(fuUIMuCh);
    m_vecMakeupNames.push_back(fuUIMuFl);
    m_vecMakeupNames.push_back(fuUIMuLa);
    m_vecMakeupNames.push_back(fuUIMuMo);
    m_vecMakeupNames.push_back(fuUIMuNe);
    m_vecMakeupNames.push_back(fuUIMuOc);
    m_vecMakeupNames.push_back(fuUIMuSe);
    m_vecMakeupNames.push_back(fuUIMuSw);
    m_vecMakeupNames.push_back(fuUIMuTo);
 

    m_lstMakeup.SetImageList(&m_cMakeupImageListNormal, LVSIL_NORMAL);
    LVITEM lvi;
    lvi.mask = LVIF_IMAGE|LVIF_TEXT;

    for (int i = 0; i < size; i++) {
        lvi.iItem = i;
        lvi.iSubItem = 0;
        lvi.pszText = m_vecMakeupNames[i];
        lvi.iImage = i;

        m_lstMakeup.InsertItem(&lvi);
    }
    m_lstMakeup.SetIconSpacing(90, 90);
    return TRUE;  // return TRUE unless you set the focus to a control
                  // EXCEPTION: OCX Property Pages should return FALSE
}

void CDlgBeautyMakeup::OnItemchangedListMakeup(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;

    POSITION pos = m_lstMakeup.GetFirstSelectedItemPosition();

    if (!pos)
        return;

    int item = m_lstMakeup.GetNextSelectedItem(pos);
    if (item < 0)
        return;

   // m_staMakupInfo.SetWindowText(m_vecMakeupNames[item]); 
    if (Nama::bundleCategory < 0)
        return;
 
    std::string itemName = Nama::categoryBundles[Nama::bundleCategory][item];

    CAgoraObject::GetAgoraObject()->UpdateBundle(itemName);
}
