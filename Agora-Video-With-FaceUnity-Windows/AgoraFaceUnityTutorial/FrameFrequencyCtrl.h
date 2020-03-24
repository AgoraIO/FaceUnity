#ifndef __FRAMEFREQUENCYCTRL_H__
#define __FRAMEFREQUENCYCTRL_H__
#include <windef.h>
//Sleep precision 1 millsecond
//GetTickCount() precision >10 millsecond
//QueryPerformanceCount ,QueryPerformanceFrequency percision < 0.01ms

namespace plusFCL_BTL
{
	class CFrameCtrl
	{//利用的是统计学
	public:
		CFrameCtrl();
		~CFrameCtrl();
		
		void setInterval(const int &Interval);
		float rate();
		int wait();
		int getFrameCount();

	private:
		
		size_t tickInterval_;
		LONG frameCount_;
		DWORD tickBegin_;
		DWORD tickEnd_;
	};


	class CHighResoluteFrameCtrl
	{//实时获取帧率.准确性更高.
	public:
		CHighResoluteFrameCtrl();
		~CHighResoluteFrameCtrl();

		void setInterval(unsigned int interval);
		float rate();
		int wait();
		int getFrameCount();

	private:
		unsigned int tickInterval_;
		int frameCount_;
		LARGE_INTEGER lfrequency_;
		LARGE_INTEGER counterInterval_;
		LARGE_INTEGER counterBegin_;
		LARGE_INTEGER counterPiror_;
		LARGE_INTEGER counterNext_;
	};
}

#endif
