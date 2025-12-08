# 음성 통화 관리 기능 구현 완료 보고서

## 📋 개요

Transit Love 앱의 음성 채팅 기능에 대한 **음소거 상태 관리**와 **통화 기록 저장 및 조회** 기능을 구현했습니다.

## ✅ 구현 완료 항목

### 1. 데이터베이스 스키마

#### 📊 voice_call_history 테이블
- 음성 통화 기록 저장
- 통화 시작/종료 시각, 시간(초), 상태 기록
- 음성 변조 사용 여부 추적

#### 📊 voice_mute_status 테이블
- 채팅방별 음소거 상태 저장
- 프로필별 실시간 음소거 상태 관리

### 2. Entity 클래스

#### ✅ CallStatus (Enum)
```java
- COMPLETED: 정상 종료
- REJECTED: 거절됨
- FAILED: 연결 실패
- MISSED: 부재중
```

#### ✅ VoiceCallHistory
- 통화 기록 Entity
- 발신자/수신자 프로필 ID
- 통화 시간 및 상태
- 음성 변조 사용 여부

#### ✅ VoiceMuteStatus
- 음소거 상태 Entity
- 채팅방-프로필 고유 제약조건
- 실시간 상태 업데이트

### 3. DTO 클래스

#### ✅ VoiceCallHistoryRequest
- 통화 기록 저장 요청
- Validation 포함

#### ✅ VoiceMuteRequest
- 음소거 상태 변경 요청
- 타임스탬프 포함

#### ✅ VoiceMuteResponse
- 음소거 상태 응답

#### ✅ VoiceCallStatisticsResponse
- 통화 통계 응답 (총 통화 수, 평균 시간 등)

#### ✅ VoiceCallHistoryResponse
- 통화 기록 응답

### 4. Repository

#### ✅ VoiceCallHistoryRepository
- 채팅방별 통화 기록 조회
- 프로필별 통화 기록 조회
- 통화 통계 쿼리 (SUM, AVG, MAX, MIN 등)

#### ✅ VoiceMuteStatusRepository
- 음소거 상태 CRUD
- 채팅방-프로필 조합 조회

### 5. Service

#### ✅ VoiceCallManagementService
- 음소거 상태 관리
- 통화 기록 저장 및 조회
- 통화 통계 계산

### 6. Controller

#### ✅ VoiceCallManagementController
- REST API 엔드포인트 제공
- JWT 인증 통합
- 에러 핸들링

### 7. WebSocket 확장

#### ✅ VoiceSignalMessage 업데이트
- `isMuted` 필드 추가
- `isSpeakerOn` 필드 추가

#### ✅ VoiceSignalType 확장
- `MUTE_STATUS_CHANGED`: 음소거 상태 변경
- `SPEAKER_STATUS_CHANGED`: 스피커 상태 변경

## 📡 API 엔드포인트

### 1. 음소거 상태 변경
```
POST /api/voice/room/{chatRoomId}/mute
```

### 2. 음소거 상태 조회
```
GET /api/voice/room/{chatRoomId}/mute/{profileId}
```

### 3. 통화 기록 저장
```
POST /api/voice/call/history
```

### 4. 채팅방별 통화 기록 조회
```
GET /api/voice/call/history?chatRoomId={chatRoomId}&page={page}&size={size}
```

### 5. 내 통화 기록 조회
```
GET /api/voice/call/history/my?page={page}&size={size}
```

### 6. 통화 통계 조회
```
GET /api/voice/call/statistics?chatRoomId={chatRoomId}
```

## 🗄️ 데이터베이스 설정

### 테이블 생성 스크립트
```bash
# MySQL에 접속하여 실행
mysql -u root -p transit_love < voice_call_schema.sql
```

또는 애플리케이션이 자동으로 테이블을 생성하도록 `application.properties`에 설정:
```properties
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 테스트 방법

### 1. 데이터베이스 테이블 생성
```bash
cd /Users/hanjiwon/Desktop/transitLove/BackEnd/transitLove
mysql -u root -p transit_love < voice_call_schema.sql
```

### 2. 애플리케이션 실행
```bash
export JAVA_HOME=/Users/hanjiwon/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home
./gradlew bootRun
```

### 3. API 테스트
```bash
# 테스트 스크립트 실행 (JWT 토큰 필요)
./test-voice-call-management.sh
```

### 4. 개별 API 테스트

#### 음소거 상태 변경
```bash
curl -X POST http://localhost:8080/api/voice/room/1/mute \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "isMuted": true,
    "timestamp": 1702000000000
  }'
```

#### 통화 기록 저장
```bash
curl -X POST http://localhost:8080/api/voice/call/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "chatRoomId": 1,
    "callerProfileId": 1,
    "receiverProfileId": 2,
    "startTime": "2024-12-08T23:30:00",
    "endTime": "2024-12-08T23:45:00",
    "duration": 900,
    "callStatus": "COMPLETED",
    "voiceModulationUsed": true
  }'
```

#### 통화 통계 조회
```bash
curl -X GET "http://localhost:8080/api/voice/call/statistics?chatRoomId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📊 통화 통계 데이터

통화 통계 API는 다음 정보를 제공합니다:

