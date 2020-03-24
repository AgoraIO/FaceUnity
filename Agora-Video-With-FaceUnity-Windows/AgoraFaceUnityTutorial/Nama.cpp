/**
* FU SDKʹ���߿��Խ��õ�������frameͼ�����Լ���ԭ����Ŀ�Խ�
* ��FU SDKʹ����ֱ�Ӳο�ʾ��������������Ӧλ��
*
* FU SDK��camera����ϣ����������ݵ���Դ��ֻҪͼ��������ȷ�ҺͿ���Ǻϼ���
*
* Created by liujia on 2018/1/3 mybbs2200@gmail.com.
*/
#include "CameraDS.h"
#include "Nama.h"
#include <fstream>
#include <iostream>

#include "GlobalValue.h"        //nama SDK ����Դ�ļ�
#include <funama.h>				//nama SDK ��ͷ�ļ�
#include <authpack.h>		//nama SDK ��key�ļ�
#pragma comment(lib, "nama.lib")//nama SDK ��lib�ļ�

using namespace NamaExampleNameSpace;
bool Nama::m_hasSetup = false;

namespace NamaExampleNameSpace
{
	size_t FileSize(std::ifstream& file)
	{
		std::streampos oldPos = file.tellg();

		file.seekg(0, std::ios::beg);
		std::streampos beg = file.tellg();
		file.seekg(0, std::ios::end);
		std::streampos end = file.tellg();

		file.seekg(oldPos, std::ios::beg);

		return static_cast<size_t>(end - beg);
	}

	bool LoadBundle(const std::string& filepath, std::vector<char>& data)
	{
		std::ifstream fin(filepath, std::ios::binary);
		if (false == fin.good())
		{
			fin.close();
			return false;
		}
		size_t size = FileSize(fin);
		if (0 == size)
		{
			fin.close();
			return false;
		}
		data.resize(size);
		fin.read(reinterpret_cast<char*>(&data[0]), size);

		fin.close();
		return true;
	}
}

std::string Nama::_filters[6] = { "origin", "delta", "electric", "slowlived", "tokyo", "warm" };

Nama::Nama()	
	:m_frameID(0),
	m_curBundleIdx(0),
	m_mode(PROP),
	m_isBeautyOn(true),
	m_isDrawProp(1),
	m_frameWidth(0),
	m_frameHeight(0),
	m_curFilterIdx(0),
	m_beautyHandles(0),
	m_gestureHandles(0),
	m_propHandles(0),
	m_curColorLevel(0.0f),
	m_curBlurLevel(0.0f),
	m_curCheekThinning(0.0f),
	m_curEyeEnlarging(0.0f),
	m_face_shape(3),
	m_redLevel(0.6f),
	m_faceShapeLevel(0.0f)
{
	m_curCameraIdx = 0;
	m_cap = std::tr1::shared_ptr<CCameraDS>(new CCameraDS);
}

Nama::~Nama()
{	
	if (true== m_hasSetup)
	{
		fuDestroyAllItems();//Note: �м�ʹ��һ���Ѿ�destroy��item
		fuOnDeviceLost();//Note: �����������nama������OpenGL��Դ

		fuDestroyLibData();
		m_hasSetup = false;
	}		
	//fuSetup��������ֻ��Ҫ����һ�Σ�����ĳ���Ӵ���ʱֻ��Ҫ������������������ 
	//Tips:����������ڻ�������Щ��Դ����ô��Դ����Ӧ���ڸ����ڡ����������ڼ�һֱ������Щ��Դ.
	
}

int Nama::CameraCount()
{
	return m_cap->CameraCount();
}

std::tr1::shared_ptr<unsigned char> Nama::QueryFrame()
{
	return m_cap->QueryFrame();
}

int Nama::CameraName(int nCamID, char* sName, int nBufferSize)
{
	return m_cap->CameraName(nCamID, sName, nBufferSize);
}

void Nama::ReOpenCamera()
{
	if (m_cap->IsConnected())
	{		
		m_cap->CloseCamera();		
		if (false == m_cap->OpenCamera(m_curCameraIdx, false, m_frameWidth, m_frameHeight))
		{
			std::cout << "ȱ������ͷ���Ƽ�ʹ�� Logitech C920��Ȼ��װ�ٷ�������" << std::endl;
			exit(1);
		}
		fuOnCameraChange();//Note: ��������������Ϣ
	}
}

