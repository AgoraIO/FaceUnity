#include "stdafx.h"
#include "BufferQueue.h"


CBufferQueue::CBufferQueue()
{
	::InitializeCriticalSection(&m_csListLock);
}


CBufferQueue::~CBufferQueue()
{
	::DeleteCriticalSection(&m_csListLock);
}


BOOL CBufferQueue::Create(int nUnitCount, SIZE_T nBytesPreUnit)
{
	_ASSERT(m_listFreeUnit.IsEmpty());

	if (nUnitCount < 0 || nBytesPreUnit == 0)
		return FALSE;

	// �Ѿ��������
	if (!m_listFreeUnit.IsEmpty())
		return TRUE;

	LPBYTE	lpBuffer = NULL;

	::EnterCriticalSection(&m_csListLock);

	// ��ʼ���ڴ��
	for (int nIndex = 0; nIndex < nUnitCount; nIndex++){
		lpBuffer = new BYTE[nBytesPreUnit];
		m_listFreeUnit.AddTail(lpBuffer);
	}

	::LeaveCriticalSection(&m_csListLock);

	m_nUnitCount = nUnitCount;
	m_nCurrentCount = nUnitCount;
	m_nBytesPreUnit = nBytesPreUnit;

	return TRUE;
}

BOOL CBufferQueue::Close()
{
	LPBYTE lpBuffer = NULL;

	_ASSERT(m_nUnitCount > 0 && m_nBytesPreUnit > 0);
	_ASSERT(m_listBusyUnit.IsEmpty());

	::EnterCriticalSection(&m_csListLock);

	if (m_listBusyUnit.GetCount() != 0){			// ����æ�飬��ֹ�ͷ�
		::LeaveCriticalSection(&m_csListLock);

		return FALSE;
	}

	while (m_listFreeUnit.GetCount() > 0){
		lpBuffer = m_listFreeUnit.RemoveHead();
		delete[] lpBuffer;
	}

	m_nBytesPreUnit = 0;
	m_nUnitCount = 0;
	m_nCurrentCount = 0;

	::LeaveCriticalSection(&m_csListLock);		// ���ڻ���ȫ��������ϣ��˳��ٽ�

	return TRUE;
}

int	CBufferQueue::GetFreeCount() const
{
	return (int)m_listFreeUnit.GetCount();
}

int	CBufferQueue::GetBusyCount() const
{
	return (int)m_listBusyUnit.GetCount();
}

LPVOID	CBufferQueue::AllocBuffer(BOOL bForceAlloc)
{
	LPBYTE		lpBuffer = NULL;	// ������
	POSITION	posHead = NULL;		// ����ͷ
	POSITION	posTail = NULL;		// ����β

	_ASSERT(m_nUnitCount > 0 && m_nBytesPreUnit > 0);

	::EnterCriticalSection(&m_csListLock);				// ��������ٽ�
	posHead = m_listFreeUnit.GetHeadPosition();			// ��������Ƿ�Ϊ��
	posTail = m_listFreeUnit.GetTailPosition();			// ����βҲҪ���

	if (posHead != NULL && posHead != posTail)			// ����ǿ���ͷβ��ͬ
		lpBuffer = m_listFreeUnit.RemoveHead();
	else if (posHead != NULL && posHead == posTail)		// ֻ��һ������
		lpBuffer = m_listFreeUnit.RemoveHead();
	else{												// �����޿���
		if (bForceAlloc){								// ����ǿ�Ʒ���
			lpBuffer = new BYTE[m_nBytesPreUnit];
			m_nCurrentCount++;
		}
		else
			lpBuffer = NULL;
	}

	if (lpBuffer == NULL) {
		::LeaveCriticalSection(&m_csListLock);
		return NULL;
	}

	m_listBusyUnit.AddTail(lpBuffer);

	::LeaveCriticalSection(&m_csListLock);

	return lpBuffer;
}

BOOL CBufferQueue::FreeBusyHead(LPVOID lpDestBuf, SIZE_T nBytesToCpoy)
{
	BOOL bRet = FALSE;
	LPBYTE lpBuffer = NULL;

	_ASSERT(m_nUnitCount > 0 && m_nBytesPreUnit > 0);

	::EnterCriticalSection(&m_csListLock);
	if (!m_listBusyUnit.IsEmpty())
		lpBuffer = m_listBusyUnit.RemoveHead();

	if (lpDestBuf != NULL && lpBuffer != NULL)
		memcpy(lpDestBuf, lpBuffer, nBytesToCpoy);

	if (lpBuffer != NULL)
		m_listFreeUnit.AddTail(lpBuffer);

	::LeaveCriticalSection(&m_csListLock);

	return lpBuffer != NULL ? TRUE : FALSE;
}

void CBufferQueue::FreeAllBusyBlock()
{
	BOOL bRet = TRUE;

	do {
		bRet = FreeBusyHead(NULL, 0);
	} while (!bRet);
}