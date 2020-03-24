#include "stdafx.h"
#include "FileIO.h"
#include <assert.h>
#include "commonFun.h"

CFileIO::CFileIO() :
fileHandle_(nullptr)
, filelimitLine_(0)
, isLog_(false)
{
}

CFileIO::~CFileIO()
{
	fileHandle_ = nullptr;
	filelimitLine_ = 0;
	isLog_ = false;
}

void CFileIO::openLog(const std::string &filepath, int fileFlage /*= OPEN_ALWAYS */)
{
	assert(nullptr == fileHandle_);
	fileHandle_ = CreateFile(
		CString(filepath.c_str()),
		GENERIC_READ | GENERIC_WRITE,
		FILE_SHARE_READ | FILE_SHARE_WRITE,
		nullptr,
		/*CREATE_NEW | OPEN_ALWAYS | TRUNCATE_EXISTING*/fileFlage,
		FILE_ATTRIBUTE_NORMAL,
		nullptr);
	if (INVALID_HANDLE_VALUE == fileHandle_)
	{
		fileHandle_ = nullptr;
		printf("文件创建失败!!!!!!\n");
		int retCode = GetLastError();
		if (ERROR_ALREADY_EXISTS == retCode)
		{
			printf("文件已经存在,创建文件失败!!!!\n");
		}
	}

	isLog_ = true;
	SetFilePointer(fileHandle_, 0, nullptr, FILE_END);
}

void CFileIO::openMedia(const std::string &filepath, int fileFlage /*= CREATE_ALWAYS*/)
{
	assert(nullptr == fileHandle_);
	fileHandle_ = CreateFile(CString(filepath.c_str()),
		GENERIC_WRITE | GENERIC_READ,
		FILE_SHARE_WRITE | FILE_SHARE_READ,
		nullptr,
		fileFlage,
		FILE_ATTRIBUTE_NORMAL,
		nullptr);
	if (INVALID_HANDLE_VALUE == fileHandle_)
	{
		fileHandle_ = nullptr;
		printf("文件创建失败!!!!!!\n");
		int retCode = GetLastError();
		if (ERROR_ALREADY_EXISTS == retCode)
		{
			printf("文件已经存在,创建文件失败!!!!\n");
		}
	}
}

void CFileIO::openReadFile(const std::string &filePath)
{
	openLog(filePath);
	FlushFileBuffers(fileHandle_);
	SetFilePointer(fileHandle_, 0, nullptr, FILE_BEGIN);
}

void CFileIO::close()
{
	CloseHandle(fileHandle_);
	fileHandle_ = nullptr;
}

int CFileIO::write(char *bufferIn, int bufferLen)
{
	if (fileHandle_)
	{
		DWORD writenLen = 0;
		int res = WriteFile(fileHandle_, bufferIn, (DWORD)bufferLen, &writenLen, nullptr);
		if (0 == res)
		{
			printf("write buffer failed..retCode: %d!!!!!\n", GetLastError());
		}
		return int(writenLen);
	}

	return 0;
}

int CFileIO::write(std::string bufferStr)
{
	filelimitLine_++;
	if (isLog_)
	{
		bufferStr += "\r\n";
	}
	if (isLog_ && 100 == filelimitLine_)
	{//清空操作
		SetFilePointer(fileHandle_, 0, nullptr, FILE_BEGIN);
		SetEndOfFile(fileHandle_);
		filelimitLine_ = 0;
	}

	return write((char*)bufferStr.c_str(), bufferStr.length());
}

int CFileIO::read(char *bufferOut, int bufferLen)
{
	DWORD readLen = 0;
	int res = ReadFile(fileHandle_, bufferOut, bufferLen, &readLen, nullptr);
	if (0 == res)
	{
		printf("read buffer from file failed!!!,retCode: %d\n", GetLastError());
		return int(readLen);
	}
	return readLen;
}

