// CDlgBeautyAR.cpp : implementation file
//

#include "stdafx.h"
#include "AgoraFaceUnityTutorial.h"
#include "CDlgBeautyAR.h"
#include "afxdialogex.h"


// CDlgBeautyAR dialog

IMPLEMENT_DYNAMIC(CDlgBeautyAR, CDialogEx)

CDlgBeautyAR::CDlgBeautyAR(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_DIALOG_BEAUTY_AR, pParent)
{

}

CDlgBeautyAR::~CDlgBeautyAR()
{
}

void CDlgBeautyAR::DoDataExchange(CDataExchange* pDX)
{
    CDialogEx::DoDataExchange(pDX);
    DDX_Control(pDX, IDC_LIST_AR, m_lstAR);
    DDX_Control(pDX, IDC_LIST_AR_ANIMOJI, m_lstAnimoji);
    DDX_Control(pDX, IDC_LIST_AR_AR, m_lstARAR);
    DDX_Control(pDX, IDC_LIST_AR_BG, m_lstARBG);
    DDX_Control(pDX, IDC_LIST_AR_EXPRESSION, m_lstARExpression);
    DDX_Control(pDX, IDC_LIST_AR_GESTURE, m_lstARGesture);
    DDX_Control(pDX, IDC_LIST_AR_HAHA, m_lstARHAHA);
    DDX_Control(pDX, IDC_LIST_AR_MUSIC, m_lstARMusic);
    DDX_Control(pDX, IDC_LIST_AR_PORTRAIT, m_lstPortrait);
    DDX_Control(pDX, IDC_LIST_AR_PROPMAP, m_lstItemSticker);
    DDX_Control(pDX, IDC_STATIC_AR_INFO, m_staARInfo);
}


BEGIN_MESSAGE_MAP(CDlgBeautyAR, CDialogEx)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR, &CDlgBeautyAR::OnItemchangedListAr)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_AR, &CDlgBeautyAR::OnItemchangedListArAr)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_BG, &CDlgBeautyAR::OnLvnItemchangedListArBg)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_EXPRESSION, &CDlgBeautyAR::OnLvnItemchangedListArExpression)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_GESTURE, &CDlgBeautyAR::OnLvnItemchangedListArGesture)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_HAHA, &CDlgBeautyAR::OnLvnItemchangedListArHaha)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_MUSIC, &CDlgBeautyAR::OnLvnItemchangedListArMusic)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_PORTRAIT, &CDlgBeautyAR::OnLvnItemchangedListArPortrait)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_PROPMAP, &CDlgBeautyAR::OnLvnItemchangedListArPropmap)
    ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_AR_ANIMOJI, &CDlgBeautyAR::OnLvnItemchangedListArAnimoji)
END_MESSAGE_MAP()


// CDlgBeautyAR message handlers

#include <vector>
BOOL CDlgBeautyAR::OnInitDialog()
{
    CDialogEx::OnInitDialog();
    InitListCtrls();    
    InitChildLists();
    return TRUE;  // return TRUE unless you set the focus to a control
                  // EXCEPTION: OCX Property Pages should return FALSE
}

void CDlgBeautyAR::HideListCtrl()
{
    m_lstAnimoji.ShowWindow(SW_HIDE);
    m_lstARAR.ShowWindow(SW_HIDE);
    m_lstARBG.ShowWindow(SW_HIDE);
    m_lstARExpression.ShowWindow(SW_HIDE);
    m_lstARGesture.ShowWindow(SW_HIDE);
    m_lstARHAHA.ShowWindow(SW_HIDE);
    m_lstPortrait.ShowWindow(SW_HIDE);
    m_lstARMusic.ShowWindow(SW_HIDE);
    m_lstItemSticker.ShowWindow(SW_HIDE);
    m_vecListCtrls.push_back(&m_lstAnimoji);
    m_vecListCtrls.push_back(&m_lstItemSticker);
    m_vecListCtrls.push_back(&m_lstARAR);

    m_vecListCtrls.push_back(&m_lstARExpression);
    m_vecListCtrls.push_back(&m_lstARMusic);
    m_vecListCtrls.push_back(&m_lstARBG);
    m_vecListCtrls.push_back(&m_lstARGesture);
    m_vecListCtrls.push_back(&m_lstARHAHA);
    m_vecListCtrls.push_back(&m_lstPortrait);

}

