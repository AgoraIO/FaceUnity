#include "StdAfx.h"
#include "AgoraObject.h"
#include <stdio.h>



//////////////////////////////
//Agora IVideoFrameobserver///
//////////////////////////////


AgoraVideoFrameObserver::AgoraVideoFrameObserver()
{
    m_fauceFrameBuffer = new uint8_t[4 * 1920 * 1080];
}

AgoraVideoFrameObserver::~AgoraVideoFrameObserver()
{
    if (m_fauceFrameBuffer) {
        delete[] m_fauceFrameBuffer;
        m_fauceFrameBuffer = nullptr;
    }
}

bool AgoraVideoFrameObserver::onCaptureVideoFrame(VideoFrame& videoFrame)
{
    if (!m_InitNama) {
        uint32_t width = videoFrame.width;
        uint32_t height = videoFrame.height;
        m_nama =  Nama::create(width, height);
        if (!Nama::initFuError.empty()) {
            ::SendMessage(m_msgHwnd, WM_FU_MSGID(EID_FU_INIT_ERROR), 0, 0);
        }
        m_InitNama = true;
        m_nama->UpdateBeauty();
    }

    if (m_needUpdateBeauty) {
        m_nama->UpdateBeauty();
        m_needUpdateBeauty = false;
    }

    if (m_needUpdateFilter) {
        m_nama->UpdateFilter(Nama::m_curFilterIdx);
        m_nama->UpdateBeauty();
        m_needUpdateFilter = false;
    }

    if (m_needUpdateBundle && !m_updateBundlename.empty()) {
        if (!m_nama->SelectBundle(m_updateBundlename)) {//select bundle failed
            ::SendMessage(m_msgHwnd, WM_FU_MSGID(EID_FU_BUNDLE_ERROR), 0, 0);
        }
        m_needUpdateBundle = false;
        m_updateBundlename = "";
    }

    int size = videoFrame.width*videoFrame.height;
    memcpy_s(m_fauceFrameBuffer, size, videoFrame.yBuffer, size);
    memcpy_s(m_fauceFrameBuffer + size, size / 4, videoFrame.uBuffer, size / 4);
    memcpy_s(m_fauceFrameBuffer + size * 5 / 4, size / 4, videoFrame.vBuffer, size / 4);
    m_nama->RenderItems(m_fauceFrameBuffer, 13);

    memcpy_s(videoFrame.yBuffer, size, m_fauceFrameBuffer, size);
    memcpy_s(videoFrame.uBuffer, size / 4, m_fauceFrameBuffer + size, size / 4);
    memcpy_s(videoFrame.vBuffer, size / 4, m_fauceFrameBuffer + size * 5 / 4, size / 4);
    return true;
}

bool AgoraVideoFrameObserver::onPreEncodeVideoFrame(VideoFrame& videoFrame)
{
    return true;
}

bool AgoraVideoFrameObserver::onRenderVideoFrame(unsigned int uid, VideoFrame& videoFrame)
{
    return true;
}


/////////////////////////////////////////////////////////////////////////////////////////////////

CAgoraObject *CAgoraObject::m_lpAgoraObject = NULL;
IRtcEngine	*CAgoraObject::m_lpAgoraEngine = NULL;
CAGEngineEventHandler CAgoraObject::m_EngineEventHandler;
CString   CAgoraObject::m_strAppID;
AVideoDeviceManager* CAgoraObject::m_pVideoDeviceManager;

std::mutex mtxNma;

CAgoraObject::CAgoraObject(void)
	: m_dwEngineFlag(0)
	, m_bVideoEnable(FALSE)
	, m_bAudioEnable(TRUE)
	, m_bLocalAudioMuted(FALSE)
	, m_bScreenCapture(FALSE)
	, m_nSelfUID(0)
	, m_nRoleType(0)
	, m_nChannelProfile(0)
	, m_nRcdVol(0)
	, m_nPlaybackVol(0)
	, m_nMixVol(0)
	, m_bFullBand(FALSE)
	, m_bStereo(FALSE)
	, m_bFullBitrate(FALSE)
	, m_bLoopBack(FALSE)
{
	m_strChannelName.Empty();
	m_bLocalVideoMuted = FALSE;

	m_bAllRemoteAudioMuted = FALSE;
	m_bAllRemoteVideoMuted = FALSE;

	m_nCanvasWidth = 640;
	m_nCanvasHeight = 360;
}

CAgoraObject::~CAgoraObject(void)
{
}

CString CAgoraObject::GetSDKVersion()
{
	int nBuildNumber = 0;
	const char *lpszEngineVer = getAgoraRtcEngineVersion(&nBuildNumber);

	CString strEngineVer;

#ifdef UNICODE
	::MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, lpszEngineVer, -1, strEngineVer.GetBuffer(256), 256);
	strEngineVer.ReleaseBuffer();
