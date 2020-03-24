#pragma once

#include "glew.h"
#include "glut.h"
#include <iostream>
#include <memory>

class CAgoraOpenGl
{
public:
	CAgoraOpenGl();
	~CAgoraOpenGl();

	HDC hDc;
	HGLRC hRC;

public:
	void Init(int nWidth,int nHeight);

	bool SetupPixelFormat(HDC hdc0);

	void Reshap(int nWidth,int nHeight);
	
	void Render(std::tr1::shared_ptr<unsigned char>frame);

private:
	GLuint textureID = 0;
	int m_nWidth;
	int m_nHeight;	
};