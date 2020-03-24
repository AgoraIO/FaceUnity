#pragma once
#include "../AgoraSDK/include/IAgoraMediaEngine.h"

class CExtendAudioFrameObserver :
	public agora::media::IAudioFrameObserver
{
public:
	CExtendAudioFrameObserver();
	~CExtendAudioFrameObserver();

	virtual bool onRecordAudioFrame(AudioFrame& audioFrame);
	virtual bool onPlaybackAudioFrame(AudioFrame& audioFrame);
	virtual bool onMixedAudioFrame(AudioFrame& audioFrame);
	virtual bool onPlaybackAudioFrameBeforeMixing(unsigned int uid, AudioFrame& audioFrame);
};