std::string CFileIO::readLine()
{
	std::string strLine;
	char readTxt[2] = { 0 };
	DWORD readLen = 0; DWORD dwValue = 0;
	BOOL res = ReadFile(fileHandle_, readTxt, 1, &readLen, nullptr);
	std::string chKey = "\r";
	while (true)
	{
		if (res && 1 == readLen && chKey == readTxt)
		{
			res = ReadFile(fileHandle_, readTxt, 1, &readLen, nullptr);
			chKey = "\n";
			if (res && 1 == readLen && chKey == readTxt)
			{
				break;
			}
			else
			{
				printf("read error ,cloud read '\r\n'\n");
				return "";
			}
		}
		else if (res && 1 == readLen &&  chKey != readTxt)
		{
			strLine += readTxt;
			//dwValue = SetFilePointer(fileHandle_, readLen, nullptr, FILE_CURRENT);
			readLen = 0; ZeroMemory(readTxt, 2); dwValue = 0;
			res = ReadFile(fileHandle_, readTxt, 1, &readLen, nullptr);
		}
		if (res && 0 == readLen)
		{
			break;
		}
	}
	return strLine;
}

std::string CFileIO::read()
{
	return "";
}

bool CFileIO::generatorFile(const std::string &path)
{
	HANDLE fileHandle = CreateFile(CString(path.c_str()),
		GENERIC_READ | GENERIC_READ,
		FILE_SHARE_READ | FILE_SHARE_WRITE,
		nullptr,
		OPEN_ALWAYS,
		FILE_ATTRIBUTE_NORMAL,
		nullptr);
	if (INVALID_HANDLE_VALUE == fileHandle)
	{
		return false;
	}
	return true;
}

CFileIni::CFileIni() :isValid_(false)
{

}

CFileIni::CFileIni(const std::string &filePath)
{
	iniFile_ = filePath;
	CFileIO::generatorFile(filePath);
}

CFileIni::~CFileIni()
{
	isValid_ = false;
}

bool CFileIni::openFile(const std::string &IniFile)
{
	iniFile_ = IniFile;
	return isValid_ = CFileIO::generatorFile(IniFile);
}

bool CFileIni::write(const std::string &section, const std::string &key, const std::string &Value)
{
	assert(isValid_);
	return (bool)(WritePrivateProfileString(s2cs(section), s2cs(key), s2cs(Value), s2cs(iniFile_)));
}

std::string CFileIni::read(const std::string &section, const std::string &key)
{
	assert(isValid_);
	std::string Value;
	TCHAR returnStr[MAXPATHLEN] = { 0 };
	GetPrivateProfileString(s2cs(section), s2cs(key), _T(""), returnStr, MAXPATHLEN, s2cs(iniFile_));
	Value = cs2s(returnStr);
	return Value;
}

bool CFileIni::getSectionName(std::vector<std::string> &vecSection)
{
	assert(isValid_);
	TCHAR returnStr[MAXPATHLEN] = { 0 }; std::string sectionItem;
	DWORD retNum = GetPrivateProfileSectionNames(returnStr, MAXPATHLEN, s2cs(iniFile_));
	if (0 < retNum)
	{
		int strLen = retNum;
		int nIndex = 0; TCHAR tchTemp = '\0';
		while (nIndex < strLen)
		{
			if ('\0' != (tchTemp = returnStr[nIndex]))
			{
				sectionItem += (tchTemp);
			}
			else
			{
				vecSection.push_back(sectionItem);
				sectionItem.clear();
			}
			nIndex++;
		}
	}
	return retNum > 0;
}

bool CFileIni::getSection(const std::string &section, std::map<std::string, std::string> &mapKeyValue)
{
	assert(isValid_);
	TCHAR returnStr[MAXPATHLEN] = { 0 }; std::string key; std::string value; bool isKey = true;
	DWORD retNum = GetPrivateProfileSection(s2cs(section), returnStr, MAXPATHLEN, s2cs(iniFile_));
	if (0 < retNum)
	{
		int strLen = retNum;
		int nIndex = 0; TCHAR tchTemp = '\0';
		while (nIndex < strLen)
		{
			if ('\0' != (tchTemp = returnStr[nIndex]))
			{
				if (L'=' == tchTemp)
				{
					isKey = false;
					nIndex++;
					continue;
				}
				if (isKey)
				{
					key += (tchTemp);
				}
				else
				{
					value += (tchTemp);
				}
			}
			else
			{
				mapKeyValue.insert(make_pair(key, value));
				key.clear(); value.clear();
				isKey = true;
			}
			nIndex++;
		}
	}
	return retNum > 0;
}

