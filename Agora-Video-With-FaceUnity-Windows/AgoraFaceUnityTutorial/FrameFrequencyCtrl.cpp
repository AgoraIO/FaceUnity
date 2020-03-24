#include "stdafx.h"
#include "FrameFrequencyCtrl.h"
#include <assert.h>

namespace plusFCL_BTL
{

	CFrameCtrl::CFrameCtrl()
	{
		tickInterval_ = 0;
		tickBegin_ = 0;
		tickEnd_ = 0;
		frameCount_ = 0;
	}

	CFrameCtrl::~CFrameCtrl()
	{
		tickInterval_ = 0;
		tickBegin_ = 0;
		tickEnd_ = 0;
		frameCount_ = 0;
	}

	void CFrameCtrl::setInterval(const int &Interval)
	{
		assert(Interval && 0 < Interval);
		if (tickInterval_ == Interval)
		{
			return;
		}
		if (!tickInterval_)
		{
			tickInterval_ = Interval;
		}
		else
		{
			tickInterval_ = Interval;
			tickBegin_ = 0;
			tickEnd_ = 0;
			frameCount_ = 0;
		}
		tickBegin_ = GetTickCount();
	}

	float CFrameCtrl::rate()
	{
		if (tickEnd_ - tickBegin_)
		{
			return (float)(frameCount_ * 1000.0 / (tickEnd_ - tickBegin_));
		}
		
		return 0.0;
	}

	int CFrameCtrl::wait()
	{
		if (!tickInterval_)
		{
			printf("please setInterval first!!!!!!\n");
			return false;
		}

		frameCount_++;
		DWORD needTickInterval = frameCount_ * tickInterval_;
		DWORD tickCurrent = GetTickCount();
		int tickRes = needTickInterval - (tickCurrent - tickBegin_);
		while (tickCurrent - tickBegin_ < needTickInterval)
		{
			Sleep(1);
			tickCurrent = GetTickCount();
		}
		tickEnd_ = tickCurrent;

		return tickRes;
	}

	int CFrameCtrl::getFrameCount()
	{
		return frameCount_;
	}


	//////////////////////////////////////////////////////////////////////////
	CHighResoluteFrameCtrl::CHighResoluteFrameCtrl()
	{
		tickInterval_ = 0;
		frameCount_ = 0;
		counterInterval_.QuadPart = 0; 
		lfrequency_.QuadPart = 0;
		counterBegin_.QuadPart = 0;
		counterPiror_.QuadPart = 0;
		counterNext_.QuadPart = 0;
	}

	CHighResoluteFrameCtrl::~CHighResoluteFrameCtrl()
	{
		tickInterval_ = 0;
		frameCount_ = 0;
		counterInterval_.QuadPart = 0;
		lfrequency_.QuadPart = 0;
		counterBegin_.QuadPart = 0;
		counterPiror_.QuadPart = 0;
		counterNext_.QuadPart = 0;
	}

	void CHighResoluteFrameCtrl::setInterval(unsigned int interval)
	{
		assert(interval && 0 < interval);
		if (tickInterval_ == interval)
		{
			return;
		}
		if (tickInterval_)
		{
			tickInterval_ = 0;
			frameCount_ = 0;
			counterInterval_.QuadPart = 0;
			lfrequency_.QuadPart = 0;
			counterBegin_.QuadPart = 0;
			counterPiror_.QuadPart = 0;
			counterNext_.QuadPart = 0;
		}
		tickInterval_ = interval;

		QueryPerformanceFrequency(&lfrequency_);
		QueryPerformanceCounter(&counterBegin_);
		counterPiror_ = counterBegin_;
		counterInterval_.QuadPart = lfrequency_.QuadPart * tickInterval_ / 1000;
	}

	float CHighResoluteFrameCtrl::rate()
	{
		LARGE_INTEGER counterCurrent;
		QueryPerformanceCounter(&counterCurrent);
		return float(frameCount_ / ((counterCurrent.QuadPart - counterBegin_.QuadPart) / lfrequency_.QuadPart));
	}

	int CHighResoluteFrameCtrl::wait()
	{
		if (!counterInterval_.QuadPart)
		{
			printf("please setInterval first!!!!!!\n");
			return false;
		}

		frameCount_++;
		LARGE_INTEGER counterCurrent;
		QueryPerformanceCounter(&counterCurrent);
		LONGLONG counterEscape = counterInterval_.QuadPart * frameCount_ - (counterCurrent.QuadPart - counterBegin_.QuadPart);
		LONGLONG res = counterEscape;

		//TO DO
		while (0 < counterEscape)
		{
			Sleep(1);
			QueryPerformanceCounter(&counterCurrent);
			counterEscape = counterInterval_.QuadPart * frameCount_ - (counterCurrent.QuadPart - counterBegin_.QuadPart);
		}

		return (int)res;
	}

	int CHighResoluteFrameCtrl::getFrameCount()
	{
		return frameCount_;
	}
}