- **총 통화 횟수**: 채팅방의 모든 통화 시도
- **완료된 통화**: 정상 종료된 통화
- **거절된 통화**: 수신자가 거절한 통화
- **부재중 통화**: 응답하지 않은 통화
- **실패한 통화**: 연결 실패한 통화
- **총 통화 시간**: 모든 통화 시간의 합 (초)
- **평균 통화 시간**: 평균 통화 길이 (초)
- **최장/최단 통화**: 가장 길고 짧은 통화 시간
- **음성 변조 사용률**: 음성 변조를 사용한 비율 (0.0 ~ 1.0)
- **첫/마지막 통화**: 첫 번째 및 마지막 통화 시각

## 🎯 주요 기능

### 1. 실시간 음소거 상태 관리
- 채팅방별, 프로필별 음소거 상태 저장
- WebSocket을 통한 실시간 동기화
- 고유 제약조건으로 중복 방지

### 2. 통화 기록 관리
- 모든 통화 시도 기록
- 통화 상태별 분류 (완료, 거절, 실패, 부재중)
- 통화 시간 및 시각 추적
- 음성 변조 사용 여부 기록

### 3. 통화 통계
- 실시간 통계 계산
- 다양한 집계 함수 활용
- 페이지네이션 지원

### 4. 보안
- JWT 인증 필수
- 사용자 권한 검증
- SQL Injection 방지 (JPA 사용)

## 📈 데이터베이스 인덱스

### voice_call_history
- `idx_chat_room_id`: 채팅방별 조회 최적화
- `idx_caller_profile_id`: 발신자별 조회 최적화
- `idx_receiver_profile_id`: 수신자별 조회 최적화
- `idx_start_time`: 시간별 조회 최적화
- `idx_call_status`: 상태별 조회 최적화

### voice_mute_status
- `uk_chat_room_profile`: 채팅방-프로필 고유 제약
- `idx_chat_room_id`: 채팅방별 조회 최적화
- `idx_profile_id`: 프로필별 조회 최적화

## 🔄 프론트엔드 통합 가이드

### 음소거 버튼 클릭 시
```typescript
const toggleMute = async () => {
  const newMuteState = !isMuted;
  
  // 서버에 상태 전송
  await fetch(`/api/voice/room/${chatRoomId}/mute`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      profileId: myProfileId,
      isMuted: newMuteState,
      timestamp: Date.now()
    })
  });
  
  // WebSocket으로 상대방에게 알림
  stompClient.send('/app/voice/signal', {}, JSON.stringify({
    type: 'MUTE_STATUS_CHANGED',
    chatRoomId,
    fromProfileId: myProfileId,
    toProfileId: partnerProfileId,
    isMuted: newMuteState,
    timestamp: Date.now()
  }));
  
  setIsMuted(newMuteState);
};
```

### 통화 종료 시 기록 저장
```typescript
const endCall = async () => {
  const endTime = new Date();
  const duration = Math.floor((endTime - callStartTime) / 1000);
  
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
      startTime: callStartTime.toISOString(),
      endTime: endTime.toISOString(),
      duration,
      callStatus: 'COMPLETED',
      voiceModulationUsed: isModulationEnabled
    })
  });
};
```

### 통화 기록 조회
```typescript
const fetchCallHistory = async (chatRoomId: number, page = 0) => {
  const response = await fetch(
    `/api/voice/call/history?chatRoomId=${chatRoomId}&page=${page}&size=20`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  const data = await response.json();
  setCallHistory(data.data.content);
};
```

## 🐛 알려진 이슈 및 제약사항

1. **시간 검증**: 통화 시작 시각이 종료 시각보다 늦으면 에러 발생
2. **타임존**: 서버 타임존에 따라 시각 처리 (UTC 권장)
3. **대용량 데이터**: 통화 기록이 많을 경우 페이지네이션 필수

## 📝 향후 개선 사항

### 기능 확장
- [ ] 통화 품질 지표 추가 (비트레이트, 지연시간 등)
- [ ] 통화 녹음 기능
- [ ] 실시간 통화 모니터링 (관리자용)
- [ ] 통화 기록 내보내기 (CSV, Excel)

### 성능 최적화
- [ ] Redis 캐싱 (자주 조회되는 통계)
- [ ] 배치 처리 (대량 통화 기록 저장)
- [ ] 파티셔닝 (시간 기반 테이블 파티셔닝)

### 분석 기능
- [ ] 일별/주별/월별 통화 트렌드
- [ ] 사용자별 통화 패턴 분석
- [ ] 음성 변조 사용 통계

## ✅ 체크리스트

- [x] Entity 클래스 작성
- [x] Repository 인터페이스 작성
- [x] Service 로직 구현
- [x] Controller API 작성
- [x] DTO 클래스 작성
- [x] 데이터베이스 ���키마 작성
- [x] WebSocket 메시지 확장
- [x] 테스트 스크립트 작성
- [x] API 문서 작성
- [x] 구현 완료 보고서 작성
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 실행
- [ ] 프론트엔드 통합

## 🎉 결론

음성 통화 관리 기능(음소거 상태 관리, 통�� 기록 저장 및 조회)이 성공적으로 구현되었습니다!

### 주요 성과
✅ **완전한 REST API**: 6개의 엔드포인트 제공  
✅ **데이터베이스 스키마**: 최적화된 인덱스 포함  
✅ **통화 통계**: 실시간 집계 기능  
✅ **WebSocket 통합**: 실시간 상태 동기화  
✅ **보안**: JWT 인증 및 권한 검증  
✅ **테스트 도구**: 자동화된 테스트 스크립트  

---

**작성일**: 2024-12-09  
**버전**: 1.0.0  
**상태**: ✅ 구현 완료