void Nama::Init(const int width, const int height)
{
	m_frameWidth = width;
	m_frameHeight = height;
	
	if (false == m_cap->OpenCamera(m_curCameraIdx, false, m_frameWidth, m_frameHeight))
	{
		std::cout << "ȱ������ͷ���Ƽ�ʹ�� Logitech C920��Ȼ��װ�ٷ�������\n Error: Missing camera! " << std::endl ;
		exit(1);
	}

	if (false == m_hasSetup)
	{
		//��ȡnama���ݿ⣬��ʼ��nama
		std::vector<char> v3data;
		if (false == LoadBundle(g_fuDataDir + g_v3Data, v3data))
		{
			std::cout << "Error:ȱ�������ļ���" << g_fuDataDir + g_v3Data << std::endl;
			exit(2);
		}		
		fuSetup(reinterpret_cast<float*>(&v3data[0]), v3data.size(), NULL, g_auth_package, sizeof(g_auth_package));

		std::vector<char> anim_model_data;
		if (false == LoadBundle(g_fuDataDir + g_anim_model, anim_model_data))
		{
			std::cout << "Error:ȱ�������ļ���" << g_fuDataDir + g_anim_model << std::endl;
			exit(2);
		}
		// ����ϵ���ȶ�
		fuLoadAnimModel(reinterpret_cast<float*>(&anim_model_data[0]), anim_model_data.size());
		fuSetExpressionCalibration(1);

		m_hasSetup = true;
	}
	else
	{
		fuOnDeviceLost();
		m_hasSetup = false;
	}

	//��ȡ���յ��ߣ��������ղ���
	{
		std::vector<char> propData;
		if (false == LoadBundle(g_fuDataDir + g_faceBeautification, propData))
		{
			std::cout << "load face beautification data failed." << std::endl;
			exit(2);
		}
		std::cout << "load face beautification data." << std::endl;

		m_beautyHandles = fuCreateItemFromPackage(&propData[0], propData.size());

		//"origin", "delta", "electric", "slowlived", "tokyo", "warm"�Ȳ���������
		fuItemSetParams(m_beautyHandles, "filter_name", &_filters[m_curFilterIdx][0]);
		fuItemSetParamd(m_beautyHandles, "is_opengl_es", 0);
		fuItemSetParamd(m_beautyHandles, "color_level", m_curColorLevel);
		fuItemSetParamd(m_beautyHandles, "blur_level", m_curBlurLevel);
		fuItemSetParamd(m_beautyHandles, "cheek_thinning", m_curCheekThinning);
		fuItemSetParamd(m_beautyHandles, "eye_enlarging", m_curEyeEnlarging);
		fuItemSetParamd(m_beautyHandles, "face_shape_level", m_faceShapeLevel);
		fuItemSetParamd(m_beautyHandles, "red_level", m_redLevel);
		fuItemSetParamd(m_beautyHandles, "face_shape", m_face_shape);
	}
	//��ȡ����ʶ�����
	{
		std::vector<char> gestureData;
		if (false == LoadBundle(g_fuDataDir + g_gestureRecongnition, gestureData))
		{
			std::cout << "load gesture recognition data failed." << std::endl;
			exit(2);
		}
		std::cout << "load gesture recognition data." << std::endl;

		m_gestureHandles = fuCreateItemFromPackage(&gestureData[0], gestureData.size());
	}
	m_propHandles.resize(g_propCount);
	//����һ��bundle
	m_curBundleIdx = -1;
	NextBundle();
	fuSetDefaultOrientation(0);
}

void Nama::SwitchRenderMode()
{
	static MODE nextMode[] = { LANDMARK , PROP };
	m_mode = nextMode[m_mode];
}

void Nama::SwitchBeauty()
{
	m_isBeautyOn = !m_isBeautyOn;
}

void Nama::PreBundle()
{
	if (!m_isDrawProp)return;

	--m_curBundleIdx;
	if (m_curBundleIdx < 0)
	{
		m_curBundleIdx += m_propHandles.size();
	}

	CreateBundle(m_curBundleIdx);
}

void Nama::NextBundle()
{
	if (!m_isDrawProp)return;

	++m_curBundleIdx;
	m_curBundleIdx %= m_propHandles.size();

	CreateBundle(m_curBundleIdx);
}

void Nama::SetCurrentBundle(int index)
{
	if (!m_isDrawProp)return;
	if (0<=index <=g_propCount)
	{		
		m_curBundleIdx = index;
		CreateBundle(m_curBundleIdx);
	}
}

void Nama::SetCurrentShape(int index)
{
	if (false == m_isBeautyOn)return;	
	if (0 <= index <= 3)
	{
		m_face_shape = index;
		int res = fuItemSetParamd(m_beautyHandles, "face_shape", m_face_shape);
	}
}

void Nama::NextShape()
{
	if (false == m_isBeautyOn)
	{
		return;
	}
	++m_face_shape;
	m_face_shape %= 3;
	int res = fuItemSetParamd(m_beautyHandles, "face_shape", m_face_shape);
	std::cout << "face_shape: " << m_face_shape << std::endl;
}

