#ifndef CCAMERA_H
#define CCAMERA_H

#define WIN32_LEAN_AND_MEAN

#define POINTER_64 __ptr64

#include <windows.h>
#include <atlbase.h>
#include <qedit.h>
#include <dshow.h>

#include <memory>

#define MYFREEMEDIATYPE(mt)	{if ((mt).cbFormat != 0)		\
					{CoTaskMemFree((PVOID)(mt).pbFormat);	\
					(mt).cbFormat = 0;						\
					(mt).pbFormat = NULL;					\
				}											\
				if ((mt).pUnk != NULL)						\
				{											\
					(mt).pUnk->Release();					\
					(mt).pUnk = NULL;						\
				}}									


class CCameraDS
{
private:
	std::tr1::shared_ptr<unsigned char> m_frame;

	//IplImage * m_pFrame;
	bool m_bConnected;	
	int m_nWidth;
	int m_nHeight;
	bool m_bLock;
	bool m_bChanged;
	long m_nBufferSize;

	CComPtr<IGraphBuilder> m_pGraph;
	CComPtr<IBaseFilter> m_pDeviceFilter;
	CComPtr<IMediaControl> m_pMediaControl;
	CComPtr<IBaseFilter> m_pSampleGrabberFilter;
	CComPtr<ISampleGrabber> m_pSampleGrabber;
	CComPtr<IPin> m_pGrabberInput;
	CComPtr<IPin> m_pGrabberOutput;
	CComPtr<IPin> m_pCameraOutput;
	CComPtr<IMediaEvent> m_pMediaEvent;
	CComPtr<IBaseFilter> m_pNullFilter;
	CComPtr<IPin> m_pNullInputPin;

private:
	bool BindFilter(int nCamIDX, IBaseFilter **pFilter);
	void SetCrossBar();

public:
	CCameraDS();
	virtual ~CCameraDS();

	//������ͷ��nCamIDָ�����ĸ�����ͷ��ȡֵ����Ϊ0,1,2,...
	//bDisplayPropertiesָʾ�Ƿ��Զ���������ͷ����ҳ
	//nWidth��nHeight���õ�����ͷ�Ŀ�͸ߣ��������ͷ��֧�����趨�Ŀ�Ⱥ͸߶ȣ��򷵻�false
	bool CCameraDS::OpenCamera(int nCamID, bool bDisplayProperties = true, int nWidth = 320, int nHeight = 240);

	bool IsConnected();

	int IsDeviceBusy(char * videoName);

	//�ر�����ͷ�������������Զ������������
	void CloseCamera();

	//��������ͷ����Ŀ
	static int CameraCount();

	//��������ͷ�ı�ŷ�������ͷ������
	//nCamID: ����ͷ���
	//sName: ���ڴ������ͷ���ֵ�����
	//nBufferSize: sName�Ĵ�С
	static int CCameraDS::CameraName(int nCamID, char* sName, int nBufferSize);

	//����ͼ����
	int GetWidth() { return m_nWidth; }

	//����ͼ��߶�
	int GetHeight() { return m_nHeight; }

	//����ͼ������ΪRGBA����
	std::tr1::shared_ptr<unsigned char> QueryFrame();

	bool m_bIsCurrentDeviceBusy;
};

#endif 