#else
	strEngineVer = lpszEngineVer;
#endif

	return strEngineVer;
}

CString CAgoraObject::GetSDKVersionEx()
{
	int nBuildNumber = 0;
	const char *lpszEngineVer = getAgoraRtcEngineVersion(&nBuildNumber);

	CString strEngineVer;
	CString strVerEx;
	SYSTEMTIME sysTime;

#ifdef UNICODE
	::MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, lpszEngineVer, -1, strEngineVer.GetBuffer(256), 256);
	strEngineVer.ReleaseBuffer();
#else
	strEngineVer = lpszEngineVer;
#endif

	::GetLocalTime(&sysTime);
	strVerEx.Format(_T("V%s, Build%d, %d/%d/%d, V%s"), strEngineVer, nBuildNumber, sysTime.wYear, sysTime.wMonth, sysTime.wDay, strEngineVer);
	
	return strVerEx;
}

IRtcEngine *CAgoraObject::GetEngine()
{
	if(m_lpAgoraEngine == NULL)
		m_lpAgoraEngine = createAgoraRtcEngine();

	return m_lpAgoraEngine;
}

CAgoraObject *CAgoraObject::GetAgoraObject(LPCTSTR lpVendorKey)
{
    if (m_lpAgoraObject == NULL)
        m_lpAgoraObject = new CAgoraObject();

    if (m_lpAgoraEngine == NULL)
        m_lpAgoraEngine = createAgoraRtcEngine();

    if (lpVendorKey == NULL)
        return m_lpAgoraObject;

    RtcEngineContext ctx;

    ctx.eventHandler = &m_EngineEventHandler;

#ifdef UNICODE
    char szVendorKey[128];

    ::WideCharToMultiByte(CP_ACP, 0, lpVendorKey, -1, szVendorKey, 128, NULL, NULL);
    ctx.appId = szVendorKey;
#else
    ctx.appId = lpVendorKey;
#endif

    m_lpAgoraEngine->initialize(ctx);
    if (lpVendorKey != NULL)
        m_strAppID = lpVendorKey;

    m_pVideoDeviceManager = new AVideoDeviceManager(m_lpAgoraEngine);

    return m_lpAgoraObject;
}

void CAgoraObject::CloseAgoraObject()
{
    if (m_lpAgoraEngine != NULL)
        m_lpAgoraEngine->release();

    if (m_lpAgoraObject != NULL)
        delete m_lpAgoraObject;

    m_lpAgoraEngine = NULL;
    m_lpAgoraObject = NULL;
}

void CAgoraObject::SetMsgHandlerWnd(HWND hWnd)
{
    m_EngineEventHandler.SetMsgReceiver(hWnd);
}

HWND CAgoraObject::GetMsgHandlerWnd()
{
    return m_EngineEventHandler.GetMsgReceiver();
}


void CAgoraObject::SetNetworkTestFlag(BOOL bEnable)
{
    if (bEnable)
        m_dwEngineFlag |= AG_ENGFLAG_ENNETTEST;
    else
        m_dwEngineFlag &= (~AG_ENGFLAG_ENNETTEST);
}

BOOL CAgoraObject::GetNetworkTestFlag()
{
    return (m_dwEngineFlag & AG_ENGFLAG_ENNETTEST) != 0;
}

void CAgoraObject::SetEchoTestFlag(BOOL bEnable)
{
    if (bEnable)
        m_dwEngineFlag |= AG_ENGFLAG_ECHOTEST;
    else
        m_dwEngineFlag &= (~AG_ENGFLAG_ECHOTEST);
}

BOOL CAgoraObject::GetEchoTestFlag()
{
    return (m_dwEngineFlag & AG_ENGFLAG_ECHOTEST) != 0;
}

void CAgoraObject::SetSpeakerphoneTestFlag(BOOL bEnable)
{
    if (bEnable)
        m_dwEngineFlag |= AG_ENGFLAG_SPKPHTEST;
    else
        m_dwEngineFlag &= (~AG_ENGFLAG_SPKPHTEST);
}

BOOL CAgoraObject::GetSpeakerphoneTestFlag()
{
	return (m_dwEngineFlag & AG_ENGFLAG_SPKPHTEST) != 0;
}

void CAgoraObject::SetMicrophoneTestFlag(BOOL bEnable)
{
	if(bEnable)
		m_dwEngineFlag |= AG_ENGFLAG_MICPHTEST;
	else
		m_dwEngineFlag &= (~AG_ENGFLAG_MICPHTEST);
}

