#include "stdafx.h"
#include "AgoraOpenGL.h"

#pragma comment(lib,"glew32.lib")
#pragma comment(lib,"glut.lib")

CAgoraOpenGl::CAgoraOpenGl()
{

}

CAgoraOpenGl::~CAgoraOpenGl()
{
	wglMakeCurrent(hDc, nullptr);
	wglDeleteContext(hRC);
}

void CAgoraOpenGl::Init(int nWidth, int nHeight)
{
#if 1
	m_nWidth = nWidth;
	m_nHeight = nHeight;
	glDisable(GL_LIGHTING);
	glDisable(GL_DEPTH_TEST);
	glEnable(GL_TEXTURE_2D);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(0, nWidth, 0, nHeight, 0, 1000);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	glGenTextures(1, &textureID);
	glBindTexture(GL_TEXTURE_2D, textureID);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

#else
	glClearColor(0.0, 1.0, 1.0, 0.0);
	glShadeModel(GL_SMOOTH);

	glMatrixMode(GL_PROJECTION);
	gluPerspective(60, (GLfloat)nWidth/(GLfloat)nHeight,0.0,100.0);

	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
#endif

}

bool CAgoraOpenGl::SetupPixelFormat(HDC hdc0)
{
	int nPixelFormat;
	hDc = hdc0;
	PIXELFORMATDESCRIPTOR pfd = {
		sizeof(PIXELFORMATDESCRIPTOR),    // pfd�ṹ�Ĵ�С  
		1,														 // �汾��  
		PFD_DRAW_TO_WINDOW |              // ֧���ڴ����л�ͼ  
		PFD_SUPPORT_OPENGL |              // ֧��OpenGL  
		PFD_DOUBLEBUFFER,                 // ˫����ģʽ  
		PFD_TYPE_RGBA,                    // RGBA ��ɫģʽ  
		24,                               // 24 λ��ɫ���  
		0, 0, 0, 0, 0, 0,                 // ������ɫλ  
		0,                                // û�з�͸���Ȼ���  
		0,                                // ������λλ  
		0,                                // ���ۼӻ���  
		0, 0, 0, 0,                       // �����ۼ�λ  
		32,                               // 32 λ��Ȼ���     
		0,                                // ��ģ�建��  
		0,                                // �޸�������  
		PFD_MAIN_PLANE,                   // ����  
		0,                                // ����  
		0, 0, 0                           // ���Բ�,�ɼ��Ժ������ģ  
	};

	if (!(nPixelFormat = ChoosePixelFormat(hDc, &pfd))){
		MessageBox(NULL, L"can not find proper mode", L"Error", MB_OK | MB_ICONEXCLAMATION);
		return false;
	}

	SetPixelFormat(hDc, nPixelFormat, &pfd);
	hRC = wglCreateContext(hDc);
	wglMakeCurrent(hDc, hRC);
	return TRUE;
}

void CAgoraOpenGl::Reshap(int nWidth, int nHeight)
{
	glViewport(0, 0, nWidth, nHeight);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluPerspective
		(60.0f,
		(GLfloat)nWidth / (GLfloat)nHeight,
		0.1f,
		100.0f
		);
	//gluLookAt(10,5,10,0,0,0,0,1,0);  
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
}

void CAgoraOpenGl::Render(std::tr1::shared_ptr<unsigned char>frame)
{
#if 0
	glClear(GL_COLOR_BUFFER_BIT);
	glColor3f(1.0, 0.0, 0.0);
	glLoadIdentity();
	glTranslatef(0.0, 0.0, -5.0);
	glBegin(GL_TRIANGLES);
	glVertex3f(0.0, 1.0, 0.0);
	glVertex3f(-1.0, -1.0, 0.0);
	glVertex3f(1.0, -1.0, 0.0);
	glEnd();
	SwapBuffers(hDc);
#else

	glClear(GL_COLOR_BUFFER_BIT);
	glClearColor(0.5, 0.5, 0.0, 1.0);
	glDisable(GL_BLEND);

	glBindTexture(GL_TEXTURE_2D, textureID);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, m_nWidth, m_nHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,frame.get());
	
	glBindTexture(GL_TEXTURE_2D, textureID);
	glBegin(GL_TRIANGLE_FAN);
	glTexCoord2f(1.0f, 0.0f);
	glVertex3f(0, 0, 0.0f);
	glTexCoord2f(1.0f, 1.0f);
	glVertex3f(0, m_nHeight, 0.0f);
	glTexCoord2f(0.0f, 1.0f);
	glVertex3f(m_nWidth, m_nHeight, 0);
	glTexCoord2f(0.0f, 0.0f);
	glVertex3f(m_nWidth, 0, 0.0f);
	glEnd();
#endif
}