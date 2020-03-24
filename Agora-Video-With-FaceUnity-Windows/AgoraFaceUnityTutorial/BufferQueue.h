#pragma once

#include <atlcoll.h>

class CBufferQueue
{
public:
	CBufferQueue();
	~CBufferQueue();

	BOOL Create(int nUnitCount, SIZE_T nBytesPreUnit);
	BOOL Close();

	SIZE_T	GetBytesPreUnit() const { return m_nBytesPreUnit; };
	int		GetUnitCount() const { return m_nUnitCount; };
	int		GetCurrentCount() const { return m_nCurrentCount; };

	int		GetFreeCount() const;
	int		GetBusyCount() const;

	LPVOID	AllocBuffer(BOOL bForceAlloc = FALSE);

	BOOL	FreeBusyHead(LPVOID lpDestBuf, SIZE_T nBytesToCpoy);
	void	FreeAllBusyBlock();

private:
	int			m_nUnitCount;		// ��Ԫ��ʼֵ
	int			m_nCurrentCount;	// ��ǰ����Ŀ
	SIZE_T		m_nBytesPreUnit;

	CAtlList<LPBYTE>	m_listFreeUnit;	// ���õ�Ԫ
	CAtlList<LPBYTE>	m_listBusyUnit;	// �����õ�Ԫ

	CRITICAL_SECTION	m_csListLock;	// æ����
};