BOOL CAgoraObject::GetMicrophoneTestFlag()
{
	return (m_dwEngineFlag & AG_ENGFLAG_MICPHTEST) != 0;
}


void CAgoraObject::SetVideoTestFlag(BOOL bEnable)
{
	if (bEnable)
		m_dwEngineFlag |= AG_ENGFLAG_VIDEOTEST;
	else
		m_dwEngineFlag &= (~AG_ENGFLAG_VIDEOTEST);
}

BOOL CAgoraObject::GetVideoTestFlag()
{
	return (m_dwEngineFlag & AG_ENGFLAG_VIDEOTEST) != 0;
}

BOOL CAgoraObject::SetLogFilePath(LPCTSTR lpLogPath)
{
	ASSERT(m_lpAgoraEngine != NULL);

	CHAR szLogPathA[MAX_PATH];
	CHAR szLogPathTrans[MAX_PATH];

	int ret = 0;
	RtcEngineParameters rep(*m_lpAgoraEngine);

	if (::GetFileAttributes(lpLogPath) == INVALID_FILE_ATTRIBUTES) {
		::GetModuleFileNameA(NULL, szLogPathA, MAX_PATH);
		LPSTR lpLastSlash = strrchr(szLogPathA, '\\')+1;
		strcpy_s(lpLastSlash, 64, "AgoraSDK.log");
	}
	else {
#ifdef UNICODE
		::WideCharToMultiByte(CP_UTF8, 0, lpLogPath, -1, szLogPathA, MAX_PATH, NULL, NULL);
#else
		::MultiByteToWideChar(CP_UTF8, 0, lpLogPath, -1, (WCHAR *)szLogPathA, MAX_PATH, NULL, NULL);
#endif
	}

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::SetVideoProfile2(int nWidth, int nHeight, int nFrameRate, int nBitRate, BOOL bFineTurn)
{
//	int nRet = m_lpAgoraEngine->setVideoProfile2(nWidth, nHeight, nFrameRate, nBitRate, bFineTurn);
	
//	return nRet == 0 ? TRUE : FALSE;

	return TRUE;
}

BOOL CAgoraObject::JoinChannel(LPCTSTR lpChannelName, UINT nUID, LPCSTR lpDynamicKey)
{
    int nRet = 0;

    LPCSTR lpStreamInfo = "{\"owner\":true,\"width\":640,\"height\":480,\"bitrate\":500}";
#ifdef UNICODE
    CHAR szChannelName[128];

    ::WideCharToMultiByte(CP_ACP, 0, lpChannelName, -1, szChannelName, 128, NULL, NULL);
    nRet = m_lpAgoraEngine->joinChannel(lpDynamicKey, szChannelName, lpStreamInfo, nUID);
#else
    nRet = m_lpAgoraEngine->joinChannel(lpDynamicKey, lpChannelName, lpStreamInfo, nUID);
#endif

    if (nRet == 0)
        m_strChannelName = lpChannelName;
    bJoinedChannel = true;
    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::LeaveCahnnel()
{
    m_lpAgoraEngine->stopPreview();
    int nRet = m_lpAgoraEngine->leaveChannel();

    m_nSelfUID = 0;
    bJoinedChannel = false;
    return nRet == 0 ? TRUE : FALSE;
}

CString CAgoraObject::GetChanelName()
{
    return m_strChannelName;
}

CString CAgoraObject::GetCallID()
{
    agora::util::AString uid;
    CString strUID;

    m_lpAgoraEngine->getCallId(uid);

#ifdef UNICODE
    ::MultiByteToWideChar(CP_ACP, 0, uid->c_str(), -1, strUID.GetBuffer(128), 128);
    strUID.ReleaseBuffer();
#else
    strUID = uid->c_str();
#endif

    return strUID;
}

BOOL CAgoraObject::EnableVideo(BOOL bEnable)
{
    int nRet = 0;

    if (bEnable)
        nRet = m_lpAgoraEngine->enableVideo();
    else
        nRet = m_lpAgoraEngine->disableVideo();

    if (nRet == 0)
        m_bVideoEnable = bEnable;

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsVideoEnabled()
{
	return m_bVideoEnable;
}
/*
BOOL CAgoraObject::EnableScreenCapture(HWND hWnd, int nCapFPS, LPCRECT lpCapRect, BOOL bEnable, int nBitrate)
{
	ASSERT(m_lpAgoraEngine != NULL);

	int ret = 0;
	RtcEngineParameters rep(*m_lpAgoraEngine);

	Rect rcCap;

	if (bEnable) {
		if (lpCapRect == NULL)
			ret = m_lpAgoraEngine->startScreenCapture(hWnd, nCapFPS, NULL, nBitrate);
		else {
			rcCap.left = lpCapRect->left;
			rcCap.right = lpCapRect->right;
			rcCap.top = lpCapRect->top;
			rcCap.bottom = lpCapRect->bottom;

			ret = m_lpAgoraEngine->startScreenCapture(hWnd, nCapFPS, &rcCap, nBitrate);
		}
	}
	else
		ret = m_lpAgoraEngine->stopScreenCapture();

	if (ret == 0)
		m_bScreenCapture = bEnable;

	return ret == 0 ? TRUE : FALSE;
}*/


BOOL CAgoraObject::EnableScreenCapture(HWND hWnd, int nCapFPS, LPCRECT lpCapRect, BOOL bEnable, int nBitrate)
{
	ASSERT(m_lpAgoraEngine != NULL);

	int ret = 0;
	RtcEngineParameters rep(*m_lpAgoraEngine);

	agora::rtc::Rectangle rcCap;
	ScreenCaptureParameters capParam;
	capParam.bitrate = nBitrate;
	capParam.frameRate = nCapFPS;

	if (bEnable) {
		if (lpCapRect == NULL){
			RECT rc;
			if (hWnd){
				::GetWindowRect(hWnd, &rc);
				capParam.dimensions.width = rc.right - rc.left;
				capParam.dimensions.height = rc.bottom - rc.top;
				ret = m_lpAgoraEngine->startScreenCaptureByWindowId(hWnd, rcCap, capParam);
			}
			else{
				GetWindowRect(GetDesktopWindow(), &rc);
				agora::rtc::Rectangle screenRegion = { rc.left, rc.right, rc.right - rc.left, rc.bottom - rc.top };
				capParam.dimensions.width = rc.right - rc.left;
				capParam.dimensions.height = rc.bottom - rc.top;
				ret = m_lpAgoraEngine->startScreenCaptureByScreenRect(screenRegion, rcCap, capParam);
			}
			//startScreenCapture(hWnd, nCapFPS, NULL, nBitrate);
		}
		else {
			capParam.dimensions.width = lpCapRect->right - lpCapRect->left;
			capParam.dimensions.height = lpCapRect->bottom - lpCapRect->top;

			rcCap.x = lpCapRect->left;
			rcCap.y = lpCapRect->top;
			rcCap.width = lpCapRect->right - lpCapRect->left;
			rcCap.height = lpCapRect->bottom - lpCapRect->top;

            if (hWnd)
                ret = m_lpAgoraEngine->startScreenCaptureByWindowId(hWnd, rcCap, capParam);
            else {

                agora::rtc::Rectangle screenRegion = rcCap;
                ret = m_lpAgoraEngine->startScreenCaptureByScreenRect(screenRegion, rcCap, capParam);
            }
		}
	}
	else
		ret = m_lpAgoraEngine->stopScreenCapture();

	if (ret == 0)
		m_bScreenCapture = bEnable;

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsScreenCaptureEnabled()
{
	return m_bScreenCapture;
}

BOOL CAgoraObject::MuteLocalAudio(BOOL bMuted)
{
	ASSERT(m_lpAgoraEngine != NULL);

	RtcEngineParameters rep(*m_lpAgoraEngine);

	int ret = rep.muteLocalAudioStream((bool)bMuted);
	if (ret == 0)
		m_bLocalAudioMuted = bMuted;

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsLocalAudioMuted()
{
	return m_bLocalAudioMuted;
}

BOOL CAgoraObject::MuteAllRemoteAudio(BOOL bMuted)
{
	ASSERT(m_lpAgoraEngine != NULL);

	RtcEngineParameters rep(*m_lpAgoraEngine);

	int ret = rep.muteAllRemoteAudioStreams((bool)bMuted);
	if (ret == 0)
		m_bAllRemoteAudioMuted = bMuted;

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsAllRemoteAudioMuted()
{
	return m_bAllRemoteAudioMuted;
}

BOOL CAgoraObject::MuteLocalVideo(BOOL bMuted)
{
	ASSERT(m_lpAgoraEngine != NULL);

	RtcEngineParameters rep(*m_lpAgoraEngine);

	int ret = rep.muteLocalVideoStream((bool)bMuted);
	if (ret == 0)
		m_bLocalVideoMuted = bMuted;

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsLocalVideoMuted()
{
	return m_bLocalVideoMuted;
}

BOOL CAgoraObject::MuteAllRemoteVideo(BOOL bMuted)
{
	ASSERT(m_lpAgoraEngine != NULL);

	RtcEngineParameters rep(*m_lpAgoraEngine);

	int ret = rep.muteAllRemoteVideoStreams((bool)bMuted);
	if (ret == 0)
		m_bAllRemoteVideoMuted = bMuted;

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsAllRemoteVideoMuted()
{
	return m_bAllRemoteVideoMuted;
}

BOOL CAgoraObject::EnableLoopBack(BOOL bEnable)
{
	int nRet = 0;

	AParameter apm(*m_lpAgoraEngine);

	if (bEnable)
		nRet = apm->setParameters("{\"che.audio.loopback.recording\":true}");
	else
		nRet = apm->setParameters("{\"che.audio.loopback.recording\":false}");

	m_bLoopBack = bEnable;

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsLoopBackEnabled()
{
	return m_bLoopBack;
}

BOOL CAgoraObject::SetChannelProfile(BOOL bBroadcastMode)
{
	int nRet = 0;

	if (!bBroadcastMode){
		m_nChannelProfile = CHANNEL_PROFILE_COMMUNICATION;
		nRet = m_lpAgoraEngine->setChannelProfile(CHANNEL_PROFILE_COMMUNICATION);
	}
	else {
		m_nChannelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
		nRet = m_lpAgoraEngine->setChannelProfile(CHANNEL_PROFILE_LIVE_BROADCASTING);
	}

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsBroadcastMode()
{
	return m_nChannelProfile == CHANNEL_PROFILE_LIVE_BROADCASTING ? TRUE : FALSE;
}

void CAgoraObject::SetWantedRole(CLIENT_ROLE_TYPE role)
{
	m_nWantRoleType = role;
}

BOOL CAgoraObject::SetClientRole(CLIENT_ROLE_TYPE role, LPCSTR lpPermissionKey)
{
	int nRet = m_lpAgoraEngine->setClientRole(role);

	m_nRoleType = role;

	return nRet == 0 ? TRUE : FALSE;
}


BOOL CAgoraObject::EnableAudioRecording(BOOL bEnable, LPCTSTR lpFilePath)
{
	int ret = 0;

	RtcEngineParameters rep(*m_lpAgoraEngine);

	if (bEnable) {
#ifdef UNICODE
		CHAR szFilePath[MAX_PATH];
		::WideCharToMultiByte(CP_ACP, 0, lpFilePath, -1, szFilePath, MAX_PATH, NULL, NULL);
		ret = rep.startAudioRecording(szFilePath, AUDIO_RECORDING_QUALITY_HIGH);
#else
		ret = rep.startAudioRecording(lpFilePath);
#endif
	}
	else
		ret = rep.stopAudioRecording();

	return ret == 0 ? TRUE : FALSE;
}


BOOL CAgoraObject::EnableLastmileTest(BOOL bEnable)
{
	int ret = 0;

	if (bEnable)
		ret = m_lpAgoraEngine->enableLastmileTest();
	else
		ret = m_lpAgoraEngine->enableLastmileTest();

	return ret == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::LocalVideoPreview(HWND hVideoWnd, BOOL bPreviewOn)
{
	int nRet = 0;

	if (bPreviewOn) {
		VideoCanvas vc;

		vc.uid = 0;
		vc.view = hVideoWnd;
		vc.renderMode = RENDER_MODE_TYPE::RENDER_MODE_HIDDEN;

		m_lpAgoraEngine->setupLocalVideo(vc);
		nRet = m_lpAgoraEngine->startPreview();
	}
	else
		nRet = m_lpAgoraEngine->stopPreview();

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::SetLogFilter(UINT logFilterType, LPCTSTR lpLogPath)
{
    int nRet = 0;
    RtcEngineParameters rep(*m_lpAgoraEngine);

    nRet = rep.setLogFilter(logFilterType);

    if (lpLogPath != NULL) {
#ifdef UNICODE
        CHAR szFilePath[MAX_PATH];
        ::WideCharToMultiByte(CP_ACP, 0, lpLogPath, -1, szFilePath, MAX_PATH, NULL, NULL);
        nRet |= rep.setLogFile(szFilePath);
#else
        nRet |= rep.setLogFile(lpLogPath);
#endif
    }

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::SetEncryptionSecret(LPCTSTR lpKey, int nEncryptType)
{
    CHAR szUTF8[MAX_PATH];

#ifdef UNICODE
    ::WideCharToMultiByte(CP_UTF8, 0, lpKey, -1, szUTF8, MAX_PATH, NULL, NULL);
#else
    WCHAR szAnsi[MAX_PATH];
    ::MultiByteToWideChar(CP_ACP, 0, lpKey, -1, szAnsi, MAX_PATH);
    ::WideCharToMultiByte(CP_UTF8, 0, szAnsi, -1, szUTF8, MAX_PATH, NULL, NULL);
#endif
    switch (nEncryptType)
    {
    case 0:
        m_lpAgoraEngine->setEncryptionMode("aes-128-xts");
        break;
    case 1:
        m_lpAgoraEngine->setEncryptionMode("aes-256-xts");
        break;
    default:
        m_lpAgoraEngine->setEncryptionMode("aes-128-xts");
        break;
    }
    int nRet = m_lpAgoraEngine->setEncryptionSecret(szUTF8);

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::EnableLocalRender(BOOL bEnable)
{
	int nRet = 0;

	AParameter apm(*m_lpAgoraEngine);

	if (bEnable)
		nRet = apm->setParameters("{\"che.video.local.render\":true}");
	else
		nRet = apm->setParameters("{\"che.video.local.render\":false}");

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::EnableWebSdkInteroperability(BOOL bEnable)
{
	RtcEngineParameters rep(*m_lpAgoraEngine);

	int	nRet = rep.enableWebSdkInteroperability(bEnable);

	return nRet == 0 ? TRUE : FALSE;
}

int CAgoraObject::CreateMessageStream()
{
    int nDataStream = 0;
    m_lpAgoraEngine->createDataStream(&nDataStream, true, true);

    return nDataStream;
}

BOOL CAgoraObject::SendChatMessage(int nStreamID, LPCTSTR lpChatMessage)
{
    _ASSERT(nStreamID != 0);
    int nMessageLen = _tcslen(lpChatMessage);
    _ASSERT(nMessageLen < 128);

    CHAR szUTF8[256];

#ifdef UNICODE
    int nUTF8Len = ::WideCharToMultiByte(CP_UTF8, 0, lpChatMessage, nMessageLen, szUTF8, 256, NULL, NULL);
#else
    int nUTF8Len = ::MultiByteToWideChar(CP_UTF8, lpChatMessage, nMessageLen, szUTF8, 256);
#endif

    int nRet = m_lpAgoraEngine->sendStreamMessage(nStreamID, szUTF8, nUTF8Len);

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::SetHighQualityAudioPreferences(BOOL bFullBand, BOOL bStereo, BOOL bFullBitrate)
{
	int nRet = 0;
	RtcEngineParameters rep(*m_lpAgoraEngine);

	nRet = rep.setHighQualityAudioParameters(bFullBand, bStereo, bFullBitrate);

	m_bFullBand = bFullBand;
	m_bStereo = bStereo;
	m_bFullBitrate = bFullBitrate;

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::StartAudioMixing(LPCTSTR lpMusicPath, BOOL bLoopback, BOOL bReplace, int nCycle)
{
	int nRet = 0;
	RtcEngineParameters rep(*m_lpAgoraEngine);

#ifdef UNICODE
	CHAR szFilePath[MAX_PATH];
	::WideCharToMultiByte(CP_UTF8, 0, lpMusicPath, -1, szFilePath, MAX_PATH, NULL, NULL);
	nRet = rep.startAudioMixing(szFilePath, bLoopback, bReplace, nCycle);
#else
	nRet = rep.startAudioMixing(lpMusicPath, bLoopback, bReplace, nCycle);
#endif

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::StopAudioMixing()
{
	int nRet = 0;
	RtcEngineParameters rep(*m_lpAgoraEngine);

	nRet = rep.stopAudioMixing();

	return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::PauseAudioMixing()
{
    int nRet = 0;
    RtcEngineParameters rep(*m_lpAgoraEngine);

    nRet = rep.pauseAudioMixing();

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::ResumeAudioMixing()
{
    int nRet = 0;
    RtcEngineParameters rep(*m_lpAgoraEngine);

    nRet = rep.resumeAudioMixing();

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::EnableAudio(BOOL bEnable)
{
    int nRet = 0;

    if (bEnable)
        nRet = m_lpAgoraEngine->enableAudio();
    else
        nRet = m_lpAgoraEngine->disableAudio();

    m_bAudioEnable = bEnable;

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::IsAudioEnabled()
{
	return m_bAudioEnable;
}

void CAgoraObject::SetSEIInfo(UINT nUID, LPSEI_INFO lpSEIInfo)
{
    SEI_INFO seiInfo;

    ASSERT(nUID != 0xcccccccc);
    memset(&seiInfo, 0, sizeof(SEI_INFO));

    seiInfo.nUID = nUID;
    if (lpSEIInfo != NULL)
        memcpy(&seiInfo, lpSEIInfo, sizeof(SEI_INFO));

    m_mapSEIInfo.SetAt(nUID, seiInfo);
}

void CAgoraObject::RemoveSEIInfo(UINT nUID)
{
	m_mapSEIInfo.RemoveKey(nUID);
}

void CAgoraObject::RemoveAllSEIInfo()
{
	m_mapSEIInfo.RemoveAll();
}

BOOL CAgoraObject::GetSEIInfo(UINT nUID, LPSEI_INFO lpSEIInfo)
{
	SEI_INFO seiInfo;

	if (!m_mapSEIInfo.Lookup(nUID, seiInfo))
		return FALSE;

	memcpy(lpSEIInfo, &seiInfo, sizeof(SEI_INFO));

	return TRUE;
}

BOOL CAgoraObject::GetSEIInfoByIndex(int nIndex, LPSEI_INFO lpSEIInfo)
{
    int		nCounter = 0;

    if (nIndex < 0 || nIndex >= m_mapSEIInfo.GetCount())
        return FALSE;

    POSITION pos = m_mapSEIInfo.GetStartPosition();
    while (pos != NULL && nCounter < nIndex) {
        m_mapSEIInfo.GetNext(pos);
        nCounter++;
    }

    *lpSEIInfo = m_mapSEIInfo.GetValueAt(pos);

    return TRUE;
}

BOOL CAgoraObject::EnableSEIPush(BOOL bEnable, COLORREF crBack)
{
    LiveTranscoding lts;
    lts.audioBitrate = 48;
    lts.audioChannels = 2;
    lts.audioCodecProfile = AUDIO_CODEC_PROFILE_HE_AAC;
    lts.audioSampleRate = AUDIO_SAMPLE_RATE_TYPE::AUDIO_SAMPLE_RATE_48000;
    lts.backgroundColor = crBack;
    lts.backgroundImage = NULL;
    lts.height = m_nCanvasHeight;
    lts.width = m_nCanvasWidth;
    lts.lowLatency = false;
    int nVideoCount = m_mapSEIInfo.GetCount();
    if (nVideoCount <= 0)
        return FALSE;

    TranscodingUser *pTsu = new TranscodingUser[nVideoCount];
    POSITION pos = m_mapSEIInfo.GetStartPosition();
    int nIndex = 0;
    while (pos != NULL) {
        SEI_INFO &seiInfo = m_mapSEIInfo.GetNextValue(pos);

        pTsu[nIndex].height = seiInfo.nHeight < m_nCanvasHeight ? (seiInfo.nHeight *1.0) / m_nCanvasHeight : 1;
        pTsu[nIndex].width = seiInfo.nWidth < m_nCanvasWidth ? seiInfo.nWidth*1.0 / m_nCanvasWidth : 1;
        pTsu[nIndex].uid = seiInfo.nUID;

        pTsu[nIndex].x = seiInfo.x < m_nCanvasWidth ? seiInfo.x*1.0 / m_nCanvasWidth : 1;
        pTsu[nIndex].y = seiInfo.y < m_nCanvasHeight ? seiInfo.y*1.0 / m_nCanvasHeight : 1;
        pTsu[nIndex].zOrder = seiInfo.nIndex;
        pTsu[nIndex].alpha = 1;
        nIndex++;
    }

    lts.transcodingUsers = pTsu;
    int nRet = m_lpAgoraEngine->setLiveTranscoding(lts);

    delete[] pTsu;
    pTsu = NULL;

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::EnableH264Compatible()
{
    AParameter apm(*m_lpAgoraEngine);

    int nRet = apm->setParameters("{\"che.video.h264_profile\":66}");

    return nRet == 0 ? TRUE : FALSE;
}

BOOL CAgoraObject::AdjustVolume(int nRcdVol, int nPlaybackVol, int nMixVol)
{
    int nRet = 0;
   
    nRet &= m_lpAgoraEngine->adjustRecordingSignalVolume(nRcdVol);
    nRet &= m_lpAgoraEngine->adjustPlaybackSignalVolume(nPlaybackVol);
    nRet &= m_lpAgoraEngine->adjustAudioMixingVolume(nMixVol);

    return nRet == 0 ? TRUE : FALSE;
}

void CAgoraObject::GetVolume(int *nRcdVol, int *nPlaybackVol, int *nMixVol)
{
    *nRcdVol = m_nRcdVol;
    *nPlaybackVol = m_nPlaybackVol;
    *nMixVol = m_nMixVol;
}

int CAgoraObject::GetAudioMixingPos()
{
     return m_lpAgoraEngine->getAudioMixingCurrentPosition();
}

int CAgoraObject::GetAudioMixingDuration()
{
      return m_lpAgoraEngine->getAudioMixingDuration();
}


void CAgoraObject::SetSelfResolution(int nWidth, int nHeight)
{
    m_nCanvasWidth = nWidth;
    m_nCanvasHeight = nHeight;
}

void CAgoraObject::GetSelfResolution(int *nWidth, int *nHeight)
{
    *nWidth = m_nCanvasWidth;
    *nHeight = m_nCanvasHeight;
}

std::string CAgoraObject::GetCurrentVideoDevice()
{
    if (!m_pVideoDeviceManager || !m_pVideoDeviceManager->get())
        return "";

    char szId[MAX_DEVICE_ID_LENGTH] = { 0 };
    int ret = (*m_pVideoDeviceManager)->getDevice(szId);

    return szId;
}

BOOL CAgoraObject::GetVideoDevices(std::unordered_map<std::string, std::string>& devices)
{
    if (!m_pVideoDeviceManager || !m_pVideoDeviceManager->get())
        return FALSE;

    IVideoDeviceCollection* videoCollection = (*m_pVideoDeviceManager)->enumerateVideoDevices();

    char szId[MAX_DEVICE_ID_LENGTH] = { 0 };
    char szName[MAX_DEVICE_ID_LENGTH] = { 0 };
    for (int i = 0; i < videoCollection->getCount(); ++i) {
        videoCollection->getDevice(i, szName, szId);
        devices.insert(std::make_pair(szId, szName));
        m_videoIds.push_back(szId);
    }

    return TRUE;
}

BOOL CAgoraObject::EnableVideoFrameObserver(BOOL bEnable)
{
    int ret = 0;

    IMediaEngine* pMediaEngine = nullptr;
    m_lpAgoraEngine->queryInterface(AGORA_IID_MEDIA_ENGINE, (void**)&pMediaEngine);
    if (!pMediaEngine)
        return FALSE;

    if (bEnable) {
        ret = pMediaEngine->registerVideoFrameObserver(&m_videoObserver);
    }
    else {
        ret = pMediaEngine->registerVideoFrameObserver(nullptr);
    }

    return ret == 0 ? TRUE : FALSE;
}

void CAgoraObject::UpdateBeauty(bool bEnable)
{
    m_videoObserver.UpdateBeautyFlag(bEnable);
}
void CAgoraObject::UpdateFilter(bool bEnable)
{
    m_videoObserver.UpdateFilterFlag(bEnable);
}

void CAgoraObject::UpdateBundle(std::string itemName, bool bEnable)
{
    m_videoObserver.UpdateBundle(itemName, bEnable);
}

CString CAgoraObject::LoadAppID()
{
    CString strAppID(APP_ID);
    if (!strAppID.IsEmpty())
        return strAppID;
    TCHAR szFilePath[MAX_PATH];
    ::GetModuleFileName(NULL, szFilePath, MAX_PATH);
    LPTSTR lpLastSlash = _tcsrchr(szFilePath, _T('\\'));

    if (lpLastSlash == NULL)
        return strAppID;

    SIZE_T nNameLen = MAX_PATH - (lpLastSlash - szFilePath + 1);
    _tcscpy_s(lpLastSlash + 1, nNameLen, _T("AgoraFaceUnity.ini"));

    if (!PathFileExists(szFilePath)) {
        HANDLE handle = CreateFile(szFilePath, GENERIC_READ | GENERIC_WRITE, 0, NULL, CREATE_NEW, 0, NULL);
        CloseHandle(handle);
    }

    TCHAR szAppid[MAX_PATH] = { 0 };
    ::GetPrivateProfileString(_T("LoginInfo"), _T("AppID"), NULL, szAppid, MAX_PATH, szFilePath);
    if (_tcslen(szAppid) == 0) {
        ::WritePrivateProfileString(_T("LoginInfo"), _T("AppID"), _T(""), szFilePath);
        ::ShellExecute(NULL, _T("open"), szFilePath, NULL, NULL, SW_MAXIMIZE);
    }

    strAppID = szAppid;

    return strAppID;
}

std::string CAgoraObject::GetToken()
{
    std::string token(APP_TOKEN);
    if (!token.empty())
        return token;

    TCHAR szFilePath[MAX_PATH];
    ::GetModuleFileName(NULL, szFilePath, MAX_PATH);
    LPTSTR lpLastSlash = _tcsrchr(szFilePath, _T('\\'));

    if (lpLastSlash == NULL)
        return token;

    SIZE_T nNameLen = MAX_PATH - (lpLastSlash - szFilePath + 1);
    _tcscpy_s(lpLastSlash + 1, nNameLen, _T("AppID.ini"));


    TCHAR szToken[MAX_PATH] = { 0 };
    char temp[MAX_PATH] = { 0 };
    ::GetPrivateProfileString(_T("AppID"), _T("AppToken"), NULL, szToken, MAX_PATH, szFilePath);
    ::WideCharToMultiByte(CP_UTF8, 0, szToken, -1, temp, 128, NULL, NULL);

    return temp;
}