void CDlgBeautyAR::InitListCtrls()
{
    HIMAGELIST hList = ImageList_Create(62, 62, ILC_COLOR8 | ILC_MASK, 6, 1);
    m_cImageListNormal.Attach(hList);
    int size = 9;
    int ARBmpId[9] = { 0 };
    int i = 0;
    ARBmpId[i++] = IDB_AR_BMP_ANIMOJI;
    ARBmpId[i++] = IDB_AR_BMP_AR;
    ARBmpId[i++] = IDB_AR_BMP_BG;
    ARBmpId[i++] = IDB_AR_BMP_EXPRESSION;
    ARBmpId[i++] = IDB_AR_BMP_GESTURE;
    ARBmpId[i++] = IDB_AR_BMP_HAHA;
    ARBmpId[i++] = IDB_AR_BMP_MUSIC;
    ARBmpId[i++] = IDB_AR_BMP_PORTRAIT;
    ARBmpId[i++] = IDB_AR_BMP_PROPMAP;


    for (int i = 0; i < size; ++i) {
        CBitmap cBmp;
        cBmp.LoadBitmap(ARBmpId[i]);
        m_cImageListNormal.Add(&cBmp, RGB(255, 255, 255));
        cBmp.DeleteObject();
    }

    std::vector<LPTSTR> vecARs;
    i = 0;
    vecARs.push_back(fuUIAR0);
    vecARs.push_back(fuUIAR1);
    vecARs.push_back(fuUIAR2);
    vecARs.push_back(fuUIAR3);
    vecARs.push_back(fuUIAR4);
    vecARs.push_back(fuUIAR5);
    vecARs.push_back(fuUIAR6);
    vecARs.push_back(fuUIAR7);
    vecARs.push_back(fuUIAR8);

    m_lstAR.SetImageList(&m_cImageListNormal, LVSIL_NORMAL);
    LVITEM lvi;
    lvi.mask = LVIF_IMAGE | LVIF_TEXT;

    for (int i = 0; i < size; i++) {
        lvi.iItem = i;
        lvi.iSubItem = 0;
        lvi.pszText = vecARs[i];//_T("");
        lvi.iImage = i;

        m_lstAR.InsertItem(&lvi);
    }
    m_lstAR.SetIconSpacing(84, 84);

    HideListCtrl();

    CRect rcChild = { 0 }, rcAR = { 0 };
    m_lstAnimoji.GetWindowRect(&rcChild);
    m_lstAR.GetWindowRect(&rcAR);
    CRect rc = { 0,rcChild.top - rcAR.top, rcChild.Width(),rcChild.top - rcAR.top + rcChild.Height() };
    m_lstARAR.MoveWindow(rc);
    m_lstARBG.MoveWindow(rc);
    m_lstARExpression.MoveWindow(rc);
    m_lstARGesture.MoveWindow(rc);
    m_lstARHAHA.MoveWindow(rc);
    m_lstPortrait.MoveWindow(rc);
    m_lstARMusic.MoveWindow(rc);
    m_lstItemSticker.MoveWindow(rc);

    m_vecExpressions.push_back(fuExpMouseOpen);
    m_vecExpressions.push_back(fuExpCheek);
    m_vecExpressions.push_back(fuExpMouse);
    m_vecExpressions.push_back(fuExpFrown);
    m_vecExpressions.push_back(fuExpSmile);
    m_vecExpressions.push_back(fuExpBlow);

    m_vecGestures.push_back(fuExpGetstureTowHands);
    m_vecGestures.push_back(fuExpGetstureHeart);
    m_vecGestures.push_back(fuExpGetstureSix);
    m_vecGestures.push_back(fuExpGetstureThumb);
}