CIniBase::CIniBase(const std::string &filePath) :pIniInstance_(nullptr)
{
	pIniInstance_ = new CFileIni(filePath);
	assert(pIniInstance_);
}

CIniBase::~CIniBase()
{
	if (pIniInstance_)
	{
		delete pIniInstance_;
		pIniInstance_ = nullptr;
	}
}

CConfigAgoraFaceUntiy::CConfigAgoraFaceUntiy() :CIniBase("")
{
	std::string path = getAbsoluteDir() + "AgoraFaceUnity.ini";
	pIniInstance_->openFile(path);
}

CConfigAgoraFaceUntiy::~CConfigAgoraFaceUntiy()
{

}

CConfigAgoraFaceUntiy::CConfigAgoraFaceUntiy(const std::string &path) :
CIniBase(path)
{

}

__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, AppId, INI_LoginInfo, INI_LoginInfo_APPID)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, AppCertEnable, INI_LoginInfo, INI_LoginInfo_AppCertEnable)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, AppCertificateId, INI_LoginInfo, INI_LoginInfo_APPCertificateID)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, ChannelName, INI_LoginInfo, INI_LoginInfo_ChannelName)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, RestartTimerStatus, INI_LoginInfo, INI_LoginInfo_RestartTimeStatus)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, RestartTimer, INI_LoginInfo, INI_LoginInfo_RestaTimer)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, VideoPreview, INI_LoginInfo, INI_LoginInfo_VideoPreview)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, ClearLogInterval, INI_LoginInfo, INI_LoginInfo_ClearLogInterval)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, LoginUid, INI_LoginInfo, INI_LoginInfo_LoginUID)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, VideoSolutinIndex, INI_LoginInfo, INI_LoginInfo_VideoSolutionIndex)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, CameraDeviceID, INI_LoginInfo, INI_LoginInfo_CameraDeviceId)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, CameraDeviceName, INI_LoginInfo, INI_LoginInfo_CameraDeviceName)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, SignalAccount, INI_LoginInfo, INI_LoginInfo_SignalAccount)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, ServerAccount, INI_LoginInfo, INI_LoginInfo_ServerAccount)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, Language, INI_LoginInfo, INI_LoginInfo_Language)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, EnableEncrypt, INI_LoginInfo, INI_LoginInfo_Encrypt)
__IMPLEMENT_INICONFIG_FUN(CConfigAgoraFaceUntiy, RtmpUrl, INI_LoginInfo, INI_LoginInfo_RtmpUrl)

__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ProcessEnable, INI_PROCESSID_Enable)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, LoginUid, INI_LoginInfo_UID)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, AudioInName, INI_DeviceInfo_AudioInName)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, AudioInComID, INI_DeviceInfo_AudioInCOMID)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, PlayOutName, INI_DeviceInfo_PlayOutName)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, PlayOutComID, INI_DeviceInfo_PlayOutCOMID)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, CameraName, INI_DeviceInfo_CameraName)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, CameraComID, INI_DeviceInfo_CameraCOMID)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, DeviceState, INI_DeviceInfo_State)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, DeviceChoose, INI_DeviceInfo_Choose)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, LeftRotate90, INI_DeviceInfo_LeftRotate90)

__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ResolutionSave, INI_DeviceInfo_ResolutionSave)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ResolutionIndex, INI_DeviceInfo_ResolutionIndex)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ResolutionWidth, INI_DeviceInfo_ResolutionWidth)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ResolutionHeight, INI_DeviceInfo_ResolutionHeight)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ResolutionFps, INI_DeviceInfo_ResolutionFps)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, ResolutionBitrate, INI_DeviceInfo_ResolutionBitrate)

__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, RtmpSave, INI_DeviceInfo_RtmpSave)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, RtmpUrl, INI_DeviceInfo_RtmpUrl)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, RtmpWidth, INI_DeviceInfo_RtmpWidth)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, RtmpHeight, INI_DeviceInfo_RtmpHeight)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, RtmpFps, INI_DeviceInfo_RtmpFps)
__IMPLEMENT_INICONFIG_SIMILAR_FUN(CConfigAgoraFaceUntiy, RtmpBitrate, INI_DeviceInfo_RtmpBitrate)