# 음성 통화 관리 API - 빠른 시작 가이드

## 🚀 빠른 시작

### 1단계: 데이터베이스 설정

```bash
# MySQL에 접속
mysql -u root -p

# 데이터베이스 선택
USE transit_love;

# 테이블 생성
SOURCE /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove/voice_call_schema.sql;

# 테이블 확인
SHOW TABLES LIKE 'voice_%';
```

### 2단계: 애플리케이션 실행

```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
export JAVA_HOME=/Users/hanjiwon/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home
./gradlew bootRun
```

### 3단계: API 테스트

#### JWT 토큰 얻기
먼저 로그인하여 JWT 토큰을 얻으세요.

#### 음소거 상태 변경 테스트
```bash
curl -X POST http://localhost:8080/api/voice/room/1/mute \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "isMuted": true,
    "timestamp": '$(date +%s000)'
  }' | jq '.'
```

예상 응답:
```json
{
  "success": true,
  "message": "음소거 상태가 업데이트되었습니다.",
  "data": {
    "chatRoomId": 1,
    "profileId": 1,
    "isMuted": true,
    "updatedAt": "2024-12-09T12:30:00"
  }
}
```

#### 통화 기록 저장 테스트
```bash
curl -X POST http://localhost:8080/api/voice/call/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "chatRoomId": 1,
    "callerProfileId": 1,
    "receiverProfileId": 2,
    "startTime": "2024-12-09T12:00:00",
    "endTime": "2024-12-09T12:15:00",
    "duration": 900,
    "callStatus": "COMPLETED",
    "voiceModulationUsed": true
  }' | jq '.'
```

예상 응답:
```json
{
  "success": true,
  "message": "통화 기록이 저장되었습니다.",
  "data": {
    "callHistoryId": 1,
    "chatRoomId": 1,
    "callerProfileId": 1,
    "receiverProfileId": 2,
    "startTime": "2024-12-09T12:00:00",
    "endTime": "2024-12-09T12:15:00",
    "duration": 900,
    "callStatus": "COMPLETED",
    "voiceModulationUsed": true,
    "createdAt": "2024-12-09T12:15:01"
  }
}
```

#### 통화 통계 조회 테스트
```bash
curl -X GET "http://localhost:8080/api/voice/call/statistics?chatRoomId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" | jq '.'
```

예상 응답:
```json
{
  "success": true,
  "message": "통화 통계를 조회했습니다.",
  "data": {
    "chatRoomId": 1,
    "totalCalls": 5,
    "completedCalls": 4,
    "rejectedCalls": 1,
    "missedCalls": 0,
    "failedCalls": 0,
    "totalDuration": 3600,
    "averageDuration": 720,
    "longestCallDuration": 1200,
    "shortestCallDuration": 300,
    "voiceModulationUsageRate": 0.8,
    "firstCallTime": "2024-12-09T10:00:00",
    "lastCallTime": "2024-12-09T12:15:00"
  }
}
```