void CDlgBeautyAR::InitChildBmp(int type, int *ARBmpId, int count)
{
    HIMAGELIST hList = ImageList_Create(64, 64, ILC_COLOR8 | ILC_MASK, 6, 1);
    m_cImageListArs[type].Attach(hList);

    for (int i = 0; i < count; ++i) {
        CBitmap cBmp;
        cBmp.LoadBitmap(ARBmpId[i]);
        m_cImageListArs[type].Add(&cBmp, RGB(255, 255, 255));
        cBmp.DeleteObject();
    }

    CListCtrl* pList = m_vecListCtrls[type];
    pList->SetImageList(&m_cImageListArs[type], LVSIL_NORMAL);
    LVITEM lvi;
    lvi.mask = LVIF_IMAGE;
    for (int i = 0; i < count; i++) {
        lvi.iItem = i;
        lvi.iSubItem = 0;
        lvi.pszText = _T("");
        lvi.iImage = i;

        pList->InsertItem(&lvi);
    }
    pList->SetIconSpacing(70, 70);
}

void CDlgBeautyAR::InitChildLists()
{
    //animoji
    {
        int ARBmpId[AR_ANIMOJI_COUNT] = {
            IDB_BMP_ANIMOJI_BAIMAO, IDB_BMP_ANIMOJI_DNQ,
             IDB_BMP_ANIMOJI_FROG, IDB_BMP_ANIMOJI_HSQ,
             IDB_BMP_ANIMOJI_HT, IDB_BMP_ANIMOJI_HY,
             IDB_BMP_ANIMOJI_KLT, IDB_BMP_ANIMOJI_QGIRL
        };
        InitChildBmp(AR_ANIMOJI, ARBmpId, AR_ANIMOJI_COUNT);
    }
    //item sticket
    {
        int ARBmpId[AR_PROMAP_COUNT] = {
            IDB_BMP_PROPMAP_BLING, IDB_BMP_PROPMAP_FENGYA_ZTT_FU,
             IDB_BMP_PROPMAP_HUDIE_LM_FU, IDB_BMP_PROPMAP_JUANHUZI_LM_FU,
             IDB_BMP_PROPMAP_MASK_HAT, IDB_BMP_PROPMAP_TOUHUA_ZTT_FU,
             IDB_BMP_PROPMAP_YAZUI, IDB_BMP_PROPMAP_YUGUAN
        };
        InitChildBmp(AR_PROMAP, ARBmpId, AR_PROMAP_COUNT);
    }

    //AR
    {
        int ARBmpId[AR_AR_COUNT] = {
            IDB_BMP_AR_AFD, IDB_BMP_AR_ARMESH,
             IDB_BMP_AR_BAOZI, IDB_BMP_AR_BLUE_BIRD,
             IDB_BMP_AR_PINKY_BUTTERFLY, IDB_BMP_AR_BLUE_BUTTERFLY,
             IDB_BMP_AR_TIGER, IDB_BMP_AR_TIGER_WHITE,
             IDB_BMP_AR_TIGER_YELLOW, IDB_BMP_AR_PANDA
        };
        InitChildBmp(AR_AR, ARBmpId, AR_AR_COUNT);
    }

    //Expression
    {
        int ARBmpId[AR_EXPRESSION_COUNT] = {
            IDB_BMP_EXP_FUTURE_WARRIOR, IDB_BMP_EXP_JET_MASK,
             IDB_BMP_EXP_KISS, IDB_BMP_EXP_sdx2,
             IDB_BMP_EXP_XIAOBIANZI, IDB_BMP_EXP_XIAOXUESHENG
        };
        InitChildBmp(AR_EXPRESSION, ARBmpId, AR_EXPRESSION_COUNT);
    }

    //Music
    {
        int ARBmpId[AR_MUSIC_COUNT] = {
            IDB_BMP_AR_MUSIC1, IDB_BMP_AR_MUSIC2
        };
        InitChildBmp(AR_MUSIC, ARBmpId, AR_MUSIC_COUNT);
    }

    // background
    {
        int ARBmpId[AR_BG_COUNT] = {
            IDB_BMP_BG_GUFEN_ZH, IDB_BMP_BG_HEZ_ZTT,
            IDB_BMP_BG_ICE_IM, IDB_BMP_BG_SEA_IM,
           IDB_BMP_BG_xiandai_ztt
        };
        InitChildBmp(AR_BG, ARBmpId, AR_BG_COUNT);
    }

    // gesture
    {
        int ARBmpId[AR_GESTURE_COUNT] = {
            IDB_BMP_GESTURE_SSD_THREAD_CUTE, IDB_BMP_GESTURE_SSD_THREAD_KORHEART,
            IDB_BMP_GESTURE_SSD_THREAD_SIX, IDB_BMP_GESTURE_SSD_THREAD_THUMB

        };
        InitChildBmp(AR_GESTURE, ARBmpId, AR_GESTURE_COUNT);
    }

    // haha
    {
        int ARBmpId[AR_HAHA_COUNT] = {
            IDB_BMP_HAHA_FACEWARP, IDB_BMP_HAHA_FACEWARP3,
            IDB_BMP_HAHA_FACEWARP4, IDB_BMP_HAHA_FACEWARP5,
            IDB_BMP_HAHA_FACEWARP6

        };
        InitChildBmp(AR_HAHA, ARBmpId, AR_HAHA_COUNT);
    }
    // portrait
    {
        int ARBmpId[AR_PORTRAIT_COUNT] = {
            IDB_BMP_PICASSO_E1, IDB_BMP_PICASSO_E2,
            IDB_BMP_PICASSO_E3

        };
        InitChildBmp(AR_PORTRAIT, ARBmpId, AR_PORTRAIT_COUNT);
    }
}