void Nama::UpdateFilter()
{
	if (false == m_isBeautyOn)return;

	fuItemSetParams(m_beautyHandles, "filter_name", &_filters[m_curFilterIdx][0]);
}

void Nama::UpdateBeauty()
{
	if (false == m_isBeautyOn)return;

	fuItemSetParamd(m_beautyHandles, "color_level", m_curColorLevel);
	fuItemSetParamd(m_beautyHandles, "blur_level", m_curBlurLevel);
	fuItemSetParamd(m_beautyHandles, "cheek_thinning", m_curCheekThinning);
	fuItemSetParamd(m_beautyHandles, "eye_enlarging", m_curEyeEnlarging);
	fuItemSetParamd(m_beautyHandles, "face_shape_level", m_faceShapeLevel);
	fuItemSetParamd(m_beautyHandles, "red_level", m_redLevel);	
}
//����ȫ�����ߣ���ʼ������
void Nama::CreateBundle()
{
	for (int i(0); i != g_propCount; ++i)
	{
		CreateBundle(i);
	}
}
//������ص��ߣ��������л���ʱ��Ῠ��һ��
void Nama::CreateBundle(const int idx)
{
	if (0 == m_propHandles[idx])
	{
		std::vector<char> propData;
		if (false == LoadBundle(g_fuDataDir + g_propName[idx], propData))
		{
			std::cout << "load prop data failed." << std::endl;
			exit(1);
		}
		std::cout << "load prop data." << std::endl;

		m_propHandles[idx] = fuCreateItemFromPackage(&propData[0], propData.size());		
	}
}
//�ü�ͼ������
void Nama::ScissorFrameBuffer(std::tr1::shared_ptr<unsigned char> frame)
{
	int size = m_frameWidth*m_frameHeight * 4;
	for (int i = 0; i < m_frameHeight; i++)
	{
		auto ptr = frame.get() + i*m_frameWidth * 4 + m_frameWidth * 4;
		auto qptr = frame.get() + i*m_frameWidth * 4;
		for (int j = 0; j < m_frameWidth * 0.31640625f; j++)
		{
			qptr[0] = 0;
			qptr[1] = 0;
			qptr[2] = 0;
			qptr[3] = 0;
			qptr += 4;
			ptr[0] = 0;
			ptr[1] = 0;
			ptr[2] = 0;
			ptr[3] = 0;
			ptr -= 4;
		}
	}
}

std::tr1::shared_ptr<unsigned char> Nama::ConvertBetweenBGRAandRGBA(std::tr1::shared_ptr<unsigned char> frame)
{
	int size = m_frameWidth*m_frameHeight * 4;
	auto temp_frame = std::tr1::shared_ptr<unsigned char>(new unsigned char[size]);
	int offset = 0;
	if (IsBadReadPtr(frame.get(), 4))//can't debug run
	{
		printf("The camera is usered by other programs��\n");
		return temp_frame;
	}
	auto output = temp_frame.get();
	auto input = frame.get();
	for (int i = 0; i < m_frameHeight; i++)
	{
		for (int j = 0; j < m_frameWidth; j++)
		{
			output[offset] = input[offset + 2];
			output[offset + 1] = input[offset + 1];
			output[offset + 2] = input[offset];
			output[offset + 3] = input[offset + 3];

			offset += 4;
		}
	}

	return temp_frame;
}

//��Ⱦ����
void Nama::RenderItems(std::tr1::shared_ptr<unsigned char> frame)
{	
	switch (m_mode)
	{
	case PROP:
		if (1 == m_isBeautyOn && 1 == m_isDrawProp)
		{
			int handle[2] = { m_beautyHandles, m_propHandles[m_curBundleIdx] };
			//֧�ֵĸ�ʽ��FU_FORMAT_BGRA_BUFFER �� FU_FORMAT_NV21_BUFFER ��FU_FORMAT_I420_BUFFER ��FU_FORMAT_RGBA_BUFFER			
			fuRenderItemsEx2(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(frame.get()), FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(frame.get()),
				m_frameWidth, m_frameHeight, m_frameID, handle, 2, NAMA_RENDER_FEATURE_FULL | NAMA_RENDER_OPTION_FLIP_X, NULL);			
		}
		else if (1 == m_isDrawProp && 0 == m_isBeautyOn)
		{						
			fuRenderItemsEx2(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(frame.get()), FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(frame.get()),
				m_frameWidth, m_frameHeight, m_frameID, &m_propHandles[m_curBundleIdx], 1, NAMA_RENDER_FEATURE_FULL | NAMA_RENDER_OPTION_FLIP_X, NULL);
		}
		else if (1 == m_isBeautyOn && 0 == m_isDrawProp)
		{			
			fuRenderItemsEx2(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(frame.get()), FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(frame.get()),
				m_frameWidth, m_frameHeight, m_frameID, &m_beautyHandles, 1, NAMA_RENDER_FEATURE_FULL | NAMA_RENDER_OPTION_FLIP_X, NULL);
		}		
		break;
	case LANDMARK:		
		DrawLandmarks(frame);
		break;
	default:
		break;
	}

	int nErrCode = fuGetSystemError();
	char logMsg[128] = { '\0' };
	sprintf_s(logMsg, "fuGetSystemError() : %d\n", nErrCode);
	OutputDebugStringA(logMsg);

	++m_frameID;	
	return;
}