## 📋 API 엔드포인트 목록

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/voice/room/{chatRoomId}/mute` | 음소거 상태 변경 |
| GET | `/api/voice/room/{chatRoomId}/mute/{profileId}` | 음소거 상태 조회 |
| POST | `/api/voice/call/history` | 통화 기록 저장 |
| GET | `/api/voice/call/history` | 채팅방별 통화 기록 조회 |
| GET | `/api/voice/call/history/my` | 내 통화 기록 조회 |
| GET | `/api/voice/call/statistics` | 통화 통계 조회 |

## 🎯 주요 사용 사례

### 사용 사례 1: 통화 중 음소거 토글

```typescript
// React 예제
const VoiceChat = () => {
  const [isMuted, setIsMuted] = useState(false);
  
  const toggleMute = async () => {
    try {
      const response = await fetch(
        `/api/voice/room/${chatRoomId}/mute`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({
            profileId: myProfileId,
            isMuted: !isMuted,
            timestamp: Date.now()
          })
        }
      );
      
      if (response.ok) {
        setIsMuted(!isMuted);
        // WebSocket으로 상대방에게 알림
        sendMuteStatusToPartner(!isMuted);
      }
    } catch (error) {
      console.error('음소거 상태 변경 실패:', error);
    }
  };
  
  return (
    <button onClick={toggleMute}>
      {isMuted ? '🔇 음소거됨' : '🔊 음소거 해제'}
    </button>
  );
};
```

### 사용 사례 2: 통화 종료 시 기록 저장

```typescript
const endCall = async () => {
  const endTime = new Date();
  const startTime = callStartTimeRef.current;
  const duration = Math.floor((endTime - startTime) / 1000);
  
  try {
    await fetch('/api/voice/call/history', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        chatRoomId,
        callerProfileId: myProfileId,
        receiverProfileId: partnerProfileId,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        duration,
        callStatus: 'COMPLETED',
        voiceModulationUsed: modulationEnabled
      })
    });
    
    console.log('통화 기록 저장 완료');
  } catch (error) {
    console.error('통화 기록 저장 실패:', error);
  }
};
```

### 사용 사례 3: 통화 기록 페이지

```typescript
const CallHistoryPage = () => {
  const [history, setHistory] = useState([]);
  const [page, setPage] = useState(0);
  
  useEffect(() => {
    fetchCallHistory();
  }, [page]);
  
  const fetchCallHistory = async () => {
    try {
      const response = await fetch(
        `/api/voice/call/history/my?page=${page}&size=20`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      const data = await response.json();
      setHistory(data.data.content);
    } catch (error) {
      console.error('통화 기록 조회 실패:', error);
    }
  };
  
  return (
    <div>
      <h1>통화 기록</h1>
      {history.map(call => (
        <div key={call.id}>
          <p>상대방: {call.partnerNickname}</p>
          <p>시간: {call.duration}초</p>
          <p>날짜: {call.startTime}</p>
        </div>
      ))}
    </div>
  );
};
```

## 🔍 트러블슈팅

### 문제 1: 401 Unauthorized
**원인**: JWT 토큰이 없거나 만료됨  
**해결**: 로그인하여 새 토큰 발급

### 문제 2: 400 Bad Request (시간 오류)
**원인**: startTime이 endTime보다 늦음  
**해결**: 시간 값 확인 및 수정

### 문제 3: 테이블이 없음
**원인**: 데이터베이스 스키마 미생성  
**해결**: `voice_call_schema.sql` 실행

### 문제 4: 음소거 상태가 동기화되지 않음
**원인**: WebSocket 연결 끊김  
**해결**: WebSocket 연결 상태 확인 및 재연결

## 📊 모니터링

### 데이터베이스 쿼리

```sql
-- 최근 통화 기록 조회
SELECT * FROM voice_call_history 
ORDER BY start_time DESC 
LIMIT 10;

-- 채팅방별 통화 통계
SELECT 
  chat_room_id,
  COUNT(*) as total_calls,
  SUM(duration) as total_duration,
  AVG(duration) as avg_duration
FROM voice_call_history
GROUP BY chat_room_id;

-- 음소거 상태 확인
SELECT * FROM voice_mute_status
WHERE chat_room_id = 1;
```

## 📝 로그 확인

```bash
# 애플리케이션 로그
tail -f /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove/logs/spring.log | grep "음성"

# 음소거 관련 로그
tail -f /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove/logs/spring.log | grep "음소거"

# 통화 기록 관련 로그
tail -f /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove/logs/spring.log | grep "통화"
```

## 🎓 추가 자료

- `VOICE_CHAT_API_README.md` - 상세한 API 문서
- `VOICE_CALL_MANAGEMENT_IMPLEMENTATION.md` - 구현 상세 보고서
- `voice_call_schema.sql` - 데이터베이스 스키마
- `test-voice-call-management.sh` - 자동화 테스트 스크립트

---

**작성일**: 2024-12-09  
**버전**: 1.0.0