void CDlgBeautyAR::OnItemchangedListAr(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    POSITION pos = m_lstAR.GetFirstSelectedItemPosition();

    if (!pos)
        return;

    int item = m_lstAR.GetNextSelectedItem(pos);
    HideListCtrl();
    m_vecListCtrls[item]->ShowWindow(SW_SHOW);

    if (item >= ChangeFace) {
        item += 1;
    }

    Nama::bundleCategory = item;
}


void CDlgBeautyAR::OnItemchangedListArAr(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    UpdateFaceUnityBundle(AR_AR);
}


void CDlgBeautyAR::OnLvnItemchangedListArBg(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    UpdateFaceUnityBundle(AR_BG);
}


void CDlgBeautyAR::OnLvnItemchangedListArExpression(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    UpdateFaceUnityBundle(AR_EXPRESSION);
}


void CDlgBeautyAR::OnLvnItemchangedListArGesture(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    UpdateFaceUnityBundle(AR_GESTURE);
}


void CDlgBeautyAR::OnLvnItemchangedListArHaha(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    UpdateFaceUnityBundle(AR_HAHA);
}


void CDlgBeautyAR::OnLvnItemchangedListArMusic(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    UpdateFaceUnityBundle(AR_MUSIC);
}


void CDlgBeautyAR::OnLvnItemchangedListArPortrait(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;

    UpdateFaceUnityBundle(AR_PORTRAIT);
}


void CDlgBeautyAR::OnLvnItemchangedListArPropmap(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;

    UpdateFaceUnityBundle(AR_PROMAP);
}


void CDlgBeautyAR::OnLvnItemchangedListArAnimoji(NMHDR *pNMHDR, LRESULT *pResult)
{
    LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
    // TODO: Add your control notification handler code here
    *pResult = 0;
    
    UpdateFaceUnityBundle(AR_ANIMOJI);
}

void CDlgBeautyAR::UpdateFaceUnityBundle(int type)
{
    CListCtrl* pList = m_vecListCtrls[type];
    POSITION pos = pList->GetFirstSelectedItemPosition();

    if (!pos)
        return;

    int item = pList->GetNextSelectedItem(pos);

    if (item < 0) {
        return;
    }

    if (type == AR_EXPRESSION) {
        m_staARInfo.SetWindowText(m_vecExpressions[item]);
    }
    else if (type == AR_GESTURE) {
        m_staARInfo.SetWindowText(m_vecGestures[item]);
    }

    std::string itemName = Nama::categoryBundles[Nama::bundleCategory][item];
    int bundle_index = type;
    if (bundle_index >= ChangeFace)
        bundle_index += 1;
    CAgoraObject::GetAgoraObject()->UpdateBundle(g_fuDataDir + Nama::gBundlePath[bundle_index] + itemName);
}