void Nama::RenderItems(unsigned char* pCameraBuffer)
{
	switch (m_mode)
	{
	case PROP:
		if (1 == m_isBeautyOn && 1 == m_isDrawProp)
		{
			int handle[2] = { m_beautyHandles, m_propHandles[m_curBundleIdx] };
			//֧�ֵĸ�ʽ��FU_FORMAT_BGRA_BUFFER �� FU_FORMAT_NV21_BUFFER ��FU_FORMAT_I420_BUFFER ��FU_FORMAT_RGBA_BUFFER			
			fuRenderItemsEx2(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(pCameraBuffer), FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(pCameraBuffer),
				m_frameWidth, m_frameHeight, m_frameID, handle, 2, NAMA_RENDER_FEATURE_FULL | NAMA_RENDER_OPTION_FLIP_X, NULL);
		}
		else if (1 == m_isDrawProp && 0 == m_isBeautyOn)
		{
			fuRenderItemsEx2(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(pCameraBuffer), FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(pCameraBuffer),
				m_frameWidth, m_frameHeight, m_frameID, &m_propHandles[m_curBundleIdx], 1, NAMA_RENDER_FEATURE_FULL | NAMA_RENDER_OPTION_FLIP_X, NULL);
		}
		else if (1 == m_isBeautyOn && 0 == m_isDrawProp)
		{
			fuRenderItemsEx2(FU_FORMAT_RGBA_BUFFER, reinterpret_cast<int*>(pCameraBuffer), FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(pCameraBuffer),
				m_frameWidth, m_frameHeight, m_frameID, &m_beautyHandles, 1, NAMA_RENDER_FEATURE_FULL | NAMA_RENDER_OPTION_FLIP_X, NULL);
		}
		break;
	case LANDMARK:
		//DrawLandmarks(frame);
		break;
	default:
		break;
	}

	int nErrCode = fuGetSystemError();
	char logMsg[128] = { '\0' };
	sprintf_s(logMsg, "fuGetSystemError() : %d\n", nErrCode);
	OutputDebugStringA(logMsg);

	++m_frameID;
	return;
}

//ֻ����nama�������ģ��
std::tr1::shared_ptr<unsigned char> Nama::RenderEx()
{
	std::tr1::shared_ptr<unsigned char> frame = m_cap->QueryFrame();

	fuBeautifyImage(FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(frame.get()),
		FU_FORMAT_BGRA_BUFFER, reinterpret_cast<int*>(frame.get()),
		m_frameWidth, m_frameHeight, m_frameID, &m_beautyHandles, 1);

	++m_frameID;

	return frame;
}

//��������������
void Nama::DrawLandmarks(std::tr1::shared_ptr<unsigned char> frame)
{
	float landmarks[150];
	float trans[3];
	float rotat[4];
	int ret = 0;

	ret = fuGetFaceInfo(0, "landmarks", landmarks, sizeof(landmarks) / sizeof(landmarks[0]));	
	for (int i(0); i != 75; ++i)
	{
		DrawPoint(frame, static_cast<int>(landmarks[2 * i]), static_cast<int>(landmarks[2 * i + 1]));
	}
}

std::string Nama::getVersion()
{
	return fuGetVersion();
}

void Nama::DrawPoint(std::tr1::shared_ptr<unsigned char> frame, int x, int y, unsigned char r, unsigned char g, unsigned char b)
{
	const int offsetX[] = { -1, 0, 1 , -1, 0, 1 , -1, 0, 1 };
	const int offsetY[] = { -1, -1, -1, 0, 0, 0, 1, 1, 1 };
	const int count = sizeof(offsetX) / sizeof(offsetX[0]);

	unsigned char* data = frame.get();
	for (int i(0); i != count; ++i)
	{
		int xx = x + offsetX[i];
		int yy = y + offsetY[i];
		if (0 > xx || xx >= m_frameWidth || 0 > yy || yy >= m_frameHeight)
		{
			continue;
		}

		data[yy * 4 * m_frameWidth + xx * 4 + 0] = b;
		data[yy * 4 * m_frameWidth + xx * 4 + 1] = g;
		data[yy * 4 * m_frameWidth + xx * 4 + 2] = r;
	}

}
